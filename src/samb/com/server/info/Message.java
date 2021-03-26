package samb.com.server.info;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Message implements Serializable {
	/* This class holds information used by the chatBox on the gamePage
	 * A Message object will be sent when a player sends a message in the chatBox
	 * */

	private static final long serialVersionUID = 2470184931776470420L;

	public String text, from;
	public ZonedDateTime time;
	
	public Message(String text, String from) {
		this.text = text;
		this.from = from;
		
		this.time = ZonedDateTime.now();
		
	}
	
}
