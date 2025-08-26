package eu.dilcis.csip.structure;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SpecificationStructure {
    public enum Part {
        TABLES, APPENDICES;

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

        public String getFolderName() {
            return this.name().toLowerCase();
        }
    }

    static final SpecificationStructure fromContentMap(final Map<Part, List<Source>> content) {
        return new SpecificationStructure(content);
    }

    static String tableStringFromTemplate(final RequirementsSource table) throws IOException {
        try (final StringWriter writer = new StringWriter()) {
            table.serialise(writer);
            return writer.toString();
        }
    }

    public final Map<Part, List<Source>> content;

    private SpecificationStructure(final Map<Part, List<Source>> content) {
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
}