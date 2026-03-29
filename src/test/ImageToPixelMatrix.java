package test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageToPixelMatrix {

    public static void main(String[] args) {
        String imagePath = "C:\\Users\\hp\\Desktop\\99daacb32c9401ce41991825f6afe16.png"; // 替换为你的图片路径
        try {
            // 1. 加载图像
            BufferedImage image = ImageIO.read(new File(imagePath));

            // 2. 获取图像宽高
            int width = image.getWidth();
            int height = image.getHeight();

            // 3. 创建像素矩阵
            int[][] pixelMatrix = new int[height][width];

            // 4. 遍历图像像素
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixelMatrix[y][x] = image.getRGB(x, y);
                }
            }

            // 5. 输出像素矩阵
            printPixelMatrix(pixelMatrix, height, width);
        } catch (IOException e) {
            System.err.println("无法加载图像: " + e.getMessage());
        }
    }

    private static void printPixelMatrix(int[][] pixelMatrix, int height, int width) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.printf("%08X ", pixelMatrix[y][x]);
            }
            System.out.println();
        }
    }
}
