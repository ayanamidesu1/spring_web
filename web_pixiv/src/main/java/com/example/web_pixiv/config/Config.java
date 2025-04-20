package com.example.web_pixiv.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 系统全局配置（硬编码不可变配置）
 */
public final class Config {

    // 私有构造器防止实例化
    private Config() {
        throw new AssertionError("不允许实例化配置类");
    }

    // 初始化基础路径
    private static final String ROOT_PATH = System.getProperty("user.dir");
    public  static final String DIR=ROOT_PATH;

    // 日志配置
    public static final class Log {
        public static final String LOG_DIR = ensureDirExists(ROOT_PATH + "/logs");
        public static final String INFO_FILE = LOG_DIR + "/info.log";
        public static final String ERROR_FILE = LOG_DIR + "/error.log";
        public static final String WARNING_FILE = LOG_DIR + "/warning.log";
        public static final String DEBUG_FILE = LOG_DIR + "/debug.log";
        public static final int MAX_SIZE_MB = 10;
        public static final int BACKUP_COUNT = 10;
    }

    // 资源路径配置
    public static final class Resource {
        public static final String IMAGE_DIR = ensureDirExists(ROOT_PATH + "/static/img");
        public static final String TEMP_DIR = ensureDirExists(ROOT_PATH + "/temp");
    }

    // 确保目录存在
    private static String ensureDirExists(String path) {
        try {
            Files.createDirectories(Paths.get(path));
            return path;
        } catch (Exception e) {
            throw new IllegalStateException("无法创建目录: " + path, e);
        }
    }
    private static String ensureFileExists(String path) {
        try {
            Path filePath = Path.of(path);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            return path;
        } catch (IOException e) {
            throw new IllegalStateException("无法创建日志文件: " + path, e);
        }
    }
}