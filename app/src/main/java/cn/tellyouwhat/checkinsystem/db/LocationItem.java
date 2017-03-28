package cn.tellyouwhat.checkinsystem.db;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.Date;


/**
 * Created by Harbor-Laptop on 2017/3/23.
 * javaBean
 */
@Table(name = "locations")
public class LocationItem {
	@Column(name = "id", isId = true, autoGen = true)
	private int id;

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
