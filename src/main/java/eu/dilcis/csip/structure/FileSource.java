package eu.dilcis.csip.structure;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

final class FileSource extends Source {
    static final FileSource fromValues(final String name, final Path source, final String heading, final String label) {
        return new FileSource(name, source, heading, label);
    }

    final Path filePath;

    private FileSource(final String name, final Path source, final String heading, final String label) {
        super(Source.SourceType.FILE, name, heading, label);
        this.filePath = source;
    }

    @Override
    public void serialise(final Writer destination) throws IOException {
        try (FileReader reader = new FileReader(this.filePath.toFile())) {
            reader.transferTo(destination);
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("FileSource [");
        if (type != null)
            builder.append("type=").append(type).append(", ");
        if (name != null)
            builder.append("name=").append(name).append(", ");
        if (filePath != null)
            builder.append("filePath=").append(filePath).append(", ");
        if (heading != null)
            builder.append("heading=").append(heading).append(", ");
        if (label != null)
            builder.append("label=").append(label);
        builder.append("]");
        return builder.toString();
    }

}
