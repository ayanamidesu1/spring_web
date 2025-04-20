package com.example.web_pixiv.logger;

import com.example.web_pixiv.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 增强版请求感知日志记录器 (Spring Boot 兼容版)
 */
public final class RequestLogger {
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static final Logger LOG = LoggerFactory.getLogger(RequestLogger.class);


    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }

    /**
     * 创建所有需要的日志目录
     */
    private static void createLogDirs() {
        try {
            Files.createDirectories(Path.of(Config.Log.LOG_DIR));
            LOG.info("Created log directory: {}", Config.Log.LOG_DIR);
        } catch (IOException e) {
            LOG.error("Failed to create log directory: {}", Config.Log.LOG_DIR, e);
        }
    }

    /**
     * 确保日志文件存在
     */
    private static Path ensureLogFile(Path file) {
        try {
            if (!Files.exists(file)) {
                Path parent = file.getParent();
                if (!Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Files.createFile(file);
                LOG.debug("Created log file: {}", file);
            }
            return file;
        } catch (IOException e) {
            LOG.error("Failed to create log file: {}", file, e);
            return file; // 仍然返回原路径，让后续写入尝试处理错误
        }
    }

    /**
     * 记录带请求上下文的日志
     */
    public static void log(Level level, String message) {
        getCurrentRequest().ifPresentOrElse(
                request -> logWithRequest(level, message, request),
                () -> logWithoutRequest(level, message)
        );
    }
    // 使用请求上下文
    private static void logWithRequest(Level level, String message, Object request) {
        String logEntry = buildLogEntry(level, message, request);
        writeToFile(getLogFile(level), logEntry);
        writeToConsole(level, logEntry);
    }
    //不使用请求上下文
    private static void logWithoutRequest(Level level, String message) {
        String logEntry = buildSimpleEntry(level, message);
        writeToFile(getLogFile(level), logEntry);
        writeToConsole(level, logEntry);
    }
    // 获取当前请求上下文
    private static Optional<Object> getCurrentRequest() {
        try {
            return Optional.of(
                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
            );
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }
    // 构建带请求上下文的日志条目
    private static String buildLogEntry(Level level, String message, Object request) {
        try {
            String ip = (String) request.getClass().getMethod("getRemoteAddr").invoke(request);
            String uri = (String) request.getClass().getMethod("getRequestURI").invoke(request);
            String method = (String) request.getClass().getMethod("getMethod").invoke(request);

            return String.format("[%s] [%s] [IP: %s] [URI: %s] [Method: %s] %s%n",
                    TIME_FORMATTER.format(LocalDateTime.now()),
                    level.name(),
                    ip,
                    uri,
                    method,
                    message
            );
        } catch (Exception e) {
            LOG.error("Failed to extract request info", e);
            return buildSimpleEntry(level, message);
        }
    }
    // 构建不带请求上下文的日志条目
    private static String buildSimpleEntry(Level level, String message) {
        return String.format("[%s] [%s] %s%n",
                TIME_FORMATTER.format(LocalDateTime.now()),
                level.name(),
                message
        );
    }
    // 获取日志文件
    private static Path getLogFile(Level level) {
        return Path.of(switch (level) {
            case DEBUG -> Config.Log.DEBUG_FILE;
            case INFO -> Config.Log.INFO_FILE;
            case WARN -> Config.Log.WARNING_FILE;
            case ERROR -> Config.Log.ERROR_FILE;
        });
    }
    // 写入日志文件
    private static synchronized void writeToFile(Path file, String content) {
        try {
            Path targetFile = ensureLogFile(file);
            Files.writeString(targetFile, content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            // 如果文件写入失败，尝试一次恢复操作
            try {
                Path targetFile = ensureLogFile(file);
                Files.writeString(targetFile, content,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            } catch (IOException ex) {
                LOG.error("Retry failed to write log file: {}", file, ex);
                // 最终回退到系统错误输出
                System.err.printf("[FALLBACK LOG] %s", content);
            }
        }
    }
    // 写入控制台
    private static void writeToConsole(Level level, String message) {
        switch (level) {
            case DEBUG -> LOG.debug(message.trim());
            case INFO -> LOG.info(message.trim());
            case WARN -> LOG.warn(message.trim());
            case ERROR -> LOG.error(message.trim());
        }
    }

    // 快捷方法
    public static void info(String msg) {
        log(Level.INFO, msg);
    }

    public static void error(String msg) {
        log(Level.ERROR, msg);
    }
    public static void warn(String msg) {
        log(Level.WARN, msg);
    }
    public static void debug(String msg) {
        log(Level.DEBUG, msg);
    }
}