package project;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.awt.Robot;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Core interactive panel for Intelligent Scissors.
 *
 * This component renders the source image, computes live-wire paths using
 * Dijkstra shortest-path search, and handles both manual and automatic seed
 * placement behavior.
 */
public class ImagePanel extends JPanel implements MouseListener, MouseMotionListener {
    private final BufferedImage originalImage; // Source image.
    private final double[][] gradient; // Edge strength map.
    private double[][] dist; // Distance map from the active seed.
    private int[][] parentX, parentY; // Parent pointers for path backtracking.
    private final List<Point> seeds = new ArrayList<>(); // User-confirmed seed points.
    private final List<List<Point>> finalPaths = new ArrayList<>(); // Confirmed contour segments.
    private List<Point> currentPath = new ArrayList<>(); // Live path from seed to cursor.
    private boolean closed = false; // Whether the contour is closed.
    private int snapRadius = 0; // Cursor snapping neighborhood radius.
    private Robot robot = null; // Optional Robot instance if pointer control is needed.
    private static final int INTERVAL = 200; // Virtual seed update interval (ms).
    private final List<List<Point>> virtualPaths = new ArrayList<>(); // Historical virtual paths.
    private final Timer updateTimer; // Timer that drives virtual path updates.
    private Point virtualSeed; // Current virtual seed for overlap checks.
    private final int overlapThreshold = 40; // Overlap threshold in pixels.
    private final double minDistance = 100.0; // Minimum spacing between virtual paths.
    private BufferedImage lastExtractedRegion; // Most recent extracted region preview.

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

        // Precompute edge strength map.
        this.gradient = GradientCalculator.computeGradient(originalImage);

        // Attempt to create a Robot instance; absence is non-fatal.
        try {
            robot = new Robot();
        } catch (Exception e) {
            robot = null;
        }

