package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;


public class MainController {

    private InvitationManager manager = new InvitationManager();// Shared data manager
    private ActivityListController activityListController;

    @FXML
    private Button createBtn;

    @FXML
    private Button joinBtn;

    @FXML
    private Button editBtn;

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
        // FAke data
        if (manager.isEmpty()) {
            manager.addInvitation(new Invitation("Alice", "2026-04-15", 9, 0, 11, 0, 4, "Basketball", "Marino Recreation Center", "All Gender"));
            manager.addInvitation(new Invitation("Bob", "2026-04-16", 14, 30, 16, 0, 2, "Squash", "SquashBusters", "Male"));
            manager.addInvitation(new Invitation("Carol", "2026-04-17", 7, 0, 8, 30, 6, "Yoga", "Cabot Center", "Female"));
            manager.addInvitation(new Invitation("David", "2026-04-18", 18, 0, 20, 0, 8, "Soccer", "Carter Playground", "All Gender"));
            manager.addInvitation(new Invitation("Eve", "2026-04-19", 10, 0, 12, 0, 3, "Tennis", "Roxbury YMCA", "Female"));
        }

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
            setupHover(carterPlayground, "Carter Playground\n★★★★\n06:00 ~ 00:00\nModerate 🟡");
        }

        if (marinoRecreationCenter != null) {
            setupHover(marinoRecreationCenter, "Marino Recreation Center\n★★★\n05:30 ~ 00:00\nCrowded 🔴");
        }

        if (cabotCenter != null) {
            setupHover(cabotCenter, "Cabot Center\n★★\n05:30 ~ 22:15\nModerate 🟡");
        }

        if (fenwayCourt != null) {
            setupHover(fenwayCourt, "Fenway Court\n★★★★\n00:00 ~ 00:00\nModerate 🟡");
        }

        if (titusSparrowPark != null) {
            setupHover(titusSparrowPark, "Titus Sparrow Park\n★★★★\n06:00 ~ 23:30\nLow 🟢");
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

    // hover helper
    private void setupHover(Polygon area, String message) {
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
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open Create Activity window.");
        }
    }
    
    //Opens the Join Activity form in a new window
    @FXML
    private void handleJoinActivity(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ActivityList.fxml"));
            Parent root = loader.load();

            activityListController = loader.getController();
            activityListController.setManager(manager);  

            Stage stage = (Stage) joinBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 500));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open Activity List.");
        }
    }
    //Opens the Edit Activity form in a new window
    @FXML
    private void handleEditActivity(ActionEvent event) {

        if (activityListController == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Activity List Not Opened");
            alert.setContentText("Please click Join Activity first, then select an activity to edit.");
            alert.showAndWait();
            return;
        }

        Activity selected = activityListController.getSelectedActivity();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("No Selection");
            alert.setContentText("Please select an activity first.");
            alert.showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("PIN Verification");
        dialog.setHeaderText("Enter the activity PIN");
        dialog.setContentText("PIN:");

        dialog.showAndWait().ifPresent(inputPin -> {
            if (inputPin.equals(selected.getPin())) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("CreateInvitation.fxml"));
                    Parent root = loader.load();

                    CreateInvitationController controller = loader.getController();
                    controller.setManager(manager);
                    controller.setEditActivity(selected);

                    Stage stage = new Stage();
                    stage.setTitle("Edit Activity");
                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Failed to open Edit Activity window.");
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Wrong PIN");
                alert.setContentText("Incorrect PIN.");
                alert.showAndWait();
            }
        });
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