package eu.dilcis.csip.structure;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import eu.dilcis.csip.profile.Requirement.RequirementId;

final class SpecificationStructure {
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

        private Table(final String name, final Path source, final String caption, final List<RequirementId> requirements) {
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
        MARKDOWN, HTML, LATEX, TABLE;

        static final SourceType fromString(final String type) throws ParseException {
            if (type != null) {
                for (SourceType st : SourceType.values()) {
                    if (st.name().equalsIgnoreCase(type)) {
                        return st;
                    }
                }
            }
            throw new ParseException("Invalid type: " + type);
        }
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

    public final List<Section> sections;

    private SpecificationStructure(final List<Section> sections) {
        super();
        this.sections = Collections.unmodifiableList(sections);
    }
}