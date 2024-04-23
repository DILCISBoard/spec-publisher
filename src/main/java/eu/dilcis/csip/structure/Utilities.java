package eu.dilcis.csip.structure;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class Utilities {
    private Utilities() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void serialiseToTemplate(final String template, final Map<String, Object> context,
            final Writer destination) throws IOException {
        final MustacheFactory mf = new DefaultMustacheFactory();
        final Mustache m = mf.compile(template);
        m.execute(destination, context).flush();
    }
}
