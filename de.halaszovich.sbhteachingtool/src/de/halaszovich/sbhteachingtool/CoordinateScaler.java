package de.halaszovich.sbhteachingtool;

public class CoordinateScaler {
	int px0,px1; // corners of coordinate system in pixels
	double x0,x1; // extend of system in value plane
	public int scale(double x) {
		return (int)((x-x0)/(x1-x0)*(px1-px0)+px0);
	}
	public CoordinateScaler(int px0, int px1, double x0, double x1) {
		super();
		this.px0 = px0;
		this.px1 = px1;
		this.x0 = x0;
		this.x1 = x1;
	}
	
}
