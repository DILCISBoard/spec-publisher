package eu.dilcis.csip;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import org.xml.sax.SAXException;

import eu.dilcis.csip.profile.MetsProfile;
import eu.dilcis.csip.profile.MetsProfileParser;
import eu.dilcis.csip.structure.ParseException;
import eu.dilcis.csip.structure.Source;
import eu.dilcis.csip.structure.SpecificationStructure;
import eu.dilcis.csip.structure.SpecificationStructure.Part;
import eu.dilcis.csip.structure.StructFileParser;
import eu.dilcis.csip.structure.Utilities;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Main class and start of CLI programming. Passes args to ProcessorOptions for
 * parsing and then invokes the SAXParser to process the XML file.
 * 
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *         <a href="https://github.com/carlwilson">carlwilson AT github</a>
 *
 * @version 0.1
 *
 *          Created 24 Oct 2018:06:37:19
 */

@Command(name = "eark-profile-publisher", mixinStandardHelpOptions = true, version = "0.1.0", description = "Produces document collatoral from METS profile XML documents.")
public final class MetsProfileProcessor implements Callable<Integer> {
    /**
     * Main method, controls top level flow from command line.
     *
     * @param args command line arg array
     */
    public static void main(final String[] args) {
        final int exitCode = new CommandLine(new MetsProfileProcessor()).execute(args);
        System.exit(exitCode);
    }

    private static void serialiseProfile(final Set<Entry<Part, List<Source>>> entries, final boolean isPdf, final Path root)
            throws IOException {
        for (final Entry<Part, List<Source>> entry : entries) {
            try (Writer writer = new FileWriter(root.resolve(entry.getKey().getFileName()).toFile())) {
                serialisePart(entry.getKey(), entry.getValue(), isPdf, writer);
            }
        }
    }

    private static void serialisePart(final Part part, final List<Source> sources, final boolean isPdf,
            final Writer writer) throws IOException {
        boolean isFirst = true;
        for (final Source section : sources) {
            if (Part.APPENDICES.equals(part)) {
                final Map<String, Object> context = new java.util.HashMap<>();
                context.put("heading", section.heading);
                context.put("label", section.label);
                context.put("isFirst", isFirst);
                isFirst = false;
                Utilities.serialiseToTemplate("eu/dilcis/csip/out/appendix_heading.mustache", context,
                        writer);
            }
            section.serialise(writer, isPdf);
            writer.write("\n");
        }
        if (Part.BODY.equals(part)) {
            writer.write("!INCLUDE \"appendices.md\"\n");
        }
        writer.flush();
    }

    @Parameters(paramLabel = "FILE", arity = "1..*", description = "A list of METS Profile documents to be processed.")
    private File[] metsProfiles;

    @Option(names = { "-f",
            "--file" }, required = true, paramLabel = "SPECIFICATION", description = "A YAML file that describes the specification structure.")
    private File structureFile;

    @Option(names = { "-o",
            "--output" }, defaultValue = "./site", paramLabel = "OUTPUT", description = "A directory to hold the collatoral produced.")
    private Path destination;

    @Override
    public Integer call() {
        int result = 0;
        if (this.destination.toFile().exists() && !this.destination.toFile().isDirectory()) {
            System.err.println("Destination must be a directory.");
            return 1;
        } else if (!this.destination.toFile().exists() && !this.destination.toFile().mkdirs()) {
            System.err.println("Failed to create destination directory.");
            return 1;
        }

        try {
            final StructFileParser structParser = StructFileParser.parserInstance(this.processProfiles());
            final SpecificationStructure specStructure = structParser.parseStructureFile(this.structureFile.toPath());
            serialiseProfile(specStructure.content.entrySet(), false, this.destination);
            serialiseProfile(specStructure.content.entrySet(), true, this.destination.resolve("../pdf"));
        } catch (SAXException | IOException excep) {
            // Basic for now, print the stack trace and trhow it
            excep.printStackTrace();
            result = 1;
        } catch (final ParseException excep) {
            excep.printStackTrace();
            result = 2;
        }
        return result;
    }

    private List<MetsProfile> processProfiles() throws SAXException, IOException {
        final List<MetsProfile> profiles = new ArrayList<>();
        for (final File profileXmlFile : this.metsProfiles) {
            try {
                final MetsProfileParser parser = MetsProfileParser.newInstance();
                final MetsProfile profile = parser.processXmlProfile(profileXmlFile.toPath());
                profiles.add(profile);
            } catch (SAXException | IOException excep) {
                // Basic for now, print the stack trace and trhow it
                excep.printStackTrace();
                throw excep;
            }
        }
        return profiles;
    }
}
