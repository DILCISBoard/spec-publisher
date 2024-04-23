package eu.dilcis.csip.profile;

import org.xml.sax.Attributes;

public final class Utilities {
    private Utilities() {
        throw new UnsupportedOperationException("Utility class");
    }

    static String getId(final Attributes attrs) {
        return getAttValue(attrs, XmlConstants.ID_ATT);
    }

    static String getNumber(final Attributes attrs) {
        return getAttValue(attrs, XmlConstants.NUMBER_ATT);
    }

    static String getLabel(final Attributes attrs) {
        return getAttValue(attrs, XmlConstants.LABEL_ATT);
    }

    static String getHref(final Attributes attrs) {
        return getAttValue(attrs, "href");
    }

    static String getAttValue(final Attributes attrs,
            final String attName) {
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name
                if (Constants.EMPTY.equals(aName))
                    aName = attrs.getQName(i);
                if (attName.equals(aName))
                    return attrs.getValue(i);
            }
        }
        return Constants.EMPTY;
    }

}
