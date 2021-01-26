package samb.com.server.info;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.List;

import samb.com.database.UserInfo;
import samb.com.utils.Circle;
import samb.com.utils.enums.TableUseCase;

public class GameInfo implements Serializable {
	/* This class contains game information which is to be sent between a client and the host
	 * Importantly, this class contains the Balls information, their positions and velocities
	 * */
	
	private static final long serialVersionUID = -340833768267080142L;
	public String id;
	public UserInfo u1, u2;
	public Dimension tDim = new Dimension(2048, 1024);
	public List<Circle> balls; // List of balls (as circle objects)
	public int red=0, yellow=0;
	
	public boolean practising = false;
	public String turn, opp;
	public TableUseCase tuc;
	
	public GameInfo(String id, UserInfo u1, UserInfo u2) {
		this.id = id;
		this.u1 = u1;
		this.u2 = u2;
		
	}

}
