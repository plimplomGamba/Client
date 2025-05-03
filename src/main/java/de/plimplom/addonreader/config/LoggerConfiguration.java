package de.plimplom.addonreader.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import de.plimplom.addonreader.util.FileUtils;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class LoggerConfiguration {
    public static void configureLogger() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        // Create console appender
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("CONSOLE");

        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(context);
        consoleEncoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n");
        consoleEncoder.setCharset(StandardCharsets.UTF_8);
        consoleEncoder.start();

        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.start();

        // Create file appender
        String logDir = FileUtils.getAppDataPath() + "/logs";
        FileUtils.createDirectoryIfNotExists(logDir);

        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setName("FILE");
        fileAppender.setFile(logDir + "/application.log");

        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(context);
        fileEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        fileEncoder.setCharset(StandardCharsets.UTF_8);
        fileEncoder.start();

        fileAppender.setEncoder(fileEncoder);

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(logDir + "/application.%d{yyyy-MM-dd}.log");
        rollingPolicy.setMaxHistory(7); // Keep logs for 7 days
        rollingPolicy.start();

        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.start();

        // Set root logger level and appenders
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);

        // Set specific logger levels
        context.getLogger("de.plimplom.addonreader").setLevel(Level.DEBUG);
    }
}
