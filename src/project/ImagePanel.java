package project;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.awt.Robot;
import java.util.List;

/**
 * ImagePanel 处理图像显示和鼠标交互。
 * 使用 Dijkstra 计算活线路径，
 * 并实现光标快照和路径冷却（自动种子点）。
 */
public class ImagePanel extends JPanel implements MouseListener, MouseMotionListener {
    private final BufferedImage originalImage; // 原始图像
    private final double[][] gradient; // 梯度大小
    private double[][] dist; // 从种子到每个点的距离图
    private int[][] parentX, parentY; // 路径的父指针
    private final List<Point> seeds = new ArrayList<>(); // 种子点列表
    private final List<List<Point>> finalPaths = new ArrayList<>(); // 最终的路径段
    private List<Point> currentPath = new ArrayList<>(); // 当前活线路径到光标
    private boolean closed = false; // 边界环是否关闭
    private int snapRadius = 0; // 光标快照的邻域半径
    private Robot robot = null; // 若需要，移动鼠标光标的 Robot
    private static final int INTERVAL = 200; // 虚拟种子更新间隔，单位为毫秒
    private final List<List<Point>> virtualPaths = new ArrayList<>(); // 存储虚拟路径
    private final Timer updateTimer; // 用于更新虚拟路径的定时器
    private Point virtualSeed; // 用于比较的虚拟种子点
    private final int overlapThreshold = 40; // 重叠阈值（虚拟路径像素重叠个数）
    private final double minDistance = 100.0; // 虚拟路径间的最小距离

    private static Point firstPoint(List<Point> points) {
        return points.get(0);
    }

    private static Point lastPoint(List<Point> points) {
        return points.get(points.size() - 1);
    }

    public ImagePanel(BufferedImage img) {
        this.originalImage = img;
        setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
        addMouseListener(this);
        addMouseMotionListener(this);

        // 计算图像的梯度大小
        this.gradient = GradientCalculator.computeGradient(originalImage);

        // 尝试创建 Robot，用于移动鼠标指针（如果允许）
        try {
            robot = new Robot();
        } catch (Exception e) {
            robot = null;
        }

        // 定时器，定期更新虚拟种子
        updateTimer = new Timer(INTERVAL,  e-> updateVirtualPath());
        updateTimer.start(); // 启动定时器
    }

    // 设置光标捕捉半径（0 = 不捕捉）
    public void setSnapRadius(int radius) {
        this.snapRadius = radius;
    }

    // 从最后一个种子位置执行 Dijkstra 算法
    private void computePathsFromSeed(int sx, int sy) {
        int w = originalImage.getWidth(), h = originalImage.getHeight();
        parentX = new int[w][h];
        parentY = new int[w][h];
        dist = new double[w][h];

        // 初始化所有距离为正无穷
        for (int x = 0; x < w; x++) {
            Arrays.fill(dist[x], Double.POSITIVE_INFINITY);
        }

        // 执行 Dijkstra 算法（优先队列）
        PriorityQueue<Node> pq = new PriorityQueue<>();
        dist[sx][sy] = 0.0;
        pq.offer(new Node(sx, sy, 0.0));

        // 方向数组
        int[][] dirs = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},          {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };
        double diagCost = Math.sqrt(2);

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            int x = node.x, y = node.y;

            // 跳过不最优的距离
            if (node.dist > dist[x][y]) continue;

