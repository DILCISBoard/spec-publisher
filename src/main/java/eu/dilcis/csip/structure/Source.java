package eu.dilcis.csip.structure;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

public abstract class Source {
    enum SourceType {
        FILE, REQUIREMENTS, METS;

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

    public final Source.SourceType type;
    public final String name;
    public final String heading;
    public final String label;

    Source(final Source.SourceType type, final String name, final String heading, final String label) {
        super();
        this.name = name;
        this.type = type;
        this.heading = heading;
        this.label = label;
    }

    public abstract void serialise(final Writer destination, final boolean isPdf) throws IOException;

    @Override
    public final int hashCode() {
        return Objects.hash(type, name, heading, label);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Source))
            return false;
        final Source other = (Source) obj;
        return type == other.type && Objects.equals(name, other.name) && Objects.equals(heading, other.heading)
                && Objects.equals(label, other.label);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Source [");
        if (type != null)
            builder.append("type=").append(type).append(", ");
        if (name != null)
            builder.append("name=").append(name).append(", ");
        if (heading != null)
            builder.append("heading=").append(heading).append(", ");
        if (label != null)
            builder.append("label=").append(label);
        builder.append("]");
        return builder.toString();
    }
}