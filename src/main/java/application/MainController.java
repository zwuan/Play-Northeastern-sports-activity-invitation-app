package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Polygon;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class MainController {

    // Handed in by Main at startup, and back again by the activity list screen.
    private InvitationManager manager;

    @FXML
    private Button joinBtn;

    // hover Label
    @FXML
    private Label infoLabel;

    // latest activities
    @FXML
    private Label latestActivityLabel;

    @FXML
    private Polygon carterPlayground;

    @FXML
    private Polygon marinoRecreationCenter;

    @FXML
    private Polygon cabotCenter;

    @FXML
    private Polygon fenwayCourt;

    @FXML
    private Polygon titusSparrowPark;

    @FXML
    public void initialize() {
        //hover layer
        if (carterPlayground != null) carterPlayground.toFront();
        if (marinoRecreationCenter != null) marinoRecreationCenter.toFront();
        if (cabotCenter != null) cabotCenter.toFront();
        if (fenwayCourt != null) fenwayCourt.toFront();
        if (titusSparrowPark != null) titusSparrowPark.toFront();
        if (infoLabel != null) infoLabel.toFront();

        if (infoLabel != null) {
            infoLabel.setText("Move your mouse for information.");
        }

        if (carterPlayground != null) {
            setupHover(carterPlayground, Location.CARTER_PLAYGROUND, "★★★★", "06:00 ~ 00:00", "Moderate 🟡");
        }

        if (marinoRecreationCenter != null) {
            setupHover(marinoRecreationCenter, Location.MARINO_RECREATION_CENTER, "★★★", "05:30 ~ 00:00", "Crowded 🔴");
        }

        if (cabotCenter != null) {
            setupHover(cabotCenter, Location.CABOT_CENTER, "★★", "05:30 ~ 22:15", "Moderate 🟡");
        }

        if (fenwayCourt != null) {
            setupHover(fenwayCourt, Location.FENWAY_COURT, "★★★★", "00:00 ~ 00:00", "Moderate 🟡");
        }

        if (titusSparrowPark != null) {
            setupHover(titusSparrowPark, Location.TITUS_SPARROW_PARK, "★★★★", "06:00 ~ 23:30", "Low 🟢");
        }

        //refresh latest activity
        refreshLatestActivityLabel();
    }

    // Update latest activity label
    private void refreshLatestActivityLabel() {
        if (latestActivityLabel != null && manager != null) {
            latestActivityLabel.setText(manager.getLatestActivityText());
        }
    }

    // hover helper. The name comes from Location so the map and the create form
    // cannot disagree about what a court is called.
    private void setupHover(Polygon area, Location location, String rating, String hours, String crowding) {
        String message = location.getDisplayName() + "\n" + rating + "\n" + hours + "\n" + crowding;

        area.setOnMouseEntered(e -> {
            area.setOpacity(0.35);
            if (infoLabel != null) {
                infoLabel.setText(message);
            }
        });

        area.setOnMouseExited(e -> {
            area.setOpacity(0.0);
            if (infoLabel != null) {
                infoLabel.setText("Move your mouse for information.");
            }
        });
    }

    //Opens the Create Invitation form in a new window
    @FXML
    private void handleCreateActivity(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreateInvitation.fxml"));
            Parent root = loader.load();

            CreateInvitationController controller = loader.getController();
            controller.setManager(manager);
            controller.setOnSaveSuccess(() -> refreshLatestActivityLabel());

            Stage stage = new Stage();
            stage.setTitle("Create Activity");
            // Modal, so the home screen cannot be navigated away from -- or a second
            // create window opened -- while this form is unsaved.
            stage.initOwner(((Button) event.getSource()).getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open Create Activity window.");
        }
    }

    // Replaces the home screen with the activity list, which is where joining
    // and editing happen.
    @FXML
    private void handleJoinActivity(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ActivityList.fxml"));
            Parent root = loader.load();

            ActivityListController activityListController = loader.getController();
            activityListController.setManager(manager);

            Stage stage = (Stage) joinBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 500));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open Activity List.");
        }
    }

    public void setManager(InvitationManager manager) {
        this.manager = manager;
        refreshLatestActivityLabel();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
