package samb.client.page.widget.animations;

import java.awt.Color;
import java.awt.Graphics2D;

public class LoadingDotsAnimation extends WidgetAnimation {
	/* This animation is used in conjunction with the BlankWidget widget to show 
	 *    an animation of dots oscillating back and forth, a loading animation
	 * */
	
	private int num = 3;
	private double period = 1000.0/0.75;  // 1000 (ms) / frequency (Hz)
	private Color colour = Color.WHITE;
	
	private double[] times;
	private long last = System.currentTimeMillis();
	
	public LoadingDotsAnimation(int[] rect) {
		super(rect);

		this.times = createTimes(num);
	}
	
	public LoadingDotsAnimation(int[] rect, int numDots, long period, Color colour) {
		super(rect);
		this.num = numDots;
		this.period = period;
		this.colour = colour;
		
		this.times = createTimes(num);
	}

	@Override
	public void tick() {
		// increases times
		long t = System.currentTimeMillis();
		for(int i=0; i<times.length; i++) {
			times[i] += (t - last) / 1000.0;
			
		}
		last = t;
	}

	@Override
	public void render(Graphics2D g) {
		// Renders animation
		int buffer = 14;
		int r = (int)(0.8 * (rect[2] - (double)buffer*(num-1)) / (2.0*num));
		int y = rect[3]/2;
		double alpha;
		
		for(int i=0; i<num; i++) { // the sin function 
			alpha = (Math.sin(times[i] * Math.PI * 2 * (1000.0/period))+1)/2.0; // calculates alpha from function: sin(kt) 
			
			g.setColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), (int)(alpha*254.0)));
			g.fillOval(rect[0]+(r*2+buffer)*i, rect[1]+y-r, r*2, r*2);
			
		}
	}
	
	
	private double[] createTimes(int n) {
		// each times element is offset from one another, so oscillate with a phase difference
		double[] times = new double[n];
		for(int i=0; i<n; i++) {
			times[i] = ((double)(n-i-1)/n) * period;
		}
		return times;
	}

}
