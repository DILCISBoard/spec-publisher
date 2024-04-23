package eu.dilcis.csip.structure;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.dilcis.csip.profile.Example;
import eu.dilcis.csip.profile.MetsProfile;
import eu.dilcis.csip.profile.Profiles;
import eu.dilcis.csip.profile.Requirement;
import eu.dilcis.csip.profile.Requirement.RequirementId;

public class SourceFactory {
    static final SourceFactory getInstance(final Collection<MetsProfile> profiles) {
        return new SourceFactory(profiles);
    }

    final Map<URI, MetsProfile> profiles;
    private Path root;

    private SourceFactory(final Collection<MetsProfile> profiles) {
        this.profiles = profiles.stream().collect(Collectors.toMap(MetsProfile::getUri, Function.identity()));
    }

    public Source sectionFromMapEntry(final Path root, final Map<String, Object> entryMap)
            throws ParseException {
        this.root = root;
        final String name = stringFromMap("unknown", "name", entryMap);
        final Source.SourceType type = Source.SourceType.fromString(stringFromMap(name, "type", entryMap));
        switch (type) {
            case METS:
                return this.metsFromMap(name, entryMap);
            case REQUIREMENTS:
                return requirementsFromMap(this.profiles.values(), name, entryMap);
            default:
                return this.fileFromMap(name, entryMap);
        }
    }

    private Source fileFromMap(final String name, final Map<String, Object> entryMap)
            throws ParseException {
        final Path source = sourceFromMap(root, name, entryMap);
        final String label = stringFromMap(name, "label", entryMap, false);
        final String heading = stringFromMap(name, "heading", entryMap, false);
        if (!Files.exists(source) || Files.isDirectory(source)) {
            throw new ParseException(
                    "Source file does not exist, or is a directory: " + source + " for section: " + name);
        }
        return FileSource.fromValues(name, source, heading, label);
    }

    private Source metsFromMap(final String name,
            final Map<String, Object> entryMap) throws ParseException {
        final String section = stringFromMap(name, "section", entryMap);
        final String label = stringFromMap(name, "label", entryMap);
        final String heading = stringFromMap(name, "heading", entryMap);
        return MetsSource.fromValues(name, heading, label, section, this.profiles.values());
    }

    private static String stringFromMap(final String name, final String key, final Map<String, Object> entryMap)
            throws ParseException {
        return stringFromMap(name, key, entryMap, true);
    }

    private static String stringFromMap(final String name, final String key, final Map<String, Object> entryMap,
            final boolean required)
            throws ParseException {
        try {
            final String value = (String) entryMap.get(key);
            if (required && (value == null || value.isEmpty())) {
                throw new ParseException("No key:" + key + " found for entry:" + name);
            }
            return value;
        } catch (final ClassCastException e) {
            throw new ParseException("Invalid value for key:" + key + " found for entry:" + name, e);
        }
    }

    private static Path sourceFromMap(final Path root, final String name, final Map<String, Object> entryMap)
            throws ParseException {
        try {
            return root.resolve(stringFromMap(name, "source", entryMap));
        } catch (final InvalidPathException e) {
            throw new ParseException("Invalid source path value for section: " + name, e);
        }
    }

    static RequirementsSource requirementsFromMap(final Collection<MetsProfile> profiles, final String name,
            final Map<String, Object> entryMap)
            throws ParseException {
        try {
            final String heading = stringFromMap(name, "heading", entryMap);
            final List<Requirement> requirements = getRequirements(profiles, name, entryMap);
            final Set<Example> examples = Profiles.examplesFromRequirments(profiles, requirements);
            return RequirementsSource.fromValues(name, heading, requirements, examples);
        } catch (final ClassCastException e) {
            throw new ParseException("Invalid key value for table: " + name, e);
        } catch (final NoSuchElementException e) {
            throw new ParseException("Not all requirements found in profiles for table: " + name, e);
        }
    }

    private static List<Requirement> getRequirements(final Collection<MetsProfile> profiles, final String name,
            final Map<String, Object> entryMap) throws ParseException {
        @SuppressWarnings("unchecked")
        final List<String> reqIds = (List<String>) entryMap.get("requirements");
        if (reqIds != null && !reqIds.isEmpty()) {
            return getReqsFromProfiles(profiles, getRequirementIds(reqIds));
        }
        final String sectionName = stringFromMap(name, "section", entryMap);
        final eu.dilcis.csip.profile.Section section = eu.dilcis.csip.profile.Section.fromEleName(sectionName);
        final List<Requirement> requirements = new ArrayList<>();
        for (final MetsProfile profile : profiles) {
            requirements.addAll(profile.getRequirementsBySection(section));
        }
        return requirements;
    }

    private static List<Requirement> getReqsFromProfiles(final Collection<MetsProfile> profiles,
            final List<RequirementId> reqIds) {
        final List<Requirement> reqs = new ArrayList<>();
        for (final MetsProfile profile : profiles) {
            for (final RequirementId reqId : reqIds) {
                final Requirement req = profile.getRequirementById(reqId);
                if (req != null) {
                    reqs.add(req);
                }
            }
        }
        return reqs;
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
}
