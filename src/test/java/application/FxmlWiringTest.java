package application;

import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the built artifact is actually runnable: the FXML and the images
 * are where the code looks for them, every FXML loads against its controller,
 * and every onAction handler named in an FXML exists on that controller.
 *
 * The resource checks are the regression test for the old build instructions,
 * which compiled the .java files but never copied the .fxml files, so
 * getResource("Main.fxml") returned null and the app died on startup. Maven
 * copies src/main/resources now, so this should stay green on its own -- but it
 * is exactly the kind of thing that silently breaks when the layout is changed.
 */
class FxmlWiringTest {

    @ParameterizedTest
    @ValueSource(strings = {"Main.fxml", "ActivityList.fxml", "CreateInvitation.fxml"})
    void fxmlSitsNextToTheClasses(String fxml) {
        assertNotNull(Main.class.getResource(fxml), fxml + " is missing from the classpath");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/Play!Northeastern.png", "/map2.jpg"})
    void imagesAreOnTheClasspathAndReadable(String path) throws Exception {
        URL url = Main.class.getResource(path);
        assertNotNull(url, path + " is missing from the classpath");
        assertTrue(url.openStream().readAllBytes().length > 0, path + " is empty");
    }

    // Main.fxml reaches the images with @../, so resolve them the way FXMLLoader
    // does rather than assuming the classpath lookup above is equivalent.
    @ParameterizedTest
    @ValueSource(strings = {"../Play!Northeastern.png", "../map2.jpg"})
    @SuppressWarnings("deprecation")
    void mainFxmlCanResolveItsImages(String relative) throws Exception {
        URL mainFxml = Main.class.getResource("Main.fxml");
        assertNotNull(mainFxml);

        URL resolved = new URL(mainFxml, relative);
        assertTrue(resolved.openStream().readAllBytes().length > 0,
                "Main.fxml cannot reach " + relative);
    }

    // Every onAction="#..." across the three FXML files. FXMLLoader resolves
    // these lazily, so a renamed handler would otherwise only blow up when a
    // user clicks the button.
    @ParameterizedTest(name = "{0}.{1}")
    @CsvSource({
        "application.MainController,             handleCreateActivity",
        "application.MainController,             handleJoinActivity",
        "application.ActivityListController,     handleFilter",
        "application.ActivityListController,     handleClearFilter",
        "application.ActivityListController,     handleJoin",
        "application.ActivityListController,     handleEditActivity",
        "application.ActivityListController,     handleBack",
        "application.CreateInvitationController, handleSave",
    })
    void actionHandlerNamedInFxmlExists(String controllerName, String handler) throws Exception {
        Class<?> controller = Class.forName(controllerName);

        boolean found = false;
        for (Method method : controller.getDeclaredMethods()) {
            if (method.getName().equals(handler)) {
                found = true;
                break;
            }
        }

        assertTrue(found, controllerName + " has no " + handler + "() for its onAction");
    }

    @Test
    void everyFxmlLoadsAndBindsItsController() throws Exception {
        FxToolkit.onFxThread(() -> {
            for (String fxml : new String[] {"Main.fxml", "ActivityList.fxml", "CreateInvitation.fxml"}) {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
                loader.load();
                assertNotNull(loader.getController(), fxml + " bound no controller");
            }
        });
    }
}
