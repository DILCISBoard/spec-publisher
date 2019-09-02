package eu.dilcis.csip.out;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.dilcis.csip.profile.Requirement;

public class GitHubMarkdownTableGenerator implements RequirementTableGenerator {

	private GitHubMarkdownTableGenerator() {
		super();
	}

	public static RequirementTableGenerator instance() {
		return new GitHubMarkdownTableGenerator();
	}

	protected final static String[] tableHeadings = { " ID    ", "Name, Location & Description", "Card & Level" }; //$NON-NLS-1$ //$NON-NLS-2$
	protected final List<Requirement> requirements = new ArrayList<>();

	@Override
	public List<String> getHeadings() {
		return Arrays.asList(tableHeadings);
	}

	@Override
	public int size() {
		return this.requirements.size();
	}

	@Override
	public boolean add(Requirement req) {
		return this.requirements.add(req);
	}

	@Override
	public void toTable(OutputHandler handler) throws IOException {
		this.toTable(handler, true);
	}

	@Override
	public void toTable(final OutputHandler outHandler, boolean addHeader) throws IOException {
		if (this.requirements.isEmpty())
			return;
		if (addHeader)
			tableHeading(outHandler);
		for (Requirement req : this.requirements) {
			tableRow(outHandler, req);
		}
	}

	static void tableHeading(final OutputHandler outHandler)
			throws IOException {
		boolean isFirst = true;
		StringBuffer headingLines = new StringBuffer();
		for (String heading : tableHeadings) {
			outHandler.emit(GitHubMarkdownFormatter.cell(heading, isFirst));
			headingLines.append(GitHubMarkdownFormatter.cell(
					GitHubMarkdownFormatter.makeHeadingLines(heading), isFirst));
			isFirst = false;
		}
		outHandler.nl();
		outHandler.emit(headingLines.toString());
		outHandler.nl();
	}

	static void tableRow(OutputHandler outputHandler, final Requirement req)
			throws IOException {
		outputHandler.emit(GitHubMarkdownFormatter
				.anchorCell(req.id.prefix + req.id.number, true));
		outputHandler.emit(GitHubMarkdownFormatter.cell(descCellText(req)));
		outputHandler.emit(GitHubMarkdownFormatter.cell(cardString(req)));
		outputHandler.nl();
	}

	static String descCellText(final Requirement req) {
		
		StringBuffer buff = new StringBuffer(nameString(req));
		buff.append(GitHubMarkdownFormatter.htmlBr);
		buff.append(GitHubMarkdownFormatter.concatDescription(req.description));
		relatedMatter(buff, req.relatedMatter());
		return buff.toString();
	}
	static String cardString(final Requirement req) {
		return boldHeadPair(req.cardinality, req.reqLevel);
	}

	static String nameString(final Requirement req) {
		return boldHeadPair(req.name, GitHubMarkdownFormatter.makeConsole(req.xPath));
	}

	static String boldHeadPair(final String head, final String secondLine) {
		StringBuffer buff = new StringBuffer(GitHubMarkdownFormatter.makeBold(head));
		buff.append(GitHubMarkdownFormatter.htmlBr);
		buff.append(secondLine);
		return buff.toString();
	}

	static StringBuffer relatedMatter(StringBuffer buff, String[] ids) {
		if (ids == null || ids.length == 0)
			return buff;
		StringBuffer relMattBuff = new StringBuffer();
		relMattBuff.append(GitHubMarkdownFormatter.htmlBr);
		relMattBuff.append(GitHubMarkdownFormatter.makeBold("See also:")); //$NON-NLS-1$
		relMattBuff.append(" ");
		String prefix = relMattBuff.toString();
		relMattBuff = new StringBuffer();
		for (String id : ids) {
			String vocabName =  SchemaAppendixGenerator.getVocabName(id);
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
}
