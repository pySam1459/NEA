package samb.client.page.widget.animations;

import java.awt.Color;
import java.awt.Graphics2D;

public class LoadingDotsAnimation extends WidgetAnimation {
	
	private int num = 3;
	private long period = (long) (1000.0/0.5);  // 1000 (ms) / frequency (Hz)
	private Color colour = Color.WHITE;
	
	private int[] amplitude;
	
	public LoadingDotsAnimation(int[] rect) {
		super(rect);

		this.amplitude = createAmplitude(num);
	}
	
	public LoadingDotsAnimation(int[] rect, int numDots, long period, Color colour) {
		super(rect);
		this.num = numDots;
		this.period = period;
		this.colour = colour;
		
		this.amplitude = createAmplitude(num);
	}

	@Override
	public void tick() {
		
		
	}

	@Override
	public void render(Graphics2D g) {
		int buffer = 2;
		int r = (rect[2] - buffer*(num-1)) / (2*num);
		int y = rect[3]/2;
		
		for(int i=0; i<num; i++) {
			g.setColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(),
					amplitude[i]*255));
			
			g.fillOval(rect[0]+(r*2+buffer)*i, y-r, r*2, r*2);
			
		}
	}
	
	
	private int[] createAmplitude(int n) {
		int[] amps = new int[n];
		for(int i=0; i<n; i++) {
			amps[i] = (int)((1.0/n) * i + 1.0);
		}
		return amps;
	}

}
