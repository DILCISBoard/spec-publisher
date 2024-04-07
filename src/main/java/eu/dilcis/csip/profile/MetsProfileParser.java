package eu.dilcis.csip.profile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;

import eu.dilcis.csip.out.OutputHandler;
import eu.dilcis.csip.out.XmlCharBuffer;
import eu.dilcis.csip.profile.MetsProfile.Details;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *         <a href="https://github.com/carlwilson">carlwilson AT github</a>
 *
 * @version 0.1
 *
 *          Created 24 Oct 2018:01:25:39
 */

public final class MetsProfileParser extends DefaultHandler {
    private static final SAXParserFactory spf = SAXParserFactory.newInstance();
    static {
        spf.setNamespaceAware(true);
    }
    static final SAXParser saxParser;
    static {
        try {
            saxParser = spf.newSAXParser();
        } catch (ParserConfigurationException | SAXException excep) {
            throw new IllegalStateException(Constants.INIT_SAX_MESS, excep);
        }
    }
    public static final MetsProfileParser newInstance() {
        return new MetsProfileParser();
    }
    static final MetsProfileParser getInstance() {
        return new MetsProfileParser();
    }
    private final XmlCharBuffer charBuff = new XmlCharBuffer();
    private String currEleName;
    private boolean inRequirement = false;
    private boolean inExtSchema = false;
    private String currDefTerm = null;
    private NamespaceSupport namespaces = new NamespaceSupport();
    private Requirement.Builder reqBuilder = new Requirement.Builder();
    private Section currentSect;
    private boolean needNewContext = true;

    private String currentHref = null;
    private ExternalSchema.Builder schemaBuilder;
    private URI profileUri;

    private String profileTitle;

    private final List<Requirement> requirements = new ArrayList<>();

    private MetsProfileParser() {
        super();
    }

    public MetsProfile processXmlProfile(final Path profilePath) throws SAXException, IOException {
        if (profilePath == null)
            throw new IllegalArgumentException(String.format(Constants.MESS_NULL_PARAM, "profilePath", "Path"));
        if (Files.isDirectory(profilePath))
            throw new IllegalArgumentException(String.format(Constants.MESS_NULL_PARAM, "profilePath", "File"));
        this.initialise();
        saxParser.parse(profilePath.toFile(), this);
        return MetsProfile.fromValues(Details.fromValues(profileUri, profileTitle), null, requirements);
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri) {
        if (needNewContext) {
            namespaces.pushContext();
            needNewContext = false;
        }
        namespaces.declarePrefix(prefix, uri);
    }

    @Override
    public void endPrefixMapping(final String prefix) {
        // add additional endElement() code...
        namespaces.popContext();
    }

    @Override
    public void startElement(final String namespaceURI, final String sName, // simple name
            final String qName, // qualified name
            final Attributes attrs) {
        if (needNewContext)
            namespaces.pushContext();
        needNewContext = true;
        // Get the current ele name
        this.currEleName = qName;
        if (XmlConstants.REQUIREMENT_ELE.equals(this.currEleName)) {
            this.processRequirementAttrs(attrs);
        } else if (this.inRequirement) {
            this.processRequirementChildStart(attrs);
        } else if (Section.isSection(this.currEleName)) {
            this.startSection();
        } else if (XmlConstants.EXTSCHEMA_ELE.equals(this.currEleName)) {
            this.inExtSchema = true;
            this.schemaBuilder = new ExternalSchema.Builder();
        }
        this.charBuff.voidBuffer();
    }

    @Override
    public void endElement(final String namespaceURI, final String sName, // simple name
            final String qName) {
        this.currEleName = qName;
        if (XmlConstants.REQUIREMENT_ELE.equals(this.currEleName)) {
            this.processRequirementEle();
        } else if (this.inRequirement) {
            this.processRequirementChild();
        } else if (XmlConstants.EXTSCHEMA_ELE.equals(this.currEleName)) {
            this.inExtSchema = false;
            this.schemaBuilder.build();
        } else if (this.inExtSchema) {
            this.processSchemaEle();
        } else if (XmlConstants.URI_ELE.equals(this.currEleName)) {
            this.profileUri = URI.create(this.charBuff.getBufferValue());
        } else if (XmlConstants.TITLE_ELE.equals(this.currEleName)) {
            this.profileTitle = this.charBuff.getBufferValue();
        }
        this.charBuff.voidBuffer();
        this.currEleName = null;
    }