            // 松弛邻居节点
            for (int[] d : dirs) {
                int nx = x + d[0], ny = y + d[1];
                if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;

                // 计算从 (x,y) 到 (nx,ny) 的成本
                double gradP = gradient[x][y];
                double gradQ = gradient[nx][ny];
                double weight = 1.0 / (1.0 + (gradP + gradQ) * 0.5);
                double cost = (d[0] != 0 && d[1] != 0) ? weight * diagCost : weight;

                double newDist = this.dist[x][y] + cost;
                if (newDist < this.dist[nx][ny]) {
                    this.dist[nx][ny] = newDist;
                    parentX[nx][ny] = x;
                    parentY[nx][ny] = y;
                    pq.offer(new Node(nx, ny, newDist));
                }
            }
        }
    }

    // 从 (tx, ty) 重新构建到最后种子的最短路径
    private List<Point> buildPath(int tx, int ty) {
        LinkedList<Point> path = new LinkedList<>();
        int x = tx, y = ty;

        // 通过父指针向回追溯直到到达种子点
        while (dist[x][y] != 0.0) {
            path.addFirst(new Point(x, y)); // 将点添加到头部以便于路径构建
            int px = parentX[x][y];
            int py = parentY[x][y];
            x = px;
            y = py;
        }
        path.addFirst(new Point(x, y)); // 加入种子点

        return path;
    }

    // 处理添加新的种子（手动或自动）
    private void addSeed(Point newSeed) {
        // 从最后一个种子到 newSeed 构建最终路径段
        if (!seeds.isEmpty()) {
            Point lastSeed = lastPoint(seeds);
            List<Point> seg = buildPath(newSeed.x, newSeed.y);
            // 确保路径以 lastSeed 开始
            if (!seg.isEmpty() && firstPoint(seg).equals(lastSeed)) {
                finalPaths.add(seg);
            } else {
                // 如果不符合预期，手动插入
                seg.add(0, lastSeed);
                finalPaths.add(seg);
            }
        }
        // 设置新的种子点
        seeds.add(newSeed);
        // 从新种子点上计算 Dijkstra
        computePathsFromSeed(newSeed.x, newSeed.y);
    }

    // MouseListener 和 MouseMotionListener 方法
    @Override
    public void mousePressed(MouseEvent e) {
        if (closed) return;
        int x = e.getX(), y = e.getY();
        // 限制在图像边界内
        x = Math.max(0, Math.min(x, originalImage.getWidth() - 1));
        y = Math.max(0, Math.min(y, originalImage.getHeight() - 1));

        if (seeds.isEmpty()) {
            // 用户放置的第一个种子点
            Point seed = new Point(x, y);
            seeds.add(seed);
            computePathsFromSeed(x, y);
        } else {
            Point firstSeed = firstPoint(seeds);
            // 如果点击接近第一个种子点，关闭环
            if (firstSeed.distance(x, y) < 5.0 && seeds.size() > 1) {
                // 完成到第一个种子的最终段
                Point lastSeed = lastPoint(seeds);
                Point target = new Point(firstSeed.x, firstSeed.y);
                List<Point> closingSeg = buildPath(target.x, target.y);
                if (!closingSeg.isEmpty() && firstPoint(closingSeg).equals(lastSeed)) {
                    finalPaths.add(closingSeg);
                }
                closed = true;
                repaint();
                extractRegion();
                return;
            }
            // 否则，在点击位置添加手动种子点
            Point newSeed = new Point(x, y);
            addSeed(newSeed);
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (seeds.isEmpty() || closed) return;
        int mx = e.getX(), my = e.getY();
        mx = Math.max(0, Math.min(mx, originalImage.getWidth() - 1));
        my = Math.max(0, Math.min(my, originalImage.getHeight() - 1));
        int snapX = mx, snapY = my;

        // 实现光标捕捉逻辑
        if (snapRadius > 0) {
            double bestGrad = gradient[mx][my];
            for (int dy = -snapRadius; dy <= snapRadius; dy++) {
                for (int dx = -snapRadius; dx <= snapRadius; dx++) {
                    int nx = mx + dx, ny = my + dy;
                    if (nx < 0 || ny < 0 || nx >= originalImage.getWidth() || ny >= originalImage.getHeight()) continue;
                    if (gradient[nx][ny] > bestGrad) {
                        bestGrad = gradient[nx][ny];
                        snapX = nx;
                        snapY = ny;
                    }
                }
            }
        }

        // 计算新的线条路径
        currentPath = buildPath(snapX, snapY); // 更新当前路径
        repaint();
    }

    // 定期更新虚拟路径
    private void updateVirtualPath() {
        if (currentPath != null && !currentPath.isEmpty() && !closed) {
            // 使用当前路径的最后一个点作为虚拟种子
            virtualSeed = lastPoint(currentPath);
            List<Point> newVirtualPath = buildPath(virtualSeed.x, virtualSeed.y);
            virtualPaths.add(newVirtualPath);

            // 检查与已有虚拟路径的重叠
            for (List<Point> existingPath : virtualPaths) {
                // 计算两个路径末端的距离
                double distance = virtualSeed.distance(lastPoint(existingPath));

                // 仅在距离条件满足时进行比较
                if (distance > minDistance) {
                    int overlapCount = getOverlapCount(existingPath, newVirtualPath);
                    if (overlapCount >= overlapThreshold) {
                        // 如果有足够的重叠，取重叠段的最后一个点
                        Point newSeed = newVirtualPath.get(Math.min(newVirtualPath.size() - 1, overlapCount - 1));
                        if (!seeds.contains(newSeed)) {
                            addSeed(newSeed); // 添加新的种子点
                        }
                        break; // 找到第一个重叠路径后退出循环
                    }
                }
            }
        }
    }

    // 计算两个路径之间重叠点的数量
    private int getOverlapCount(List<Point> path1, List<Point> path2) {
        int count = 0;
        int minSize = Math.min(path1.size(), path2.size());
        for (int i = 0; i < minSize; i++) {
            if (path1.get(i).equals(path2.get(i))) {
                count++;
            } else {
                break; // 在第一次不匹配时停止计数
            }
        }
        return count;
    }

    // 绘制组件
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 绘制原始图像
        g.drawImage(originalImage, 0, 0, null);

        Graphics2D g2 = (Graphics2D) g;
        // 将最终的路径段绘制为蓝色
        g2.setColor(Color.CYAN);
        g2.setStroke(new BasicStroke(2));
        for (List<Point> seg : finalPaths) {
            if (seg.size() < 2) continue;
            Path2D path = new Path2D.Double();
            Point start = firstPoint(seg);
            path.moveTo(start.x + 0.5, start.y + 0.5);
            for (Point p : seg) {
                path.lineTo(p.x + 0.5, p.y + 0.5);
            }
            g2.draw(path);
        }
        // 将当前的活线路径绘制为红色
        if (!currentPath.isEmpty()) {
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(1));
            Path2D path = new Path2D.Double();
            Point start = firstPoint(currentPath);
            path.moveTo(start.x + 0.5, start.y + 0.5);
            for (Point p : currentPath) {
                path.lineTo(p.x + 0.5, p.y + 0.5);
            }
            g2.draw(path);
        }
        // 将种子点绘制为小方块
        g2.setColor(Color.YELLOW);
        for (Point seed : seeds) {
            g2.fillRect(seed.x - 3, seed.y - 3, 7, 7);
        }
    }

    // 提取所选区域为新图像并显示
    private void extractRegion() {
        if (finalPaths.isEmpty()) return;
        // 从最终路径构建多边形路径
        Path2D region = new Path2D.Double();
        boolean first = true;
        for (List<Point> seg : finalPaths) {
            for (Point p : seg) {
                if (first) {
                    region.moveTo(p.x, p.y);
                    first = false;
                } else {
                    region.lineTo(p.x, p.y);
                }
            }
        }
        region.closePath();

        // 计算边界框
        Rectangle bounds = region.getBounds();
        int minX = Math.max(0, bounds.x);
        int minY = Math.max(0, bounds.y);
        int maxX = Math.min(originalImage.getWidth() - 1, bounds.x + bounds.width);
        int maxY = Math.min(originalImage.getHeight() - 1, bounds.y + bounds.height);
        int w = maxX - minX + 1;
        int h = maxY - minY + 1;
        if (w <= 0 || h <= 0) return;

        // 创建具有透明背景的输出图像
        BufferedImage extracted = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                int px = minX + xx, py = minY + yy;
                if (region.contains(px, py)) {
                    extracted.setRGB(xx, yy, originalImage.getRGB(px, py) | 0xFF000000);
                } else {
                    extracted.setRGB(xx, yy, 0x00000000); // 透明部分
                }
            }
        }

        // 在新窗口中显示提取的图像
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("提取的区域");
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setLayout(new BorderLayout());
            JLabel label = new JLabel(new ImageIcon(extracted));
            f.add(label, BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(this);
            f.setVisible(true);
        });
    }

    // 未使用的监听方法
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}

    // Dijkstra 算法的辅助类，用于优先队列
    private static class Node implements Comparable<Node> {
        int x, y;
        double dist;

        Node(int x, int y, double d) {
            this.x = x;
            this.y = y;
            this.dist = d;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.dist, other.dist);
        }
    }
}
