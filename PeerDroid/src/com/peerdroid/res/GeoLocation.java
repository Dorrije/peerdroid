/**
 * 
 */
package com.peerdroid.res;

/**
 * @author linghu
 *
 */
public class GeoLocation {
	private double x;
	private double y;
	
	public GeoLocation(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public double distance(GeoLocation p1) {
		double d = Math.pow((p1.x - x), 2) + Math.pow(p1.y - y, 2);
		return Math.sqrt(d);
	}
	
	public static double distance(GeoLocation p1, GeoLocation p2) {
		double d = Math.pow((p1.x - p2.x), 2) + Math.pow(p1.y - p2.y, 2);
		return Math.sqrt(d);
	}
}
