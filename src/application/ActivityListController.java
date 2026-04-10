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

    @FXML private TableView<Activity> activityTable;
    @FXML private TableColumn<Activity, String> colName;
    @FXML private TableColumn<Activity, String> colOrganizer;
    @FXML private TableColumn<Activity, String> colTimeSlot;
    @FXML private TableColumn<Activity, String> colDate;
    @FXML private TableColumn<Activity, String> colLocation;
    @FXML private TableColumn<Activity, String> colStatus;
    @FXML private Label messageLabel;
    @FXML private TextField filterField;

    // 使用真正的 invitation 資料來源
    private InvitationManager manager;

    // 給 table 用的 activity list
    private ObservableList<Activity> activityList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        colStatus.setCellValueFactory(
            data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus()));

        activityTable.setItems(activityList);
    }

    // MainController 呼叫這個，把共用 manager 傳進來
    public void setManager(InvitationManager manager) {
        this.manager = manager;
        loadInvitations();
    }

    // 從 InvitationManager 載入資料到 table
    private void loadInvitations() {
        loadInvitations("");
    }

    private void loadInvitations(String keyword) {
        activityList.clear();

        if (manager == null) {
            activityTable.setItems(activityList);
            return;
        }

        String normalizedKeyword;
        if (keyword == null) {
            normalizedKeyword = "";
        } else {
            normalizedKeyword = keyword.trim().toLowerCase();
        }
        Iterator<Invitation> iterator = manager.getInvitationList().iterator();

        while (iterator.hasNext()) {
            Invitation inv = iterator.next();
            if (!matchesFilter(inv, normalizedKeyword)) {
                continue;
            }

            activityList.add(new Activity(
                inv.getSport(),
                inv.getOrganizer(),
                inv.getDate(),
                inv.getLocation(),
                inv.getJoinedCount() + " / " + inv.getCount(),
                inv.getPin(),
                inv.getTimeSlot()
            ));
        }

        activityTable.setItems(activityList);
    }

    private boolean matchesFilter(Invitation invitation, String keyword) {
       
    	if (keyword.isEmpty()) {
            return true;
        }
        return invitation.getSport().toLowerCase().contains(keyword)
            || invitation.getOrganizer().toLowerCase().contains(keyword)
            || invitation.getDate().toLowerCase().contains(keyword)
            || invitation.getTimeSlot().toLowerCase().contains(keyword)
            || invitation.getLocation().toLowerCase().contains(keyword);
    }

    @FXML
    private void handleFilter() {
        loadInvitations(filterField.getText());
        messageLabel.setText("Showing filtered activities.");
    }

    @FXML
    private void handleClearFilter() {
        filterField.clear();
        loadInvitations();
        messageLabel.setText("Filter cleared.");
    }

    @FXML
    private void handleJoin() {
        Activity selected = activityTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messageLabel.setText("Please select one activity.");
            return;
        }

        Invitation inv = manager.findByPin(selected.getPin());
        if (inv == null) {
            messageLabel.setText("Activity not found.");
            return;
        }

        if (inv.getJoinedCount() >= inv.getCount()) {
            messageLabel.setText("This activity is already full.");
            return;
        }

        inv.incrementJoined();
        messageLabel.setText("Joined: " + selected.getActivityName()
            + "  (" + inv.getJoinedCount() + "/" + inv.getCount() + ")");

        refreshTable();  // 刷新 table 讓人數即時更新
    }
    
    @FXML
    private void handleEditActivity() {
        Activity selected = activityTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messageLabel.setText("Please select one activity first.");
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

                    // 把 manager 傳進去，避免 edit 或 save 時拿不到同一份資料
                    controller.setManager(manager);

                    // 把目前選到的 activity 傳進去做 edit
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
                messageLabel.setText("Wrong PIN.");
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

    // 給 MainController 取得目前選到的 activity
    public Activity getSelectedActivity() {
        return activityTable.getSelectionModel().getSelectedItem();
    }

    // 手動新增 activity 到 table
    public void addActivity(Activity activity) {
        if (activity != null) {
            activityList.add(activity);
        }
    }

    public ObservableList<Activity> getActivityList() {
        return activityList;
    }

    public void refreshTable() {
        loadInvitations(filterField == null ? "" : filterField.getText());
    }
}
