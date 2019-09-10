package eu.dilcis.csip.out;

import java.util.List;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *         <a href="https://github.com/carlwilson">carlwilson AT github</a>
 *
 * @version 0.1
 * 
 *          Created 17 Nov 2018:16:17:30
 */

enum LatexFormatter {
	INSTANCE;
	private final static String empty = ""; //$NON-NLS-1$
	private final static String space = " "; //$NON-NLS-1$
	private static final String vtop = "\\vtop";
	private static final String hbox = "\\hbox";
	private static final String strut = "\\strut ";
	private static final String textbf = "\\textbf";
	private static final String texttt = "\\texttt";

	static String texttt(final String val) {
		StringBuffer buff = new StringBuffer(texttt);
		buff = parenthesise(buff, val);
		return buff.toString();
	}

	private static String hbox(final String val) {
		StringBuffer buff = new StringBuffer(hbox);
		buff = parenthesise(buff, strut(val));
		return buff.toString();
	}

	private static String strut(final String val) {
		StringBuffer buff = new StringBuffer(strut);
		buff.append(val);
		return buff.toString();
	}

	static String pandocTableLine(final String value) {
		StringBuffer buff = new StringBuffer(vtop);
		buff = parenthesise(buff, hbox(value));
		return buff.toString();
	}

	static String pandocTableLines(final List<String> values) {
		StringBuffer buff = new StringBuffer(vtop);
		StringBuffer valBuffer = new StringBuffer();
		for (String value : values) {
			valBuffer.append(hbox(value));
		}
		buff = parenthesise(buff, valBuffer.toString());
		return buff.toString();
	}

	static String makePandocBold(final String toBold) {
		if (toBold == null || toBold.isEmpty())
			return empty;
		StringBuffer buff = new StringBuffer(textbf);
		buff = parenthesise(buff, toBold);
		return buff.toString();
	}

	static String concatDescription(List<String> description) {
		if (description.isEmpty())
			return space;
		StringBuffer buff = new StringBuffer(description.get(0));
		for (int i = 1; i < description.size(); i++) {
			buff.append(description.get(i));
		}
		return buff.toString();
	}

	private static StringBuffer parenthesise(final StringBuffer buff, final String value) {
		return buff.append("{").append(value).append("}");
	}
}
