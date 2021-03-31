package samb.com.utils.data;

import java.io.Serializable;

public class Pointf implements Serializable {
	// This class represents a Point object which can handle float values
	
	private static final long serialVersionUID = 7275992100716149913L;
	public double x, y;
	
	public Pointf() {
		this.x = 0;
		this.y = 0;
	}
	
	public Pointf(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString() {
		return String.format("Pointf{x=%f, y=%f}", this.x, this.y);
		
	}

}
