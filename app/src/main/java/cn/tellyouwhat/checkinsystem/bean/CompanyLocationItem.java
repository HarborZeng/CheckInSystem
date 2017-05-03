package cn.tellyouwhat.checkinsystem.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by Harbor-Laptop on 2017/5/1.
 * 把公司位置信息存在数据库中的一个BEAN
 */

@Table(name = "companies_info")
public class CompanyLocationItem {

	@Column(name = "building_id", property = "UNIQUE")
	private int locationID;

	@Column(name = "building_name")
	private String locationName;

	@Column(name = "x1")
	private String x1;

	@Column(name = "y1")
	private String y1;

	@Column(name = "x2")
	private String x2;

	@Column(name = "y2")
	private String y2;

	@Column(name = "x3")
	private String x3;

	@Column(name = "y3")
	private String y3;

	@Column(name = "x4")
	private String x4;

	@Column(name = "y4")
	private String y4;

	public int getLocationID() {
		return locationID;
	}

	public void setLocationID(int locationID) {
		this.locationID = locationID;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getX1() {
		return x1;
	}

	public void setX1(String x1) {
		this.x1 = x1;
	}

	public String getY1() {
		return y1;
	}

	public void setY1(String y1) {
		this.y1 = y1;
	}

	public String getX2() {
		return x2;
	}

	public void setX2(String x2) {
		this.x2 = x2;
	}

	public String getY2() {
		return y2;
	}

	public void setY2(String y2) {
		this.y2 = y2;
	}

	public String getX3() {
		return x3;
	}

	public void setX3(String x3) {
		this.x3 = x3;
	}

	public String getY3() {
		return y3;
	}

	public void setY3(String y3) {
		this.y3 = y3;
	}

	public String getX4() {
		return x4;
	}

	public void setX4(String x4) {
		this.x4 = x4;
	}

	public String getY4() {
		return y4;
	}

	public void setY4(String y4) {
		this.y4 = y4;
	}
}
