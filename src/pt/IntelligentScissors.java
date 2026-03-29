package pt;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Main class to initialize the Intelligent Scissors application.
 * Loads an image, sets up the GUI, and handles file opening.
 */
public class IntelligentScissors {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Open a file chooser dialog to select an image
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select an Image");
            int ret = fileChooser.showOpenDialog(null);
            if (ret != JFileChooser.APPROVE_OPTION) {
                System.exit(0);
            }
            File file = fileChooser.getSelectedFile();
            BufferedImage image = null;
            try {
                image = ImageIO.read(file);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error loading image: " + e.getMessage());
                System.exit(1);
            }
            if (image == null) {
                JOptionPane.showMessageDialog(null, "Unsupported image format.");
                System.exit(1);
            }

            // Create main frame
            JFrame frame = new JFrame("Intelligent Scissors");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create ImagePanel with the loaded image
            ImagePanel imgPanel = new ImagePanel(image);

            // Set up cursor snap slider (radius from 0 to 10, corresponding to 1*1 to 10*10 window)
            JSlider snapSlider = new JSlider(0, 10, 5);
            snapSlider.setPaintTicks(true);
            snapSlider.setPaintLabels(true);
            snapSlider.setMajorTickSpacing(1);
            snapSlider.setBorder(BorderFactory.createTitledBorder("Cursor Snap Radius (0=no snap)"));
            snapSlider.addChangeListener(_ -> {
                int radius = snapSlider.getValue();
                imgPanel.setSnapRadius(radius);
            });

            // Layout
            frame.setLayout(new BorderLayout());
            frame.add(imgPanel, BorderLayout.CENTER);
            frame.add(snapSlider, BorderLayout.SOUTH);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

