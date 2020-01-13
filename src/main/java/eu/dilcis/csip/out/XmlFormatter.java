package eu.dilcis.csip.out;

import java.util.Arrays;
import java.util.Enumeration;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * @author  <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *          <a href="https://github.com/carlwilson">carlwilson AT github</a>
 *
 * @version 0.1
 * 
 * Created 17 Nov 2018:20:59:05
 */

enum XmlFormatter {
	INSTANCE;
	private static final String empty = ""; //$NON-NLS-1$
	private static final char spaceChar = ' ';
	private static final String space = new String(new char[] {spaceChar});
	private static final String attOpen = "=\""; //$NON-NLS-1$
	private static final String attClose = "\""; //$NON-NLS-1$
	private static final String eleStartOpen = "<"; //$NON-NLS-1$
	private static final String eleFinishOpen = "</"; //$NON-NLS-1$
	private static final String eleClose = ">"; //$NON-NLS-1$

	static String indent(final int indentCount, final int indentSpaces) {
		int indentSize = indentCount * indentSpaces;
		if (indentSize < 1)
			return empty;
		char[] chars = new char[indentSize];
		Arrays.fill(chars, ' ');
		return new String(chars);
	}

	static String eleStartTag(final String eleName, final Attributes attrs, final NamespaceSupport namespaces) {
		StringBuffer retVal = makeEleTag(eleName, false);
		if ("mets:mets".equals(eleName)) {
			addNsDecs(retVal, namespaces);
		}

		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getQName(i); // Attr name
				if (empty.equals(aName))
					aName = attrs.getLocalName(i);
				eleAttribute(retVal, aName, attrs.getValue(i));
			}
		}
		return retVal.append(eleClose).toString();
	}

	static StringBuffer addNsDecs(final StringBuffer buff, final NamespaceSupport namespaces) {
		for (Enumeration<String> prefixs = namespaces.getPrefixes(); prefixs.hasMoreElements();) {
			final String prefix = prefixs.nextElement();
			if ("xml".equals(prefix)) {
				continue;
			}
			eleAttribute(buff, "xmlns:" + prefix, namespaces.getURI(prefix));
		}
		return buff;
	}

	static StringBuffer addAttributes(final StringBuffer buff, final Attributes attrs) {
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getQName(i); // Attr name
				if (empty.equals(aName))
					aName = attrs.getLocalName(i);
				eleAttribute(buff, aName, attrs.getValue(i));
			}
		}
		return buff;
	}

	private static StringBuffer eleAttribute(final StringBuffer buff, final String attName, final String attValue) {
		buff.append(space);
		buff.append(attName);
		buff.append(attOpen);
		buff.append(attValue);
		buff.append(attClose);
		return buff;
	}

	static String eleEndTag(final String eleName) {
		StringBuffer retVal = makeEleTag(eleName, true);
		retVal.append(eleClose);
		return retVal.toString();
	}

	private static StringBuffer makeEleTag(final String eleName, final boolean isEnd) {
		StringBuffer retVal = (isEnd) ? new StringBuffer(eleFinishOpen) : new StringBuffer(eleStartOpen);
		retVal.append(eleName);
		return retVal;
	}
}
