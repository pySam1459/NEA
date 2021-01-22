package samb.client.utils.datatypes;

public class Pointf {
	
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
		return new Pointf(this.x * m, this.y * m);
		
	}

}
