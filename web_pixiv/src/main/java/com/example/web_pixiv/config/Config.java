package com.example.web_pixiv.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

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
    //jwt token 安全秘钥
    // 使用Base64编码的安全秘钥（推荐）
    public static final String SECRET_KEY_BASE64 = "mFx4uC+gGeZkxKkXzPpI8tTWRbz0S4vQb0uB9HgE6MA="; // 示例秘钥

    // 获取解码后的秘钥字节
    public static byte[] getJwtSecretBytes() {
        byte[] decoded = Base64.getDecoder().decode(SECRET_KEY_BASE64);
        if (decoded.length < 32) {
            throw new IllegalStateException("JWT秘钥长度不足256位");
        }
        return decoded; // 已经是标准长度，不需要截断
    }

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