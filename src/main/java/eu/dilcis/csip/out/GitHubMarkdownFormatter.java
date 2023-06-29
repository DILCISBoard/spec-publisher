package eu.dilcis.csip.out;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *         <a href="https://github.com/carlwilson">carlwilson AT github</a>
 *
 * @version 0.1
 * 
 *          Created 17 Nov 2018:16:17:30
 */

enum GitHubMarkdownFormatter {
    INSTANCE;

    // Markdown Tags
    private static final String EMPTY = ""; //$NON-NLS-1$
    private static final String SPACE = " "; //$NON-NLS-1$
    private static final String cellDiv = "|"; //$NON-NLS-1$
    private static final String cellDivCls = SPACE + cellDiv; // $NON-NLS-1$
    private static final String cellDivOpen = cellDiv + SPACE; // $NON-NLS-1$
    private static final char hyphen = '-';
    private static final String mdBoldMarker = "**"; //$NON-NLS-1$
    private static final String mdConsoleMarker = "`"; //$NON-NLS-1$
    static final String mdInlineMarker = "```"; //$NON-NLS-1$
    private static final String XML = "xml"; //$NON-NLS-1$
    static final String mdInlineXml = mdInlineMarker + XML;

    // HTML Tags
    private static final String anchorOpen = "<a name=\""; //$NON-NLS-1$
    private static final String anchorClose = "\"></a>"; //$NON-NLS-1$
    private static final String hrefEleStart = " <a href=\""; //$NON-NLS-1$
    static final String htmlBr = " <br/> "; //$NON-NLS-1$

    static String makeBold(final String toBold) {
        if (toBold == null || toBold.isEmpty())
            return EMPTY;
        StringBuilder buff = new StringBuilder(mdBoldMarker);
        buff.append(toBold);
        buff.append(mdBoldMarker);
        return buff.toString();
    }

    private static String makePandocBold(final String toBold) {
        if (toBold == null || toBold.isEmpty())
            return EMPTY;
        StringBuilder buff = new StringBuilder("\\\\textbf{");
        buff.append(toBold);
        buff.append("}");
        return buff.toString();
    }

    static String makeConsole(final String toConsole) {
        if (toConsole == null || toConsole.isEmpty())
            return EMPTY;
        StringBuilder buff = new StringBuilder(mdConsoleMarker);
        buff.append(toConsole);
        buff.append(mdConsoleMarker);
        return buff.toString();
    }

    static String anchorCell(final String cellVal) {
        return anchorCell(cellVal, false);
    }

    static String anchorCell(final String cellVal, final boolean isFirst) {
        StringBuilder buff = new StringBuilder(anchor(cellVal));
        buff.append(makeBold(cellVal));
        return cell(buff.toString(), isFirst);
    }

    static String anchor(final String val) {
        StringBuilder buff = new StringBuilder(anchorOpen);
        buff.append(val);
        buff.append(anchorClose);
        return buff.toString();
    }

    static String h1(final String heading) {
        StringBuilder buff = new StringBuilder("# ");
        buff.append(heading);
        return buff.toString();
    }

    static String h2(final String heading) {
        StringBuilder buff = new StringBuilder("## ");
        buff.append(heading);
        return buff.toString();
    }

    static String h3(final String heading) {
        StringBuilder buff = new StringBuilder("### ");
        buff.append(heading);
        return buff.toString();
    }

    static String h4(final String heading) {
        StringBuilder buff = new StringBuilder("#### ");
        buff.append(heading);
        return buff.toString();
    }

    static String cell(final String cellVal) {
        return cell(cellVal, false);
    }

    static String cell(final String cellVal, boolean isFirst) {
        StringBuilder buff = (isFirst) ? new StringBuilder(cellDivOpen) : new StringBuilder(SPACE);
        buff.append(cellVal);
        buff.append(cellDivCls);
        return buff.toString();
    }

    static String makeHeadingLines(final String heading) {
        int len = (heading == null || heading.isEmpty()) ? 1 : heading.length();
        char[] chars = new char[len];
        Arrays.fill(chars, hyphen);
        return new String(chars);
    }

    static String concatDescription(List<String> description) {
        if (description.isEmpty())
            return SPACE;
        StringBuilder buff = new StringBuilder(description.get(0));
        for (int i = 1; i < description.size(); i++) {
            buff.append(htmlBr);
            buff.append(description.get(i));
        }
        return buff.toString();
    }

    static String href(final String href, final String textVal) {
        StringBuilder buff = new StringBuilder("["); //$NON-NLS-1$
        buff.append(textVal + "](");
        buff.append(href + ")");
        return buff.toString();
    }
}
