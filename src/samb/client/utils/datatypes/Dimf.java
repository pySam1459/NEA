package samb.client.utils.datatypes;

public class Dimf {
	// This class represents a Dimension object which can handle float values
	
	public double width, height;
	
	public Dimf() {
		this.width = 0.0;
		this.height = 0.0;
	}
	
	public Dimf(double w, double h) {
		this.width = w;
		this.height = h;
		
	}
	
	@Override
	public String toString() {
		return String.format("Dimf{width=%f, height=%f}", this.width, this.height);
		
	}

}
