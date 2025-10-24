package edu.hitsz.gui;

import edu.hitsz.dao.GameScore;
import edu.hitsz.service.ScoreService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 游戏结束界面
 * 显示排行榜，支持添加成绩和删除记录
 * 
 * @author hitsz
 */
public class GameOverFrame extends JFrame {
    
    private final int finalScore;
    private final int gameDuration;
    private final ScoreService scoreService;
    
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JButton addScoreButton;
    private JButton deleteButton;
    private JButton newGameButton;
    private JButton exitButton;
    
    private static final String[] COLUMN_NAMES = {"排名", "玩家名称", "分数", "游戏时长(秒)", "游戏时间"};
    
    public GameOverFrame(int finalScore, int gameDuration) {
        this.finalScore = finalScore;
        this.gameDuration = gameDuration;
        this.scoreService = new ScoreService();
        
        initializeFrame();
        createComponents();
        loadScoreData();
    }
    
    private void initializeFrame() {
        setTitle("游戏结束 - 最终得分: " + finalScore);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void createComponents() {
        setLayout(new BorderLayout());
        
        // 顶部面板：游戏结果显示
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // 中央面板：排行榜表格
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // 底部面板：操作按钮
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        topPanel.setBackground(new Color(240, 248, 255));
        
        // 游戏结果标题
        JLabel titleLabel = new JLabel("游戏结束!");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(220, 20, 60));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // 分数信息
        JPanel scorePanel = new JPanel(new FlowLayout());
        scorePanel.setBackground(new Color(240, 248, 255));
        
        JLabel scoreLabel = new JLabel("本局得分: " + finalScore + " 分");
        scoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        scoreLabel.setForeground(new Color(25, 25, 112));
        
        JLabel durationLabel = new JLabel("游戏时长: " + gameDuration + " 秒");
        durationLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        durationLabel.setForeground(new Color(25, 25, 112));
        
        scorePanel.add(scoreLabel);
        scorePanel.add(Box.createHorizontalStrut(30));
        scorePanel.add(durationLabel);
        
        // 添加成绩输入面板
        JPanel inputPanel = createScoreInputPanel();
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(scorePanel, BorderLayout.CENTER);
        topPanel.add(inputPanel, BorderLayout.SOUTH);
        
        return topPanel;
    }
    
    private JPanel createScoreInputPanel() {
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setBackground(new Color(240, 248, 255));
        inputPanel.setBorder(BorderFactory.createTitledBorder("添加到排行榜"));
        
        JLabel nameLabel = new JLabel("玩家名称:");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        nameField = new JTextField(15);
        nameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        addScoreButton = new JButton("添加成绩");
        addScoreButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        addScoreButton.setBackground(new Color(34, 139, 34));
        addScoreButton.setForeground(Color.WHITE);
        addScoreButton.setFocusPainted(false);
        
        addScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addScore();
            }
        });
        
        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(addScoreButton);
        
        return inputPanel;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("排行榜"));
        
        // 创建表格
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 不允许编辑
            }
        };
        
        scoreTable = new JTable(tableModel);
        scoreTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        scoreTable.setRowHeight(25);
        scoreTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 设置列宽
        TableColumnModel columnModel = scoreTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);  // 排名
        columnModel.getColumn(1).setPreferredWidth(120); // 玩家名称
        columnModel.getColumn(2).setPreferredWidth(80);  // 分数
        columnModel.getColumn(3).setPreferredWidth(100); // 游戏时长
        columnModel.getColumn(4).setPreferredWidth(150); // 游戏时间
        
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        return centerPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        deleteButton = new JButton("删除选中记录");
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        
        newGameButton = new JButton("新游戏");
        newGameButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        newGameButton.setBackground(new Color(70, 130, 180));
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setFocusPainted(false);
        
        exitButton = new JButton("退出游戏");
        exitButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        exitButton.setBackground(new Color(128, 128, 128));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        
        // 添加事件监听器
        deleteButton.addActionListener(e -> deleteSelectedRecord());
        newGameButton.addActionListener(e -> startNewGame());
        exitButton.addActionListener(e -> System.exit(0));
        
        bottomPanel.add(deleteButton);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(newGameButton);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(exitButton);
        
        return bottomPanel;
    }
    
    private void loadScoreData() {
        // 清空表格
        tableModel.setRowCount(0);
        
        // 获取排行榜数据
        List<GameScore> scores = scoreService.getAllScores();
        
        // 按分数排序（降序）
        scores.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));
        
        // 添加到表格
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < scores.size(); i++) {
            GameScore score = scores.get(i);
            Object[] rowData = {
                i + 1,  // 排名
                score.getPlayerName(),
                score.getScore(),
                score.getDuration(),
                dateFormat.format(java.sql.Timestamp.valueOf(score.getGameTime()))
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void addScore() {
        String playerName = nameField.getText().trim();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入玩家名称!", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        boolean success = scoreService.saveGameScore(finalScore, playerName, gameDuration);
        if (success) {
            JOptionPane.showMessageDialog(this, "成绩添加成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
            nameField.setText(""); // 清空输入框
            loadScoreData(); // 刷新表格
        } else {
            JOptionPane.showMessageDialog(this, "成绩添加失败!", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteSelectedRecord() {
        int selectedRow = scoreTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的记录!", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String playerName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int option = JOptionPane.showConfirmDialog(this, 
            "确定要删除玩家 \"" + playerName + "\" 的所有记录吗?", 
            "确认删除", 
            JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            boolean success = scoreService.deletePlayerRecords(playerName);
            if (success) {
                JOptionPane.showMessageDialog(this, "删除成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadScoreData(); // 刷新表格
            } else {
                JOptionPane.showMessageDialog(this, "删除失败!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void startNewGame() {
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            new DifficultySelectionFrame().setVisible(true);
        });
    }
}