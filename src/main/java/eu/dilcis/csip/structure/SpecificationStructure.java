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
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import eu.dilcis.csip.profile.MetsProfile;
import eu.dilcis.csip.profile.Requirement;
import eu.dilcis.csip.profile.Requirement.RequirementId;

public final class SpecificationStructure {
    public static class Section {
        public final String name;
        public final Path source;
        public final SourceType type;

        private Section(final String name, final Path source, final SourceType type) {
            super();
            this.name = name;
            this.source = source;
            this.type = type;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(name, source, type);
        }

        @Override
        public final boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Section))
                return false;
            final Section other = (Section) obj;
            return Objects.equals(name, other.name) && Objects.equals(source, other.source) && type == other.type;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("Section [");
            if (name != null)
                builder.append("name=").append(name).append(", ");
            if (source != null)
                builder.append("source=").append(source).append(", ");
            if (type != null)
                builder.append("type=").append(type);
            builder.append("]");
            return builder.toString();
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

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("Table [");
            if (name != null)
                builder.append("name=").append(name).append(", ");
            if (source != null)
                builder.append("source=").append(source).append(", ");
            if (type != null)
                builder.append("type=").append(type).append(", ");
            if (caption != null)
                builder.append("caption=").append(caption).append(", ");
            if (requirements != null)
                builder.append("requirements=").append(requirements);
            builder.append("]");
            return builder.toString();
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

    public enum Part {
        PREFACE, BODY, APPENDICES, POSTFACE;

        static final Part fromString(final String partName) throws ParseException {
            if (partName != null) {
                for (final Part part : Part.values()) {
                    if (part.name().equalsIgnoreCase(partName)) {
                        return part;
                    }
                }
            }
            throw new ParseException("Invalid part: " + partName);
        }

        public String getFileName() {
            return this.name().toLowerCase() + ".md";
        }
    }

    static Path tableToFile(final Table table, final Collection<MetsProfile> profiles, final String template)
            throws IOException {
        try (Writer writer = new FileWriter(table.source.toFile())) {
            serialiseTable(table, profiles, template, writer);
        }
        return table.source;
    }

    static String htmlTable(final Table table, final Collection<MetsProfile> profiles) throws IOException {
        return tableStringFromTemplate(table, profiles, "eu/dilcis/csip/out/table.mustache");
    }

    static String markdownTable(final Table table, final Collection<MetsProfile> profiles) throws IOException {
        return tableStringFromTemplate(table, profiles, "eu/dilcis/csip/out/table_markdown.mustache");
    }

    static final void serialiseTable(final Table table, final Collection<MetsProfile> profiles, final String template,
            final Writer destination) throws IOException {
        final List<Requirement> requirements = profiles.stream().flatMap(p -> p.getRequirements().stream())
                .filter(r -> table.requirements.contains(r.id)).collect(Collectors.toList());
        if (requirements.size() != table.requirements.size()) {
            throw new NoSuchElementException("Not all requirements found in profiles." + requirements);
        }
        final MustacheFactory mf = new DefaultMustacheFactory();
        final Mustache m = mf.compile(template);
        final Map<String, Object> context = Map.of("requirements", requirements, "caption", table.caption);
        m.execute(destination, context).flush();
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

    static final SpecificationStructure fromContentMap(final Map<Part, List<Section>> content) {
        return new SpecificationStructure(content);
    }

    private static String tableStringFromTemplate(final Table table, final Collection<MetsProfile> profiles,
            final String template) throws IOException {
        try (final StringWriter writer = new StringWriter()) {
            serialiseTable(table, profiles, template, writer);
            return writer.toString();
        }
    }

    public final Map<Part, List<Section>> content;

    private SpecificationStructure(final Map<Part, List<Section>> content) {
        super();
        this.content = Collections.unmodifiableMap(content);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SpecificationStructure [");
        if (content != null)
            builder.append("sections=").append(content);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SpecificationStructure))
            return false;
        final SpecificationStructure other = (SpecificationStructure) obj;
        return Objects.equals(content, other.content);
    }

    public void serialiseSiteStructure(final Collection<MetsProfile> profiles) {
        serialiseStructure(profiles, "eu/dilcis/csip/out/table_markdown.mustache");
    }

    public void serialisePdfStructure(final Collection<MetsProfile> profiles) {
        serialiseStructure(profiles, "eu/dilcis/csip/out/table_pdf.mustache");
    }

    private void serialiseStructure(final Collection<MetsProfile> profiles, final String template) {
        for (Entry<Part, List<Section>> entry : content.entrySet()) {
            for (final Section section : entry.getValue()) {
                if (section instanceof Table) {
                    final Table table = (Table) section;
                    try {
                        tableToFile(table, profiles, template);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}