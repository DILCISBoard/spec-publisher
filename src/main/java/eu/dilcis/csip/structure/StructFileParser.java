package eu.dilcis.csip.structure;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

import eu.dilcis.csip.profile.Appendix;
import eu.dilcis.csip.profile.ControlledVocabulary;
import eu.dilcis.csip.profile.Example;
import eu.dilcis.csip.profile.ExternalSchema;
import eu.dilcis.csip.profile.MetsProfile;
import eu.dilcis.csip.profile.Profiles;
import eu.dilcis.csip.profile.Requirement.RequirementId;
import eu.dilcis.csip.structure.SpecificationStructure.Part;
import eu.dilcis.csip.structure.SpecificationStructure.SourceType;

public final class StructFileParser {
    public static StructFileParser parserInstance(final Collection<MetsProfile> profiles) {
        return new StructFileParser(profiles);
    }

    static Table tableFromMap(final Collection<MetsProfile> profiles, final Path root, final String name,
            final Map<String, Object> entryMap)
            throws ParseException {
        try {
            final String heading = stringFromMap(name, "heading", entryMap);
            final Path path = pathFromMap(root, name, entryMap);
            if (Files.isDirectory(path)) {
                throw new ParseException("Target file is a directory: " + path + " for table: " + name);
            }
            final List<RequirementId> reqIds = getRequirementIds(profiles, name, entryMap);
            final Set<Example> examples = Profiles.examplesFromRequirments(profiles, reqIds);
            return SpecificationStructure.tableFromValues(name, path, heading, reqIds, examples);
        } catch (final ClassCastException e) {
            throw new ParseException("Invalid key value for table: " + name, e);
        } catch (final NoSuchElementException e) {
            throw new ParseException("Not all requirements found in profiles for table: " + name, e);
        }
    }

    private static List<RequirementId> getRequirementIds(final Collection<MetsProfile> profiles, final String name,
            final Map<String, Object> entryMap) throws ParseException {
        @SuppressWarnings("unchecked")
        final List<String> reqIds = (List<String>) entryMap.get("requirements");
        if (reqIds != null && !reqIds.isEmpty()) {
            return getRequirementIds(reqIds);
        }
        final String sectionName = stringFromMap(name, "section", entryMap);
        final eu.dilcis.csip.profile.Section section = eu.dilcis.csip.profile.Section.fromEleName(sectionName);
        final List<RequirementId> reqList = new ArrayList<>();
        for (final MetsProfile profile : profiles) {
            reqList.addAll(
                    profile.getRequirementsBySection(section).stream().map(r -> r.id).collect(Collectors.toList()));
        }
        return reqList;
    }

    private static List<RequirementId> getRequirementIds(final List<String> reqIds) throws ParseException {
        final List<RequirementId> ids = new ArrayList<>();
        for (final String reqId : reqIds) {
            if (reqId == null || reqId.isEmpty()) {
                throw new ParseException("Invalid requirement id: " + reqId);
            }
            final RequirementId id = Profiles.requirementIdFromString(reqId);
            if (id == null) {
                throw new ParseException("Invalid requirement id: " + reqId);
            }
            ids.add(id);
        }
        return ids;
    }

    private static Path pathFromMap(final Path root, final String name, final Map<String, Object> entryMap)
            throws ParseException {
        try {
            return root.resolve(stringFromMap(name, "path", entryMap));
        } catch (final InvalidPathException e) {
            throw new ParseException("Invalid path value for section: " + name, e);
        }
    }

    private static String stringFromMap(final String name, final String key, final Map<String, Object> entryMap)
            throws ParseException {
        try {
            final String value = (String) entryMap.get(key);
            if (value == null) {
                throw new ParseException("No key:" + key + " found for entry:" + name);
            }
            return value;
        } catch (final ClassCastException e) {
            throw new ParseException("Invalid value for key:" + key + " found for entry:" + name, e);
        }
    }

    private final Map<URI, MetsProfile> profiles;

    private Path root;

    private StructFileParser(final Collection<MetsProfile> profiles) {
        this.profiles = profiles.stream().collect(Collectors.toMap(MetsProfile::getUri, Function.identity()));
    }

    public SpecificationStructure parseStructureFile(final Path source) throws ParseException {
        if (source == null) {
            throw new IllegalArgumentException("Parameter source is null");
        }
        if (!Files.exists(source) || Files.isDirectory(source)) {
            throw new IllegalArgumentException("File does not exist, or is a directory: " + source);
        }
        this.root = source.toFile().getParentFile().toPath();
        return this.fromSource(source);
    }

    public List<MetsProfile> getProfiles() {
        return new ArrayList<>(this.profiles.values());
    }

