package samb.client.page.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import samb.client.utils.Consts;

public class TextInfo {
	/* This class contains general information about any text which a Widget would use
	 * Having a specific class handle this information reduces complexity and code in other classes
	 * */
	
	public String text;
	public Font font;
	public Color col;
	
	public Dimension dim;
	
	public TextInfo(String text, Font font, Color col) {
		this.text = text;
		this.font = font;
		this.col = col;
		
		this.dim = calculateDims(-1);
	}
	
	
	// Getters
	public String getText() {
		return this.text;
		
	}
	
	public int getSize() {
		return this.font.getSize();
		
	}

	// Setters
	public void setText(String text) {
		this.text = text;
		this.dim = calculateDims(-1);
		
	}
	
	public void setFont(Font font) {
		this.font = font;
		this.dim = calculateDims(-1);
		
	}
	
	public void setColour(Color colour) {
		this.col = colour;
		
	} 
	
	public void setSize(int size) {
		this.font = new Font(font.getFontName(), font.getStyle(), size);
		this.dim = calculateDims(-1);
		
	}
	
	
	public Dimension calculateDims(int i) {
		// This method calculates the dimensions of the rendered text
		// Unfortunately, the methods java provides to do this isn't accurate, so an estimation is used instead
		
		if(text == null || "".equals(text)) {
			return new Dimension(0, 0);
		}

		Consts.fmg.setFont(font);
		FontMetrics fm = Consts.fmg.getFontMetrics();
		Rectangle2D r = fm.getStringBounds(i >= 0 ? text.substring(0, Math.min(i, text.length())) : text, Consts.fmg);
		return new Dimension((int)r.getWidth(), (int)(r.getHeight()*0.6));
	}
	
	
	// Render Methods
	public void render(Graphics2D g, Point xy) {
		g.setColor(col);
		g.setFont(font);
		g.drawString(text, xy.x, xy.y);
		
	}
	
	public void render(Graphics2D g, Point xy, Color col) {
		g.setColor(col);
		g.setFont(font);
		g.drawString(text, xy.x, xy.y);
		
	}

}
