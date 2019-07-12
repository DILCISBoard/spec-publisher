package eu.dilcis.csip.out;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.dilcis.csip.profile.Requirement;

public class PandocMarkdownTableGenerator implements RequirementTableGenerator {
	final static String[] tableHeadings = { " ID ", " Name & Loc ", //$NON-NLS-1$ //$NON-NLS-2$
			" Description & usage ", " Card & Level " };  //$NON-NLS-1$ //$NON-NLS-2$
	final static String tableRow = PandocMarkdownFormatter.tableRowLine(tableHeadings);
	final static String headingRow = PandocMarkdownFormatter.tableHeadingLine(tableHeadings);

	final List<Requirement> requirements = new ArrayList<>();

	private PandocMarkdownTableGenerator() {
		super();
	}

	public List<String> getHeadings() {
		return Arrays.asList(tableHeadings);
	}

	public int size() {
		return this.requirements.size();
	}

	public boolean add(Requirement req) {
		return this.requirements.add(req);
	}

	public void toTable(final OutputHandler outHandler) throws IOException {
		this.toTable(outHandler, true);
	}

	public void toTable(final OutputHandler outHandler, boolean addHeader)
			throws IOException {
		if (this.requirements.isEmpty())
			return;
		if (addHeader)
			tableHeading(outHandler);
		for (Requirement req : this.requirements) {
			tableRow(outHandler, req);
		}
	}

	public static RequirementTableGenerator instance() {
		return new PandocMarkdownTableGenerator();
	}

	static void tableHeading(final OutputHandler outHandler)
			throws IOException {
		outHandler.emit(tableRow);
		outHandler.nl();
		outHandler.emit(PandocMarkdownFormatter.tableHeadingsLine(tableHeadings));
		outHandler.nl();
		outHandler.emit(headingRow);
		outHandler.nl();
	}

	static void tableRow(OutputHandler outputHandler, final Requirement req)
			throws IOException {
		outputHandler.emit(PandocMarkdownFormatter
				.cell(req.id.prefix + req.id.number, true));
		outputHandler.emit(PandocMarkdownFormatter.cell(nameString(req)));
		StringBuffer desc = new StringBuffer(
				PandocMarkdownFormatter.concatDescription(req.description));
		desc = relatedMatter(desc, req.relatedMatter());
		outputHandler.emit(PandocMarkdownFormatter.cell(desc.toString()));
		outputHandler.emit(PandocMarkdownFormatter.cell(cardString(req)));
		outputHandler.nl();
		outputHandler.emit(tableRow);
		outputHandler.nl();
	}

	static String cardString(final Requirement req) {
		return boldHeadPair(req.cardinality, req.reqLevel);
	}

	static String nameString(final Requirement req) {
		return boldHeadPair(req.name, PandocMarkdownFormatter.makeConsole(req.xPath));
	}

	static String boldHeadPair(final String head, final String secondLine) {
		StringBuffer buff = new StringBuffer(PandocMarkdownFormatter.makeBold(head));
		buff.append(PandocMarkdownFormatter.htmlBr);
		buff.append(secondLine);
		return buff.toString();
	}

	static StringBuffer relatedMatter(StringBuffer buff, String[] ids) {
		if (ids == null || ids.length == 0)
			return buff;
		StringBuffer relMattBuff = new StringBuffer();
		relMattBuff.append(PandocMarkdownFormatter.htmlBr);
		relMattBuff.append(PandocMarkdownFormatter.makeBold("See also:")); //$NON-NLS-1$
		String prefix = relMattBuff.toString();
		relMattBuff = new StringBuffer();
		for (String id : ids) {
			String vocabName =  SchemaAppendixGenerator.getVocabName(id);
			if (vocabName != null) {
				buff.append(prefix);
				buff.append(PandocMarkdownFormatter.href(relMattHref(id), vocabName));
				prefix = ", ";
			}
		}
		return buff;
	}
	
	private static String relMattHref(final String id) {
		return "#" + id;
	}
}
