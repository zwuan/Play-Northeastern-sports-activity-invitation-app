# Play! Northeastern Sports Activity Invitation App

A JavaFX desktop application for creating, browsing, joining, and editing sports activity invitations around Northeastern-related locations.

This project was built as a course project using Java and JavaFX, with a focus on GUI interaction, object-oriented design, and core data structure concepts.

## Features

- Create a new sports activity invitation
- Generate a unique PIN for each activity
- Browse all activities in a table view
- Filter activities by keyword
- Join an activity and update player counts
- Edit an existing activity after PIN verification
- Show the latest created activity on the home screen
- Display court/location information through an interactive map UI

## Tech Stack

- Java
- JavaFX
- FXML
- Eclipse-style Java project structure

## Project Structure

```text
src/
  application/
    Main.java
    MainController.java
    ActivityListController.java
    CreateInvitationController.java
    Invitation.java
    InvitationManager.java
    Activity.java
    Main.fxml
    ActivityList.fxml
    CreateInvitation.fxml
```

## How It Works

### Main Screen

The home screen shows an interactive map and the latest activity added to the system.

### Create Activity

Users can create an activity by entering:

- organizer name
- date
- start and end time
- player count
- sport
- location
- gender restriction

After saving, the app generates a PIN for that activity.

PIN structure:
```
XX(Location Initials) + XXX(Random Generated Numbers) + X(Gender Initials)
```
Example PIN 1: Marino Recreation Center, Female
```
MR168F
```

### Join Activity

Users can open the activity list, select an activity, and join it if the activity is not full.

### Edit Activity

Users can edit an existing activity after entering the correct PIN.

## Course CSYE6200 Requirement Coverage

This project uses JavaFX and currently covers at least 5 required topics:

1. Class Definition
   Examples: `Invitation`, `InvitationManager`, `Activity`
2. Inheritance
   Example: `Main extends Application`
3. Interfaces
   Example: `ActivityListController implements Initializable`
4. Generics / Collections / Iterators
   Examples: `TableView<Activity>`, `ObservableList<Activity>`, `Iterator<Invitation>`
5. Lists
   Example: `ArrayList<Invitation>`
6. Stacks
   Example: `Stack<String>` for latest activity tracking

## Running the Project

### Option 1: Run from Eclipse

1. Import the project into Eclipse
2. Make sure JavaFX is configured in your build path
3. Run `Main.java`

### Option 2: Compile and Run from Terminal

If your JavaFX SDK is located at `/path/to/javafx-sdk/lib`, use:

```bash
javac -d bin --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml src/application/*.java
java -cp bin --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml application.Main
```


## Notes

- The app currently uses in-memory data only.
- Sample activities are preloaded on startup.
- No external database or backend is required.

## Future Improvements

- Save activity data to files or a database
- Add user accounts and authentication
- Support deleting activities
- Improve validation and error handling

