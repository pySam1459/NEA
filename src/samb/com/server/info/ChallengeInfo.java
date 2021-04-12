package samb.com.server.info;

import java.io.Serializable;

import samb.com.server.packet.Error;

public class ChallengeInfo implements Serializable {
	/* This class contains information about a challenge which can bee issued
	 * */

	private static final long serialVersionUID = -9115899184492142893L;
	public String oppId;
	public Error err;
	public boolean accepted = false;
	
	public ChallengeInfo(String opp) {
		this.oppId = opp;
		
	}
	
}
