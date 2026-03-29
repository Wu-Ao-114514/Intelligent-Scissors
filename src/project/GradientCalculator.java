package project;

import java.awt.image.BufferedImage;

/**
 * Utility class for Sobel-based gradient magnitude computation.
 */
public class GradientCalculator {
    /**
     * Computes gradient magnitude for the input image using Sobel kernels.
     *
     * @param img source image
     * @return 2D gradient magnitude map indexed by [x][y]
     */
    public static double[][] computeGradient(BufferedImage img) {
        int width = img.getWidth(); // Image width
        int height = img.getHeight(); // Image height
        double[][] grad = new double[width][height]; // Gradient magnitude output

        // Sobel kernels
        int[][] sx = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}}; // Horizontal gradient kernel
        int[][] sy = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}}; // Vertical gradient kernel

        // Convert to luminance and apply convolution on the inner region.
        for (int x = 1; x < width - 1; x++) { // Skip border pixels
            for (int y = 1; y < height - 1; y++) { // Skip border pixels
                double gx = 0, gy = 0; // Accumulate horizontal/vertical responses
                for (int dx = -1; dx <= 1; dx++) { // Iterate 3x3 neighborhood
                    for (int dy = -1; dy <= 1; dy++) {
                        int rgb = img.getRGB(x + dx, y + dy); // Source RGB value
                        int r = (rgb >> 16) & 0xFF; // Red channel
                        int g = (rgb >> 8) & 0xFF;  // Green channel
                        int b = rgb & 0xFF;         // Blue channel
                        double gray = 0.299 * r + 0.587 * g + 0.114 * b; // Luminance
                        gx += gray * sx[dy + 1][dx + 1]; // Horizontal response
                        gy += gray * sy[dy + 1][dx + 1]; // Vertical response
                    }
                }
                grad[x][y] = Math.hypot(gx, gy); // Gradient magnitude
            }
        }
        return grad; // Final gradient field
    }
}
