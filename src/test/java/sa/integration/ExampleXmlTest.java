package sa.integration;

import beast.base.core.Log;
import beast.base.inference.Logger;
import beast.base.inference.MCMC;
import beast.base.parser.XMLParser;
import beast.base.util.Randomizer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parses and briefly runs every XML in {@code examples/}.
 *
 * <p>This mirrors {@code test.beast.integration.ExampleXmlParsingTest} in
 * beast-base. Without it the BEAST 3 spec migration shipped three examples that
 * could not be parsed at all: the migration linter only looks for deprecated
 * class references, and the rest of this suite never touches {@code examples/},
 * so nothing exercised them. Every defect involved was a type mismatch that only
 * appears once the parser resolves and wires the objects up.</p>
 *
 * <p>Running, not just parsing, is the point: the sampled-ancestor operators only
 * reach the interesting tree states after some churn, and the interval-scaling
 * bug that corrupted these trees first appeared around sample 145.</p>
 */
public class ExampleXmlTest {

    /** Long enough for the SA operators to move the tree off its starting state. */
    private static final long CHAIN_LENGTH = 10000L;

    /** Marker in the parser's error for an example needing an uninstalled package. */
    private static final String MISSING_PACKAGE = "package that is not installed";

    @Test
    public void examplesParseAndRun() {
        // surefire runs with workingDirectory=target/test-classes, so the project
        // root comes from the sa.basedir property the pom sets; user.dir is the
        // fallback for running this test outside Maven.
        String basedir = System.getProperty("sa.basedir", System.getProperty("user.dir"));
        File exampleDir = new File(basedir, "examples");
        assertTrue(exampleDir.isDirectory(),
                "examples directory not found at " + exampleDir);

        String[] exampleFiles = exampleDir.list((dir, name) -> name.endsWith(".xml"));
        assertTrue(exampleFiles != null && exampleFiles.length > 0,
                "no example XMLs found in " + exampleDir);

        Logger.FILE_MODE = Logger.LogFileMode.overwrite;

        List<String> failed = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        int seed = 127;

        for (String fileName : exampleFiles) {
            Randomizer.setSeed(seed);
            seed += 10;   // distinct seeds so log files do not collide
            Log.info.println("Processing " + fileName);
            try {
                beast.base.inference.Runnable runnable =
                        new XMLParser().parseFile(new File(exampleDir, fileName));
                if (runnable instanceof MCMC mcmc) {
                    mcmc.setInputValue("preBurnin", 0);
                    mcmc.setInputValue("chainLength", CHAIN_LENGTH);
                    mcmc.run();
                }
            } catch (Exception e) {
                String message = String.valueOf(e.getMessage());
                if (message.contains(MISSING_PACKAGE)) {
                    // e.g. brachiopods.xml needs MM (morphmodels), which has no
                    // BEAST 3 release yet. Report it rather than failing, so this
                    // example starts being covered as soon as the package exists.
                    Log.warning.println("SKIPPED " + fileName + ": " + message);
                    skipped.add(fileName);
                } else {
                    Log.err.println("FAILED " + fileName + ": " + message);
                    e.printStackTrace();
                    failed.add(fileName);
                }
            }
        }

        if (!skipped.isEmpty()) {
            Log.warning.println("Examples skipped (missing packages): " + skipped);
        }
        assertTrue(failed.isEmpty(), "examples failed to parse or run: " + failed);
    }
}
