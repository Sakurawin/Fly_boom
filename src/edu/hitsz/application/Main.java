package edu.hitsz.application;

import edu.hitsz.gui.DifficultySelectionFrame;

import javax.swing.*;
import java.awt.*;

/**
 * 程序入口
 * @author hitsz
 */
public class Main {

    public static final int WINDOW_WIDTH = 512;
    public static final int WINDOW_HEIGHT = 768;

    public static void main(String[] args) {

        System.out.println("Hello Aircraft War");
        
        // 检查是否支持GUI
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Error: 当前环境不支持GUI界面，请在图形界面环境下运行");
            return;
        }

        // 启动难度选择界面
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("正在创建难度选择界面...");
                DifficultySelectionFrame frame = new DifficultySelectionFrame();
                System.out.println("难度选择界面创建成功，正在显示...");
                frame.setVisible(true);
                System.out.println("难度选择界面已显示");
            } catch (Exception e) {
                System.err.println("创建难度选择界面时出错: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
