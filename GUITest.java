import javax.swing.*;
import java.awt.*;

public class GUITest {
    public static void main(String[] args) {
        System.out.println("Testing GUI support...");
        
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Headless mode detected - GUI not supported");
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("GUI Test");
            frame.setSize(300, 200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            
            JLabel label = new JLabel("GUI Test Successful!", JLabel.CENTER);
            frame.add(label);
            
            frame.setVisible(true);
            System.out.println("GUI window should be visible now");
        });
    }
}