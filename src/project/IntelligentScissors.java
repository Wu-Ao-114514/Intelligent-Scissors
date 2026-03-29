package project;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * 主类，用于初始化智能剪刀应用。
 * 加载图像，设置 GUI，并处理文件打开操作。
 */
public class IntelligentScissors {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 打开文件选择对话框以选择图像
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择图像");
            int ret = fileChooser.showOpenDialog(null);
            if (ret != JFileChooser.APPROVE_OPTION) {
                System.exit(0); // 如果未选择文件则退出程序
            }
            File file = fileChooser.getSelectedFile();
            BufferedImage image = null;
            try {
                image = ImageIO.read(file); // 读取所选图像文件
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "加载图像时出错: " + e.getMessage());
                System.exit(1); // 如果加载图像失败则退出
            }
            if (image == null) {
                JOptionPane.showMessageDialog(null, "不支持的图像格式。");
                System.exit(1); // 如果图像为空则提示错误并退出
            }

            // 创建主窗口
            JFrame frame = new JFrame("智能剪刀");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // 使用加载的图像创建 ImagePanel
            ImagePanel imgPanel = new ImagePanel(image);

            // 设置光标快照滑块（半径从 0 到 10，对应于 1*1 到 10*10 的窗口）
            JSlider snapSlider = new JSlider(0, 10, 5);
            snapSlider.setPaintTicks(true); // 显示刻度线
            snapSlider.setPaintLabels(true); // 显示刻度标签
            snapSlider.setMajorTickSpacing(1); // 主刻度间距为 1
            snapSlider.setBorder(BorderFactory.createTitledBorder("光标捕捉半径（0=不捕捉）")); // 设置标题
            snapSlider.addChangeListener(event -> {
                int radius = snapSlider.getValue(); // 获取滑块当前值
                imgPanel.setSnapRadius(radius); // 设置 ImagePanel 的快照半径
            });

            // 布局设置
            frame.setLayout(new BorderLayout());
            frame.add(imgPanel, BorderLayout.CENTER); // 将图像面板添加到中心
            frame.add(snapSlider, BorderLayout.SOUTH); // 将滑块添加到底部

            // 窗口包装和显示
            frame.pack();
            frame.setLocationRelativeTo(null); // 窗口居中显示
            frame.setVisible(true);
        });
    }
}