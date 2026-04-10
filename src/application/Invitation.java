package application;

import java.util.Random;

public class Invitation {
	
	private String name;
	private String date;
	private int startH;
	private int startM;
	private int endH;
	private int endM;
	private int count;
	private String sport;
	private String location;
	private String gender;
	private String pin;
	private int joinedCount = 0;

	public Invitation( String name, String date, int startH, int startM, int endH, int endM, int count, String sport, String location,
			String gender) {

		if (!isValidTime(startH, startM) || !isValidTime(endH, endM)) {
			throw new IllegalArgumentException("Invalid time input");
		}

		if (count <= 0) {
			throw new IllegalArgumentException("Invalid count input");
		}
		
		if (endH * 60 + endM <= startH * 60 + startM) {
		    throw new IllegalArgumentException("End time must be after start time");
		}
		
        this.name = name;
		this.date = date;
		this.startH = startH;
		this.startM = startM;
		this.endH = endH;
		this.endM = endM;
		this.count = count;
		this.sport = sport;
		this.location = location;
		this.gender = gender;
		this.pin = generatePIN();

	}
	
	private boolean isValidTime(int h, int m) {
		if (h >= 0 && h <= 23 && m >= 0 && m <= 59) {
			return true;
		}
		return false;
	}

	private String generatePIN() {
		String locCode = getLocationCode(location);
		String rand = String.format("%03d", new Random().nextInt(1000));
		String genderCode = getGenderCode(gender);
		String pin = locCode + rand + genderCode;
		return pin;
	}

	private String getLocationCode(String location) {
		switch (location) {
		case "Marino Recreation Center":
			return "MR";
		case "Cabot Center":
			return "CC";
		case "Carter Playground":
			return "CP";
		case "SquashBusters":
			return "SB";
		case "Roxbury YMCA":
			return "RY";
		default:
			return "UN";
		}
	}
	
	private String getGenderCode(String gender) {
        if (gender == null) {
        	return "A";
        }

        switch (gender.toLowerCase()) {
            case "male":
                return "M";
            case "female":
                return "F";
            case "all gender":
                return "A";
            default:
                return "O";
        }
    }
	
	public String getOrganizer() {
	    return name;
	}
	
	public String getDate() {
	    return date;
	}
	
	public int getStartH() {
		return startH;
	}

	public int getStartM() {
		return startM;
	}

	public int getEndH() {
		return endH;
	}

	public int getEndM() {
		return endM;
	}

	public int getCount() {
		return count;
	}

	public String getSport() {
		return sport;
	}

	public String getLocation() {
		return location;
	}

	public String getGender() {
		return gender;
	}

	public String getPin() {
		return pin;
	}
	
	public String getTimeSlot() {
        return String.format("%02d:%02d ~ %02d:%02d", startH, startM, endH, endM);
    }
	
//	public void setOrganizer(String organizer) {
//	    this.organizer = organizer;
//	}

	public void setDate(String date) {
	    this.date = date;
	}

	public void setStartH(int startH) {
	    this.startH = startH;
	}

	public void setStartM(int startM) {
	    this.startM = startM;
	}

	public void setEndH(int endH) {
	    this.endH = endH;
	}

	public void setEndM(int endM) {
	    this.endM = endM;
	}

	public void setCount(int count) {
	    this.count = count;
	}

	public void setSport(String sport) {
	    this.sport = sport;
	}

	public void setLocation(String location) {
	    this.location = location;
	}

	public void setGender(String gender) {
	    this.gender = gender;
	}
	
	public int getJoinedCount() {
	    return joinedCount;
	}

	public void incrementJoined() {
	    joinedCount++;
	}

	
	
}
