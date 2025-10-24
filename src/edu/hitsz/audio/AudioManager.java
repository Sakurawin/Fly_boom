package edu.hitsz.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 音频管理器
 * 负责播放背景音乐和音效
 * 
 * @author hitsz
 */
public class AudioManager {
    
    private static AudioManager instance;
    private final ExecutorService audioExecutor;
    private final ConcurrentHashMap<String, Clip> clips;
    private boolean musicEnabled;
    
    // 音频文件路径
    private static final String AUDIO_PATH = "src/videos/";
    
    // 音频文件名
    public static final String BGM_GAME = "bgm.wav";
    public static final String BGM_BOSS = "bgm_boss.wav";
    public static final String SOUND_BULLET = "bullet.wav";
    public static final String SOUND_HIT = "bullet_hit.wav";
    public static final String SOUND_BOMB = "bomb_explosion.wav";
    public static final String SOUND_SUPPLY = "get_supply.wav";
    public static final String SOUND_GAME_OVER = "game_over.wav";
    
    private Clip currentBgm;
    
    private AudioManager() {
        this.audioExecutor = Executors.newCachedThreadPool();
        this.clips = new ConcurrentHashMap<>();
        this.musicEnabled = true;
        preloadSounds();
    }
    
    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * 预加载音效文件
     */
    private void preloadSounds() {
        String[] soundFiles = {
            SOUND_BULLET, SOUND_HIT, SOUND_BOMB, 
            SOUND_SUPPLY, SOUND_GAME_OVER
        };
        
        for (String soundFile : soundFiles) {
            loadSound(soundFile);
        }
    }
    
    /**
     * 加载音频文件
     */
    private void loadSound(String fileName) {
        try {
            File audioFile = new File(AUDIO_PATH + fileName);
            if (!audioFile.exists()) {
                System.err.println("Audio file not found: " + audioFile.getAbsolutePath());
                return;
            }
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clips.put(fileName, clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound: " + fileName + " - " + e.getMessage());
        }
    }
    
    /**
     * 播放背景音乐（循环）
     */
    public void playBackgroundMusic(String fileName) {
        if (!musicEnabled) return;
        
        audioExecutor.submit(() -> {
            try {
                // 停止当前背景音乐
                stopBackgroundMusic();
                
                File audioFile = new File(AUDIO_PATH + fileName);
                if (!audioFile.exists()) {
                    System.err.println("Background music file not found: " + audioFile.getAbsolutePath());
                    return;
                }
                
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                currentBgm = AudioSystem.getClip();
                currentBgm.open(audioInputStream);
                currentBgm.loop(Clip.LOOP_CONTINUOUSLY);
                currentBgm.start();
            } catch (Exception e) {
                System.err.println("Error playing background music: " + fileName + " - " + e.getMessage());
            }
        });
    }
    
    /**
     * 停止背景音乐
     */
    public void stopBackgroundMusic() {
        if (currentBgm != null && currentBgm.isRunning()) {
            currentBgm.stop();
            currentBgm.close();
            currentBgm = null;
        }
    }
    
    /**
     * 播放音效
     */
    public void playSound(String fileName) {
        if (!musicEnabled) return;
        
        audioExecutor.submit(() -> {
            Clip clip = clips.get(fileName);
            if (clip != null) {
                // 重置到开始位置
                clip.setFramePosition(0);
                clip.start();
            } else {
                System.err.println("Sound not found: " + fileName);
            }
        });
    }
    
    /**
     * 设置音乐开关
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopBackgroundMusic();
        }
    }
    
    /**
     * 获取音乐开关状态
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    /**
     * 释放资源
     */
    public void shutdown() {
        stopBackgroundMusic();
        
        for (Clip clip : clips.values()) {
            if (clip != null) {
                clip.close();
            }
        }
        clips.clear();
        
        audioExecutor.shutdown();
    }
}