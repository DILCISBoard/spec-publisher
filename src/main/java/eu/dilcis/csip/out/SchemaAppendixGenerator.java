package eu.dilcis.csip.out;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.dilcis.csip.profile.ControlledVocabulary;
import eu.dilcis.csip.profile.ExternalSchema;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *         <a href="https://github.com/carlwilson">carlwilson AT github</a>
 *
 * @version 0.1
 * 
 *          Created 20 Nov 2018:00:45:00
 */

public final class SchemaAppendixGenerator {
	static final String vtop ="\\\\vtop{";
	static final String hbox ="\\\\hbox{";
	static final String strut ="\\\\strut ";
	final List<ExternalSchema> schema = new ArrayList<>();
	final List<ControlledVocabulary> vocabs = new ArrayList<>();
	static final Map<String, String> vocabLookup = new HashMap<>();

	/**
	 * 
	 */
	public SchemaAppendixGenerator() {
		super();
	}

	public boolean add(final ExternalSchema toAdd) {
		return this.schema.add(toAdd);
	}

	public boolean add(final ControlledVocabulary toAdd) {
		SchemaAppendixGenerator.vocabLookup.put(toAdd.id, toAdd.name);
		return this.vocabs.add(toAdd);
	}

	public static String getVocabName(final String id) {
		return SchemaAppendixGenerator.vocabLookup.get(id);
	}
	
	static String pandocLink(final String link) {
		return " {#" + link + "}";
	}

	public void generateAppendix(final Path projRoot) throws IOException {
		OutputHandler handler = OutputHandler.toAppendix(projRoot, "schema"); //$NON-NLS-1$
		handler.nl();
		for (ExternalSchema scheme : this.schema) {
			handler.nl();
			handler.emit(GitHubMarkdownFormatter.h3(scheme.name));
			handler.nl();
			handler.emit(headString("Location:", GitHubMarkdownFormatter.href(scheme.url.toString(), scheme.url.toString())));
			handler.nl();
			handler.emit(headString("Context:", scheme.context));
			handler.nl();
			handler.emit(headString("Note:", " "));
			handler.nl();
			for (String para : scheme.note) {
				handler.emit(para);
				handler.emit(GitHubMarkdownFormatter.htmlBr);
				handler.nl();
			}
			handler.nl();
		}
		handler = OutputHandler.toAppendix(projRoot, "vocabs"); //$NON-NLS-1$
		for (ControlledVocabulary vocab : this.vocabs) {
			handler.nl();
			handler.emit(GitHubMarkdownFormatter.h3(vocab.name));
//			handler.emit("  ");
//			handler.emit(pandocLink(vocab.id));
			handler.nl();
			handler.emit(GitHubMarkdownFormatter.anchor(vocab.id));
			handler.nl();
			handler.nl();
			handler.emit(headString("Maintained By:", vocab.maintenanceAgency));
			handler.emit("  ");
			handler.nl();
			handler.emit(headString("Location:", GitHubMarkdownFormatter.href(vocab.uri.toString(), vocab.uri.toString())));
			handler.nl();
			handler.emit(headString("Context:", vocab.context));
			handler.emit("  ");
			handler.nl();
			handler.emit(headString("Description:", " "));
			handler.nl();
			for (String para : vocab.description) {
				handler.emit(para);
				handler.emit("  ");
				handler.nl();
			}
			handler.emit("  ");
			handler.nl();
		}
	}

	private static String headString(final String head, final String val) {
		StringBuffer buff = new StringBuffer(GitHubMarkdownFormatter.makeBold(head));
		buff.append(" ");
		buff.append(val);
		buff.append("   \n");
		return buff.toString();
	}

//	private static String pandocHeadString(final String head, final String val) {
//		StringBuffer buff = new StringBuffer(GitHubMarkdownFormatter.makePandocBold(head));
//		buff.append(" ");
//		buff.append(val);
//		return buff.toString();
//	}
	
	private static String vtop(final String val) {
		StringBuffer buff = new StringBuffer(vtop);
		buff.append(val);
		buff.append("}");
		return buff.toString();
	}
	
	private static String hbox(final String val) {
		StringBuffer buff = new StringBuffer(hbox);
		buff.append(val);
		buff.append("}");
		return buff.toString();
	}
	
	private static String strut(final String val) {
		StringBuffer buff = new StringBuffer(strut);
		buff.append(val);
		return buff.toString();
	}
	
	private static String pandocTableLine(final String val) {
		return vtop(hbox(strut(val)));
	}
}
