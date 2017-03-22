package me.blog.korn123.easyphotomap.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.mindpipe.android.logging.log4j.LogConfigurator;
import me.blog.korn123.easyphotomap.constant.Constant;

/**
 * Created by CHO HANJOONG on 2016-01-12.
 */
public class AAFLogger {

    static boolean isInit = false;

    private static void init() {
        final LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(Constant.LOG_DIRECTORY + "AAFactory.log");
        logConfigurator.setRootLevel(Level.INFO);
        logConfigurator.setLevel("org.apache", Level.INFO);
//        logConfigurator.setFilePattern("%d[%l]%-5p: %m%n");
        logConfigurator.setFilePattern("%d{ISO8601} [%t] %c %x %-5p %m%n");
//        default 512 * 1024
//        logConfigurator.setMaxFileSize(1024 * 512);
        logConfigurator.configure();
    }

    public static void info(String message, Class c) {
//        Logger.getLogger(c).info(message);
        if (!isInit) {
            init();
            isInit = true;
        }
        Logger.getLogger(c).info(message);
    }

    public static void error(String message, Class c) {
        if (!isInit) {
            init();
            isInit = true;
        }
        Logger.getLogger(c).error(message);
    }
}
