import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CapersTest {
    private static final String COMMAND_BASE = "java capers.Main ";
    private static final int DELAY_MS = 150;
    private static final String TESTING_DIR = "testing";
    private static final String DOG_FORMAT = "Woof! My name is %s and I am a %s! I am %d years old! Woof!";
    private static final String DOG_BIRTHDAY = "Happy birthday! Woof! Woof!";

    private static final PrintStream OG_OUT = System.out;
    private static final ByteArrayOutputStream OUT = new ByteArrayOutputStream();

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
            violations.forEach(System.err::println);
            fail("Nontrivial static fields found, see above. " +
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
                if (status == 0 || status == -1) {
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
     * Return captured output and flush the output stream
     */
    public static String getOutput() {
        String ret = OUT.toString();
        OUT.reset();
        return ret;
    }

    /**
     * Copied from Python testing script (correctProgramOutput). Intended to adjust for whitespace issues.
     * Remove trailing spaces on lines, and replace multi-spaces with single spaces.
     *
     * @param s -- string to normalize
     * @return normalized output
     */
    public static String normalizeStdOut(String s) {
        return s.replace("\r\n", "\n")
                .replaceAll("[ \\t]+\n", "\n")
                .replaceAll("(?m)^[ \\t]+", " ");
    }

    public static void checkOutput(String expected) {
        expected = normalizeStdOut(expected).stripTrailing();
        String actual = normalizeStdOut(getOutput()).stripTrailing();
        assertEquals("ERROR (incorrect output)", expected, actual);
    }

    public static String createCommand(String[] args) {
        StringBuilder sb = new StringBuilder();
        for(String arg : args) {
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

    public static void capersCommand(String[] args, String expectedOutput) {
        try {
            OG_OUT.println(COMMAND_BASE + createCommand(args));
            capers.Main.main(args);
        } catch (SecurityException ignored) {
        } catch (Exception e) {
            // Wrap IOException and other checked for poor implementations;
            // can't directly catch it because it's checked and the compiler complains
            // that it's not thrown
            throw new RuntimeException(e);
        }
        checkOutput(expectedOutput);
    }

    @Test
    public void test01_StorySimple() {
        String[] cmd = {"story", "hello world"};
        String expected = "hello world";
        capersCommand(cmd, expected);
    }

    @Test
    public void test02_testStoryPersistence() {
        String[] cmd = {"story", "Hello"};
        String expected = "Hello";
        capersCommand(cmd, expected);

        cmd = new String[]{"story", "World"};
        expected += "\nWorld";
        capersCommand(cmd, expected);
    }

    @Test
    public void test03_testDog() {
        String name = "Fido";
        String breed = "dalmatian";
        int age = 3;
        String[] cmd = {"dog", name, breed, String.valueOf(3)};
        String expected = String.format(DOG_FORMAT, name, breed, age);
        capersCommand(cmd, expected);
    }

    @Test
    public void test04_testBirthday() {
        String name = "Scruffy";
        String breed = "poodle";
        int age = 1;
        String[] cmd = {"dog", name, breed, String.valueOf(age)};
        String expected = String.format(DOG_FORMAT, name, breed, age);
        capersCommand(cmd, expected);

        cmd = new String[]{"birthday", name};
        expected = String.format(DOG_FORMAT, name, breed, age + 1) + "\n" + DOG_BIRTHDAY + "\n";
        capersCommand(cmd, expected);
    }

    @Test
    public void test05_testSecondDog() {
        String name1 = "Sparky";
        String breed1 = "labrador";
        int age1 = 2;
        String[] cmd = {"dog", name1, breed1, String.valueOf(age1)};
        String expected = String.format(DOG_FORMAT, name1, breed1, age1);
        capersCommand(cmd, expected);

        String name2 = "Dash";
        String breed2 = "labradoodle";
        int age2 = 1;
        cmd = new String[]{"dog", name2, breed2, String.valueOf(age2)};
        expected = String.format(DOG_FORMAT, name2, breed2, age2);
        capersCommand(cmd, expected);
    }

    @Test
    public void test06_testBothDogsBirthdays() {
        String name1 = "Sparky";
        String breed1 = "labrador";
        int age1 = 2;
        String[] cmd = {"dog", name1, breed1, String.valueOf(age1)};
        String expected = String.format(DOG_FORMAT, name1, breed1, age1);
        capersCommand(cmd, expected);

        String name2 = "Dash";
        String breed2 = "labradoodle";
        int age2 = 1;
        cmd = new String[]{"dog", name2, breed2, String.valueOf(age2)};
        expected = String.format(DOG_FORMAT, name2, breed2, age2);
        capersCommand(cmd, expected);

        cmd = new String[]{"birthday", name2};
        expected = String.format(DOG_FORMAT, name2, breed2, age2 + 1)
                + "\n" + DOG_BIRTHDAY + "\n";
        capersCommand(cmd, expected);

        cmd = new String[]{"birthday", name1};
        expected = String.format(DOG_FORMAT, name1, breed1, age1 + 1)
                + "\n" + DOG_BIRTHDAY + "\n";
        capersCommand(cmd, expected);
    }
}
