package pt;

import java.awt.image.BufferedImage;

/**
 * Utility class to compute image gradients using Sobel filters.
 */
public class GradientCalculator {
    /**
     * Computes the gradient magnitude of the image using Sobel operators.
     * Returns a 2D array of gradient magnitudes.
     */
    public static double[][] computeGradient(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        double[][] grad = new double[width][height];

        // Sobel kernels
        int[][] sx = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sy = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        // Convert to grayscale and compute gradients
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                double gx = 0, gy = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int rgb = img.getRGB(x + dx, y + dy);
                        // Convert to grayscale luminance
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        double gray = 0.299*r + 0.587*g + 0.114*b;
                        gx += gray * sx[dy+1][dx+1];
                        gy += gray * sy[dy+1][dx+1];
                    }
                }
                grad[x][y] = Math.hypot(gx, gy);
            }
        }
        return grad;
    }
}

