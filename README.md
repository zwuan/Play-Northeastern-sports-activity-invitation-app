# Play! Northeastern Sports Activity Invitation App

_A JavaFX desktop application for creating, browsing, joining, and editing sports activity invitations from locations around Northeastern's campus. This project was built using Java and JavaFX, with a focus on GUI interaction, object-oriented design, and core data structure concepts._

## Features

- Create a new sports activity invitation
- Generate a random PIN for each activity, unique across all activities
- Browse all activities in a table view
- Filter activities by entering keywords
- Join an activity and update player counts
- Edit an existing activity after PIN verification
- Show the latest created activity on the home screen
- Display court/location information through an interactive map UI

## Tech Stack

- Java 25
- JavaFX 25 (FXML)
- Maven (wrapper included — no local install needed)
- JUnit 5

## Project Structure

Standard Maven layout, so Maven handles compilation, resource copying and tests
without any hand-written build steps.

```text
pom.xml
src/
  main/
    java/application/          Main, controllers, Invitation, InvitationManager,
                               Activity, Location, Gender
    resources/
      map2.jpg                 loaded by Main.fxml as @../map2.jpg
      Play!Northeastern.png    loaded by Main.fxml as @../Play!Northeastern.png
      application/             Main.fxml, ActivityList.fxml,
                               CreateInvitation.fxml, application.css
  test/
    java/application/          InvitationTest, InvitationManagerTest,
                               LocationTest, FxmlWiringTest, ControllerTest
target/                        build output (gitignored)
```

The images sit at the resources root, not inside `application/`, because
`Main.fxml` reaches them with `@../` — Maven copies `src/main/resources` to the
classpath root, so `target/classes/map2.jpg` ends up one level above
`target/classes/application/Main.fxml`, which is exactly what that path needs.
`FxmlWiringTest` asserts this resolves, so moving the images will fail the build
rather than produce an app with no map.

## How It Works

### Main Screen

The home screen shows an interactive map and the latest activity added to the system.

### Create Activities

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

The PIN is derived from the activity's current location and gender rather than
stored, so editing either one re-issues the PIN — the update dialog shows the new
value. `InvitationManager` assigns the random middle and guarantees no two
activities share a PIN, which caps each location/gender pair at 1000 activities.

### Activity List filter

Users can filter out the search by entering **any** keywords (upper/lower case does **not** matter)

### Join Activities

Users can open the activity list, select an activity, and join it if the activity is not yet full.

### Edit Activities

Open the activity list, select an activity, and click **Edit**. PIN entry is
case-insensitive. Editing runs through the same validation as creating, so an
edit cannot store a time range, player count or location that the create form
would have rejected, and the player count cannot be dropped below the number of
players who have already joined.

## Course CSYE6200 Requirement Coverage

This project uses JavaFX and covers the required topics:

1. Class Definition: `Invitation`, `InvitationManager`, `Activity`
2. Inheritance/Polymorphism: `Main extends Application`
3. Interfaces: `ActivityListController implements Initializable`
4. Generics / Collections / Iterators: `TableView<Activity>`, `ObservableList<Activity>`, `Iterator<Invitation>`
5. Lists: `ArrayList<Invitation>`
6. Stacks: `Stack<Invitation>` for latest activity tracking
7. Enums: `Location`, `Gender`
8. Unit testing: JUnit 5, see [Tests](#tests)

## Running the Project

Requires JDK 25. Nothing else — `mvnw` fetches Maven itself, and Maven fetches
the JavaFX jars for your platform, so there is no SDK to install and no path to
configure.

```bash
./mvnw javafx:run
```

On Windows use `mvnw.cmd` in place of `./mvnw`.

The wrapper pins Maven 3.9.14 (see `.mvn/wrapper/maven-wrapper.properties`) so
the build does not drift with whatever version happens to be installed. It is
the `only-script` flavour, so there is no wrapper jar committed to the repo.

## Tests

```bash
./mvnw test
```

77 tests across five classes:

| Class | Covers | Needs a display |
|---|---|---|
| `InvitationTest` | validation shared by create and edit, PIN derivation, capacity, null and blank input | no |
| `InvitationManagerTest` | PIN uniqueness and exhaustion, lookup, latest-activity tracking | no |
| `LocationTest` | every court on the map is bookable, PIN codes are distinct, enum round-trips | no |
| `FxmlWiringTest` | FXML and images resolve off the classpath, every `onAction` handler exists, every FXML loads | partly |
| `ControllerTest` | the list and create controllers driven on the FX thread | yes |

Tests that need the JavaFX toolkit are **skipped, not failed**, on a headless
machine — see `FxToolkit`. `./mvnw test` still exercises the model and the enums
there.

`handleSave()` is not covered: it ends in `Alert.showAndWait()`, which blocks the
FX thread, so form validation cannot be driven from a test. Extracting that
validation into a function that returns a result instead of showing a dialog
would fix it.

### Running in Eclipse

The old `.classpath` and `.project` were deleted — `pom.xml` replaces them, and
they pointed at the pre-Maven layout. Import with **File → Import → Existing
Maven Projects** and m2e will regenerate them.

## Notes

- The app currently uses in-memory data only.
- Sample activities are preloaded on startup.
- No external database or backend is required.

## Future Improvements

- Save activity data to files or a database
- Add user accounts and authentication
- Support deleting activities
- Improve validation and error handling
