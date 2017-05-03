package cn.tellyouwhat.checkinsystem.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by Harbor-Laptop on 2017/3/23.
 * javaBean
 */
@Table(name = "locations")
public class LocationItem {
	@Column(name = "id", isId = true, autoGen = true)
	private int id;

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	@Column(name = "user_id")
	private String userID;

	@Column(name = "time")
	private String time;

	@Column(name = "radius")
	private float radius;

	@Column(name = "location_type")
	private int locationType;

	@Column(name = "from_service")
	private boolean gotFromService;

	@Column(name = "building_ID")
	private int buildingID;

	@Column(name = "longitude")
	private String longitude;

	@Column(name = "latitude")
	private String latitude;

	@Column(name = "address")
	private String address;

	@Column(name = "building_desc")
	private String buildingDesc;

	public String getBuildingDesc() {
		return buildingDesc;
	}

	public void setBuildingDesc(String buildingDesc) {
		this.buildingDesc = buildingDesc;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLocationDescription() {
		return locationDescription;
	}

	public void setLocationDescription(String locationDescription) {
		this.locationDescription = locationDescription;
	}

	@Column(name = "location_desc")

	private String locationDescription;

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public LocationItem() {
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public int getLocationType() {
		return locationType;
	}

	public void setLocationType(int locationType) {
		this.locationType = locationType;
	}

	public boolean isGotFromService() {
		return gotFromService;
	}

	public void setGotFromService(boolean gotFromService) {
		this.gotFromService = gotFromService;
	}

	public int getBuildingID() {
		return buildingID;
	}

	public void setBuildingID(int buildingID) {
		this.buildingID = buildingID;
	}
}
