package eu.dilcis.csip.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Requirement {
    public static final class RequirementId implements Comparable<RequirementId> {
        static final String DEF_PREFIX = "PREF"; //$NON-NLS-1$
        static final String DEF_SUFFIX = "SUFF"; //$NON-NLS-1$
        static final int DEF_NUMBER = -1;
        public static final RequirementId DEFAULT_ID = new RequirementId();

        static RequirementId fromIdString(final String idString) {
            final StringBuilder prefixBuff = new StringBuilder();
            final StringBuilder suffixBuff = new StringBuilder();
            final StringBuilder numBuff = new StringBuilder();
            boolean isPrefix = true;
            for (int i = 0; i < idString.length(); i++) {
                final char c = idString.charAt(i);
                if (Character.isDigit(c)) {
                    numBuff.append(c);
                    isPrefix = false;
                } else if (isPrefix) {
                    prefixBuff.append(c);
                } else {
                    suffixBuff.append(c);
                }
            }
            return new RequirementId(prefixBuff.toString(),
                    Integer.parseInt(numBuff.toString()), suffixBuff.toString());
        }

        public final String prefix;
        public final int number;
        public final String suffix;

        private RequirementId() {
            this(DEF_PREFIX, DEF_NUMBER, DEF_SUFFIX);
        }

        private RequirementId(final String prefix, final int number, final String suffix) {
            super();
            this.prefix = prefix;
            this.number = number;
            this.suffix = suffix;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return this.prefix + this.number + this.suffix; // $NON-NLS-1$
        }

        @Override
        public int compareTo(final RequirementId other) {
            if (this.prefix.equals(other.prefix) && this.suffix.equals(other.suffix)) {
                if (this.number < other.number)
                    return -1;
                return (this.number == other.number) ? 0 : 1;
            }
            if (!this.prefix.equals(other.prefix)) {
                return this.prefix.compareTo(other.suffix);
            }
            return this.suffix.compareTo(other.prefix);
        }

        @Override
        public int hashCode() {
            return Objects.hash(prefix, number, suffix);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof RequirementId))
                return false;
            final RequirementId other = (RequirementId) obj;
            return Objects.equals(prefix, other.prefix) && number == other.number
                    && Objects.equals(suffix, other.suffix);
        }
    }

    public static final class Details {
        static Details fromValues(final String name, final Section section, final Level level) {
            return new Details(name, section, level);
        }

        public final String name;

        public final Level level;

        public final Section section;

        private Details(final String name, final Section section, final Level level) {
            this.name = name;
            this.section = section;
            this.level = level;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("Details [");
            if (name != null)
                builder.append("name=").append(name).append(", ");
            if (level != null)
                builder.append("level=").append(level);
            builder.append("]");
            return builder.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, section, level);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Details))
                return false;
            final Details other = (Details) obj;
            return Objects.equals(name, other.name) && level == other.level && section == other.section;
        }

    }

    public enum Level {
        MUST,
        SHOULD,
        MAY;

        static Level fromString(final String level) {
            for (final Level lvl : Level.values()) {
                if (lvl.name().equalsIgnoreCase(level)) {
                    return lvl;
                }
            }
            return null;
        }
    }

    static class Builder {
        private RequirementId id;
        private String name;
        private Level level;
        private Section section;
        private String relMat;
        private List<String> description;
        private List<String> examples;
        private String xPath;
        private String cardinality;
        private String descParts = "";

        public Builder() {
            this(Requirement.DEFAULT);
        }

        public Builder(final Builder builder) {
            this(builder.build());
        }

        public Builder(final Requirement req) {
            super();
            this.id = req.id;
            this.name = req.details.name;
            this.section = req.details.section;
            this.level = req.details.level;
            this.relMat = req.relMat;
            this.description = new ArrayList<>(req.description);
            this.examples = new ArrayList<>(req.examples);
            this.xPath = req.xPath;
            this.cardinality = req.cardinality;
        }

        public Builder processAttr(final String attName,
                final String attValue) {
            switch (attName) {
                case "ID": //$NON-NLS-1$
                    this.id(RequirementId.fromIdString(attValue));
                    break;

                case "REQLEVEL": //$NON-NLS-1$
                    this.reqLevel(Level.fromString(attValue));
                    break;

                case "RELATEDMAT": //$NON-NLS-1$
                    this.relMat(attValue);
                    break;

                case "EXAMPLES": //$NON-NLS-1$
                    for (final String example : attValue.split(" ")) { //$NON-NLS-1$
                        this.example(example);
                    }
                    break;

                default:
                    break;
            }
            return this;
        }

        /**
         * @param id
         *           the id to set
         */
        public Builder id(final RequirementId iD) {
            this.id = iD;
            return this;
        }

        /**
         * @param name
         *             the name to set
         */
        public Builder name(final String nm) {
            this.name = nm;
            return this;
        }

        /**
         * @param section
         *                the section to set
         */
        public Builder section(final Section section) {
            this.section = section;
            return this;
        }

        /**
         * @param level
         *              the reqLevel to set
         */
        public Builder relMat(final String rlMt) {
            this.relMat = rlMt;
            return this;
        }

        /**
         * @param level
         *              the reqLevel to set
         */
        public Builder reqLevel(final Level level) {
            this.level = level;
            return this;
        }

        public Builder descPart(final String part) {
            this.descParts += part;
            return this;
        }

        /**
         * @param description
         *                    the description to set
         */
        public Builder description(final String dscrptn) {
            if (dscrptn == null || dscrptn.isEmpty())
                return this;
            this.description.add(this.descParts + dscrptn);
            this.descParts = "";
            return this;
        }

        /**
         * @param description
         *                    the description to set
         */
        public Builder descriptions(final List<String> dscrptns) {
            this.description = new ArrayList<>(dscrptns);
            return this;
        }

        /**
         * @param examples
         *                 the examples to set
         */
        public Builder examples(final List<String> xmpls) {
            this.examples = new ArrayList<>(xmpls);
            return this;
        }

        /**
         * @param example
         *                the example to add
         */
        public Builder example(final String xmpl) {
            this.examples.add(xmpl);
            return this;
        }

        /**
         * @param xPath
         *              the xPath to set
         */
        public Builder defPair(final String term, final String def) {
            switch (term) {
                case Constants.XPATH_TERM:
                    return this.xPath(def);
                case Constants.CARD_TERM:
                    return this.cardinality(def);
                default:
                    break;
            }
            return this;
        }

        /**
         * @param xPath
         *              the xPath to set
         */
        public Builder xPath(final String xPth) {
            this.xPath = xPth;
            return this;
        }

        /**
         * @param cardinality
         *                    the cardinality to set
         */
        public Builder cardinality(final String crdnlty) {
            this.cardinality = crdnlty;
            return this;
        }

        public Requirement build() {
            if (this.descParts.length() > 0) {
                this.description.add(this.descParts);
            }
            return new Requirement(this.id, new Details(this.name, this.section, this.level),
                    this.relMat, this.description, this.examples, this.xPath,
                    this.cardinality);
        }
    }

    public static final Requirement DEFAULT = new Requirement();

    public final RequirementId id;
    public final Details details;
    final String relMat;
    public final List<String> description;
    final List<String> examples;
    public final String xPath;
    public final String cardinality;

    Requirement(final RequirementId id, final Details details, final String relMat,
            final List<String> description, final List<String> examples,
            final String xPath, final String cardinality) {
        super();
        this.id = id;
        this.details = details;
        this.relMat = relMat.trim();
        this.description = description;
        this.examples = examples;
        this.xPath = xPath;
        this.cardinality = cardinality;
    }

    private Requirement() {
        this(RequirementId.DEFAULT_ID, new Details(Constants.EMPTY, Section.ROOT,
                Level.MAY), Constants.EMPTY, Collections.emptyList(),
                Collections.emptyList(), Constants.EMPTY, Constants.EMPTY);
    }

    public RequirementId getId() {
        return this.id;
    }

    public String[] relatedMatter() {
        if (this.relMat == null || this.relMat.isEmpty())
            return new String[] {};
        return (this.relMat.contains(" ")) ? this.relMat.split(" ")
                : new String[] { this.relMat };
    }

    @Override
    public String toString() {
        final StringBuilder builder2 = new StringBuilder();
        builder2.append("Requirement [");
        if (id != null)
            builder2.append("id=").append(id).append(", ");
        if (details != null)
            builder2.append("details=").append(details).append(", ");
        if (relMat != null)
            builder2.append("relMat=").append(relMat).append(", ");
        if (description != null)
            builder2.append("description=").append(description).append(", ");
        if (examples != null)
            builder2.append("examples=").append(examples).append(", ");
        if (xPath != null)
            builder2.append("xPath=").append(xPath).append(", ");
        if (cardinality != null)
            builder2.append("cardinality=").append(cardinality);
        builder2.append("]");
        return builder2.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, details, relMat, description, examples, xPath, cardinality);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Requirement))
            return false;
        final Requirement other = (Requirement) obj;
        return Objects.equals(id, other.id) && Objects.equals(details, other.details)
                && Objects.equals(relMat, other.relMat) && Objects.equals(description, other.description)
                && Objects.equals(examples, other.examples) && Objects.equals(xPath, other.xPath)
                && Objects.equals(cardinality, other.cardinality);
    }
}