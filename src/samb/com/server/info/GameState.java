package samb.com.server.info;

import java.io.Serializable;

public class GameState implements Serializable {
	
	private static final long serialVersionUID = 9041019111536124031L;
	
	public String redID, yellowID;
	public int red=0, yellow=0, turnCol = 0;
	public boolean redBlack=false, yellowBlack=false;
	public Win win;

}
