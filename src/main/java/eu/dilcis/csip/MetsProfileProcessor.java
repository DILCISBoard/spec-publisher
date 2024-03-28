package eu.dilcis.csip;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.xml.sax.SAXException;

import eu.dilcis.csip.profile.MetsProfileXmlHandler;
import picocli.CommandLine;
import picocli.CommandLine.Command;
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
    @Parameters(paramLabel = "FILE", arity = "1..*", description = "A list of METS Profile documents to be processed.")
    private File[] toProcessFiles;

    private MetsProfileProcessor() {
        throw new IllegalStateException();
    }

    @Override
    public Integer call() {
        int result = 0;
        for (File file : this.toProcessFiles) {
            try {
                // Create new SAX Parser handler initialised with opts and process
                MetsProfileXmlHandler handler = new MetsProfileXmlHandler(file.toPath());
                handler.processProfile();
            } catch (SAXException | IOException excep) {
                // Basic for now, print the stack trace and trhow it
                excep.printStackTrace();
                result = 1;
            }
        }
        return result;
    }

    /**
     * Main method, controls top level flow from command line.
     * 
     * @param args command line arg array
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MetsProfileProcessor()).execute(args);
        System.exit(exitCode);
    }
}
