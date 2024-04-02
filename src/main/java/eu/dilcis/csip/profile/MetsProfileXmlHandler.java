package eu.dilcis.csip.profile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;

import eu.dilcis.csip.out.ExampleGenerator;
import eu.dilcis.csip.out.GitHubMarkdownTableGenerator;
import eu.dilcis.csip.out.OutputHandler;
import eu.dilcis.csip.out.RequirementTableGenerator;
import eu.dilcis.csip.out.SchemaAppendixGenerator;
import eu.dilcis.csip.out.XmlCharBuffer;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *         <a href="https://github.com/carlwilson">carlwilson AT github</a>
 *
 * @version 0.1
 * 
 *          Created 24 Oct 2018:01:25:39
 */

public final class MetsProfileXmlHandler extends DefaultHandler {
    private static final SAXParserFactory spf = SAXParserFactory.newInstance();
    static {
        spf.setNamespaceAware(true);
    }
    static final SAXParser saxParser;
    static {
        try {
            saxParser = spf.newSAXParser();
        } catch (ParserConfigurationException | SAXException excep) {
            throw new IllegalStateException(Constants.initSaxMess, excep);
        }
    }

    private static String getId(final Attributes attrs) {
        return getAttValue(attrs, XmlConstants.ID_ATT);
    }
    private static String getLabel(final Attributes attrs) {
        return getAttValue(attrs, XmlConstants.LABEL_ATT);
    }
    private static String getNumber(final Attributes attrs) {
        return getAttValue(attrs, XmlConstants.NUMBER_ATT);
    }
    private static String getAttValue(final Attributes attrs,
            final String attName) {
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name
                if (Constants.empty.equals(aName))
                    aName = attrs.getQName(i);
                if (attName.equals(aName))
                    return attrs.getValue(i);
            }
        }
        return Constants.empty;
    }
    private final XmlCharBuffer charBuff = new XmlCharBuffer();
    private String currEleName;
    private boolean inRequirement = false;
    private boolean inExample = false;
    private boolean inAppendix = false;
    private boolean inExtSchema = false;
    private boolean inVocab = false;
    private RequirementTableGenerator tableGen;
    private Requirement.Builder reqBuilder = new Requirement.Builder();
    private int reqCounter = 0;
    private String currDefTerm = null;
    private Section currentSect;
    private final NamespaceSupport namespaces = new NamespaceSupport();

    private boolean needNewContext = true;
    private final Path projectRoot;
    private final Path profilePath;
    private String currentHref = null;

    private final Map<Section, Set<String>> exampleMap = new EnumMap<>(Section.class);
    private final Map<Section, ExampleGenerator> exampleHandlers = new EnumMap<>(Section.class);
    private ExampleGenerator appendixGenerator = null;

    private RequirementTableGenerator reqsAppndxGen;

    private final SchemaAppendixGenerator schemaGen = new SchemaAppendixGenerator();

    private ExternalSchema.Builder schemaBuilder;

    private ControlledVocabulary.Builder vocabBuilder;

    public MetsProfileXmlHandler(final Path profilePath) {
        super();
        this.profilePath = profilePath;
        this.projectRoot = profilePath.getParent().getParent();
    }
    // ===========================================================
    // SAX DocumentHandler methods
    // ===========================================================

    public void processProfile() throws SAXException, IOException {
        this.reqsAppndxGen = GitHubMarkdownTableGenerator.instance();
        saxParser.parse(this.profilePath.toFile(), this);
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
            final Attributes attrs) throws SAXException {
        if (needNewContext)
            namespaces.pushContext();
        needNewContext = true;
        // Get the current ele name
        this.currEleName = qName;
        if (Requirement.isRequirementEle(this.currEleName)) {
            this.processRequirementAttrs(attrs);
        } else if (this.inRequirement) {
            this.processRequirementChildStart(attrs);
        } else if (Section.isSection(this.currEleName)) {
            this.startSection();
        } else if (XmlConstants.EXAMPLE_ELE.equals(this.currEleName)) {
            this.startExample(attrs);
        } else if (this.inExample) {
            this.fragStart(this.getSectionExampleHandler(this.currentSect),
                    attrs, this.namespaces);
        } else if (XmlConstants.APPENDIX_ELE.equals(this.currEleName)) {
            this.startAppendix(attrs);
        } else if (this.inAppendix) {
            this.fragStart(this.appendixGenerator, attrs, this.namespaces);
        } else if (XmlConstants.EXTSCHEMA_ELE.equals(this.currEleName)) {
            this.inExtSchema = true;
            this.schemaBuilder = new ExternalSchema.Builder();
        } else if (XmlConstants.VOCAB_ELE.equals(this.currEleName)) {
            this.inVocab = true;
            this.vocabBuilder = new ControlledVocabulary.Builder().id(getId(attrs));
        }
        this.charBuff.voidBuffer();
    }

    @Override
    public void endElement(final String namespaceURI, final String sName, // simple name
            final String qName // qualified name
    ) throws SAXException {
        this.currEleName = qName;
        if (Requirement.isRequirementEle(this.currEleName)) {
            this.processRequirementEle();
        } else if (this.inRequirement) {
            this.processRequirementChild();
        } else if (XmlConstants.EXAMPLE_ELE.equals(this.currEleName)) {
            endExample();
        } else if (this.inExample) {
            fragEnd(this.getSectionExampleHandler(this.currentSect));
        } else if (Section.isSection(this.currEleName)) {
            endSection();
        } else if (XmlConstants.APPENDIX_ELE.equals(this.currEleName)) {
            this.endAppendix();
        } else if (this.inAppendix) {
            fragEnd(this.appendixGenerator);
        } else if (XmlConstants.EXTSCHEMA_ELE.equals(this.currEleName)) {
            this.inExtSchema = false;
            this.schemaGen.add(this.schemaBuilder.build());
        } else if (this.inExtSchema) {
            this.processSchemaEle();
        } else if (XmlConstants.VOCAB_ELE.equals(this.currEleName)) {
            this.inVocab = false;
            this.schemaGen.add(this.vocabBuilder.build());
        } else if (this.inVocab) {
            this.processVocabEle();
        }
        this.charBuff.voidBuffer();
        this.currEleName = null;
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            this.reqsAppndxGen.toTable(OutputHandler.toAppendix(this.projectRoot, "requirements"));
            this.schemaGen.generateAppendix(this.projectRoot);

            final OutputHandler outHandler = OutputHandler.toStdOut();
            for (final Entry<Section, Set<String>> entry : this.exampleMap.entrySet()) {
                System.out.println(entry.getKey().sectName);
                for (final String ex : entry.getValue()) {
                    System.out.println(ex);
                }
            }
            outHandler.nl();
            outHandler.emit("======================================="); //$NON-NLS-1$
            outHandler.nl();
            outHandler.emit("Total Requirements: " + this.reqCounter); //$NON-NLS-1$
            outHandler.nl();
        } catch (final IOException excep) {
            throw new SAXException(Constants.ioExcepMess, excep);
        }
    }

    @Override
    public void characters(final char[] buf, final int offset, final int len) {
        final String toAdd = new String(buf, offset, len);
        this.charBuff.addToBuffer(toAdd);
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
            if (XmlConstants.EXAMPLE_ATT.equals(aName)) {
                this.processExampleAtt(attrs.getValue(i));
            }
        }
    }

    private void processExampleAtt(final String attVal) {
        if (attVal == null || attVal.isEmpty()) {
            return;
        }
        final String[] exampleIds = (attVal.contains(Constants.space)) ? attVal.split(Constants.space)
                : new String[] { attVal };
        for (final String exKey : exampleIds) {
            this.exampleMap.get(this.currentSect).add(exKey);
        }
    }

    private void processRequirementEle() {
        this.inRequirement = false;
        final Requirement req = this.reqBuilder.build();
        if (req.id == eu.dilcis.csip.profile.Requirement.RequirementId.DEFAULT_ID)
            return;
        this.tableGen.add(req);
        this.reqsAppndxGen.add(req);
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

    private void processVocabEle() {
        switch (this.currEleName) {
            case XmlConstants.NAME_ELE:
                this.vocabBuilder.name(this.charBuff.getBufferValue());
                break;
            case XmlConstants.MAINT_ELE:
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
        this.currentSect = Section.fromEleName(this.currEleName);
        this.exampleMap.put(this.currentSect, new HashSet<>());
        this.tableGen = GitHubMarkdownTableGenerator.instance();
    }

    private void endSection() throws SAXException {
        this.currentSect = Section.fromEleName(this.currEleName);
        try {
            this.tableGen.toTable(OutputHandler
                    .toSectionRequirements(this.projectRoot, this.currentSect));
        } catch (final IOException excep) {
            throw new SAXException(Constants.ioExcepMess, excep);
        }
        this.reqCounter += this.tableGen.size();
    }

    private void startExample(final Attributes attrs) throws SAXException {
        this.inExample = true;
        final String id = getId(attrs);
        for (final Entry<Section, Set<String>> sectionEntry : this.exampleMap.entrySet()) {
            if (sectionEntry.getValue().contains(id)) {
                final ExampleGenerator gene = this.getSectionExampleHandler(sectionEntry.getKey());
                try {
                    gene.startExample(getLabel(attrs));
                } catch (final IOException excep) {
                    throw new SAXException(Constants.ioExcepMess, excep);
                }
                return;
            }
        }
    }

    private void endExample() throws SAXException {
        final ExampleGenerator gene = this.getSectionExampleHandler(this.currentSect);
        try {
            gene.endExample();
        } catch (final IOException excep) {
            throw new SAXException(Constants.ioExcepMess, excep);
        }
        this.inExample = false;
    }

    private void startAppendix(final Attributes attrs) throws SAXException {
        this.inAppendix = true;
        try {
            if (this.appendixGenerator == null)
                this.appendixGenerator = new ExampleGenerator(OutputHandler
                        .toAppendix(this.projectRoot, XmlConstants.EXAMPLE_ATT.toLowerCase()));
            this.appendixGenerator.startExample(getLabel(attrs),
                    getNumber(attrs));
        } catch (final IOException excep) {
            throw new SAXException(Constants.ioExcepMess, excep);
        }
    }

    private void endAppendix() throws SAXException {
        try {
            this.appendixGenerator.endExample();
        } catch (final IOException excep) {
            throw new SAXException(Constants.ioExcepMess, excep);
        }
        this.inAppendix = false;
    }

    private void fragStart(final ExampleGenerator generator, final Attributes attrs, final NamespaceSupport namespaces)
            throws SAXException {
        try {
            generator.outputEleStart(this.currEleName, attrs, namespaces);
        } catch (final IOException excep) {
            throw new SAXException(Constants.ioExcepMess, excep);
        }
    }

    private void fragEnd(final ExampleGenerator generator) throws SAXException {
        try {
            generator.outputEleEnd(this.currEleName,
                    this.charBuff.voidBuffer());
        } catch (final IOException excep) {
            throw new SAXException(Constants.ioExcepMess, excep);
        }
    }

    private ExampleGenerator getSectionExampleHandler(final Section section)
            throws SAXException {
        ExampleGenerator gene = this.exampleHandlers.get(section);
        try {
            if (gene == null) {
                final OutputHandler handler = OutputHandler
                        .toSectionExamples(this.projectRoot, section);
                gene = new ExampleGenerator(handler);
                this.exampleHandlers.put(section, gene);
            }
        } catch (final IOException e) {
            throw new SAXException(String.format(Constants.sectIoMess, section.sectName),
                    e);
        }
        this.currentSect = section;
        return gene;
    }
}
