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
import java.util.Set;
import java.util.stream.Collectors;

import eu.dilcis.csip.profile.Example;
import eu.dilcis.csip.profile.MetsProfile;
import eu.dilcis.csip.profile.Requirement;
import eu.dilcis.csip.profile.Requirement.RequirementId;

public final class SpecificationStructure {
    enum SourceType {
        MARKDOWN, TABLE, METS;

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

    static final void serialiseTable(final Table table, final Collection<MetsProfile> profiles, final String template,
            final Writer destination) throws IOException {
        final List<Requirement> requirements = profiles.stream().flatMap(p -> p.getRequirements().stream())
                .filter(r -> table.requirements.contains(r.id)).collect(Collectors.toList());
        if (requirements.size() != table.requirements.size()) {
            throw new NoSuchElementException("Not all requirements found in profiles." + requirements);
        }
        Utilities.serialiseToTemplate(template, Map.of("requirements", requirements, "caption", table.caption),
                destination);
    }

    static final Table tableFromValues(final String name, final Path source, final String caption,
            final List<RequirementId> requirements, final Set<Example> examples) {
        return new Table(name, source, caption, requirements, examples);
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

    static String tableStringFromTemplate(final Table table, final Collection<MetsProfile> profiles,
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