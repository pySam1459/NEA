package samb.host.game;

import samb.com.server.packet.GameInfo;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.utils.Circle;
import samb.com.utils.Func;
import samb.host.database.UserDBManager;

public class Game extends GameInfo {
	/* This subclass contains data about a singular game between 2 users
	 * This class is used to synchronise data between clients (either players or spectators),
	 *   and to keep a record of the game state
	 * */
	
	private static final long serialVersionUID = -2660503939136758429L;
	public String abandoner;
	
	public Game(String id, String u1, String u2) {
		super(id, UserDBManager.getUI(u1), UserDBManager.getUI(u2));

		this.balls = Func.createDefaultBalls(tDim, Circle.DEFAULT_BALL_RADIUS);
		this.turn = u1;
		
	}
	
	public void update(Packet p) {
		this.balls = p.gameInfo.balls;
		this.turn = p.gameInfo.turn;
		
	}
	
	public Packet createUpdatePacket() {
		Packet p = new Packet(Header.updateGame);
		p.gameInfo = this;
		
		return p;
		
	}
	
}
