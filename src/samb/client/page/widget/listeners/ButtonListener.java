package samb.client.page.widget.listeners;

import samb.client.page.widget.Button;

public interface ButtonListener {
	// This interface provides a way for actions to occur when a button is pressed/released
	
	public void onClick(Button b);
	
	public void onRelease(Button b);

}