    @Override
    public void endDocument() throws SAXException {
        try {

            final OutputHandler outHandler = OutputHandler.toStdOut();
            outHandler.emit("Total Requirements: " + this.requirements.size()); //$NON-NLS-1$
            outHandler.nl();
        } catch (final IOException excep) {
            throw new SAXException(Constants.MESS_IO_EXCEP, excep);
        }
    }

    @Override
    public void characters(final char[] buf, final int offset, final int len) {
        final String toAdd = new String(buf, offset, len);
        this.charBuff.addToBuffer(toAdd);
    }

    // ===========================================================
    // SAX DocumentHandler methods
    // ===========================================================
    private final void initialise() {
        this.profileUri = URI.create(Constants.DEFAULT_URI);
        this.profileTitle = Constants.EMPTY;
        this.requirements.clear();
        this.namespaces = new NamespaceSupport();
        this.reqBuilder = new Requirement.Builder();
        this.charBuff.voidBuffer();
    }

    private void processRequirementAttrs(final Attributes attrs) {
        this.inRequirement = true;
        if (attrs == null)
            return;
        for (int i = 0; i < attrs.getLength(); i++) {
            String aName = attrs.getLocalName(i); // Attr name
            if (Constants.EMPTY.equals(aName))
                aName = attrs.getQName(i);
            this.reqBuilder.processAttr(aName, attrs.getValue(i));
        }
    }

    private void processRequirementEle() {
        this.inRequirement = false;
        this.reqBuilder.section(this.currentSect);
        final Requirement req = this.reqBuilder.build();
        if (req.id == eu.dilcis.csip.profile.Requirement.RequirementId.DEFAULT_ID)
            return;
        this.requirements.add(req);
        this.reqBuilder = new Requirement.Builder();
    }

    private void processRequirementChildStart(final Attributes eleAtts) {
        if (XmlConstants.ANCHOR_ELE.equals(this.currEleName)) {
            this.reqBuilder.descPart(this.charBuff.getBufferValue());
            this.currentHref = this.getHref(eleAtts);
        }
    }

    private String getHref(final Attributes attrs) {
        if (attrs == null)
            return null;
        for (int i = 0; i < attrs.getLength(); i++) {
            final String aName = attrs.getLocalName(i); // Attr name
            if ("href".equals(aName))
                return attrs.getValue(i);
        }
        return null;
    }

    private void processRequirementChild() {
        switch (this.currEleName) {
            case XmlConstants.HEAD_ELE:
                this.reqBuilder.name(this.charBuff.getBufferValue());
                break;
            case XmlConstants.DEFTERM_ELE:
                this.currDefTerm = this.charBuff.getBufferValue();
                break;
            case XmlConstants.DEFDEF_ELE:
                this.reqBuilder.defPair(this.currDefTerm,
                        this.charBuff.getBufferValue());
                break;
            case XmlConstants.PARA_ELE:
                this.reqBuilder.description(this.charBuff.getBufferValue());
                break;
            case XmlConstants.ANCHOR_ELE:
                final String buffVal = this.charBuff.getBufferValue();
                this.reqBuilder.descPart(" [" + buffVal + "](" + this.currentHref + ") ");
                break;
            default:
                break;
        }
    }

    private void processSchemaEle() {
        switch (this.currEleName) {
            case XmlConstants.NAME_ELE:
                this.schemaBuilder.name(this.charBuff.getBufferValue());
                break;
            case XmlConstants.URL_ELE:
                this.schemaBuilder.url(this.charBuff.getBufferValue());
                break;
            case XmlConstants.CONTEXT_ELE:
                this.schemaBuilder.context(this.charBuff.getBufferValue());
                break;
            case XmlConstants.PARA_ELE:
                this.schemaBuilder.note(this.charBuff.getBufferValue());
                break;
            default:
                break;
        }
    }

    private void startSection() {
        this.currentSect = Section.fromEleName(this.currEleName);
    }
}
