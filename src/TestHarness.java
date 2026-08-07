// Minimal test harness, no external deps (no Maven Central access in this env)
public class TestHarness {
    static int passed = 0;
    static int failed = 0;

    static void assertEquals(Object expected, Object actual, String testName) {
        if (java.util.Objects.equals(expected, actual)) {
            passed++;
            System.out.println("PASS: " + testName);
        } else {
            failed++;
            System.out.println("FAIL: " + testName + " -- expected <" + expected + "> but got <" + actual + ">");
        }
    }

    static void assertTrue(boolean condition, String testName) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + testName);
        } else {
            failed++;
            System.out.println("FAIL: " + testName);
        }
    }

    static void assertThrows(Runnable r, String testName) {
        try {
            r.run();
            failed++;
            System.out.println("FAIL: " + testName + " -- expected exception, none thrown");
        } catch (Exception e) {
            passed++;
            System.out.println("PASS: " + testName);
        }
    }

    static void summary() {
        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
