package samb.client.utils.inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import samb.client.main.Client;

public class Keyboard extends ArrayList<Boolean> implements KeyListener {
	// This subclass stores information about key events, ie which keys are pressed/not pressed
	// I found that making the class a subclass of ArrayList, an efficient and cleaner method
	//     than having an List/ArrayList attribute
	
	private static final long serialVersionUID = 4904165755373607895L;

	public Keyboard() {
		for(int i=0; i<256; i++) {
			add(false);
			
		}
		
		Client.getWindow().addKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent k) {
		if(k.getKeyCode() > size()) {
			for(int i=size(); i<k.getKeyCode()+1; i++) {
				add(false);
			}
		}
		set(k.getKeyCode(), true);
	}

	@Override
	public void keyReleased(KeyEvent k) {
		set(k.getKeyCode(), false);

	}

	// This method is not used
	@Override
	public void keyTyped(KeyEvent arg0) {}

}
