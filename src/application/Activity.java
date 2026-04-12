package application;

import javafx.beans.property.SimpleStringProperty;

public class Activity { 
	
	//Private data fields using SimpleStringProperty for JavaFX property binding
	private SimpleStringProperty activityName;
	private SimpleStringProperty organizer;
	private SimpleStringProperty date;
	private SimpleStringProperty location;
	private SimpleStringProperty timeSlot;
	private SimpleStringProperty gender;
	private SimpleStringProperty status;
	private SimpleStringProperty pin;
    
	//Creates an Activity Object with all Required Fields.
	public Activity(String activityName, String organizer, String date, String location, String status, String pin, String timeSlot, String gender) {
		this.activityName = new SimpleStringProperty(activityName);
		this.organizer = new SimpleStringProperty(organizer);
		this.date = new SimpleStringProperty(date);
		this.location = new SimpleStringProperty(location);
		this.status = new SimpleStringProperty(status);
		this.pin = new SimpleStringProperty(pin);
		this.timeSlot = new SimpleStringProperty(timeSlot);
		this.gender = new SimpleStringProperty(gender);
	}
	
	//getter and setter method (Gender,TimeSlot,Pin,ActivityName,Organizer,Location,Status)
	// .get() and .set() are used to read/write the value inside SimpleStringProperty
	public String getGender() {
		return gender.get();
	}

	public String getTimeSlot() {
		return timeSlot.get();
	}
	
	public String getPin() {
		return pin.get();
	}

	public String getActivityName() {
		return activityName.get();
	}

	public String getOrganizer() {
		return organizer.get();
	}

	public String getDate() {
		return date.get();
	}

	public String getLocation() {
		return location.get();
	}

	public String getStatus() {
		return status.get();
	}
	
	
	public void setGender(String gender) {
		this.gender.set(gender);
	}
	
	public void setTimeSlot(String timeSlot) {
		this.timeSlot.set(timeSlot);
	}
	
	public void setPin(String pin) {
		this.pin.set(pin);
	}
	
	public void setActivityName(String activityName) {
        this.activityName.set(activityName);
    }

    public void setOrganizer(String organizer) {
        this.organizer.set(organizer);
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public void setStatus(String status) {
        this.status.set(status);
    }
}
