package eu.dilcis.csip.structure;

import java.nio.file.Path;
import java.util.Objects;

import eu.dilcis.csip.structure.SpecificationStructure.SourceType;

public class Section {
    public final String name;
    public final Path source;
    public final SourceType type;

    Section(final String name, final Path source, final SourceType type) {
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