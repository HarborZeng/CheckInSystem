package cn.tellyouwhat.checkinsystem.utils;

/**
 * Created by Harbor-Laptop on 2017/3/24.
 *
 * @author HarborZeng
 * @version 1.0
 */
public class LocationConformer {
	private double x1;
	private double y1;

	private double x2;
	private double y2;

	private double x3;
	private double y3;

	private double x4;
	private double y4;

	public LocationConformer(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.x3 = x3;
		this.y3 = y3;
		this.x4 = x4;
		this.y4 = y4;
	}

	public boolean isInRange(double x, double y) {
		boolean belowTop = false;
		boolean onTheRightOfLeft = false;
		boolean onTheLeftOfRight = false;
		boolean aboveDown = false;

		if (y1 != y2 && x1 != x2) {
			if (Math.abs((y - y2) / (y1 - y2)) > Math.abs((x - x2) / (x1 - x2))) {
				belowTop = true;
			} else {
				belowTop = false;
			}
		} else {
			if (y < y1) {
				belowTop = true;
			}
		}

		if (y2 != y3 && x2 != x3) {
			if (Math.abs((y - y3) / (y2 - y3)) < Math.abs((x - x3) / (x2 - x3))) {
				onTheLeftOfRight = true;
			} else {
				onTheLeftOfRight = false;
			}
		} else {
			if (x < x3) {
				onTheLeftOfRight = true;
			}
		}

		if (y3 != y4 && x3 != x4) {
			if (Math.abs((y - y4) / (y3 - y4)) > Math.abs((x - x3) / (x3 - x4))) {
				aboveDown = true;
			} else {
				aboveDown = false;
			}
		} else {
			if (y > y3) {
				aboveDown = true;
			}
		}

		if (y4 != y1 && x4 != x1) {
			if (Math.abs((y - y1) / (y4 - y1)) < Math.abs((x - x4) / (x4 - x1))) {
				onTheRightOfLeft = true;
			} else {
				onTheRightOfLeft = false;
			}
		} else {
			if (x > x4) {
				onTheRightOfLeft = true;
			}
		}

		return belowTop && onTheLeftOfRight && onTheRightOfLeft && aboveDown;
	}
}