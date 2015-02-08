/*
 * Copyright (C) 2015 drrb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.drrb.javarust.build;

import java.io.File;
import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import static java.util.Arrays.asList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class CompileRustCrates {

    private static final Date EPOCH = new Date(0);
    private static final Path RUST_OUTPUT_DIR = Paths.get("target", "rust-libs");

    public static void main(String[] args) throws Exception {
        Paths.get("target", "rust-libs").toFile().mkdirs();
        if (changesDetected()) {
            System.out.println("Changes detected. Compiling all Rust crates!");
            for (Path crate : crates()) {
                compile(crate);
            }
        } else {
            System.out.println("No changes detected. Not recompiling Rust crates.");
        }
    }

    private static boolean changesDetected() throws IOException {
        Date lastSourceChange = newestChange(rustSources());
        Date lastCompilation = newestChange(compiledRustLibraries());
        return lastSourceChange.getTime() > lastCompilation.getTime();
    }

    private static void compile(Path sourceFile) {
        System.out.format("Compiling crate %s%n", sourceFile);
        try {
            Process process = rustcProcess(sourceFile).inheritIO().start();
            process.waitFor();
            if (process.exitValue() != 0) {
                throw new RuntimeException(String.format("rustc exited nonzero (status code = %s)", process.exitValue()));
            }
            for (Path compiledRustLibrary : compiledRustLibraries()) {
                moveLibIntoClasspath(compiledRustLibrary);
            }
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static ProcessBuilder rustcProcess(Path crateFile) {
        List<String> commandParts;
        if (inNetbeans() && new File("/bin/bash").isFile()) {
            System.out.println("(running rustc via bash because we're in NetBeans)");
            commandParts = asList("/bin/bash", "-lc", String.format("rustc --out-dir %s %s", RUST_OUTPUT_DIR, crateFile));
        } else {
            commandParts = asList("rustc", "--out-dir", RUST_OUTPUT_DIR.toString(), crateFile.toString());
        }
        System.out.format("Running command: %s%n", commandParts);
        return new ProcessBuilder(commandParts);
    }

    private static void moveLibIntoClasspath(Path library) {
        try {
            Path outputDir = outputDir();
            outputDir.toFile().mkdirs();
            System.out.format("Installing %s into %s%n", library, outputDir);
            Files.copy(library, outputDir.resolve(library.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Path outputDir() {
        return Paths.get("target", "classes", osArchName());
    }

    private static String osArchName() {
        return Os.getCurrent().jnaArchString();
    }

    private static List<Path> crates() throws IOException {
        List<Path> crates = new LinkedList<>();
        for (Path rustSource : rustSources()) {
            if (isCrate(rustSource)) {
                crates.add(rustSource);
            }
        }
        return crates;
    }

    private static List<Path> rustSources() throws IOException {
        return findFiles(Paths.get("src", "main", "rust"), new FileFinder() {

            @Override
            protected boolean accept(Path file, BasicFileAttributes attrs) {
                return isRustSource(file, attrs);
            }
        });
    }

    private static List<Path> compiledRustLibraries() throws IOException {
        return findFiles(RUST_OUTPUT_DIR, new FileFinder() {

            @Override
            protected boolean accept(Path file, BasicFileAttributes attrs) {
                return isDylib(file, attrs);
            }
        });
    }

    private static List<Path> findFiles(Path startPath, FileFinder finder) throws IOException {
        Files.walkFileTree(startPath, finder);
        return finder.getFound();
    }

    private static boolean inNetbeans() {
        for (Map.Entry<String, String> envVars : System.getenv().entrySet()) {
            String key = envVars.getKey();
            String value = envVars.getValue();
            if (key.matches("JAVA_MAIN_CLASS_\\d+") && value.equals("org.netbeans.Main")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRustSource(Path path, BasicFileAttributes attributes) {
        return attributes.isRegularFile() && path.toString().endsWith(".rs");
    }

    private static boolean isCrate(Path path) {
        try {
            return path.toFile().isFile()
                    && path.toString().endsWith(".rs")
                    && new String(Files.readAllBytes(path), UTF_8).contains("#![crate_type");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean isDylib(Path path, BasicFileAttributes attributes) {
        String pathString = path.toString();
        String pathExtension = pathString.substring(pathString.lastIndexOf("."));
        List<String> dylibExtensions = asList(".dylib", ".so", ".dll");
        return attributes.isRegularFile() && dylibExtensions.contains(pathExtension);
    }

    private static Date newestChange(List<Path> paths) {
        Date lastChange = EPOCH;
        for (Path path : paths) {
            Date change = mtime(path);
            if (change.getTime() > lastChange.getTime()) {
                lastChange = change;
            }
        }
        return lastChange;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static Date mtime(Path path) {
        try {
            return new Date(Files.getLastModifiedTime(path).toMillis());
        } catch (IOException ex) {
            ex.printStackTrace();
            return EPOCH;
        }
    }

    private enum Os {
        MAC_OS("mac", "darwin") {
            @Override
            public String jnaArchString() {
                return "darwin";
            }
        },
        WINDOWS("win") {
            @Override
            public String jnaArchString() {
                if (currentIs64Bit()) {
                    return "win32-x86-64";
                } else {
                    return "win32-x86";
                }
            }
        },
        GNU_SLASH_LINUX("nux") {
            @Override
            public String jnaArchString() {
                if (currentIs64Bit()) {
                    return "linux-x86-64";
                } else {
                    return "linux-x86";
                }
            }
        },
        UNKNOWN() {
            @Override
            public String jnaArchString()  {
                throw new RuntimeException("Unknown platform. Can't tell what platform we're running on!");
            }
        };
        private final String[] substrings;

        private Os(String... substrings) {
            this.substrings = substrings;
        }

        public abstract String jnaArchString();

        public static Os getCurrent() {
            for (Os os : values()) {
                if (os.isCurrent()) {
                    return os;
                }
            }
            return UNKNOWN;
        }

        public boolean isCurrent() {
            for (String substring : substrings) {
                if (currentOsString().contains(substring)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean currentIs64Bit() {
            return System.getProperty("os.arch").contains("64");
        }

        private static String currentOsString() {
            return System.getProperty("os.name", "unknown").toLowerCase(Locale.ENGLISH);
        }
    }

    private static abstract class FileFinder implements FileVisitor<Path> {
        private final List<Path> found = new LinkedList<>();

        public List<Path> getFound() {
            return found;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (accept(file, attrs)) {
                found.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        protected abstract boolean accept(Path file, BasicFileAttributes attrs);
    }
}
