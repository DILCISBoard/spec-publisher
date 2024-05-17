package eu.dilcis.csip.profile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;

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
    private String currentElementName;
    private boolean inRequirement = false;
    private boolean inExtSchema = false;
    private boolean inVocab = false;
    private boolean inExample = false;
    private boolean inTool = false;
    private boolean inAppendix = false;
    private String currDefTerm = null;
    private NamespaceSupport namespaces = new NamespaceSupport();
    private Requirement.Builder reqBuilder = new Requirement.Builder();
    private ControlledVocabulary.Builder vocabBuilder;
    private Appendix.Builder appBuilder;
    private Example.Builder exBuilder;
    private Section currentSect;
    private boolean needNewContext = true;

    private String currentHref = null;
    private ExternalSchema.Builder schemaBuilder;
    private URI profileUri;

    private String profileTitle;

    private final List<Requirement> requirements = new ArrayList<>();
    private final Map<String, Example> examples = new HashMap<>();
    private final List<Appendix> appendices = new ArrayList<>();
    private final List<ExternalSchema> extSchemas = new ArrayList<>();
    private final List<ControlledVocabulary> vocabs = new ArrayList<>();

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
        return MetsProfile.fromValues(Details.fromValues(profileUri, profileTitle), null, requirements,
                examples, appendices, extSchemas, vocabs);
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
        this.currentElementName = qName;
        if (XmlConstants.REQUIREMENT_ELE.equals(this.currentElementName)) {
            this.processRequirementAttrs(attrs);
        } else if (this.inRequirement) {
            this.processRequirementChildStart(attrs);
        } else if (Section.isSection(this.currentElementName)) {
            this.startSection();
        } else if (XmlConstants.EXTSCHEMA_ELE.equals(this.currentElementName)) {
            this.inExtSchema = true;
            this.schemaBuilder = new ExternalSchema.Builder();
        } else if (XmlConstants.EXAMPLE_ELE.equals(this.currentElementName)) {
            this.exBuilder = new Example.Builder(attrs);
            this.inExample = true;
        } else if (this.inExample) {
            this.exBuilder.eleStart(this.currentElementName, attrs, namespaces);
        } else if (XmlConstants.VOCAB_ELE.equals(this.currentElementName)) {
            this.inVocab = true;
            this.vocabBuilder = new ControlledVocabulary.Builder();
            this.vocabBuilder.id(Utilities.getId(attrs));
        } else if (XmlConstants.APPENDIX_ELE.equals(this.currentElementName)) {
            this.startAppendix(attrs);
        } else if (this.inAppendix) {
            this.appBuilder.eleStart(this.currentElementName, attrs, this.namespaces);
        } else if (XmlConstants.TOOL_ELE.equals(this.currentElementName)) {
            this.inTool = true;
        }
        this.charBuff.voidBuffer();
    }

    @Override
    public void endElement(final String namespaceURI, final String sName, // simple name
            final String qName) {
        this.currentElementName = qName;
        if (XmlConstants.REQUIREMENT_ELE.equals(this.currentElementName)) {
            this.processRequirementEle();
        } else if (this.inRequirement) {
            this.processRequirementChild();
        } else if (XmlConstants.EXTSCHEMA_ELE.equals(this.currentElementName)) {
            this.inExtSchema = false;
            this.extSchemas.add(this.schemaBuilder.build());
        } else if (this.inExtSchema) {
            this.processSchemaEle();
        } else if (XmlConstants.VOCAB_ELE.equals(this.currentElementName)) {
            this.inVocab = false;
            this.vocabs.add(this.vocabBuilder.build());
        } else if (this.inVocab) {
            this.processVocabEle();
        } else if (XmlConstants.TOOL_ELE.equals(this.currentElementName)) {
            this.inTool = false;
        } else if (XmlConstants.EXAMPLE_ELE.equals(this.currentElementName)) {
            this.inExample = false;
            final Example ex = this.exBuilder.build();
            this.examples.put(ex.id, ex);
        } else if (this.inExample) {
            this.exBuilder.eleEnd(this.currentElementName,
                    this.charBuff.voidBuffer());
        } else if (XmlConstants.URI_ELE.equals(this.currentElementName) && !this.inTool) {
            this.profileUri = URI.create(this.charBuff.getBufferValue());
        } else if (XmlConstants.TITLE_ELE.equals(this.currentElementName)) {
            this.profileTitle = this.charBuff.getBufferValue();
        } else if (XmlConstants.APPENDIX_ELE.equals(this.currentElementName)) {
            this.inAppendix = false;
            this.appendices.add(this.appBuilder.build());
        } else if (this.inAppendix) {
            this.appBuilder.eleEnd(this.currentElementName, this.charBuff.voidBuffer());
        }
        this.charBuff.voidBuffer();
        this.currentElementName = null;
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
        this.appendices.clear();
        this.namespaces = new NamespaceSupport();
        this.reqBuilder = new Requirement.Builder();
        this.charBuff.voidBuffer();
        this.inAppendix = this.inExample = this.inExtSchema = this.inRequirement = this.inTool = this.inVocab = false;
        this.examples.clear();
        this.extSchemas.clear();
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
        if (XmlConstants.ANCHOR_ELE.equals(this.currentElementName)) {
            this.reqBuilder.descPart(this.charBuff.getBufferValue());
            this.currentHref = Utilities.getHref(eleAtts);
        }
    }

    private void processRequirementChild() {
        switch (this.currentElementName) {
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
            case XmlConstants.TEST_XML_ELE:
                this.reqBuilder.xPath(this.charBuff.getBufferValue());
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
        switch (this.currentElementName) {
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

    private void processVocabEle() {
        switch (this.currentElementName) {
            case XmlConstants.NAME_ELE:
                this.vocabBuilder.name(this.charBuff.getBufferValue());
                break;
            case XmlConstants.MAINT_ELE:
                this.vocabBuilder.maintenanceAgency(this.charBuff.getBufferValue());
                break;
            case XmlConstants.URI_ELE:
                this.vocabBuilder.uri(this.charBuff.getBufferValue());
                break;
            case XmlConstants.CONTEXT_ELE:
                this.vocabBuilder.context(this.charBuff.getBufferValue());
                break;
            case XmlConstants.PARA_ELE:
                this.vocabBuilder.description(this.charBuff.getBufferValue());
                break;
            default:
                break;
        }
    }

    private void startSection() {
        this.currentSect = Section.fromEleName(this.currentElementName);
    }

    private void startAppendix(final Attributes attrs) {
        this.inAppendix = true;
        this.appBuilder = new Appendix.Builder(attrs);
    }
}
