package project;

import java.awt.image.BufferedImage;

/**
 * 实用类，用于使用 Sobel 滤波器计算图像的梯度。
 */
public class GradientCalculator {
    /**
     * 使用 Sobel 算子的方式计算图像的梯度大小。
     * 返回一个二维数组表示梯度大小。
     *
     * @param img 输入的图像
     * @return 计算得到的梯度大小二维数组
     */
    public static double[][] computeGradient(BufferedImage img) {
        int width = img.getWidth(); // 图像宽度
        int height = img.getHeight(); // 图像高度
        double[][] grad = new double[width][height]; // 用于存储梯度值的数组

        // Sobel 核
        int[][] sx = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}}; // X 方向 Sobel 核
        int[][] sy = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}}; // Y 方向 Sobel 核

        // 转换为灰度图并计算梯度
        for (int x = 1; x < width - 1; x++) { // 从 1 到 width-1，避免边界
            for (int y = 1; y < height - 1; y++) { // 从 1 到 height-1，避免边界
                double gx = 0, gy = 0; // 初始化 X 和 Y 方向的梯度
                for (int dx = -1; dx <= 1; dx++) { // 遍历 Sobel 核的 3x3 区域
                    for (int dy = -1; dy <= 1; dy++) {
                        int rgb = img.getRGB(x + dx, y + dy); // 获取当前像素的 RGB 值
                        // 转换为灰度亮度
                        int r = (rgb >> 16) & 0xFF; // 红色通道
                        int g = (rgb >> 8) & 0xFF;  // 绿色通道
                        int b = rgb & 0xFF;         // 蓝色通道
                        double gray = 0.299 * r + 0.587 * g + 0.114 * b; // 计算灰度值
                        gx += gray * sx[dy + 1][dx + 1]; // 计算 X 方向的梯度
                        gy += gray * sy[dy + 1][dx + 1]; // 计算 Y 方向的梯度
                    }
                }
                // 计算当前像素的梯度大小
                grad[x][y] = Math.hypot(gx, gy); // 计算向量的大小
            }
        }
        return grad; // 返回梯度大小数组
    }
}