        // Periodically evaluate virtual path overlap and auto-seeding.
        updateTimer = new Timer(INTERVAL,  e-> updateVirtualPath());
        updateTimer.start(); // Start periodic updates.
    }

    // Set cursor snap radius (0 disables snapping).
    public void setSnapRadius(int radius) {
        this.snapRadius = radius;
    }

    // Compute shortest-path tree from the latest seed using Dijkstra.
    private void computePathsFromSeed(int sx, int sy) {
        int w = originalImage.getWidth(), h = originalImage.getHeight();
        parentX = new int[w][h];
        parentY = new int[w][h];
        dist = new double[w][h];

        // Initialize distance field to infinity.
        for (int x = 0; x < w; x++) {
            Arrays.fill(dist[x], Double.POSITIVE_INFINITY);
        }

        // Dijkstra search with a priority queue.
        PriorityQueue<Node> pq = new PriorityQueue<>();
        dist[sx][sy] = 0.0;
        pq.offer(new Node(sx, sy, 0.0));

        // 8-neighborhood directions.
        int[][] dirs = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},          {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };
        double diagCost = Math.sqrt(2);

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            int x = node.x, y = node.y;

            // Skip stale queue entries.
            if (node.dist > dist[x][y]) continue;

            // Relax neighboring vertices.
            for (int[] d : dirs) {
                int nx = x + d[0], ny = y + d[1];
                if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;

                // Compute edge cost from (x, y) to (nx, ny).
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

    // Reconstruct shortest path from target (tx, ty) back to active seed.
    private List<Point> buildPath(int tx, int ty) {
        LinkedList<Point> path = new LinkedList<>();
        int x = tx, y = ty;

        // Backtrack through parent pointers until the seed is reached.
        while (dist[x][y] != 0.0) {
            path.addFirst(new Point(x, y)); // Prepend to preserve forward order.
            int px = parentX[x][y];
            int py = parentY[x][y];
            x = px;
            y = py;
        }
        path.addFirst(new Point(x, y)); // Include seed point.

        return path;
    }

    // Add a new seed point (manual click or virtual insertion).
    private void addSeed(Point newSeed) {
        // Build a finalized segment from previous seed to new seed.
        if (!seeds.isEmpty()) {
            Point lastSeed = lastPoint(seeds);
            List<Point> seg = buildPath(newSeed.x, newSeed.y);
            // Ensure segment starts at the previous seed.
            if (!seg.isEmpty() && firstPoint(seg).equals(lastSeed)) {
                finalPaths.add(seg);
            } else {
                // Fallback: prepend previous seed if reconstruction start differs.
                seg.add(0, lastSeed);
                finalPaths.add(seg);
            }
        }
        // Commit seed and refresh shortest-path tree.
        seeds.add(newSeed);
        computePathsFromSeed(newSeed.x, newSeed.y);
    }

    // Mouse listener and motion handling.
    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            requestStopAndOptionalSave();
            return;
        }

        if (closed) return;
        int x = e.getX(), y = e.getY();
        // Clamp to image bounds.
        x = Math.max(0, Math.min(x, originalImage.getWidth() - 1));
        y = Math.max(0, Math.min(y, originalImage.getHeight() - 1));

        if (seeds.isEmpty()) {
            // Place the first seed.
            Point seed = new Point(x, y);
            seeds.add(seed);
            computePathsFromSeed(x, y);
        } else {
            Point firstSeed = firstPoint(seeds);
            // Close contour if click is near the first seed.
            if (firstSeed.distance(x, y) < 5.0 && seeds.size() > 1) {
                // Finalize closing segment.
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
            // Otherwise add a new manual seed.
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

        // Cursor snapping to the strongest edge response in local neighborhood.
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

        // Update live-wire path to the current cursor target.
        currentPath = buildPath(snapX, snapY);
        repaint();
    }

    // Periodic virtual path update for automatic seed insertion.
    private void updateVirtualPath() {
        if (currentPath != null && !currentPath.isEmpty() && !closed) {
            // Use live path endpoint as candidate virtual seed.
            virtualSeed = lastPoint(currentPath);
            List<Point> newVirtualPath = buildPath(virtualSeed.x, virtualSeed.y);
            virtualPaths.add(newVirtualPath);

            // Compare against existing virtual paths.
            for (List<Point> existingPath : virtualPaths) {
                // Skip paths that are too close to avoid over-seeding.
                double distance = virtualSeed.distance(lastPoint(existingPath));

                if (distance > minDistance) {
                    int overlapCount = getOverlapCount(existingPath, newVirtualPath);
                    if (overlapCount >= overlapThreshold) {
                        // Promote overlap endpoint to a confirmed seed.
                        Point newSeed = newVirtualPath.get(Math.min(newVirtualPath.size() - 1, overlapCount - 1));
                        if (!seeds.contains(newSeed)) {
                            addSeed(newSeed);
                        }
                        break; // Stop after first qualifying overlap.
                    }
                }
            }
        }
    }

    // Count common prefix overlap between two paths.
    private int getOverlapCount(List<Point> path1, List<Point> path2) {
        int count = 0;
        int minSize = Math.min(path1.size(), path2.size());
        for (int i = 0; i < minSize; i++) {
            if (path1.get(i).equals(path2.get(i))) {
                count++;
            } else {
                break; // Stop at first mismatch.
            }
        }
        return count;
    }

    // Render source image, finalized contour, live path, and seed markers.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw source image.
        g.drawImage(originalImage, 0, 0, null);

        Graphics2D g2 = (Graphics2D) g;
        // Draw finalized contour segments.
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
        // Draw live-wire segment.
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
        // Draw confirmed seed markers.
        g2.setColor(Color.YELLOW);
        for (Point seed : seeds) {
            g2.fillRect(seed.x - 3, seed.y - 3, 7, 7);
        }
    }

    // Extract selected region and display it in a separate window.
    private void extractRegion() {
        if (finalPaths.isEmpty()) return;
        // Build polygon from finalized contour segments.
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

        // Compute bounded extraction area.
        Rectangle bounds = region.getBounds();
        int minX = Math.max(0, bounds.x);
        int minY = Math.max(0, bounds.y);
        int maxX = Math.min(originalImage.getWidth() - 1, bounds.x + bounds.width);
        int maxY = Math.min(originalImage.getHeight() - 1, bounds.y + bounds.height);
        int w = maxX - minX + 1;
        int h = maxY - minY + 1;
        if (w <= 0 || h <= 0) return;

        // Create output image with transparent background.
        BufferedImage extracted = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                int px = minX + xx, py = minY + yy;
                if (region.contains(px, py)) {
                    extracted.setRGB(xx, yy, originalImage.getRGB(px, py) | 0xFF000000);
                } else {
                    extracted.setRGB(xx, yy, 0x00000000); // Transparent pixel.
                }
            }
        }
        lastExtractedRegion = extracted;

        // Display extracted region in a new dialog window.
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Extracted Region");
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setLayout(new BorderLayout());
            JLabel label = new JLabel(new ImageIcon(extracted));
            f.add(label, BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(this);
            f.setVisible(true);

            int saveChoice = JOptionPane.showConfirmDialog(
                    f,
                    "Save the extracted result as a PNG file?",
                    "Save Extracted Region",
                    JOptionPane.YES_NO_OPTION
            );

            if (saveChoice == JOptionPane.YES_OPTION) {
                saveExtractedRegion(extracted, f);
            }
        });
    }

    private void requestStopAndOptionalSave() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Stop the application now?\nChoose Yes to save extracted result before exit.",
                "Stop Application",
                JOptionPane.YES_NO_CANCEL_OPTION
        );

        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        if (choice == JOptionPane.YES_OPTION) {
            if (lastExtractedRegion == null) {
                int proceed = JOptionPane.showConfirmDialog(
                        this,
                        "No extracted result is currently available. Exit without saving?",
                        "No Result Available",
                        JOptionPane.YES_NO_OPTION
                );
                if (proceed != JOptionPane.YES_OPTION) {
                    return;
                }
            } else {
                boolean saved = saveExtractedRegion(lastExtractedRegion, this);
                if (!saved) {
                    return;
                }
            }
        }

        System.exit(0);
    }

    private boolean saveExtractedRegion(BufferedImage extracted, Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Extracted Region");
        chooser.setSelectedFile(new File("extracted_region.png"));
        int result = chooser.showSaveDialog(parent);

        if (result != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File outFile = chooser.getSelectedFile();
        String name = outFile.getName().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".png")) {
            outFile = new File(outFile.getParentFile(), outFile.getName() + ".png");
        }

        try {
            ImageIO.write(extracted, "png", outFile);
            JOptionPane.showMessageDialog(parent, "Saved to: " + outFile.getAbsolutePath());
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Failed to save image: " + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    // Unused listener callbacks.
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}

    // Priority-queue node used by Dijkstra search.
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
