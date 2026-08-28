package application;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Starts the JavaFX toolkit once per JVM and runs test bodies on the FX thread.
 *
 * The toolkit needs a display, so tests that use this are skipped rather than
 * failed on a headless machine -- a CI run should not go red for something the
 * machine cannot do.
 */
final class FxToolkit {

    private static Boolean available;

    private FxToolkit() {
    }

    /** Skips the calling test if the toolkit cannot be started here. */
    static synchronized void require() {
        if (available == null) {
            available = startToolkit();
        }
        assumeTrue(available, "JavaFX toolkit unavailable (headless machine?)");
    }

    private static boolean startToolkit() {
        try {
            CountDownLatch ready = new CountDownLatch(1);
            Platform.startup(ready::countDown);
            if (!ready.await(30, TimeUnit.SECONDS)) {
                return false;
            }
            // Nothing here shows a window; without this the toolkit would shut
            // itself down the first time a scene is created and discarded.
            Platform.runLater(() -> Platform.setImplicitExit(false));
            return true;
        } catch (IllegalStateException alreadyStarted) {
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    interface FxBody {
        void run() throws Exception;
    }

    /** Runs the body on the FX thread, rethrowing whatever it threw. */
    static void onFxThread(FxBody body) throws Exception {
        require();

        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                body.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(60, TimeUnit.SECONDS), "the FX thread body never finished");

        Throwable thrown = failure.get();
        if (thrown instanceof Exception e) {
            throw e;
        }
        if (thrown != null) {
            throw new AssertionError(thrown);
        }
    }
}
