package eu.dilcis.csip.profile;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * Abstract class to handle the pain of XML element generation, e.g.
 * indentation.
 * 
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *         <a href="https://github.com/carlwilson">carlwilson AT github</a>
 *
 * @version 0.1
 * 
 *          Created 17 Nov 2018:20:59:46
 */

abstract class XmlFragmentGenerator {
    protected static final int DEFAULT_SPACES = 2;
    protected static final int DEFAULT_INDENT = 0;
    private final int indentSpaces;
    private int indent;
    protected final List<String> content = new ArrayList<>();

    /**
     * Constructs a default {@link XmlFragmentGenerator} instance with
     * {@link XmlFragmentGenerator.defaultSpaces} spaces and a
     * {@link XmlFragmentGenerator.defaultIndent} starting indent point.
     */
    protected XmlFragmentGenerator() {
        this(DEFAULT_SPACES);
    }

    protected XmlFragmentGenerator(final int indentSpaces) {
        this(indentSpaces, DEFAULT_INDENT);
    }

    protected XmlFragmentGenerator(final int indentSpaces, final int indent) {
        this.indentSpaces = indentSpaces;
        this.indent = indent;
    }

    public void eleStart(final String eleName, final Attributes attrs, final NamespaceSupport namespaces) {
        this.content.add(XmlFormatter.indent(this.indent, this.indentSpaces)
                + XmlFormatter.eleStartTag(eleName, attrs, namespaces));
        this.indent++;
    }

    public void eleEnd(final String eleName, final String eleVal) {
        if (eleVal != null && !eleVal.trim().isEmpty()) {
            this.content.add(XmlFormatter.indent(this.indent, this.indentSpaces) + eleVal.trim());
        }
        this.indent--;
        this.content.add(XmlFormatter.indent(this.indent, this.indentSpaces) + XmlFormatter.eleEndTag(eleName));
    }
}
