# Play! Northeastern Sports Activity Invitation App

A JavaFX desktop application for creating, browsing, joining, and editing sports activity invitations around Northeastern-related locations.

This project was built as a course project using Java and JavaFX, with a focus on GUI interaction, object-oriented design, and core data structure concepts.

## Features

- Create a new sports activity invitation
- Generate a unique, random PIN for each activity
- Browse all activities in a table view
- Filter activities by entering keywords
- Join an activity and update player counts
- Edit an existing activity after PIN verification
- Show the latest created activity on the home screen
- Display court/location information through an interactive map UI

## Tech Stack

- Java
- JavaFX
- FXML

## Project Structure

```text
src/
  map2.jpg
  Play!Northeastern.png
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

## Local Image Assets

The logo and map images used by the JavaFX UI are local project files included in this repository.

- Logo image: `src/Play!Northeastern.png`
- Map image: `src/map2.jpg`

They are loaded from [`src/application/Main.fxml`](/Users/jimmyzuang/git/Play-Northeastern-sports-activity-invitation-app/src/application/Main.fxml) using relative paths:

```xml
<Image url="@../map2.jpg" />
<Image url="@../Play!Northeastern.png" />
```

This means the image files **MUST** stay available in the compiled runtime resource path as siblings of the `application/` folder.

### If the logo or map does not display

Common fixes:

1. Make sure `map2.jpg` and `Play!Northeastern.png` are present in `src/`.
2. Make sure those files are also copied into the runtime output folder.
3. Do not rename or move the image files unless you also update the paths inside `Main.fxml`.

Example manual fix 1:

```bash
cp src/map2.jpg bin/
cp src/Play!Northeastern.png bin/
```

If you change the folder structure later, update the image references in `Main.fxml` to match the new relative locations.

## How It Works

### Main Screen

The home screen shows an interactive map and the latest activity added to the system.

### Create Activity

Users can create an activity by entering:

- organizer name
- date
- start / end time
- player counts
- sport type
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

Users can open the activity list, select an activity, and join it if the activity is not yet full.

### Edit Activity

Users can edit an existing activity after entering the correct PIN.

## Course CSYE6200 Requirement Coverage

This project uses JavaFX and covers 6 required topics:

1. Class Definition
   Ex: `Invitation`, `InvitationManager`, `Activity`
2. Inheritance
   Ex: `Main extends Application`
3. Interfaces
   Example: `ActivityListController implements Initializable`
4. Generics / Collections / Iterators
   Ex: `TableView<Activity>`, `ObservableList<Activity>`, `Iterator<Invitation>`
5. Lists
   Ex: `ArrayList<Invitation>`
6. Stacks
   Ex: `Stack<String>` for latest activity tracking

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
