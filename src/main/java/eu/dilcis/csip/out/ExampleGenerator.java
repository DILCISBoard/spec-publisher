package eu.dilcis.csip.out;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Class to handle the generation of the examples both insine and outside of
 * Appendices. Extends the XmlFragmentGenerator.
 * 
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *         <a href="https://github.com/carlwilson">carlwilson AT github</a>
 *
 * @version 0.1
 * 
 *          Created 17 Nov 2018:22:04:03
 */

public final class ExampleGenerator extends XmlFragmentGenerator {
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final String EXAMPLE_HEAD = "Example:"; //$NON-NLS-1$
	private static final String EXAMPLE_NUM_HEAD = "Example %s:"; //$NON-NLS-1$

	public ExampleGenerator() throws UnsupportedEncodingException {
		super();
	}

	public ExampleGenerator(final OutputHandler handler) {
		super(handler);
	}

	public void startExample(final String label) throws IOException {
		this.strExmpl(EXAMPLE_HEAD, label);
	}

	public void startExample(final String label, final String number)
			throws IOException {
		this.strExmpl(String.format(EXAMPLE_NUM_HEAD, number), label);
	}

	private void strExmpl(final String head, final String label)
			throws IOException {
		this.handler.nl();
		this.handler.emit(GitHubMarkdownFormatter.makeBold(head) + SPACE + label);
		this.handler.nl();
		this.handler.nl();
		this.handler.emit(GitHubMarkdownFormatter.mdInlineXml);
	}

	public void endExample() throws IOException {
		this.handler.nl();
		this.handler.emit(GitHubMarkdownFormatter.mdInlineMarker);
		this.handler.nl();
		this.handler.nl();
	}
}
