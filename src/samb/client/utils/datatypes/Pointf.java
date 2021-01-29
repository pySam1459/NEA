package samb.client.utils.datatypes;

public class Pointf {
	// This class represents a Point object which can handle float values
	
	public double x, y;
	
	public Pointf() {
		this.x = 0;
		this.y = 0;
	}
	
	public Pointf(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Pointf mul(double m) {
		// TODO remove if unnecessary 
		return new Pointf(this.x * m, this.y * m);
		
	}
	
	@Override
	public String toString() {
		return String.format("Pointf{x=%f, y=%f}", this.x, this.y);
		
	}

}
