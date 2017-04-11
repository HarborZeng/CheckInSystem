package cn.tellyouwhat.checkinsystem.bean;

/**
 * Created by Harbor-Laptop on 2017/4/11.
 */

public class CheckInRecord {
	private String checkInID;
	private String checkInTime;
	private String checkOutTime;
	private String oriCheckInTime;
	private String oriCheckOutTime;
	private boolean hasCheckOut;

	public String getCheckInID() {
		return checkInID;
	}

	public void setCheckInID(String checkInID) {
		this.checkInID = checkInID;
	}

	public String getCheckInTime() {
		return checkInTime;
	}

	public void setCheckInTime(String checkInTime) {
		this.checkInTime = checkInTime;
	}

	public String getCheckOutTime() {
		return checkOutTime;
	}

	public void setCheckOutTime(String checkOutTime) {
		this.checkOutTime = checkOutTime;
	}

	public String getOriCheckInTime() {
		return oriCheckInTime;
	}

	public void setOriCheckInTime(String oriCheckInTime) {
		this.oriCheckInTime = oriCheckInTime;
	}

	public String getOriCheckOutTime() {
		return oriCheckOutTime;
	}

	public void setOriCheckOutTime(String oriCheckOutTime) {
		this.oriCheckOutTime = oriCheckOutTime;
	}

	public boolean isHasCheckOut() {
		return hasCheckOut;
	}

	public void setHasCheckOut(boolean hasCheckOut) {
		this.hasCheckOut = hasCheckOut;
	}
}
