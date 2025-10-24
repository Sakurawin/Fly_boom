package edu.hitsz.gui;

import edu.hitsz.application.Game;
import edu.hitsz.application.Main;
import edu.hitsz.audio.AudioManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 游戏难度选择界面
 * 
 * @author hitsz
 */
public class DifficultySelectionFrame extends JFrame {
    
    public enum Difficulty {
        EASY("Easy", Color.GREEN),
        NORMAL("Normal", Color.ORANGE), 
        HARD("Hard", Color.RED);
        
        private final String name;
        private final Color color;
        
        Difficulty(String name, Color color) {
            this.name = name;
            this.color = color;
        }
        
        public String getName() {
            return name;
        }
        
        public Color getColor() {
            return color;
        }
    }
    
    public DifficultySelectionFrame() {
        System.out.println("Initializing DifficultySelectionFrame...");
        initializeFrame();
        createComponents();
        System.out.println("DifficultySelectionFrame initialization complete");
    }
    
    private JCheckBox musicCheckBox;
    
    private void initializeFrame() {
        System.out.println("Setting up window properties...");
        setTitle("Aircraft War - Difficulty Selection");
        setSize(400, 350); // 增加高度以容纳音乐选项
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set background color
        getContentPane().setBackground(new Color(240, 248, 255));
        System.out.println("Window properties setup complete");
    }
    
    private void createComponents() {
        System.out.println("Creating components...");
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.CENTER);
        
        // Music option panel
        JPanel musicPanel = createMusicPanel();
        add(musicPanel, BorderLayout.SOUTH);
        
        System.out.println("Components created");
    }
    
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(240, 248, 255));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Aircraft War");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleLabel.setForeground(new Color(25, 25, 112));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JLabel subtitleLabel = new JLabel("Select Difficulty");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(70, 70, 70));
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        return titlePanel;
    }
    
    private JPanel createButtonPanel() {
        System.out.println("Creating button panel...");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        buttonPanel.setBackground(new Color(240, 248, 255));
        
        // Create three difficulty buttons
        System.out.println("Creating difficulty buttons...");
        for (Difficulty difficulty : Difficulty.values()) {
            System.out.println("Creating button: " + difficulty.getName());
            JButton button = createDifficultyButton(difficulty);
            buttonPanel.add(button);
            System.out.println("Button added: " + difficulty.getName());
        }
        
        System.out.println("Button panel created with " + buttonPanel.getComponentCount() + " buttons");
        return buttonPanel;
    }
    
    private JButton createDifficultyButton(Difficulty difficulty) {
        System.out.println("Creating button: " + difficulty.getName() + ", color: " + difficulty.getColor());
        JButton button = new JButton(difficulty.getName());
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setBackground(difficulty.getColor());
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 50));
        button.setOpaque(true);
        
        // Add hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(difficulty.getColor().brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(difficulty.getColor());
            }
        });
        
        // Add click event
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Button clicked: " + difficulty.getName());
                startGame(difficulty);
            }
        });
        
        System.out.println("Button created: " + difficulty.getName());
        return button;
    }
    
    private JPanel createMusicPanel() {
        JPanel musicPanel = new JPanel();
        musicPanel.setBackground(new Color(240, 248, 255));
        musicPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        musicPanel.setLayout(new BorderLayout());
        
        // Music checkbox
        musicCheckBox = new JCheckBox("Enable Music & Sound Effects", true);
        musicCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        musicCheckBox.setBackground(new Color(240, 248, 255));
        musicCheckBox.setForeground(new Color(25, 25, 112));
        
        // Info label
        JLabel infoLabel = new JLabel("Note: All difficulties have same game mechanics in current version");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(128, 128, 128));
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        
        musicPanel.add(musicCheckBox, BorderLayout.NORTH);
        musicPanel.add(infoLabel, BorderLayout.SOUTH);
        
        return musicPanel;
    }
    
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(240, 248, 255));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JLabel infoLabel = new JLabel("Note: All difficulties have same game mechanics in current version");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(128, 128, 128));
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        
        infoPanel.add(infoLabel);
        return infoPanel;
    }
    
    private void startGame(Difficulty difficulty) {
        // Hide difficulty selection interface
        this.setVisible(false);
        
        // Set music preference
        AudioManager.getInstance().setMusicEnabled(musicCheckBox.isSelected());
        
        // Create game window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame gameFrame = new JFrame("Aircraft War - " + difficulty.getName() + " Mode");
        gameFrame.setSize(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        gameFrame.setResizable(false);
        gameFrame.setBounds(((int) screenSize.getWidth() - Main.WINDOW_WIDTH) / 2, 0,
                Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Use AtomicReference to solve variable access problem in Lambda expressions
        AtomicReference<Game> gameRef = new AtomicReference<>();
        
        // Create game instance with difficulty and callback
        Game game = new Game(difficulty, musicCheckBox.isSelected(), () -> {
            // Game over callback: close game window, show game over interface
            SwingUtilities.invokeLater(() -> {
                gameFrame.dispose();
                Game currentGame = gameRef.get();
                if (currentGame != null) {
                    new GameOverFrame(currentGame.getFinalScore(), currentGame.getGameDuration()).setVisible(true);
                }
            });
        });
        
        gameRef.set(game); // Set reference
        
        gameFrame.add(game);
        gameFrame.setVisible(true);
        game.action();
        
        // Close difficulty selection window
        this.dispose();
    }
}