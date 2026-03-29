package pt;

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
 * ImagePanel handles image display and mouse interactions.
 * It maintains seed points, computes live-wire paths using Dijkstra,
 * and implements cursor snapping and path cooling (automatic seeds).
 */
public class ImagePanel extends JPanel implements MouseListener, MouseMotionListener {
    private final BufferedImage originalImage;
    private final double[][] gradient; // gradient magnitude
    private double[][] dist; // distance map from seed
    private int[][] parentX, parentY; // parent pointers for the path
    private final List<Point> seeds = new ArrayList<>(); // list of seed points
    private final List<List<Point>> finalPaths = new ArrayList<>(); // finalized segments
    private List<Point> currentPath = new ArrayList<>(); // current live-wire path to cursor
    private boolean closed = false; // whether boundary loop is closed
    private int snapRadius = 0; // neighborhood radius for cursor snapping
    private Robot robot = null; // Robot for moving mouse cursor if needed
    // number of frames for stability
    private static final int INTERVAL = 100; // Virtual seed update interval in milliseconds
    private final List<List<Point>> virtualPaths = new ArrayList<>(); // store virtual paths
    private final Timer updateTimer; // for updating virtual paths
    private Point virtualSeed; // virtual seed point for comparisons
    private final int overlapThreshold = 2; // Minimum overlap length to consider
    private final double minDistance = 120.0; // Minimum distance between virtual paths

    public ImagePanel(BufferedImage img) {
        this.originalImage = img;
        setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
        addMouseListener(this);
        addMouseMotionListener(this);

        // Precompute gradient magnitude for the image
        this.gradient = GradientCalculator.computeGradient(originalImage);

        // Try to create Robot for moving mouse pointer (if allowed)
        try {
            robot = new Robot();
        } catch (Exception e) {
            robot = null;
        }

        // Timer to periodically update the virtual seed
        updateTimer = new Timer(INTERVAL, _ -> updateVirtualPath());
        updateTimer.start(); // start the timer
    }

    // Set cursor snap radius (0 = no snap)
    public void setSnapRadius(int radius) {
        this.snapRadius = radius;
    }

    // Perform Dijkstra from the last seed
    private void computePathsFromSeed(int sx, int sy) {
        int w = originalImage.getWidth(), h = originalImage.getHeight();
        parentX = new int[w][h];
        parentY = new int[w][h];
        dist = new double[w][h];

        // Initialize distances to infinity
        for (int x = 0; x < w; x++) {
            Arrays.fill(dist[x], Double.POSITIVE_INFINITY);
        }

        // Use a priority queue for Dijkstra's algorithm
        PriorityQueue<Node> pq = new PriorityQueue<>();
        dist[sx][sy] = 0.0;
        pq.offer(new Node(sx, sy, 0.0));

        // Directions for 8-connectivity
        int[][] dirs = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},          {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };
        double diagCost = Math.sqrt(2);

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            int x = node.x, y = node.y;

            // Skip if this distance is not optimal
            if (node.dist > dist[x][y]) continue;

