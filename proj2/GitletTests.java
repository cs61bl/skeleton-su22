import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ComparisonFailure;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitletTests {
    static final Path SRC = Path.of("../test_files");
    static final Path WUG = SRC.resolve("wug.txt");
    static final Path NOTWUG = SRC.resolve("notwug.txt");
    static final Path WUG2 = SRC.resolve("wug2.txt");
    static final Path WUG3 = SRC.resolve("wug3.txt");
    static final Path CONFLICT1 = SRC.resolve("conflict1.txt");
    static final Path CONFLICT2 = SRC.resolve("conflict2.txt");
    static final Path CONFLICT3 = SRC.resolve("conflict3.txt");
    static final Path CONFLICT4 = SRC.resolve("conflict4.txt");
    static final Path CONFLICT5 = SRC.resolve("conflict5.txt");
    static final Path CONFLICT6 = SRC.resolve("conflict6.txt");
    static final Path A = SRC.resolve("a.txt");
    static final Path B = SRC.resolve("b.txt");
    static final Path C = SRC.resolve("c.txt");
    static final Path D = SRC.resolve("d.txt");
    static final Path E = SRC.resolve("e.txt");
    static final Path F = SRC.resolve("f.txt");
    static final Path G = SRC.resolve("g.txt");
    static final Path NOTA = SRC.resolve("nota.txt");
    static final Path NOTB = SRC.resolve("notb.txt");
    static final Path NOTF = SRC.resolve("notf.txt");
    static final String DATE = "Date: \\w\\w\\w \\w\\w\\w \\d+ \\d\\d:\\d\\d:\\d\\d \\d\\d\\d\\d [-+]\\d\\d\\d\\d";
    static final String COMMIT_HEAD = "commit ([a-f0-9]+)[ \\t]*\\n(?:Merge:\\s+[0-9a-f]{7}\\s+[0-9a-f]{7}[ ]*\\n)?" + DATE;
    static final String COMMIT_LOG = "(===[ ]*\\ncommit [a-f0-9]+[ ]*\\n(?:Merge:\\s+[0-9a-f]{7}\\s+[0-9a-f]{7}[ ]*\\n)?${DATE}[ ]*\\n(?:.|\\n)*?(?=\\Z|\\n===))"
            .replace("${DATE}", DATE);
    static final String ARBLINE = "[^\\n]*(?=\\n|\\Z)";
    static final String ARBLINES = "(?:(?:.|\\n)*(?:\\n|\\Z)|\\A|\\Z)";

    private static final String COMMAND_BASE = "java gitlet.Main ";
    private static final int DELAY_MS = 150;
    private static final String TESTING_DIR = "testing";

    private static final PrintStream OG_OUT = System.out;
    private static final ByteArrayOutputStream OUT = new ByteArrayOutputStream();

    /**
     * Asserts that the test suite is being run in TESTING_DIR.
     * <p>
     * Gitlet does dangerous file operations, and is affected by the existence
     * of other files. Therefore, we must ensure that we are working in a known
     * directory that (hopefully) has no files.
     */
    public static void verifyWD() {
        Path wd = Path.of(System.getProperty("user.dir"));
        if (!wd.getFileName().endsWith(TESTING_DIR)) {
            fail("This test is not being run in the `testing` directory. " +
                    "Please see the spec for information on how to fix this.");
        }
    }

    @BeforeClass
    public static void setup01_verifyWD() {
        verifyWD();
    }

    /**
     * Asserts that no class uses nontrivial statics.
     * <p>
     * Using a JUnit tester over a multiple-execution script means that
     * we are running in a single invocation of the JVM, which means that
     * static variables keep their values. Rather than attempting to restore
     * static state (which is nontrivial), we simply ban any static state
     * aside from primitives, Strings (immutable), Paths (immutable),
     * Files (immutable), SimpleDateFormat (not immutable, but can't carry
     * useful info), and a couple utility classes for tests.
     * <p>
     * This test is not a game to be defeated. Even if you manage to smuggle
     * static state, the autograder will test your program by running it
     * over multiple invocations, and your static variables will be reset.
     *
     * @throws IOException
     */
    @BeforeClass
    public static void setup02_noNontrivialStatics() throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        for (String s : System.getProperty("java.class.path")
                .split(System.getProperty("path.separator"))) {
            if (s.endsWith(".jar")) continue;
            Path p = Path.of(s);
            Files.walkFileTree(p, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.toString().endsWith(".idea")) return FileVisitResult.SKIP_SUBTREE;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!file.toString().toLowerCase().endsWith(".class")) return FileVisitResult.CONTINUE;

                    String qualifiedName = p.relativize(file)
                            .toString()
                            .replace(File.separatorChar, '.');
                    qualifiedName = qualifiedName.substring(0, qualifiedName.length() - 6);
                    try {
                        classes.add(Class.forName(qualifiedName));
                    } catch (ClassNotFoundException ignored) {
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        List<String> violations = new ArrayList<>();
        List<Class<?>> allowedClasses = List.of(
                byte.class,
                short.class,
                int.class,
                long.class,
                float.class,
                double.class,
                boolean.class,
                char.class,
                String.class,
                Path.class,
                File.class,
                SimpleDateFormat.class,
                // Utils
                FilenameFilter.class,
                // For testing stdout; not actually for use by students.
                ByteArrayOutputStream.class,
                PrintStream.class
        );
        for (Class<?> clazz : classes) {
            List<Field> staticFields = Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> Modifier.isStatic(f.getModifiers()))
                    .toList();
            for (Field f : staticFields) {
                if (!Modifier.isFinal(f.getModifiers())) {
                    violations.add("Non-final static field `" + f.getName() + "` found in " + clazz);
                }
                if (!allowedClasses.contains(f.getType())) {
                    violations.add("Static field `" + f.getName() + "` in " + clazz.getCanonicalName() +
                            " is of disallowed type " + f.getType().getSimpleName());
                }
            }
        }

        if (violations.size() > 0) {
            violations.forEach(OG_OUT::println);
            fail("Nontrivial static fields found, see class-level test output for GitletTests.\n" +
                    "These indicate that you might be trying to keep global state.");
        }
    }

    @BeforeClass
    public static void setup03_redirectStdout() {
        System.setOut(new PrintStream(OUT));
    }

    @BeforeClass
    @SuppressWarnings("removal")
    public static void setup04_trapSystemExit() {
        // https://openjdk.java.net/jeps/411
        // https://bugs.openjdk.java.net/browse/JDK-8199704
        // TODO: this is deprecated. See issues above.
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkExit(int status) {
                if (status == 0) {
                    throw new SecurityException("Allowable exit code, interrupting: " + status);
                }
            }

            // Default allow all - this isn't security sensitive
            @Override
            public void checkPermission(Permission perm) {
            }

            @Override
            public void checkPermission(Permission perm, Object context) {
            }
        });
    }

    @AfterClass
    @SuppressWarnings("removal")
    public static void restoreSecurity() {
        // JUnit uses system.exit(0) internally somewhere, so hand control back
        // to the JVM before we leave the test class
        // TODO: this is deprecated. See the other call for relevant issue.
        System.setSecurityManager(null);
    }

    public void recursivelyCleanWD() throws IOException {
        // DANGEROUS: We're wiping the directory.
        // Must ensure that we're in the right directory, even though we did in setup01_verifyWD.
        verifyWD();

        // Recursively wipe the directory
        Files.walkFileTree(Path.of(System.getProperty("user.dir")), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                // Don't delete the directory itself, we're about to work in it!
                if (dir.toString().equals(System.getProperty("user.dir"))) {
                    return FileVisitResult.CONTINUE;
                }
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed
                    throw e;
                }
            }
        });
    }

    @Before
    public void startWithEmptyWD() throws IOException, InterruptedException {
        recursivelyCleanWD();
        TimeUnit.MILLISECONDS.sleep(DELAY_MS);
    }

    @After
    public void endWithEmptyWD() throws IOException {
        recursivelyCleanWD();
    }

    /**
     * Returns captured output and flush the output stream
     */
    public static String getOutput() {
        String ret = OUT.toString();
        OUT.reset();
        return ret;
    }

    /**
     * Copies a source testing file into the current testing directory.
     *
     * @param src -- Path to source testing file
     * @param dst -- filename to write to; may exist
     */
    public static void writeFile(Path src, String dst) {
        try {
            Files.copy(src, Path.of(dst), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a file from the current testing directory.
     *
     * @param path -- filename to delete; must exist
     */
    public static void deleteFile(String path) {
        try {
            Files.delete(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asserts that a file exists in the current testing directory.
     *
     * @param path
     */
    public static void assertFileExists(String path) {
        if (!Files.exists(Path.of(path))) {
            fail("Expected file " + path + " to exist; does not.");
        }
    }

    /**
     * Asserts that a file does not exist in the current testing directory.
     *
     * @param path
     */
    public static void assertFileDoesNotExist(String path) {
        if (Files.exists(Path.of(path))) {
            fail("Expected file " + path + " to not exist; does.");
        }
    }

    /**
     * Asserts that a file both exists in current testing directory and has
     * identical content to a source testing file.
     *
     * @param src        -- source testing file containin expected content
     * @param pathActual -- filename in current testing directory to check
     */
    public static void assertFileEquals(Path src, String pathActual) {
        assertFileExists(pathActual);
        try {
            String expected = Files.readString(src).replace("\r\n", "\n");
            String actual = Files.readString(Path.of(pathActual)).replace("\r\n", "\n");
            assertEquals("File contents of src file " + src + " and actual file " + pathActual + " are not equal",
                    expected, actual);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copied from Python testing script (`correctProgramOutput`). Intended to adjust for whitespace issues.
     * Removes trailing spaces on lines, and replaces multi-spaces with single spaces.
     *
     * @param s -- string to normalize
     * @return normalized output
     */
    public static String normalizeStdOut(String s) {
        return s.replace("\r\n", "\n")
                .replaceAll("[ \\t]+\n", "\n")
                .replaceAll("(?m)^[ \\t]+", " ");
    }

    /**
     * Asserts that printed content to System.out is correct.
     *
     * @param expected -- expected printed content
     */
    public static void checkOutput(String expected) {
        expected = normalizeStdOut(expected).stripTrailing();
        String actual = normalizeStdOut(getOutput()).stripTrailing();
        assertEquals("ERROR (incorrect output)", expected, actual);
    }

    /**
     * Builds a command-line command from a provided arugments list
     *
     * @param args
     * @return command-line command, i.e. `java MyMain arg1 "arg with space"`
     */
    public static String createCommand(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            if (arg.contains(" ")) {
                sb.append('"').append(arg).append('"');
            } else {
                sb.append(arg);
            }
        }
        return sb.toString();
    }

    /**
     * Runs the given Gitlet command.
     *
     * @param args
     */
    public static void runGitletCommand(String[] args) {
        try {
            OG_OUT.println(COMMAND_BASE + createCommand(args));
            gitlet.Main.main(args);
        } catch (SecurityException ignored) {
        } catch (Exception e) {
            // Wrap IOException and other checked for poor implementations;
            // can't directly catch it because it's checked and the compiler complains
            // that it's not thrown
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the given gitlet command and checks the exact output.
     *
     * @param args
     * @param expectedOutput
     */
    public static void gitletCommand(String[] args, String expectedOutput) {
        runGitletCommand(args);
        checkOutput(expectedOutput);
    }

    /**
     * Constructs a regex matcher against the output, for tests to extract groups.
     *
     * @param pattern
     * @return
     */
    public static Matcher checkOutputRegex(String pattern) {
        String actual = getOutput();
        pattern = normalizeStdOut(pattern).stripTrailing();
        String ogP = pattern;
        pattern += "\\Z";
        actual = normalizeStdOut(actual);
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(actual);
        if (!m.matches()) {
            m = p.matcher(actual.stripTrailing());
            if (!m.matches()) {
                // Manually raise a comparison error to get a rich diff for typo catching
                throw new ComparisonFailure("Pattern does not match the output",
                        ogP, actual.stripTrailing());
            }
        }
        return m;
    }

    /**
     * Runs the given gitlet command and checks that the output matches a provided regex
     *
     * @param args
     * @param pattern
     * @return Matcher from the pattern, for group extraction
     */
    public static Matcher gitletCommandP(String[] args, String pattern) {
        runGitletCommand(args);
        return checkOutputRegex(pattern);
    }

    public static void i_prelude1() {
        gitletCommand(new String[]{"init"}, "");
    }

    public static void i_setup1() {
        i_prelude1();
        writeFile(WUG, "f.txt");
        writeFile(NOTWUG, "g.txt");
        gitletCommand(new String[]{"add", "g.txt"}, "");
        gitletCommand(new String[]{"add", "f.txt"}, "");
    }

    public static void i_setup2() {
        i_setup1();
        gitletCommand(new String[]{"commit", "Two files"}, "");
    }

    public static void i_blankStatus() {
        gitletCommand(new String[]{"status"}, """
                === Branches ===
                *main

                === Staged Files ===

                === Removed Files ===

                === Modifications Not Staged For Commit ===

                === Untracked Files ===
                                
                """);
    }

    public static void i_blankStatus2() {
        gitletCommand(new String[]{"status"}, """
                === Branches ===
                *main
                other

                === Staged Files ===

                === Removed Files ===

                === Modifications Not Staged For Commit ===

                === Untracked Files ===
                                
                """);
    }

    @Test
    public void test01_init() {
        gitletCommand(new String[]{"init"}, "");
    }

    @Test
    public void test02_basicCheckout() {
        gitletCommand(new String[]{"init"}, "");
        writeFile(WUG, "wug.txt");
        gitletCommand(new String[]{"add", "wug.txt"}, "");
        gitletCommand(new String[]{"commit", "added wug"}, "");
        writeFile(NOTWUG, "wug.txt");
        gitletCommand(new String[]{"checkout", "--", "wug.txt"}, "");
        assertFileEquals(WUG, "wug.txt");
    }

    @Test
    public void test03_basicLog() {
        gitletCommand(new String[]{"init"}, "");
        writeFile(WUG, "wug.txt");
        gitletCommand(new String[]{"add", "wug.txt"}, "");
        gitletCommand(new String[]{"commit", "added wug"}, "");
        gitletCommandP(new String[]{"log"}, """
                ===
                ${HEADER}
                ${DATE}
                added wug
                                
                ===
                ${HEADER}
                ${DATE}
                initial commit
                                
                """
                .replace("${HEADER}", "commit [a-f0-9]+")
                .replace("${DATE}", DATE));
    }

    @Test
    public void test04_prevCheckout() {
        gitletCommand(new String[]{"init"}, "");
        writeFile(WUG, "wug.txt");
        gitletCommand(new String[]{"add", "wug.txt"}, "");
        gitletCommand(new String[]{"commit", "version 1 of wug.txt"}, "");
        writeFile(NOTWUG, "wug.txt");
        gitletCommand(new String[]{"add", "wug.txt"}, "");
        gitletCommand(new String[]{"commit", "version 2 of wug.txt"}, "");
        assertFileEquals(NOTWUG, "wug.txt");
        Matcher logMatch = gitletCommandP(new String[]{"log"}, """
                ===
                ${HEADER}
                ${DATE}
                version 2 of wug.txt
                                
                ===
                ${HEADER}
                ${DATE}
                version 1 of wug.txt
                                
                ===
                ${HEADER}
                ${DATE}
                initial commit
                                
                """
                .replace("${HEADER}", "commit ([a-f0-9]+)")
                .replace("${DATE}", DATE));
        String uid2 = logMatch.group(1);
        String uid1 = logMatch.group(2);
        gitletCommand(new String[]{"checkout", uid1, "--", "wug.txt"}, "");
        assertFileEquals(WUG, "wug.txt");
        gitletCommand(new String[]{"checkout", uid2, "--", "wug.txt"}, "");
        assertFileEquals(NOTWUG, "wug.txt");
    }

    @Test
    public void test10_initErr() {
        i_prelude1();
        gitletCommand(new String[]{"init"}, "A Gitlet version-control system already exists in the current directory.");
    }

    @Test
    public void test11_basicStatus() {
        i_prelude1();
        i_blankStatus();
    }

    @Test
    public void test12_addStatus() {
        i_setup1();
        gitletCommand(new String[]{"status"}, """
                === Branches ===
                *main

                === Staged Files ===
                f.txt
                g.txt

                === Removed Files ===

                === Modifications Not Staged For Commit ===

                === Untracked Files ===

                """);
    }

    @Test
    public void test13_removeStatus() {
        i_setup2();
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        assertFileDoesNotExist("f.txt");
        gitletCommand(new String[]{"status"}, """
                === Branches ===
                *main

                === Staged Files ===

                === Removed Files ===
                f.txt

                === Modifications Not Staged For Commit ===

                === Untracked Files ===

                """);
    }

    @Test
    public void test14_addRemoveStatus() {
        i_setup1();
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        gitletCommandP(new String[]{"status"}, """
                === Branches ===
                \\*main
                                
                === Staged Files ===
                g.txt
                                
                === Removed Files ===
                                
                === Modifications Not Staged For Commit ===
                                
                === Untracked Files ===
                ${ARBLINES}
                                
                """.replace("${ARBLINES}", ARBLINES));
        assertFileEquals(WUG, "f.txt");
    }

    @Test
    public void test15_removeAddStatus() {
        i_setup2();
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        assertFileDoesNotExist("f.txt");
        writeFile(WUG, "f.txt");
        gitletCommand(new String[]{"add", "f.txt"}, "");
        i_blankStatus();
    }

    @Test
    public void test16_emptyCommitErr() {
        i_prelude1();
        gitletCommand(new String[]{"commit", "Nothing here"}, "No changes added to the commit.");
    }

    @Test
    public void test17_emptyCommitMessageErr() {
        i_prelude1();
        writeFile(WUG, "f.txt");
        gitletCommand(new String[]{"add", "f.txt"}, "");
        gitletCommand(new String[]{"commit", ""}, "Please enter a commit message.");
    }

    @Test
    public void test18_nopAdd() {
        i_setup2();
        gitletCommand(new String[]{"add", "f.txt"}, "");
        i_blankStatus();
    }

    @Test
    public void test19_addMissingErr() {
        i_prelude1();
        gitletCommand(new String[]{"add", "f.txt"}, "File does not exist.");
        i_blankStatus();
    }

    @Test
    public void test20_statusAfterCommit() {
        i_setup2();
        i_blankStatus();
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "Removed f.txt"}, "");
        i_blankStatus();
    }

    @Test
    public void test21_nopRemoveErr() {
        i_prelude1();
        writeFile(WUG, "f.txt");
        gitletCommand(new String[]{"rm", "f.txt"}, "No reason to remove the file.");
    }

    @Test
    public void test22_removeDeletedFile() {
        i_setup2();
        deleteFile("f.txt");
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        gitletCommand(new String[]{"status"}, """
                === Branches ===
                *main
                                
                === Staged Files ===
                                
                === Removed Files ===
                f.txt
                                
                === Modifications Not Staged For Commit ===
                                
                === Untracked Files ===
                                
                """);
    }

    @Test
    public void test23_globalLog() {
        i_setup2();
        String noTzDate = "Date: \\w\\w\\w \\w\\w\\w \\d+ \\d\\d:\\d\\d:\\d\\d \\d\\d\\d\\d";
        String commitLog = "(===[ ]*\\ncommit [a-f0-9]+[ ]*\\n(?:Merge:\\s+[0-9a-f]{7}\\s+[0-9a-f]{7}[ ]*\\n)?${DATE1}) [-+](\\d\\d\\d\\d[ ]*\\n(?:.|\\n)*?(?=\\Z|\\n===))".replace("${DATE1}", noTzDate);
        writeFile(WUG, "h.txt");
        gitletCommand(new String[]{"add", "h.txt"}, "");
        gitletCommand(new String[]{"commit", "Add h"}, "");
        Matcher m = gitletCommandP(new String[]{"log"}, """
                ${COMMIT_LOG}
                ${COMMIT_LOG}
                ${COMMIT_LOG}
                """.replace("${COMMIT_LOG}", commitLog));
        String L1 = m.group(1) + " [-+]" + m.group(2);
        String L2 = m.group(3) + " [-+]" + m.group(4);
        String L3 = m.group(5) + " [-+]" + m.group(6);
        gitletCommandP(new String[]{"global-log"}, ARBLINES + L1 + ARBLINES);
        gitletCommandP(new String[]{"global-log"}, ARBLINES + L2 + ARBLINES);
        gitletCommandP(new String[]{"global-log"}, ARBLINES + L3 + ARBLINES);
    }

    @Test
    public void test24_globalLogPrev() {
        i_setup2();
        String noTzDate = "Date: \\w\\w\\w \\w\\w\\w \\d+ \\d\\d:\\d\\d:\\d\\d \\d\\d\\d\\d";
        String commitLog = "(===[ ]*\\ncommit [a-f0-9]+[ ]*\\n(?:Merge:\\s+[0-9a-f]{7}\\s+[0-9a-f]{7}[ ]*\\n)?${DATE1}) [-+](\\d\\d\\d\\d[ ]*\\n(?:.|\\n)*?(?=\\Z|\\n===))".replace("${DATE1}", noTzDate);
        writeFile(WUG, "h.txt");
        gitletCommand(new String[]{"add", "h.txt"}, "");
        gitletCommand(new String[]{"commit", "Add h"}, "");
        Matcher m = gitletCommandP(new String[]{"log"}, """
                ${COMMIT_LOG}
                ${COMMIT_LOG}
                ${COMMIT_LOG}
                """.replace("${COMMIT_LOG}", commitLog));
        String L1 = m.group(1) + " [-+]" + m.group(2);
        m = gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Add h
                                
                ===
                ${COMMIT_HEAD}${ARBLINES}
                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD)
                .replace("${ARBLINES}", ARBLINES));
        String id = m.group(2);
        gitletCommand(new String[]{"reset", id}, "");
        gitletCommandP(new String[]{"global-log"}, ARBLINES + L1 + "?" + ARBLINES);
    }

    @Test
    public void test25_successfulFind() {
        i_setup2();
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "Remove one file"}, "");
        writeFile(NOTWUG, "f.txt");
        gitletCommand(new String[]{"add", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "Two files"}, "");
        Matcher m = gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Two files

                ===
                ${COMMIT_HEAD}
                Remove one file
                   
                ===
                ${COMMIT_HEAD}
                Two files

                ===
                ${COMMIT_HEAD}
                initial commit

                """.replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String uid1 = m.group(4);
        String uid2 = m.group(3);
        String uid3 = m.group(2);
        String uid4 = m.group(1);
        gitletCommandP(
                new String[]{"find", "Two files"},
                "(${UID4}\n${UID2}|${UID2}\n${UID4})"
                        .replace("${UID2}", uid2)
                        .replace("${UID4}", uid4)
        );
        gitletCommand(new String[]{"find", "initial commit"}, uid1);
        gitletCommand(new String[]{"find", "Remove one file"}, uid3);
    }

    @Test
    public void test26_successfulFindOrphan() {
        i_setup2();
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "Remove one file"}, "");
        Matcher m = gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Remove one file
                   
                ===
                ${COMMIT_HEAD}
                Two files

                ===
                ${COMMIT_HEAD}
                initial commit

                """.replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String uid2 = m.group(2);
        String uid3 = m.group(1);
        gitletCommand(new String[]{"reset", uid2}, "");
        gitletCommand(new String[]{"find", "Remove one file"}, uid3);
    }

    @Test
    public void test27_unsuccessfulFindErr() {
        i_setup2();
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "Remove one file"}, "");
        gitletCommand(new String[]{"find", "Add another file"}, "Found no commit with that message.");
    }

    @Test
    public void test28_checkoutDetail() {
        i_prelude1();
        writeFile(WUG, "wug.txt");
        gitletCommand(new String[]{"add", "wug.txt"}, "");
        gitletCommand(new String[]{"commit", "version 1 of wug.txt"}, "");
        writeFile(NOTWUG, "wug.txt");
        gitletCommand(new String[]{"add", "wug.txt"}, "");
        gitletCommand(new String[]{"commit", "version 2 of wug.txt"}, "");
        assertFileEquals(NOTWUG, "wug.txt");
        String header = "commit ([a-f0-9]+)";
        Matcher m = gitletCommandP(new String[]{"log"}, """
                ===
                ${HEADER}
                ${DATE}
                version 2 of wug.txt

                ===
                ${HEADER}
                ${DATE}
                version 1 of wug.txt

                ===
                ${HEADER}
                ${DATE}
                initial commit

                """.replace("${HEADER}", header).replace("${DATE}", DATE));
        String uid1 = m.group(2);
        gitletCommand(new String[]{"checkout", uid1, "--", "wug.txt"}, "");
        gitletCommandP(new String[]{"status"}, """
                === Branches ===
                \\*main
                                
                === Staged Files ===
                                
                === Removed Files ===
                                
                === Modifications Not Staged For Commit ===
                (${ARBLINE}\\n\\r?)?
                === Untracked Files ===
                                
                """.replace("${ARBLINE}", ARBLINE));
    }

    @Test
    public void test29_badCheckoutsErr() {
        i_prelude1();
        writeFile(WUG, "wug.txt");
        gitletCommand(new String[]{"add", "wug.txt"}, "");
        gitletCommand(new String[]{"commit", "version 1 of wug.txt"}, "");
        writeFile(NOTWUG, "wug.txt");
        gitletCommand(new String[]{"add", "wug.txt"}, "");
        gitletCommand(new String[]{"commit", "version 2 of wug.txt"}, "");
        Matcher m = gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                version 2 of wug.txt
                                
                ===
                ${COMMIT_HEAD}
                version 1 of wug.txt
                                
                ===
                ${COMMIT_HEAD}
                initial commit
                                
                """.replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String uid2 = m.group(1);
        gitletCommand(new String[]{"checkout", uid2, "--", "warg.txt"}, "File does not exist in that commit.");
        gitletCommand(new String[]{"checkout", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "--", "wug.txt"},
                "No commit with that id exists.");
        gitletCommand(new String[]{"checkout", uid2, "++", "wug.txt"}, "Incorrect operands.");
        gitletCommand(new String[]{"checkout", "foobar"}, "No such branch exists.");
        gitletCommand(new String[]{"checkout", "main"}, "No need to checkout the current branch.");
    }

    @Test
    public void test30_duplicateBranchErr() {
        i_prelude1();
        gitletCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG, "f.txt");
        gitletCommand(new String[]{"add", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "File f.txt"}, "");
        gitletCommand(new String[]{"checkout", "other"}, "");
        writeFile(NOTWUG, "g.txt");
        gitletCommand(new String[]{"add", "g.txt"}, "");
        gitletCommand(new String[]{"commit", "File g.txt"}, "");
        gitletCommand(new String[]{"checkout", "main"}, "");
        gitletCommand(new String[]{"rm-branch", "other"}, "");
        gitletCommand(new String[]{"checkout", "other"}, "No such branch exists.");
        assertFileDoesNotExist("g.txt");
        assertFileEquals(WUG, "f.txt");
    }

    @Test
    public void test31_duplicateBranchErr() {
        i_prelude1();
        gitletCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG, "f.txt");
        writeFile(WUG, "g.txt");
        gitletCommand(new String[]{"add", "g.txt"}, "");
        gitletCommand(new String[]{"add", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "Main two files"}, "");
        gitletCommand(new String[]{"branch", "other"}, "A branch with that name already exists.");
    }

    @Test
    public void test31_rmBranchErr() {
        i_prelude1();
        gitletCommand(new String[]{"branch", "other"}, "");
        gitletCommand(new String[]{"checkout", "other"}, "");
        writeFile(WUG, "f.txt");
        gitletCommand(new String[]{"add", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "File f.txt"}, "");
        gitletCommand(new String[]{"rm-branch", "other"}, "Cannot remove the current branch.");
        assertFileExists("f.txt");
        gitletCommand(new String[]{"rm-branch", "foo"}, "A branch with that name does not exist.");
    }

    @Test
    public void test32_fileOverwrite() {
        i_prelude1();
        gitletCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG, "f.txt");
        writeFile(NOTWUG, "g.txt");
        gitletCommand(new String[]{"add", "g.txt"}, "");
        gitletCommand(new String[]{"add", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "Main two files"}, "");
        assertFileExists("f.txt");
        assertFileExists("g.txt");
        gitletCommand(new String[]{"checkout", "other"}, "");
        writeFile(NOTWUG, "f.txt");
        gitletCommand(new String[]{"checkout", "main"}, "There is an untracked file in the way; delete it, or add and commit it first.");
    }

    @Test
    public void test33_mergeNoConflicts() {
        i_setup2();
        gitletCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG2, "h.txt");
        gitletCommand(new String[]{"add", "h.txt"}, "");
        gitletCommand(new String[]{"rm", "g.txt"}, "");
        gitletCommand(new String[]{"commit", "Add h.txt and remove g.txt"}, "");
        gitletCommand(new String[]{"checkout", "other"}, "");
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        writeFile(WUG3, "k.txt");
        gitletCommand(new String[]{"add", "k.txt"}, "");
        gitletCommand(new String[]{"commit", "Add k.txt and remove f.txt"}, "");
        gitletCommand(new String[]{"checkout", "main"}, "");
        gitletCommand(new String[]{"merge", "other"}, "");
        assertFileDoesNotExist("f.txt");
        assertFileDoesNotExist("g.txt");
        assertFileEquals(WUG2, "h.txt");
        assertFileEquals(WUG3, "k.txt");
        gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Merged other into main\\.

                ${ARBLINES}
                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD)
                .replace("${ARBLINES}", ARBLINES));
        i_blankStatus2();
    }

    @Test
    public void test34_mergeConflicts() {
        i_setup2();
        gitletCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG2, "h.txt");
        gitletCommand(new String[]{"add", "h.txt"}, "");
        gitletCommand(new String[]{"rm", "g.txt"}, "");
        writeFile(WUG2, "f.txt");
        gitletCommand(new String[]{"add", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "Add h.txt, remove g.txt, and change f.txt"}, "");
        gitletCommand(new String[]{"checkout", "other"}, "");
        writeFile(NOTWUG, "f.txt");
        gitletCommand(new String[]{"add", "f.txt"}, "");
        writeFile(WUG3, "k.txt");
        gitletCommand(new String[]{"add", "k.txt"}, "");
        gitletCommand(new String[]{"commit", "Add k.txt and modify f.txt"}, "");
        gitletCommand(new String[]{"checkout", "main"}, "");
        Matcher m = gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                ${ARBLINES}
                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD)
                .replace("${ARBLINES}", ARBLINES));
        String mainHead = m.group(1);
        gitletCommand(new String[]{"merge", "other"}, "Encountered a merge conflict.");
        assertFileDoesNotExist("g.txt");
        assertFileEquals(WUG2, "h.txt");
        assertFileEquals(WUG3, "k.txt");
        assertFileEquals(CONFLICT1, "f.txt");
        gitletCommandP(new String[]{"log"}, """
                ${COMMIT_LOG}
                ===
                commit ${main_HEAD}
                ${ARBLINES}
                """
                .replace("${COMMIT_LOG}", COMMIT_LOG)
                .replace("${main_HEAD}", mainHead)
                .replace("${ARBLINES}", ARBLINES));
        gitletCommandP(new String[]{"status"}, """
                === Branches ===
                \\*main
                other

                === Staged Files ===

                === Removed Files ===

                === Modifications Not Staged For Commit ===

                === Untracked Files ===

                """);
    }

    @Test
    public void test35_mergeRmConflicts() {
        i_setup2();
        gitletCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG2, "h.txt");
        gitletCommand(new String[]{"add", "h.txt"}, "");
        gitletCommand(new String[]{"rm", "g.txt"}, "");
        writeFile(WUG2, "f.txt");
        gitletCommand(new String[]{"add", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "Add h.txt, remove g.txt, and change f.txt"}, "");
        gitletCommand(new String[]{"checkout", "other"}, "");
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        writeFile(WUG3, "k.txt");
        gitletCommand(new String[]{"add", "k.txt"}, "");
        gitletCommand(new String[]{"commit", "Add k.txt and remove f.txt"}, "");
        gitletCommand(new String[]{"checkout", "main"}, "");
        Matcher m = gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                ${ARBLINES}
                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD)
                .replace("${ARBLINES}", ARBLINES));
        String mainHead = m.group(1);
        gitletCommand(new String[]{"merge", "other"}, "Encountered a merge conflict.");
        assertFileDoesNotExist("g.txt");
        assertFileEquals(WUG2, "h.txt");
        assertFileEquals(WUG3, "k.txt");
        assertFileEquals(CONFLICT2, "f.txt");
        gitletCommandP(new String[]{"log"}, """
                ${COMMIT_LOG}
                ===
                commit ${main_HEAD}
                ${ARBLINES}
                """
                .replace("${COMMIT_LOG}", COMMIT_LOG)
                .replace("${main_HEAD}", mainHead)
                .replace("${ARBLINES}", ARBLINES));
        gitletCommandP(new String[]{"status"}, """
                === Branches ===
                \\*main
                other

                === Staged Files ===

                === Removed Files ===

                === Modifications Not Staged For Commit ===

                === Untracked Files ===

                """);
    }

    @Test
    public void test36_mergeErr() {
        i_setup2();
        gitletCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG2, "h.txt");
        gitletCommand(new String[]{"add", "h.txt"}, "");
        gitletCommand(new String[]{"rm", "g.txt"}, "");
        gitletCommand(new String[]{"commit", "Add h.txt and remove g.txt"}, "");
        gitletCommand(new String[]{"checkout", "other"}, "");
        gitletCommand(new String[]{"merge", "other"}, "Cannot merge a branch with itself.");
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        writeFile(WUG3, "k.txt");
        gitletCommand(new String[]{"add", "k.txt"}, "");
        gitletCommand(new String[]{"commit", "Add k.txt and remove f.txt"}, "");
        gitletCommand(new String[]{"checkout", "main"}, "");
        gitletCommand(new String[]{"merge", "foobar"}, "A branch with that name does not exist.");
        writeFile(WUG, "k.txt");
        gitletCommand(new String[]{"merge", "other"},
                "There is an untracked file in the way; delete it, or add and commit it first.");
        deleteFile("k.txt");
        i_blankStatus2();
        writeFile(WUG, "k.txt");
        gitletCommand(new String[]{"add", "k.txt"}, "");
        gitletCommand(new String[]{"merge", "other"}, "You have uncommitted changes.");
        gitletCommand(new String[]{"rm", "k.txt"}, "");
        deleteFile("k.txt");
        i_blankStatus2();
    }

    @Test
    public void test36_mergeParent2() {
        i_prelude1();
        gitletCommand(new String[]{"branch", "B1"}, "");
        gitletCommand(new String[]{"branch", "B2"}, "");
        gitletCommand(new String[]{"checkout", "B1"}, "");
        writeFile(WUG, "h.txt");
        gitletCommand(new String[]{"add", "h.txt"}, "");
        gitletCommand(new String[]{"commit", "Add h.txt"}, "");
        gitletCommand(new String[]{"checkout", "B2"}, "");
        writeFile(WUG, "f.txt");
        gitletCommand(new String[]{"add", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "Add f.txt"}, "");
        gitletCommand(new String[]{"branch", "C1"}, "");
        writeFile(NOTWUG, "g.txt");
        gitletCommand(new String[]{"add", "g.txt"}, "");
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "g.txt added, f.txt removed"}, "");

        assertFileEquals(NOTWUG, "g.txt");
        assertFileDoesNotExist("f.txt");
        assertFileDoesNotExist("h.txt");

        gitletCommand(new String[]{"checkout", "B1"}, "");
        assertFileEquals(WUG, "h.txt");
        assertFileDoesNotExist("f.txt");
        assertFileDoesNotExist("g.txt");

        gitletCommand(new String[]{"merge", "C1"}, "");
        assertFileEquals(WUG, "f.txt");
        assertFileEquals(WUG, "h.txt");
        assertFileDoesNotExist("g.txt");

        gitletCommand(new String[]{"merge", "B2"}, "");
        assertFileDoesNotExist("f.txt");
        assertFileEquals(NOTWUG, "g.txt");
        assertFileEquals(WUG, "h.txt");
    }

    @Test
    public void test37_reset1() {
        i_setup2();
        gitletCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG2, "h.txt");
        gitletCommand(new String[]{"add", "h.txt"}, "");
        gitletCommand(new String[]{"rm", "g.txt"}, "");
        gitletCommand(new String[]{"commit", "Add h.txt and remove g.txt"}, "");
        gitletCommand(new String[]{"checkout", "other"}, "");
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        writeFile(WUG3, "k.txt");
        gitletCommand(new String[]{"add", "k.txt"}, "");
        gitletCommand(new String[]{"commit", "Add k.txt and remove f.txt"}, "");
        Matcher m = gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Add k.txt and remove f.txt
                                
                ===
                ${COMMIT_HEAD}
                Two files
                                
                ===
                ${COMMIT_HEAD}
                initial commit
                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String two = m.group(2);
        gitletCommand(new String[]{"checkout", "main"}, "");
        m = gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Add h.txt and remove g.txt
                                
                ===
                ${COMMIT_HEAD}
                Two files
                                
                ===
                ${COMMIT_HEAD}
                initial commit
                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String main1 = m.group(1);
        writeFile(WUG, "m.txt");
        gitletCommand(new String[]{"add", "m.txt"}, "");
        gitletCommand(new String[]{"reset", two}, "");
        gitletCommandP(new String[]{"status"}, """
                === Branches ===
                \\*main
                other
                                
                === Staged Files ===
                                
                === Removed Files ===
                                
                === Modifications Not Staged For Commit ===
                                
                === Untracked Files ===
                (m\\.txt\\n)?\\s*
                """);
        gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Two files

                ===
                ${COMMIT_HEAD}
                initial commit
                """.replace("${COMMIT_HEAD}", COMMIT_HEAD));
        gitletCommand(new String[]{"checkout", "other"}, "");
        gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Add k.txt and remove f.txt

                ===
                ${COMMIT_HEAD}
                Two files

                ===
                ${COMMIT_HEAD}
                initial commit
                """.replace("${COMMIT_HEAD}", COMMIT_HEAD));
        gitletCommand(new String[]{"checkout", "main"}, "");
        gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Two files
                                
                ===
                ${COMMIT_HEAD}
                initial commit
                """.replace("${COMMIT_HEAD}", COMMIT_HEAD));
        gitletCommand(new String[]{"reset", main1}, "");
        gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Add h.txt and remove g.txt
                                
                ===
                ${COMMIT_HEAD}
                Two files
                                
                ===
                ${COMMIT_HEAD}
                initial commit

                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD));
    }

    @Test
    public void test38_badResetsErr() {
        i_setup2();
        gitletCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG2, "h.txt");
        gitletCommand(new String[]{"add", "h.txt"}, "");
        gitletCommand(new String[]{"rm", "g.txt"}, "");
        gitletCommand(new String[]{"commit", "Add h.txt and remove g.txt"}, "");
        Matcher m = gitletCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Add h.txt and remove g.txt
                                
                ===
                ${COMMIT_HEAD}
                Two files
                                
                ===
                ${COMMIT_HEAD}
                initial commit

                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String main1 = m.group(1);
        gitletCommand(new String[]{"checkout", "other"}, "");
        gitletCommand(new String[]{"reset", "025052f2b193d417df998517a4c539918801b430"}, "No commit with that id exists.");
        writeFile(WUG3, "h.txt");
        gitletCommand(new String[]{"reset", main1},
                "There is an untracked file in the way; delete it, or add and commit it first.");
    }

    @Test
    public void test39_shortUid() {
        gitletCommand(new String[]{"init"}, "");
        writeFile(WUG, "wug.txt");
        gitletCommand(new String[]{"add", "wug.txt"}, "");
        gitletCommand(new String[]{"commit", "version 1 of wug.txt"}, "");
        writeFile(NOTWUG, "wug.txt");
        gitletCommand(new String[]{"add", "wug.txt"}, "");
        gitletCommand(new String[]{"commit", "version 2 of wug.txt"}, "");
        assertFileEquals(NOTWUG, "wug.txt");
        String header = "commit ([a-f0-9]{8})[a-f0-9]+";
        Matcher m = gitletCommandP(new String[]{"log"}, """
                ===
                ${HEADER}
                ${DATE}
                version 2 of wug.txt
                                
                ===
                ${HEADER}
                ${DATE}
                version 1 of wug.txt
                                
                ===
                ${HEADER}
                ${DATE}
                initial commit

                """
                .replace("${HEADER}", header)
                .replace("${DATE}", DATE));
        String uid2 = m.group(1);
        String uid1 = m.group(2);
        gitletCommand(new String[]{"checkout", uid1, "--", "wug.txt"}, "");
        assertFileEquals(WUG, "wug.txt");
        gitletCommand(new String[]{"checkout", uid2, "--", "wug.txt"}, "");
        assertFileEquals(NOTWUG, "wug.txt");
    }

    @Test
    public void test40_specialMergeCases() {
        i_setup2();
        gitletCommand(new String[]{"branch", "b1"}, "");
        writeFile(WUG2, "h.txt");
        gitletCommand(new String[]{"add", "h.txt"}, "");
        gitletCommand(new String[]{"commit", "Add h.txt"}, "");
        gitletCommand(new String[]{"branch", "b2"}, "");
        gitletCommand(new String[]{"rm", "f.txt"}, "");
        gitletCommand(new String[]{"commit", "Remove f.txt"}, "");
        gitletCommand(new String[]{"merge", "b1"}, "Given branch is an ancestor of the current branch.");
        gitletCommand(new String[]{"checkout", "b2"}, "");
        assertFileEquals(WUG, "f.txt");
        gitletCommand(new String[]{"merge", "main"}, "Current branch fast-forwarded.");
        assertFileDoesNotExist("f.txt");
    }

    @Test
    public void test41_noCommandErr() {
        i_prelude1();
        gitletCommand(new String[]{"glorp", "foo"}, "No command with that name exists.");
    }

    @Test
    public void test42_otherErr() {
        gitletCommand(new String[]{}, "Please enter a command.");
        gitletCommand(new String[]{"status"}, "Not in an initialized Gitlet directory.");
    }

    @Test
    public void test44_baiMerge() {
        gitletCommand(new String[]{"init"}, "");
        writeFile(A, "A.txt");
        writeFile(B, "B.txt");
        writeFile(C, "C.txt");
        writeFile(D, "D.txt");
        writeFile(E, "E.txt");
        gitletCommand(new String[]{"add", "A.txt"}, "");
        gitletCommand(new String[]{"add", "B.txt"}, "");
        gitletCommand(new String[]{"add", "C.txt"}, "");
        gitletCommand(new String[]{"add", "D.txt"}, "");
        gitletCommand(new String[]{"add", "E.txt"}, "");
        gitletCommand(new String[]{"commit", "msg1"}, "");
        assertFileEquals(A, "A.txt");
        assertFileEquals(B, "B.txt");
        assertFileEquals(C, "C.txt");
        assertFileEquals(D, "D.txt");
        assertFileEquals(E, "E.txt");
        gitletCommand(new String[]{"branch", "branch1"}, "");
        gitletCommand(new String[]{"rm", "C.txt"}, "");
        gitletCommand(new String[]{"rm", "D.txt"}, "");
        writeFile(NOTA, "A.txt");
        writeFile(NOTF, "F.txt");
        gitletCommand(new String[]{"add", "A.txt"}, "");
        gitletCommand(new String[]{"add", "F.txt"}, "");
        gitletCommand(new String[]{"commit", "msg2"}, "");
        assertFileEquals(NOTA, "A.txt");
        assertFileEquals(B, "B.txt");
        assertFileDoesNotExist("C.txt");
        assertFileDoesNotExist("D.txt");
        assertFileEquals(E, "E.txt");
        assertFileEquals(NOTF, "F.txt");
        gitletCommand(new String[]{"checkout", "branch1"}, "");
        assertFileEquals(A, "A.txt");
        assertFileEquals(B, "B.txt");
        assertFileEquals(C, "C.txt");
        assertFileEquals(D, "D.txt");
        assertFileEquals(E, "E.txt");
        assertFileDoesNotExist("f.txt");
        gitletCommand(new String[]{"rm", "C.txt"}, "");
        gitletCommand(new String[]{"rm", "E.txt"}, "");
        writeFile(NOTB, "B.txt");
        writeFile(G, "G.txt");
        gitletCommand(new String[]{"add", "B.txt"}, "");
        gitletCommand(new String[]{"add", "G.txt"}, "");
        gitletCommand(new String[]{"commit", "msg3"}, "");
        assertFileEquals(A, "A.txt");
        assertFileEquals(NOTB, "B.txt");
        assertFileDoesNotExist("C.txt");
        assertFileEquals(D, "D.txt");
        assertFileDoesNotExist("E.txt");
        assertFileDoesNotExist("F.txt");
        assertFileEquals(G, "G.txt");
        gitletCommand(new String[]{"merge", "main"}, "");
        assertFileEquals(NOTA, "A.txt");
        assertFileEquals(NOTB, "B.txt");
        assertFileDoesNotExist("C.txt");
        assertFileDoesNotExist("D.txt");
        assertFileDoesNotExist("E.txt");
        assertFileEquals(NOTF, "F.txt");
        assertFileEquals(G, "G.txt");
    }
}