    SpecificationStructure fromYamlStream(final InputStream source, final Path root) throws ParseException {
        if (source == null) {
            throw new IllegalArgumentException("Parameter source is null");
        }
        if (root == null || !Files.exists(root) || !Files.isDirectory(root)) {
            throw new IllegalArgumentException("Parameter root is null, or is not an existing directory.");
        }
        this.root = root;
        final Map<Part, List<Section>> content = new EnumMap<>(Part.class);
        for (final Entry<String, List<Map<String, Object>>> entryMap : this.parseYamlStream(source).entrySet()) {
            final List<Section> sections = new ArrayList<>();
            for (final Map<String, Object> entry : entryMap.getValue()) {
                sections.add(this.sectionFromMapEntry(entry));
            }
            content.put(Part.fromString(entryMap.getKey()), sections);
        }
        return SpecificationStructure.fromContentMap(content);
    }

    private Section sectionFromMap(final String name, final SourceType type, final Map<String, Object> entryMap)
            throws ParseException {
        final Path path = pathFromMap(this.root, name, entryMap);
        if (!Files.exists(path) || Files.isDirectory(path)) {
            throw new ParseException(
                    "Source file does not exist, or is a directory: " + path + " for section: " + name);
        }
        return SpecificationStructure.sectionFromValues(name, path, type);
    }

    private Section sectionFromMapEntry(final Map<String, Object> entryMap) throws ParseException {
        final String name = (String) entryMap.get("name");
        if (name == null) {
            throw new ParseException("No name found for entry.");
        }
        final SourceType type = SourceType.fromString((String) entryMap.get("type"));
        if (type == null) {
            throw new ParseException("No type found for entry: " + name);
        }
        if (SourceType.TABLE.equals(type)) {
            return tableFromMap(this.profiles.values(), this.root, name, entryMap);
        }
        if (SourceType.METS.equals(type)) {
            return this.sectionFromMetsMapEntry(name, type, entryMap);
        }
        return this.sectionFromMap(name, type, entryMap);
    }

    private Section sectionFromMetsMapEntry(final String name, final SourceType type,
            final Map<String, Object> entryMap) throws ParseException {
        final String source = stringFromMap(name, "source", entryMap);
        final Path path = pathFromMap(this.root, name, entryMap);
        Map<String, Object> context = new HashMap<>();
        context.put("label", stringFromMap(name, "label", entryMap));
        context.put("name", name);
        try {
            if ("Appendix".equals(source)) {
                serialiseAppendices(path, context);
            } else if ("external_schema".equals(source)) {
                serialiseExternalSchema(path, context);
            } else if ("vocabulary".equals(source)) {
                serialiseVocabs(path, context);
            } else if ("requirements".equals(source)) {
                context.put("profiles", this.profiles.values());
                Utilities.serialiseToTemplate("eu/dilcis/csip/out/appendix_requirements.mustache", context,
                        new FileWriter(path.toFile()));
            } else {
                throw new ParseException("Invalid source value for entry: " + name);
            }

        } catch (IOException e) {
            throw new ParseException(source + " serialisation failed for entry: " + name, e);
        }
        return this.sectionFromMap(name, type, entryMap);
    }

    private void serialiseAppendices(final Path path, final Map<String, Object> context) throws IOException {
        List<Appendix> appendices = new ArrayList<>();
        for (MetsProfile profile : this.profiles.values()) {
            appendices.addAll(profile.getAppendices());
        }
        context.put("appendices", appendices);
        try (Writer writer = new FileWriter(path.toFile())) {
            Utilities.serialiseToTemplate("eu/dilcis/csip/out/appendices.mustache", context, writer);
        }
    }

    private void serialiseExternalSchema(final Path path, final Map<String, Object> context) throws IOException {
        List<ExternalSchema> schema = new ArrayList<>();
        for (MetsProfile profile : this.profiles.values()) {
            schema.addAll(profile.getSchema());
        }
        context.put("schema", schema);
        try (Writer writer = new FileWriter(path.toFile())) {
            Utilities.serialiseToTemplate("eu/dilcis/csip/out/schema.mustache", context, writer);
        }
    }

    private void serialiseVocabs(final Path path, final Map<String, Object> context) throws IOException {
        List<ControlledVocabulary> vocabularies = new ArrayList<>();
        for (MetsProfile profile : this.profiles.values()) {
            vocabularies.addAll(profile.getVocabularies());
        }
        context.put("vocabularies", vocabularies);
        try (Writer writer = new FileWriter(path.toFile())) {
            Utilities.serialiseToTemplate("eu/dilcis/csip/out/vocabularies.mustache", context, writer);
        }
    }

    private Map<String, List<Map<String, Object>>> parseYamlStream(final InputStream source) throws ParseException {
        try {
            final Map<String, List<Map<String, Object>>> mapList = new Yaml().load(source);
            if (mapList == null || mapList.isEmpty()) {
                throw new ParseException("No entries found in source Stream.");
            }
            return mapList;
        } catch (final ClassCastException e) {
            throw new ParseException("YAML stream is not a list of objects.", e);
        }
    }

    private SpecificationStructure fromSource(final Path source) throws ParseException {
        try {
            return this.fromYamlStream(Files.newInputStream(source), this.root);
        } catch (ParseException | IOException e) {
            throw new ParseException("Exception caught when parsing source: " + source, e);
        }
    }
}
