package eu.dilcis.csip.out;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.dilcis.csip.profile.Requirement;

public class GitHubMarkdownTableGenerator implements RequirementTableGenerator {

	protected static final String[] tableHeadings = { " ID    ", "Name, Location & Description", "Card & Level" }; //$NON-NLS-1$ //$NON-NLS-2$

	public static RequirementTableGenerator instance() {
		return new GitHubMarkdownTableGenerator();
	}

	static void tableHeading(final OutputHandler outHandler)
			throws IOException {
		boolean isFirst = true;
		final StringBuilder headingLines = new StringBuilder();
		for (final String heading : tableHeadings) {
			outHandler.emit(GitHubMarkdownFormatter.cell(heading, isFirst));
			headingLines.append(GitHubMarkdownFormatter.cell(
					GitHubMarkdownFormatter.makeHeadingLines(heading), isFirst));
			isFirst = false;
		}
		outHandler.nl();
		outHandler.emit(headingLines.toString());
		outHandler.nl();
	}
	static void tableRow(final OutputHandler outputHandler, final Requirement req)
			throws IOException {
		outputHandler.emit(GitHubMarkdownFormatter
				.anchorCell(req.id.prefix + req.id.number, true));
		outputHandler.emit(GitHubMarkdownFormatter.cell(descCellText(req)));
		outputHandler.emit(GitHubMarkdownFormatter.cell(cardString(req)));
		outputHandler.nl();
	}

	static String descCellText(final Requirement req) {
		
		final StringBuilder buff = new StringBuilder(nameString(req));
		buff.append(GitHubMarkdownFormatter.htmlBr);
		buff.append(GitHubMarkdownFormatter.concatDescription(req.description));
		relatedMatter(buff, req.relatedMatter());
		return buff.toString();
	}

	static String cardString(final Requirement req) {
		return boldHeadPair(req.cardinality, req.details.level.name());
	}

	static String nameString(final Requirement req) {
		return boldHeadPair(req.details.name, GitHubMarkdownFormatter.makeConsole(req.xPath));
	}

	static String boldHeadPair(final String head, final String secondLine) {
		final StringBuilder buff = new StringBuilder(GitHubMarkdownFormatter.makeBold(head));
		buff.append(GitHubMarkdownFormatter.htmlBr);
		buff.append(secondLine);
		return buff.toString();
	}

	static StringBuilder relatedMatter(final StringBuilder buff, final String[] ids) {
		if (ids == null || ids.length == 0)
			return buff;
        final StringBuilder relMattBuff = new StringBuilder();
		relMattBuff.append(GitHubMarkdownFormatter.htmlBr);
		relMattBuff.append(GitHubMarkdownFormatter.makeBold("See also:")); //$NON-NLS-1$
		relMattBuff.append(" ");
		String prefix = relMattBuff.toString();
		for (final String id : ids) {
			final String vocabName =  SchemaAppendixGenerator.getVocabName(id);
			if (vocabName != null) {
				buff.append(prefix);
				buff.append(GitHubMarkdownFormatter.href(relMattHref(id), vocabName));
				prefix = ", ";
			}
		}
		return buff;
	}

	private static String relMattHref(final String id) {
		return "#" + id;
	}

	protected final List<Requirement> requirements = new ArrayList<>();

	private GitHubMarkdownTableGenerator() {
		super();
	}
	@Override
	public List<String> getHeadings() {
		return Arrays.asList(tableHeadings);
	}

	@Override
	public int size() {
		return this.requirements.size();
	}

	@Override
	public boolean add(final Requirement req) {
		return this.requirements.add(req);
	}

	@Override
	public void toTable(final OutputHandler handler) throws IOException {
		this.toTable(handler, true);
	}
	
	@Override
	public void toTable(final OutputHandler outHandler, final boolean addHeader) throws IOException {
		if (this.requirements.isEmpty())
			return;
		if (addHeader)
			tableHeading(outHandler);
		for (final Requirement req : this.requirements) {
			tableRow(outHandler, req);
		}
	}
}
