package net;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingHandler {


    /**
     * if logging is enabled, this method sets up the log levels and locations
     */
    public static void enableLogging(boolean b) {
        if (b) {
            //get access to the root logger
            ConfigurationBuilder<BuiltConfiguration> builder =
                ConfigurationBuilderFactory.newConfigurationBuilder();
            RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.ALL);

            //set filter for the console output
            FilterComponentBuilder threshold =
                builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT,
                    Filter.Result.DENY);
            threshold.addAttribute("level", Level.WARN);

            //add appender for the console
            AppenderComponentBuilder stdout = builder.newAppender("stdout", "Console");
            stdout.add(threshold);


            //add appender for file
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");
            String date = LocalDateTime.now().format(format);
            File logDir = new File(System.getProperty("user.dir") + "/logs/");
            if (!logDir.exists()) {
                logDir.mkdir();
            }
            AppenderComponentBuilder fileAppender =
                builder.newAppender("logfile", "File");
            fileAppender.addAttribute("fileName", "logs/" + date + ".log");


            //add format for logging statements
            LayoutComponentBuilder layout = builder.newLayout("PatternLayout");
            layout.addAttribute("pattern",
                "%d %highlight{[%t] %-5level: %c %msg%n%throwable}");
            fileAppender.add(layout);
            stdout.add(layout);

            //add appenders to root logger and initialize
            builder.add(stdout);
            builder.add(fileAppender);
            rootLogger.add(builder.newAppenderRef("logfile"));
            rootLogger.add(builder.newAppenderRef("stdout"));
            builder.add(rootLogger);
            Configurator.initialize(builder.build());

        } else {
            //disable all logging
            Configurator.setRootLevel(Level.OFF);
        }
    }

}
