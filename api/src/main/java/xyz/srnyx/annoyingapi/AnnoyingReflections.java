package xyz.srnyx.annoyingapi;

import javassist.bytecode.ClassFile;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.Store;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.NameHelper;
import org.reflections.vfs.Vfs;

import xyz.srnyx.annoyingapi.parents.Registrable;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.reflections.scanners.Scanners.SubTypes;


/**
 * This class is a modified version of {@link Reflections} used for automatic {@link Registrable} in {@link AnnoyingPlugin}
 * <p>Only made this to avoid a logger being created which caused an irrelevant error to be logged on start-up
 * <p>It has the added bonus of less logic which makes it about 7x faster (about 46ms faster with 2 packages, 19 total classes)
 */
public class AnnoyingReflections implements NameHelper {
    @NotNull private final Set<String> packages;
    @NotNull private final Store store;

    /**
     * Construct a new {@link AnnoyingReflections} for the given packages
     *
     * @param   packages    the packages to scan
     */
    public AnnoyingReflections(@NotNull Set<String> packages) {
        this.packages = packages;
        this.store = getStore();
    }

    /**
     * Get all classes that are assignable to the given type
     *
     * @param   type    the type to check
     *
     * @return          a set of classes that are assignable to the given type
     *
     * @param   <T>     the type to check
     */
    @NotNull
    public <T> Set<Class<? extends T>> getSubTypesOf(@NotNull Class<T> type) {
        //noinspection unchecked
        return (Set<Class<? extends T>>) SubTypes.of(type).as((Class<? extends T>) Class.class).apply(store);
    }

    @NotNull
    private Store getStore() {
        final Map<String, Set<String>> storeMap = new HashMap<>();
        final Set<Pattern> patterns = packages.stream()
                .map(pkg -> {
                    if (!pkg.endsWith(".")) pkg += ".";
                    return Pattern.compile(pkg.replace(".", "\\.").replace("$", "\\$") + ".*");
                })
                .collect(Collectors.toSet());
        final Predicate<String> filter = string -> {
            if (!string.endsWith(".class")) return false;
            for (final Pattern pattern : patterns) if (pattern.matcher(string).matches()) return true;
            return false;
        };

        packages.stream()
                .map(ClasspathHelper::forPackage)
                .flatMap(Collection::stream)
                .parallel()
                .forEach(url -> {
                    Vfs.Dir dir = null;
                    try {
                        dir = Vfs.fromURL(url);
                        for (final Vfs.File file : dir.getFiles()) {
                            final String path = file.getRelativePath();
                            if (!filter.test(path) && !filter.test(path.replace('/', '.'))) continue;

                            try {
                                List<Map.Entry<String, String>> entries = SubTypes.scan(file);
                                if (entries == null) entries = SubTypes.scan(getClassFile(file));
                                if (entries != null) for (final Map.Entry<String, String> entry : entries) {
                                    final String key = entry.getKey();
                                    if (key == null) continue;
                                    // Not using computeIfAbsent as it throws ConcurrentModificationException on Java 9+
                                    Set<String> values = storeMap.get(key);
                                    if (values == null) values = new HashSet<>();
                                    values.add(entry.getValue());
                                    storeMap.put(key, values);
                                }
                            } catch (final Exception e) {
                                AnnoyingPlugin.log(Level.WARNING, "Could not scan file " + file.getRelativePath(), e);
                            }
                        }
                    } catch (final Exception e) {
                        AnnoyingPlugin.log(Level.WARNING, "Could not create Vfs.Dir from " + url + ". Ignoring the exception and continuing", e);
                    } finally {
                        if (dir != null) dir.close();
                    }
                });

        // expand super types
        if (!storeMap.isEmpty()) {
            final Set<String> subTypesKeys = new LinkedHashSet<>(storeMap.keySet());
            subTypesKeys.removeAll(storeMap.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet()));
            subTypesKeys.remove("java.lang.Object");
            for (final String key : subTypesKeys) {
                final Class<?> type = forClass(key);
                if (type != null) expandSupertypes(storeMap, key, type);
            }
        }

        // wrap
        final Map<String, Map<String, Set<String>>> finalMap = new HashMap<>();
        finalMap.put(SubTypes.index(), storeMap);
        return new Store(finalMap);
    }

    @NotNull @Contract("_ -> new")
    private ClassFile getClassFile(@NotNull Vfs.File file) {
        try (final DataInputStream dis = new DataInputStream(new BufferedInputStream(file.openInputStream()))) {
            return new ClassFile(dis);
        } catch (final Exception e) {
            throw new ReflectionsException("Could not create class object from file " + file.getRelativePath(), e);
        }
    }

    private void expandSupertypes(@NotNull Map<String, Set<String>> subTypesStore, @NotNull String key, @NotNull Class<?> type) {
        for (final Class<?> supertype : ReflectionUtils.getSuperTypes(type)) {
            final String supertypeName = supertype.getName();
            if (subTypesStore.containsKey(supertypeName)) {
                subTypesStore.get(supertypeName).add(key);
                continue;
            }
            subTypesStore.computeIfAbsent(supertypeName, s -> new HashSet<>()).add(key);
            expandSupertypes(subTypesStore, supertypeName, supertype);
        }
    }
}
