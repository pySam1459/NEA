package samb.client.page.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import samb.client.main.Client;
import samb.client.page.widget.listeners.TextBoxListener;
import samb.client.utils.Consts;
import samb.com.utils.Func;

public class TextBox extends Widget implements KeyListener {
	/* This class is a subclass of the Widget class and has a KeyListener interface
	 * This class functions as a text box for the user to input data(text) into and the program to get data(text) from
	 * This class has been designed to be versatile and easily modifiable so that multiple TextBox objects can exhibit different behaviours
	 * Some of the features include: hide characters, only accept characters which match a regex, animations, cursor animation & character selection,
	 *     variable colours, character limits, auto-scrolling textbox
	 * */
	
	private int buffer;
	public Color BACKGROUND_COLOUR = new Color(0, 154, 159, 200);
	public Color TEXT_COLOUR = Consts.PAL1;
	
	public boolean round = true;
	public boolean underline = false;
	public Color UNDERLINE_COLOUR = new Color(227, 251, 247, 127);
	
	private TextInfo promptTi, ti, hideTi;
	private int textOff=0;
	
	public String textRegex = "^[% -~]"; // no control characters
	public int charLimit = -1;
	public boolean HIDE_CHARS = false;
	
	private int cursorPos = 0, cursorRot = 0, cursorDelay = 500;
	private boolean selected = false;
	
	private List<TextBoxListener> listeners;
	
	public TextBox(int[] rect, String prompt) {
		super(rect);
		
		this.buffer = (int)(rect[3]*0.1);
		
		// These TextInfo objects contain the inputed and rendered data given by the user and displayed by the program
		promptTi = new TextInfo(prompt, new Font("Inter", Font.PLAIN, (int)(rect[3]*0.4)), Consts.DARK_PAL1);
		ti = new TextInfo("", new Font("Inter", Font.PLAIN, (int)(rect[3]*0.45)), TEXT_COLOUR);
		hideTi = new TextInfo("", new Font("Inter", Font.PLAIN, (int)(rect[3]*0.45)), TEXT_COLOUR);
		
		Client.getWindow().addKeyListener(this);
		this.listeners = new ArrayList<>();
		
	}
	
	public String getText() {
		return ti.getText();	
	}
	
	public void setText(String txt) {
		ti.setText(txt);
		cursorPos = txt.length();
		if(txt.equals("")) {
			textOff = 0;
		}
	}

	@Override
	public void tick() {
		if(!HIDDEN) {
			if(selected) {
				cursorRot = (cursorRot+1) % (int)(Client.TPS*((double)cursorDelay/1000.0)*2);
	
			}
			checkSelected();
			super.animTick();
		}
	}
	
