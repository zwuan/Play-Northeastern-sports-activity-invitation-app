package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestMain extends Application {

    @Override
    public void start(Stage stage) {
        try {
        
            InvitationManager manager = new InvitationManager();
          
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreateInvitation.fxml"));
            Parent root = loader.load();

            CreateInvitationController controller = loader.getController();

            controller.setManager(manager);

            stage.setScene(new Scene(root));
            stage.setTitle("Test Create Invitation");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
