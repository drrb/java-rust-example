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
import java.util.Arrays;
import static java.util.stream.Collectors.toList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Provides the functionality to compile Rust crates
 * as a maven action.
 */
public class CompileRustCrates {

    private static final Date EPOCH = new Date(0);
    private static final Path RUST_OUTPUT_DIR = Paths.get("target", "rust-libs");

    public static void main(String[] args) throws Exception {
        Paths.get("target", "rust-libs").toFile().mkdirs();
        if (changesDetected()) {
            System.out.println("Changes detected. Compiling all Rust crates!");
            crates().forEach(CompileRustCrates::compile);
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
        return rustSources().stream()
                .filter(CompileRustCrates::isCrate)
                .collect(toList());
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
        return System.getenv().entrySet()
                .stream()
                .anyMatch(envVars -> {
                    String key = envVars.getKey();
                    String value = envVars.getValue();
                    return key.matches("JAVA_MAIN_CLASS_\\d+") && value.equals("org.netbeans.Main");
                });
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
        return paths.stream()
                .map(CompileRustCrates::mtime)
                .max(Comparator.comparingLong(Date::getTime))
                .orElse(EPOCH);
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
                return currentIs64Bit() ? "win32-x86-64" : "win32-x86";
            }
        },
        GNU_SLASH_LINUX("nux") {
            @Override
            public String jnaArchString() {
                return currentIs64Bit() ? "linux-x86-64" : "linux-x86";
            }
        },
        UNKNOWN() {
            @Override
            public String jnaArchString()  {
                throw new RuntimeException("Unknown platform. Can't tell what platform we're running on!");
            }
        };
        private final String[] substrings;

        Os(String... substrings) {
            this.substrings = substrings;
        }

        public abstract String jnaArchString();

        public static Os getCurrent() {
            return Arrays.stream(values())
                    .filter(Os::isCurrent)
                    .findFirst()
                    .orElse(UNKNOWN);
        }

        public boolean isCurrent() {
            return Arrays.stream(substrings)
                    .anyMatch(substring -> currentOsString().contains(substring));
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

        List<Path> getFound() {
            return found;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (accept(file, attrs)) {
                found.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        protected abstract boolean accept(Path file, BasicFileAttributes attrs);
    }
}
