package samb.host.game;

import samb.com.server.info.GameInfo;
import samb.com.server.info.GameState;
import samb.com.server.packet.Packet;
import samb.com.server.packet.UHeader;
import samb.com.utils.Func;
import samb.host.database.StatsDBManager;
import samb.host.database.UserDBManager;

public class Game extends GameInfo {
	/* This subclass contains data about a singular game between 2 users
	 * This class is used to synchronise data between clients (either players or spectators),
	 *   and to keep a record of the game state
	 * */
	
	private static final long serialVersionUID = -2660503939136758429L;
	public GameState state;
	public String winnerId;
	
	public Game(String id, String u1, String u2) {
		super(id, UserDBManager.getUI(u1), UserDBManager.getUI(u2));

		this.balls = Func.createDefaultBalls(tDim);
		this.state = new GameState();
		
		setElos();
		
	}
	
	private void setElos() {
		u1.elo = StatsDBManager.getElo(u1.id);
		u2.elo = StatsDBManager.getElo(u2.id);
		
	}
	
	public void update(Packet p) {
		if(p.gameState != null) {
			this.state = p.gameState;
			
		} if(p.gameInfo != null) {
			this.balls = p.gameInfo.balls;
			
		} if(p.updateInfo != null) {
			if(p.updateInfo.header == UHeader.win) {
				winnerId = p.updateInfo.winner;
			}
		}
	}
	
	public String getOppId(String id) {
		return u1.id.equals(id) ? u2.id : u1.id;
		
	}
	
}
