package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
        Parent root = loader.load();

        // The manager is created once, here, and handed from screen to screen.
        // It used to be created inside MainController, which meant every trip
        // back to the home screen built a fresh one and re-seeded it, only for
        // the previous screen to hand the real one back.
        MainController controller = loader.getController();
        controller.setManager(sampleManager());

        primaryStage.setTitle("Play! Northeastern");
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }

    /**
     * There is no persistence yet, so each run starts from the same sample data.
     * The dates are relative to today: they were hard-coded to April 2026, which
     * has since passed, leaving every sample activity in the past.
     */
    private static InvitationManager sampleManager() {
        InvitationManager manager = new InvitationManager();
        LocalDate today = LocalDate.now();

        manager.addInvitation(new Invitation("Alice", today.plusDays(1).toString(), 9, 0, 11, 0, 4, "Basketball",
                Location.MARINO_RECREATION_CENTER, Gender.ALL_GENDER));
        manager.addInvitation(new Invitation("Bob", today.plusDays(2).toString(), 14, 30, 16, 0, 2, "Squash",
                Location.SQUASH_BUSTERS, Gender.MALE));
        manager.addInvitation(new Invitation("Carol", today.plusDays(3).toString(), 7, 0, 8, 30, 6, "Yoga",
                Location.CABOT_CENTER, Gender.FEMALE));
        manager.addInvitation(new Invitation("David", today.plusDays(4).toString(), 18, 0, 20, 0, 8, "Soccer",
                Location.CARTER_PLAYGROUND, Gender.ALL_GENDER));
        manager.addInvitation(new Invitation("Eve", today.plusDays(5).toString(), 10, 0, 12, 0, 3, "Tennis",
                Location.ROXBURY_YMCA, Gender.FEMALE));

        return manager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
