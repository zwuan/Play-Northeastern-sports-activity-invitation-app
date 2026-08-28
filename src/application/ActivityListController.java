package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

public class ActivityListController implements Initializable {

    // TableView and its columns, injected from ActivityList.fxml
    @FXML private TableView<Activity> activityTable;
    @FXML private TableColumn<Activity, String> colName;
    @FXML private TableColumn<Activity, String> colOrganizer;
    @FXML private TableColumn<Activity, String> colTimeSlot;
    @FXML private TableColumn<Activity, String> colDate;
    @FXML private TableColumn<Activity, String> colLocation;
    @FXML private TableColumn<Activity, String> colGender;
    @FXML private TableColumn<Activity, String> colStatus;
    @FXML private Label messageLabel;
    @FXML private TextField filterField;

    // invitation source
    private InvitationManager manager;

    // Activity list for table
    private ObservableList<Activity> activityList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        activityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Each column uses a cell value factory to pull the matching field from Activity
        colName.setCellValueFactory(
            data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getActivityName()));

        colOrganizer.setCellValueFactory(
            data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getOrganizer()));

        colDate.setCellValueFactory(
            data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDate()));

        colTimeSlot.setCellValueFactory(
            data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getTimeSlot()));

        colLocation.setCellValueFactory(
            data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getLocation()));

        colGender.setCellValueFactory(
            data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getGender()));

        colStatus.setCellValueFactory(
            data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus()));

        // Bind the observable list to the table so any changes auto-update the UI
        activityTable.setItems(activityList);
    }

    // MainController call manager the shared InvitationManager instance
    public void setManager(InvitationManager manager) {
        this.manager = manager;
        loadInvitations();
    }

    // InvitationManager to table, Loads all invitations with no keyword filter
    private void loadInvitations() {
        loadInvitations("");
    }

    // Loads invitations with keyword filter
    private void loadInvitations(String keyword) {
        activityList.clear();

        if (manager == null) {
            activityTable.setItems(activityList);
            return;
        }

        // Change normalizedKeyword to LowerCase
        String normalizedKeyword;
        if (keyword == null) {
            normalizedKeyword = "";
        } else {
            normalizedKeyword = keyword.trim().toLowerCase();
        }

        // Iterator method through all invitations and add matching ones to the table
        Iterator<Invitation> iterator = manager.getInvitationList().iterator();

        while (iterator.hasNext()) {
            Invitation inv = iterator.next();
            if (!matchesFilter(inv, normalizedKeyword)) {
                continue;
            }

            // Convert Invitation to Activity for display in the TableView
            activityList.add(Activity.of(inv));
        }

        activityTable.setItems(activityList);
    }

    // Searches across sport, organizer, date, time slot, and location fields.
    private boolean matchesFilter(Invitation invitation, String keyword) {

        if (keyword.isEmpty()) {
            return true;
        }
        return invitation.getSport().toLowerCase().contains(keyword)
            || invitation.getOrganizer().toLowerCase().contains(keyword)
            || invitation.getDate().toLowerCase().contains(keyword)
            || invitation.getTimeSlot().toLowerCase().contains(keyword)
            || invitation.getLocation().getDisplayName().toLowerCase().contains(keyword)
            || matchesGenderFilter(invitation.getGender().getDisplayName(), keyword);
    }

    // independent search for gender, cuz "female" contains "male"
    private boolean matchesGenderFilter(String gender, String keyword) {
        String normalizedGender = gender.trim().toLowerCase();

        if (isWholeGenderKeyword(keyword)) {
            return (" " + normalizedGender + " ").contains(" " + keyword + " ");
        }

        return normalizedGender.contains(keyword);
    }

    private boolean isWholeGenderKeyword(String keyword) {
        return "male".equals(keyword) || "female".equals(keyword) || "all gender".equals(keyword);
    }

    // Reloads the table with only activities matching the filter field text.
    @FXML
    private void handleFilter() {
        loadInvitations(filterField.getText());
        messageLabel.setText("Showing filtered activities.");
    }

    // Clear field data
    @FXML
    private void handleClearFilter() {
        filterField.clear();
        loadInvitations();
        messageLabel.setText("Filter cleared.");
    }

    // Handle the joined count of the selected activity.
    @FXML
    private void handleJoin() {
        Activity selected = activityTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messageLabel.setText("Please select one activity.");
            return;
        }

        // find invitation by Pin
        Invitation inv = manager.findByPin(selected.getPin());
        if (inv == null) {
            messageLabel.setText("Activity not found.");
            return;
        }

        // can not join an activity over capacity
        if (inv.isFull()) {
            messageLabel.setText("This activity is already full.");
            return;
        }

        // display join status joined/total count
        inv.incrementJoined();
        messageLabel.setText("Joined: " + selected.getActivityName() + "  (" + inv.getJoinedCount() + "/"
                + inv.getCount() + ")");

        refreshTable();
        selectByPin(inv.getPin());
    }

    // Edit the existed activity.
    @FXML
    private void handleEditActivity() {
        Activity selected = activityTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messageLabel.setText("Please select one activity first.");
            return;
        }

        Invitation target = manager.findByPin(selected.getPin());
        if (target == null) {
            messageLabel.setText("Activity not found.");
            return;
        }

        // Dialog message
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("PIN Verification");
        dialog.setHeaderText("Enter the activity PIN");
        dialog.setContentText("PIN:");

        // type in pin and update
        dialog.showAndWait().ifPresent(inputPin -> {
            // Case-insensitive, to match how findByPin looks PINs up.
            if (inputPin.trim().equalsIgnoreCase(target.getPin())) {
                try {
                    // call CreateInvitation.fxml and controller
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("CreateInvitation.fxml"));
                    Parent root = loader.load();

                    CreateInvitationController controller = loader.getController();

                    // use manager to sync edit+save
                    controller.setManager(manager);

                    // select activity to edit
                    controller.setEditActivity(selected);

                    Stage stage = new Stage();
                    stage.setTitle("Edit Activity");
                    stage.initOwner(activityTable.getScene().getWindow());
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.setScene(new Scene(root));
                    // An edit can change the PIN, so re-select using the new one.
                    stage.setOnHidden(e -> {
                        refreshTable();
                        selectByPin(target.getPin());
                    });
                    stage.show();

                } catch (Exception e) {
                    e.printStackTrace();
                    messageLabel.setText("Failed to open edit window.");
                }
            } else {
                // Show error
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Wrong PIN");
                alert.setContentText("Incorrect PIN.");
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setManager(manager);

            Stage stage = (Stage) activityTable.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 500));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Refresh Table update data field
    public void refreshTable() {
        String keyword;
        if (filterField == null) {
            keyword = "";
        } else {
            keyword = filterField.getText();
        }
        loadInvitations(keyword);
    }

    // refreshTable() rebuilds every row, which drops the selection, so it has to
    // be restored by PIN afterwards.
    private void selectByPin(String pin) {
        for (Activity activity : activityList) {
            if (activity.getPin().equalsIgnoreCase(pin)) {
                activityTable.getSelectionModel().select(activity);
                return;
            }
        }
    }
}