	private void checkSelected() {
		// Checks if the user selected the textbox
		if(Client.getMouse().left && Client.getMouse().forleft < 2) {
			Point xy = Client.getMouse().getXY();
			if(inRect(xy)) {
				selected = true;
				cursorPos = getCursorPos(xy);
				updateTextOff();
				
			} else { selected = false; }
			cursorRot = 0;
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		// This method is called by the Canvas object (Window) and receives KeyEvents when the user types on their keyboard
		
		if(selected) {
			String str = ti.getText();
			switch(e.getKeyCode()) {
			case KeyEvent.VK_BACK_SPACE:
				if(str.length() > 0 && cursorPos != 0) {
					ti.setText(str.substring(0, cursorPos-1) + str.substring(cursorPos));
					cursorPos--;
				}
				break;
				
			case KeyEvent.VK_DELETE:
				if(str.length() > 0 && cursorPos != str.length()) {
					ti.setText(str.substring(0, cursorPos) + str.substring(cursorPos+1));
				}
				break;
				
			case KeyEvent.VK_ENTER:
				selected = false;
				for(TextBoxListener tbl: listeners) {
					tbl.onEnter(this);
				}
				break;
				
			case KeyEvent.VK_LEFT:
				if(cursorPos > 0) {
					if(Client.getKeyboard().get(KeyEvent.VK_CONTROL)) {
						cursorPos = 0;
					} else {
						cursorPos--;
					}
				}
				break;
				
			case KeyEvent.VK_RIGHT:
				if(cursorPos < str.length()) {
					if(Client.getKeyboard().get(KeyEvent.VK_CONTROL)) {
						cursorPos = str.length();
					} else {
						cursorPos++;
					}
				}
				break;
				
			default:
				// If the character typed matches with the regex, it will be added into the textbox
				if((e.getKeyChar()+"").matches(textRegex) && (str.length()+1 < charLimit || charLimit < 0)) {
					str = str.substring(0, cursorPos) + e.getKeyChar() + str.substring(cursorPos);
					ti.setText(str);
					cursorPos++;
				}
			}
			cursorRot = 0;
			
			updateHideTi();
			updateTextOff();
		}
	}
	
	private void updateHideTi() {
		if(HIDE_CHARS) {
			hideTi.setText(Func.copyChar('*', ti.getText().length()));
			
		}
	}
	
	private void updateTextOff() {
		int coff, txoff;
		TextInfo text = getRdTi();
		
		txoff = text.calculateDims(textOff).width;
		coff = text.calculateDims(cursorPos).width;
		if(coff - txoff > rect[2]*0.95) {
			textOff++;
			
		} else if(coff - txoff < rect[2]*0.2 && textOff > 0) {
			textOff--;
			
		}
	}
	
	@Override
	public void render(Graphics2D graph) {
		// Renders the text Box, this is a complicated method as the variability offered increases the complexity of the rendering process
		if(!HIDDEN) {
			BufferedImage img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) img.getGraphics();
			
			TextInfo text = getRdTi(); // gets displayed textInfo
			
			// Render
			g.setColor(BACKGROUND_COLOUR);
			if(round) {
				int s = Math.min(rect[2], rect[3]);
				g.fillRoundRect(0, 0, rect[2], rect[3], s/2, s/2); // Draws background
			} else {
				g.fillRect(0, 0, rect[2], rect[3]);
			}
			
			if(underline) {
				g.setColor(UNDERLINE_COLOUR);
				g.drawLine(buffer, rect[3]-buffer, rect[2]-buffer, rect[3]-buffer);
			}
			
			Point xy;
			int woff=0, adjust=0;
			
			if(ti.getText() == null || "".equals(ti.getText())) {
				xy = new Point(buffer*4, promptTi.dim.height/2 + rect[3]/2);
				g.setColor(Color.GRAY);
				promptTi.render(g, xy);  // Draws prompt text
				
			} else {
				woff = text.calculateDims(textOff).width;
				adjust = HIDE_CHARS ? text.font.getSize()/4: 0;
				xy = new Point(buffer*4-woff, text.dim.height/2 + rect[3]/2 +adjust);
				text.render(g, xy);  // Draws the to-be-rendered text
			}
			
			if(selected && cursorRot < Client.TPS * cursorDelay/1000) { // Draws the cursor
				g.setColor(Consts.PALE);
				Dimension coff = text.calculateDims(cursorPos);
				if(woff != 0) {
					coff.width -= woff;
				}
				g.setStroke(new BasicStroke(3));
				g.drawLine(coff.width+buffer*4, buffer*2, coff.width+buffer*4, rect[3]-buffer*2);
			}
			
			graph.drawImage(img, rect[0], rect[1], null);
			
			super.animRender(graph);
		}
	}
	
	private int getCursorPos(Point xy) {
		// each character is a different length and the text maybe offset by the auto-scrolling feature, to get the cursor is not trivial
		xy = new Point(xy.x-rect[0]-buffer*3, xy.y-rect[1]);
		TextInfo text = getRdTi();
		
		int l = text.getText().length();
		if(l == 0) {
			return 0;
		} else {
			int w;
			for(int i=textOff; i<=text.getText().length(); i++) {
				w = text.calculateDims(i).width - text.calculateDims(textOff).width;
				if(xy.x < w) {
					return i-1;
				} else if(w > rect[2]) {
					return i;
				}
				
			}
			return l;
		}
	}

	private TextInfo getRdTi() { // If the characters are hidden, the hideTi TextInfo object should be rendered instead of ti
		return HIDE_CHARS ? hideTi : ti;
	}
	
	
	public void addListener(TextBoxListener tbl) {
		listeners.add(tbl);
	}
	
	
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}

	
}
