package eu.dilcis.csip.structure;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import eu.dilcis.csip.profile.Example;
import eu.dilcis.csip.profile.Requirement.RequirementId;
import eu.dilcis.csip.structure.SpecificationStructure.SourceType;

final class Table extends Section {
    public final String caption;
    public final List<RequirementId> requirements;
    public final Set<Example> examples;

    Table(final String name, final Path source, final String caption,
            final List<RequirementId> requirements, final Set<Example> examples) {
        this(name, source, SourceType.TABLE, caption, requirements, examples);
    }

    private Table(final String name, final Path source, final SourceType type, final String caption,
            final List<RequirementId> requirements, final Set<Example> examples) {
        super(name, source, type);
        this.caption = caption;
        this.requirements = Collections.unmodifiableList(requirements);
        this.examples = Collections.unmodifiableSet(examples);
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
        if (examples != null)
            builder.append("examples=").append(examples);
        builder.append("]");
        return builder.toString();
    }
}