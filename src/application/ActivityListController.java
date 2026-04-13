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
    	
    	// TableView and its columns, injected from ActivityList.fxml
        activityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
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
    
    
    //Loads invitations with keyword filter
    private void loadInvitations(String keyword) {
        activityList.clear();

        if (manager == null) {
            activityTable.setItems(activityList);
            return;
        }
        
        //Change normalizedKeyword to LowerCase
        String normalizedKeyword;
        if (keyword == null) {
            normalizedKeyword = "";
        } else {
            normalizedKeyword = keyword.trim().toLowerCase();
        }
        
        //Iterator method through all invitations and add matching ones to the table
        Iterator<Invitation> iterator = manager.getInvitationList().iterator();

        while (iterator.hasNext()) {
            Invitation inv = iterator.next();
            if (!matchesFilter(inv, normalizedKeyword)) {
                continue;
            }
            
            //Convert Invitation to Activity for display in the TableView
            activityList.add(new Activity(
                inv.getSport(),
                inv.getOrganizer(),
                inv.getDate(),
                inv.getLocation(),
                inv.getJoinedCount() + " / " + inv.getCount(),
                inv.getPin(),
                inv.getTimeSlot(),
                inv.getGender()
            ));
        }

        activityTable.setItems(activityList);
    }
    
    //Searches across sport, organizer, date, time slot, and location fields.
    private boolean matchesFilter(Invitation invitation, String keyword) {
       
    	if (keyword.isEmpty()) {
            return true;
        }
        return invitation.getSport().toLowerCase().contains(keyword)
            || invitation.getOrganizer().toLowerCase().contains(keyword)
            || invitation.getDate().toLowerCase().contains(keyword)
            || invitation.getTimeSlot().toLowerCase().contains(keyword)
            || invitation.getLocation().toLowerCase().contains(keyword)
            || matchesGenderFilter(invitation.getGender(), keyword);
    }
    
    //independent search for gender, cuz "female" contains "male"
    private boolean matchesGenderFilter(String gender, String keyword) {
        String normalizedGender;
        if (gender == null) {
            normalizedGender = "";
        } else {
            normalizedGender = gender.trim().toLowerCase();
        }

        if (isWholeGenderKeyword(keyword)) {
            return (" " + normalizedGender + " ").contains(" " + keyword + " ");
        }

        return normalizedGender.contains(keyword);
    }

    private boolean isWholeGenderKeyword(String keyword) {
        return "male".equals(keyword) || "female".equals(keyword) || "all gender".equals(keyword);
    }
    
    //Reloads the table with only activities matching the filter field text.
    @FXML
    private void handleFilter() {
        loadInvitations(filterField.getText());
        messageLabel.setText("Showing filtered activities.");
    }
    
    //Clear field data
    @FXML
    private void handleClearFilter() {
        filterField.clear();
        loadInvitations();
        messageLabel.setText("Filter cleared.");
    }
    
    //Handle the joined count of the selected activity.
    @FXML
    private void handleJoin() {
        Activity selected = activityTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messageLabel.setText("Please select one activity.");
            return;
        }
        
        //find invitation by Pin
        Invitation inv = manager.findByPin(selected.getPin());
        if (inv == null) {
            messageLabel.setText("Activity not found.");
            return;
        }
        
        //can not join an activity over capacity
        if (inv.getJoinedCount() >= inv.getCount()) {
            messageLabel.setText("This activity is already full.");
            return;
        }
        
        //display join status joined/total count
        inv.incrementJoined();
        messageLabel.setText("Joined: " + selected.getActivityName() + "  (" + inv.getJoinedCount() + "/" + inv.getCount() + ")");

        refreshTable();
    }
    
    //Edit the existed activity.
    @FXML
    private void handleEditActivity() {
        Activity selected = activityTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messageLabel.setText("Please select one activity first.");
            return;
        }
        
        //Dialog message
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("PIN Verification");
        dialog.setHeaderText("Enter the activity PIN");
        dialog.setContentText("PIN:");
        
        //type in pin and update
        dialog.showAndWait().ifPresent(inputPin -> {
            if (inputPin.equals(selected.getPin())) {
                try {
                	//call CreateInvitation.fxm and controller
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("CreateInvitation.fxml"));
                    Parent root = loader.load();
                 
                    CreateInvitationController controller = loader.getController();

                    // use manager to sync edit+save
                    controller.setManager(manager);

                    // select activity to edit
                    controller.setEditActivity(selected);

                    Stage stage = new Stage();
                    stage.setTitle("Edit Activity");
                    stage.setScene(new Scene(root));
                    stage.setOnHidden(e -> refreshTable());
                    stage.show();

                } catch (Exception e) {
                    e.printStackTrace();
                    messageLabel.setText("Failed to open edit window.");
                }
            } else {
            	//Show error
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

    //  MainController get selected activity
    public Activity getSelectedActivity() {
        return activityTable.getSelectionModel().getSelectedItem();
    }
    
    // activity to table
    public void addActivity(Activity activity) {
        if (activity != null) {
            activityList.add(activity);
        }
    }
    
    // Returns the full observable activity list bound to the TableView
    public ObservableList<Activity> getActivityList() {
        return activityList;
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
}
