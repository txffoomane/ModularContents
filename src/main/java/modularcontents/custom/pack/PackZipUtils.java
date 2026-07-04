package modularcontents.custom.pack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PackZipUtils {
    private static final Logger LOGGER = LogManager.getLogger("ModularContents");

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
                    String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
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
