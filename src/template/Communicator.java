package template;

import battlecode.common.*;

public abstract class Communicator {
	//xor every message by this
	public static final int MESSAGE_HASH = 4923936;

	//location sending
	public static final int LOCATION_MOD = 128;

	public static int encodeLocation(int x, int y) {
		return ((x%LOCATION_MOD)*LOCATION_MOD)|(y%LOCATION_MOD);
	}

	public static MapLocation decodeLocation(int locationX, int locationY, int message) {
		return new MapLocation(decodeCoordinate(locationX, (message/LOCATION_MOD)%LOCATION_MOD), decodeCoordinate(locationY, message%LOCATION_MOD));
	}

	public static int decodeCoordinate(int coordinate, int message) {
		//coordinate should be >= 10000 >= message
		int diff = (coordinate-message)%LOCATION_MOD;
		if(diff<64) {//message is right of coordinate
			return coordinate-diff;
		}else if(diff>64) {//message is left of coordinate
			return coordinate+LOCATION_MOD-diff;
		}
		//invalid coordinates
		throw new IllegalArgumentException("There can't be two cells with distance greater than 64");
	}

	protected final RobotController robotController;

	public Communicator(RobotController robotController) throws GameActionException {
		this.robotController = robotController;
		setFlag(0);
	}

	//helper methods
	//inline if necessary to save bytecode
	protected void setFlag(int flag) throws GameActionException {
		robotController.setFlag(flag^MESSAGE_HASH);
	}

	protected int getFlag(int id) throws GameActionException {
		return robotController.getFlag(id)^MESSAGE_HASH;
	}
}
