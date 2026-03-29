package project;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Application entry point for the Intelligent Scissors demo.
 *
 * Responsibilities:
 * - Prompt the user to choose an image file.
 * - Initialize the Swing UI.
 * - Wire controls to the segmentation panel.
 */
public class IntelligentScissors {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Open a file chooser to select the input image.
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Image");
            int ret = fileChooser.showOpenDialog(null);
            if (ret != JFileChooser.APPROVE_OPTION) {
                System.exit(0); // Exit if no file is selected.
            }
            File file = fileChooser.getSelectedFile();
            BufferedImage image = null;
            try {
                image = ImageIO.read(file); // Read selected image file.
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to load image: " + e.getMessage());
                System.exit(1); // Exit on load failure.
            }
            if (image == null) {
                JOptionPane.showMessageDialog(null, "Unsupported image format.");
                System.exit(1); // Exit if decoded image is null.
            }

            // Create main window.
            JFrame frame = new JFrame("Intelligent Scissors");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Build the interactive segmentation panel.
            ImagePanel imgPanel = new ImagePanel(image);

            // Slider controls cursor snap radius (0 disables snapping).
            JSlider snapSlider = new JSlider(0, 10, 5);
            snapSlider.setPaintTicks(true); // Render tick marks.
            snapSlider.setPaintLabels(true); // Render tick labels.
            snapSlider.setMajorTickSpacing(1); // Tick spacing of 1.
            snapSlider.setBorder(BorderFactory.createTitledBorder("Cursor Snap Radius (0 = Off)"));
            snapSlider.addChangeListener(event -> {
                int radius = snapSlider.getValue(); // Read current slider value.
                imgPanel.setSnapRadius(radius); // Apply snapping radius to panel.
            });

            // Layout configuration.
            frame.setLayout(new BorderLayout());
            frame.add(imgPanel, BorderLayout.CENTER); // Main drawing area.
            frame.add(snapSlider, BorderLayout.SOUTH); // Control strip.

            // Finalize and display.
            frame.pack();
            frame.setLocationRelativeTo(null); // Center on screen.
            frame.setVisible(true);
        });
    }
}