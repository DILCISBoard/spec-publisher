package eu.dilcis.csip;

import java.util.logging.Logger;

import picocli.CommandLine.Help.Ansi;

public enum ConsoleFormatter {
    INSTANCE;
    private static Logger logger = Logger.getLogger(ConsoleFormatter.class.getName());

    public static final String COL_GREEN = "green";
    public static final String COL_RED = "red";
    public static final String COL_YELLOW = "yellow";
    public static final String COL_ERR = COL_RED;
    public static final String COL_INFO = COL_GREEN;
    public static final String COL_WARN = COL_YELLOW;

    public static final void error(final String message) {
        colourise(message, COL_ERR);
    }

    public static final void info(final String message) {
        colourise(message, COL_INFO);
    }

    public static final void warn(final String message) {
        colourise(message, COL_WARN);
    }

    private static final void colourise(final String message, final String colour) {
        logger.info(Ansi.AUTO.string(String.format("@|%s %s |@", colour, message)));
    }

    public static final void newline() {
        logger.info("");
    }
}
