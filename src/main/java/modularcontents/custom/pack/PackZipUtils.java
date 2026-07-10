package modularcontents.custom.pack;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PackZipUtils {
    private static final Logger LOGGER = LogManager.getLogger("ModularContents");
    private static final Gson GSON = new Gson();

    public interface ZipJsonConsumer {
        void accept(String fileName, Reader reader, String packName) throws Exception;
    }

    private PackZipUtils() {
    }

    public static boolean isZipName(String name) {
        return name.toLowerCase(Locale.ROOT).endsWith(".zip");
    }

    public static File[] listZips(File rootPacksDir) {
        File[] zips = rootPacksDir.listFiles((d, name) -> isZipName(name));
        return zips != null ? zips : new File[0];
    }

    public static boolean isEntryInFolder(String entryName, String folder, String extension) {
        String lower = entryName.replace('\\', '/').toLowerCase(Locale.ROOT);
        String folderLower = folder.toLowerCase(Locale.ROOT);
        return (lower.startsWith(folderLower + "/") || lower.contains("/" + folderLower + "/")) && lower.endsWith(extension);
    }

    public static boolean zipHasEntryInFolder(File zipFile, String folder, String extension) {
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && isEntryInFolder(entry.getName(), folder, extension)) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read pack archive: " + zipFile.getName(), e);
        }
        return false;
    }

    public static void loadJsonEntries(File rootPacksDir, String folder, ZipJsonConsumer consumer) {
        for (File zipFile : listZips(rootPacksDir)) {
            try (ZipFile zip = new ZipFile(zipFile)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.isDirectory() || !isEntryInFolder(entry.getName(), folder, ".json")) {
                        continue;
                    }
                    String normalized = entry.getName().replace('\\', '/');
                    String lower2 = normalized.toLowerCase(Locale.ROOT); String folderLower2 = folder.toLowerCase(Locale.ROOT); int idx = lower2.indexOf("/" + folderLower2 + "/"); String fileName; if (idx != -1) { fileName = normalized.substring(idx + folderLower2.length() + 2); } else if (lower2.startsWith(folderLower2 + "/")) { fileName = normalized.substring(folderLower2.length() + 1); } else { fileName = normalized.substring(normalized.lastIndexOf('/') + 1); }
                    try (Reader reader = new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
                        consumer.accept(fileName, reader, zipFile.getName());
                    } catch (Exception e) {
                        LOGGER.error("Failed to load " + entry.getName() + " from pack " + zipFile.getName(), e);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to open pack archive: " + zipFile.getName(), e);
            }
        }
    }

    public static InputStream findZipResource(File rootPacksDir, String pathSuffix) {
        String suffix = pathSuffix.replace('\\', '/');
        for (File zipFile : listZips(rootPacksDir)) {
            try (ZipFile zip = new ZipFile(zipFile)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.isDirectory()) continue;
                    String normalized = entry.getName().replace('\\', '/');
                    if (normalized.equals(suffix) || normalized.endsWith("/" + suffix)) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buf = new byte[8192];
                        try (InputStream in = zip.getInputStream(entry)) {
                            int read;
                            while ((read = in.read(buf)) != -1) {
                                out.write(buf, 0, read);
                            }
                        }
                        return new ByteArrayInputStream(out.toByteArray());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to read pack archive: " + zipFile.getName(), e);
            }
        }
        return null;
    }

    public static String normalizePackName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".zip") ? lower.substring(0, lower.length() - 4) : lower;
    }

    private static boolean dirHasFiles(File dir, String extension) {
        if (!dir.isDirectory()) return false;
        File[] files = dir.listFiles();
        if (files == null) return false;
        for (File file : files) {
            if (file.isDirectory()) {
                if (dirHasFiles(file, extension)) return true;
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static String getClientRequiredPacksJson(File gameDir) {
        List<String> names = new ArrayList<>();
        File rootPacksDir = new File(gameDir, "ModularContents");

        File[] packDirs = rootPacksDir.listFiles(File::isDirectory);
        if (packDirs != null) {
            for (File packDir : packDirs) {
                if (dirHasFiles(new File(packDir, "items"), ".json") || dirHasFiles(new File(packDir, "textures"), ".png")) {
                    names.add(normalizePackName(packDir.getName()));
                }
            }
        }

        for (File zip : listZips(rootPacksDir)) {
            if (zipHasEntryInFolder(zip, "items", ".json") || zipHasEntryInFolder(zip, "textures", ".png")) {
                names.add(normalizePackName(zip.getName()));
            }
        }
        return GSON.toJson(names);
    }

    public static List<String> findMissingPacks(File gameDir, String serverPacksJson) {
        List<String> missing = new ArrayList<>();
        try {
            String[] serverPacks = GSON.fromJson(serverPacksJson, String[].class);
            if (serverPacks == null || serverPacks.length == 0) return missing;

            Set<String> localPacks = new HashSet<>();
            File rootPacksDir = new File(gameDir, "ModularContents");
            File[] packDirs = rootPacksDir.listFiles(File::isDirectory);
            if (packDirs != null) {
                for (File packDir : packDirs) {
                    localPacks.add(normalizePackName(packDir.getName()));
                }
            }
            for (File zip : listZips(rootPacksDir)) {
                localPacks.add(normalizePackName(zip.getName()));
            }

            for (String pack : serverPacks) {
                if (pack != null && !pack.isEmpty() && !localPacks.contains(pack)) {
                    missing.add(pack);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to compare content packs with server", e);
        }
        return missing;
    }

    public static boolean zipResourceExists(File rootPacksDir, String pathSuffix) {
        String suffix = pathSuffix.replace('\\', '/');
        for (File zipFile : listZips(rootPacksDir)) {
            try (ZipFile zip = new ZipFile(zipFile)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.isDirectory()) continue;
                    String normalized = entry.getName().replace('\\', '/');
                    if (normalized.equals(suffix) || normalized.endsWith("/" + suffix)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to read pack archive: " + zipFile.getName(), e);
            }
        }
        return false;
    }
}
