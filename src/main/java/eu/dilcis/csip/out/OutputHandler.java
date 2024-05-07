package eu.dilcis.csip.out;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import eu.dilcis.csip.profile.Section;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *         <a href="https://github.com/carlwilson">carlwilson AT github</a>
 *
 * @version 0.1
 * 
 *          Created 25 Oct 2018:09:00:06
 */

public final class OutputHandler {
	private static final String LINE_SEP_PROP = "line.separator"; //$NON-NLS-1$
	private static final String LINE_END = System.getProperty(LINE_SEP_PROP);
	private static final String MD_EXT = ".md"; //$NON-NLS-1$
	private static final String REQUIREMENTS_MD = "requirements" + MD_EXT; //$NON-NLS-1$
	private static final String EXAMPLES_MD = "examples" + MD_EXT; //$NON-NLS-1$
	public static OutputHandler toStdOut() {
		return new OutputHandler();
	}

	public static OutputHandler toSectionRequirements(final Path projRoot,
			final Section sect) throws IOException {
		return new OutputHandler(getSectionPath(projRoot, sect.sectName)
				.resolve(Paths.get(REQUIREMENTS_MD)).toFile());
	}

	public static OutputHandler toSectionExamples(final Path projRoot, final Section sect)
			throws IOException {
		return new OutputHandler(getSectionPath(projRoot, sect.sectName)
				.resolve(Paths.get(EXAMPLES_MD)).toFile());
	}

	// ===========================================================
	// Utility Methods ...
	// ===========================================================

	public static OutputHandler toAppendix(final Path metsReqRoot,
			final String appndxName) throws IOException {
		return new OutputHandler(metsReqRoot.resolve(Paths.get("specification",
				"appendices", appndxName, appndxName + MD_EXT)).toFile());
	}

	private static Path getSectionPath(final Path projRoot,
			final String sectName) {
		final Path toReqRoot = Paths.get("specification", "implementation", //$NON-NLS-1$ //$NON-NLS-2$
				"metadata", "mets", sectName); //$NON-NLS-1$ //$NON-NLS-2$
		return projRoot.resolve(toReqRoot);
	}

	private final Writer out;

	/**
	 * Default constructor, output to STDOUT
	 */
	private OutputHandler() {
		super();
		this.out = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
	}

	/**
	 * Constructor to output to a file
	 */
	private OutputHandler(final File outFile) throws IOException {
		super();
		if (!outFile.getParentFile().exists()) {
			outFile.getParentFile().mkdirs();
		}
		final File output = new File(outFile.getParentFile(), outFile.getName());
		this.out = new FileWriter(output);
	}

	// Wrap I/O exceptions in SAX exceptions, to
	// suit handler signature requirements
	public void emit(final String s) throws IOException {
		if (s == null)
			return;
		this.out.write(s);
		this.out.flush();
	}

	// Start a new line
	public void nl() throws IOException {
		this.out.write(LINE_END);
		this.out.flush();
	}

}