            // Relax neighboring nodes
            for (int[] d : dirs) {
                int nx = x + d[0], ny = y + d[1];
                if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;

                // Calculate the cost from (x,y) to (nx,ny)
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

    // Reconstruct the shortest path from (tx, ty) back to last seed via parent pointers
    private List<Point> buildPath(int tx, int ty) {
        LinkedList<Point> path = new LinkedList<>();
        int x = tx, y = ty;

        // Follow parent pointers until reaching the seed
        while (dist[x][y] != 0.0) {
            path.addFirst(new Point(x, y)); // Add to head for easier path construction
            int px = parentX[x][y];
            int py = parentY[x][y];
            x = px;
            y = py;
        }
        path.addFirst(new Point(x, y)); // Add the seed point

        return path;
    }

    // Handle adding a new seed (manual or automatic)
    private void addSeed(Point newSeed) {
        // Build final segment from last seed to newSeed
        if (!seeds.isEmpty()) {
            Point lastSeed = seeds.getLast();
            List<Point> seg = buildPath(newSeed.x, newSeed.y);
            // Ensure the path starts at lastSeed
            if (!seg.isEmpty() && seg.getFirst().equals(lastSeed)) {
                finalPaths.add(seg);
            } else {
                // This should not happen, but if it does, we manually insert
                seg.addFirst(lastSeed);
                finalPaths.add(seg);
            }
        }
        // Set new seed
        seeds.add(newSeed);
        // Compute Dijkstra from new seed
        computePathsFromSeed(newSeed.x, newSeed.y);
    }

    // MouseListener and MouseMotionListener methods

    @Override
    public void mousePressed(MouseEvent e) {
        if (closed) return;
        int x = e.getX(), y = e.getY();
        // Clamp to image bounds
        x = Math.max(0, Math.min(x, originalImage.getWidth() - 1));
        y = Math.max(0, Math.min(y, originalImage.getHeight() - 1));

        if (seeds.isEmpty()) {
            // First seed placed by user
            Point seed = new Point(x, y);
            seeds.add(seed);
            computePathsFromSeed(x, y);
        } else {
            Point firstSeed = seeds.getFirst();
            // If clicked near first seed, close the loop
            if (firstSeed.distance(x, y) < 5.0 && seeds.size() > 1) {
                // Complete final segment to first seed
                Point lastSeed = seeds.getLast();
                Point target = new Point(firstSeed.x, firstSeed.y);
                List<Point> closingSeg = buildPath(target.x, target.y);
                if (!closingSeg.isEmpty() && closingSeg.getFirst().equals(lastSeed)) {
                    finalPaths.add(closingSeg);
                }
                closed = true;
                repaint();
                extractRegion();
                return;
            }
            // Otherwise, add manual seed at click position
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

        // Apply cursor snapping logic
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

        // Calculate new line path
        currentPath = buildPath(snapX, snapY); // Update current path
        repaint();
    }

    // Update the virtual path every second
    private void updateVirtualPath() {
        if (currentPath != null && !currentPath.isEmpty() && !closed) {
            // Use current path's last point as a virtual seed
            virtualSeed = currentPath.getLast();
            List<Point> newVirtualPath = buildPath(virtualSeed.x, virtualSeed.y);
            virtualPaths.add(newVirtualPath);

            // Check if there is an overlap with existing virtual paths
            for (List<Point> existingPath : virtualPaths) {
                // Calculate distance between the two last points
                double distance = virtualSeed.distance(existingPath.getLast());

                // Compare only if the distance condition is met
                if (distance > minDistance) {
                    int overlapCount = getOverlapCount(existingPath, newVirtualPath);
                    if (overlapCount >= overlapThreshold) {
                        // If there is enough overlap, take the last point of the overlap
                        Point newSeed = newVirtualPath.get(Math.min(newVirtualPath.size() - 1, overlapCount - 1));
                        if (!seeds.contains(newSeed)) {
                            addSeed(newSeed); // Add a new seed point
                        }
                        break; // Break after finding the first overlapping path
                    }
                }
            }
        }
    }

    // Calculate the count of overlapping points between two paths
    private int getOverlapCount(List<Point> path1, List<Point> path2) {
        int count = 0;
        int minSize = Math.min(path1.size(), path2.size());
        for (int i = 0; i < minSize; i++) {
            if (path1.get(i).equals(path2.get(i))) {
                count++;
            } else {
                break; // Stop count on the first mismatch
            }
        }
        return count;
    }

    // Paint the component
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the original image
        g.drawImage(originalImage, 0, 0, null);

        Graphics2D g2 = (Graphics2D) g;
        // Draw finalized segments in blue
        g2.setColor(Color.CYAN);
        g2.setStroke(new BasicStroke(2));
        for (List<Point> seg : finalPaths) {
            if (seg.size() < 2) continue;
            Path2D path = new Path2D.Double();
            Point start = seg.getFirst();
            path.moveTo(start.x + 0.5, start.y + 0.5);
            for (Point p : seg) {
                path.lineTo(p.x + 0.5, p.y + 0.5);
            }
            g2.draw(path);
        }
        // Draw current live-wire path in red
        if (!currentPath.isEmpty()) {
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(1));
            Path2D path = new Path2D.Double();
            Point start = currentPath.getFirst();
            path.moveTo(start.x + 0.5, start.y + 0.5);
            for (Point p : currentPath) {
                path.lineTo(p.x + 0.5, p.y + 0.5);
            }
            g2.draw(path);
        }
        // Draw seed points as small squares
        g2.setColor(Color.YELLOW);
        for (Point seed : seeds) {
            g2.fillRect(seed.x - 3, seed.y - 3, 7, 7);
        }
    }

    // Extract the selected region as a new image and display it
    private void extractRegion() {
        if (finalPaths.isEmpty()) return;
        // Build polygon path from finalPaths
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

        // Compute bounding box
        Rectangle bounds = region.getBounds();
        int minX = Math.max(0, bounds.x);
        int minY = Math.max(0, bounds.y);
        int maxX = Math.min(originalImage.getWidth() - 1, bounds.x + bounds.width);
        int maxY = Math.min(originalImage.getHeight() - 1, bounds.y + bounds.height);
        int w = maxX - minX + 1;
        int h = maxY - minY + 1;
        if (w <= 0 || h <= 0) return;

        // Create output image with transparent background
        BufferedImage extracted = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                int px = minX + xx, py = minY + yy;
                if (region.contains(px, py)) {
                    extracted.setRGB(xx, yy, originalImage.getRGB(px, py) | 0xFF000000);
                } else {
                    extracted.setRGB(xx, yy, 0x00000000); // transparent
                }
            }
        }

        // Show extracted image in new frame
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Extracted Region");
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setLayout(new BorderLayout());
            JLabel label = new JLabel(new ImageIcon(extracted));
            f.add(label, BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(this);
            f.setVisible(true);
        });
    }

    // Unused listener methods
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}

    // Helper class for priority queue in Dijkstra
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