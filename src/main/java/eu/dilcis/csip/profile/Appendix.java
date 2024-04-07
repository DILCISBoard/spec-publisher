package eu.dilcis.csip.profile;

import java.util.Objects;

public final class Appendix {
    public static Appendix fromValues(final int number, final String label, final String content) {
        return new Appendix(number, label, content);
    }

    public final int number;
    public final String label;

    public final String content;

    private Appendix(final int number, final String label, final String content) {
        this.number = number;
        this.label = label;
        this.content = content;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, label, content);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Appendix))
            return false;
        final Appendix other = (Appendix) obj;
        return number == other.number && Objects.equals(label, other.label) && Objects.equals(content, other.content);
    }
}
