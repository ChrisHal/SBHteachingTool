package de.halaszovich.sbhteachingtool;

public class CoordinateScaler {
	int px0,px1; // corners of coordinate system in pixels
	double x0,x1; // extend of system in value plane
	double log_x1x0;
	boolean isLog;
	public int scale(double x) {
		if(isLog) {
			return (int)(Math.log10(x/x0)/log_x1x0*(px1-px0)+px0);
		} else {
			return (int)((x-x0)/(x1-x0)*(px1-px0)+px0);
		}
		
	}
	public CoordinateScaler(int px0, int px1, double x0, double x1, boolean isLog) {
		super();
		this.px0 = px0;
		this.px1 = px1;
		this.x0 = x0;
		this.x1 = x1;
		log_x1x0 = Math.log10(x1/x0);
		this.isLog = isLog;
	}
	
}
