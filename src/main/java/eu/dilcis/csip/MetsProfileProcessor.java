package eu.dilcis.csip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.xml.sax.SAXException;

import eu.dilcis.csip.profile.MetsProfile;
import eu.dilcis.csip.profile.MetsProfileParser;
import eu.dilcis.csip.structure.ParseException;
import eu.dilcis.csip.structure.SpecificationStructure;
import eu.dilcis.csip.structure.StructFileParser;
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

    @Parameters(paramLabel = "FILE", arity = "1..*", description = "A list of METS Profile documents to be processed.")
    private File[] metsProfiles;

    @Option(names = { "-f",
            "--file" }, required = true, paramLabel = "SPECIFICATION", description = "A YAML file that describes the specification structure.")
    private File structureFile;

    @Override
    public Integer call() {
        int result = 0;
        try {
            final StructFileParser structParser = StructFileParser.parserInstance(this.processProfiles());
            final SpecificationStructure specStructure = structParser.parseStructureFile(this.structureFile.toPath());
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
        final MetsProfileParser parser = MetsProfileParser.newInstance();
        for (final File profileXmlFile : this.metsProfiles) {
            try {
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
