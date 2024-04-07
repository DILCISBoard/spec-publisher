package eu.dilcis.csip.structure;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import eu.dilcis.csip.profile.MetsProfile;
import eu.dilcis.csip.profile.Requirement;
import eu.dilcis.csip.profile.Requirement.RequirementId;

public final class SpecificationStructure {
    static class Section {
        public final String name;
        public final Path source;
        public final SourceType type;

        private Section(final String name, final Path source, final SourceType type) {
            super();
            this.name = name;
            this.source = source;
            this.type = type;
        }
    }

    static final class Table extends Section {
        public final String caption;
        public final List<RequirementId> requirements;

        private Table(final String name, final Path source, final String caption,
                final List<RequirementId> requirements) {
            this(name, source, SourceType.HTML, caption, requirements);
        }

        private Table(final String name, final Path source, final SourceType type, final String caption,
                final List<RequirementId> requirements) {
            super(name, source, type);
            this.caption = caption;
            this.requirements = Collections.unmodifiableList(requirements);
        }
    }

    enum SourceType {
        MARKDOWN, HTML, LATEX, TABLE, METS;

        static final SourceType fromString(final String type) throws ParseException {
            if (type != null) {
                for (final SourceType st : SourceType.values()) {
                    if (st.name().equalsIgnoreCase(type) || type.toLowerCase().startsWith(st.delimitedName())) {
                        return st;
                    }
                }
            }
            throw new ParseException("Invalid type: " + type);
        }

        public String delimitedName() {
            return this.name().toLowerCase() + ".";
        }
    }

    static Path tableToFile(final Table table, final Collection<MetsProfile> profiles, final String template) throws IOException {
        serialiseTable(table, profiles, template, new FileWriter(table.source.toFile()));
        return table.source;
    }

    static String htmlTable(final Table table, final Collection<MetsProfile> profiles) {
        return tableStringFromTemplate(table, profiles, "eu/dilcis/csip/out/table.mustache");
    }

    static String markdownTable(final Table table, final Collection<MetsProfile> profiles) {
        return tableStringFromTemplate(table, profiles, "eu/dilcis/csip/out/table_markdown.mustache");
    }

    static final void serialiseTable(final Table table, final Collection<MetsProfile> profiles, final String template,
            final Writer destination) {
        final List<Requirement> requirements = profiles.stream().flatMap(p -> p.getRequirements().stream())
                .filter(r -> table.requirements.contains(r.id)).collect(Collectors.toList());
        if (requirements.size() != table.requirements.size()) {
            throw new NoSuchElementException("Not all requirements found in profiles." + requirements);
        }
        final MustacheFactory mf = new DefaultMustacheFactory();
        final Mustache m = mf.compile(template);
        final Map<String, Object> context = Map.of("requirements", requirements, "caption", table.caption);
        m.execute(destination, context);
    }

    static final Table tableFromValues(final String name, final Path source, final String caption,
            final List<RequirementId> requirements) {
        return new Table(name, source, caption, requirements);
    }

    static final Section sectionFromValues(final String name, final Path source) {
        return sectionFromValues(name, source, SourceType.MARKDOWN);
    }

    static final Section sectionFromValues(final String name, final Path source, final SourceType type) {
        return new Section(name, source, type);
    }

    static final SpecificationStructure fromSections(final List<Section> sections) {
        return new SpecificationStructure(sections);
    }

    private static String tableStringFromTemplate(final Table table, final Collection<MetsProfile> profiles, final String template) {
        final StringWriter writer = new StringWriter();
        serialiseTable(table, profiles, template, writer);
        return writer.toString();
    }

    public final List<Section> sections;

    private SpecificationStructure(final List<Section> sections) {
        super();
        this.sections = Collections.unmodifiableList(sections);
    }
}