package eco;

import battlecode.common.*;
import utilities.FastRandom;
import utilities.Logger;

import static utilities.Communications.encodeLocation;
import static utilities.Communications.encodePrefix;

/**
 * <p><dt><strong>Communications:</strong></dt>
 * <dd>The muckraker only uses its communications for the sending
 * of neutral ECs. If the muckraker is sending a neutral EC,
 * the prefix will be the influence and the message the location.
 * Otherwise the flag will be 0</dd></p>
 */
public class Muckraker {
	//remove begin
	public static final boolean LOG = true;
	public static final Logger flagLogger
			= new Logger(LOG ? 200 : 0, 3, 15000, "Muckraker flag processing");
	public static final Logger pathfindLogger
			= new Logger(LOG ? 200 : 0, 3, 15000, "Muckraker pathfinding");
	//remove end
	//bit to set in prefix which indicates that the communicated EC isn't neutral
	public static final int ENEMY_EC_PREFIX = 512; //1<<9
	//velocity for bouncing pathfinding
	public static double vx = 0, vy = 0;
	//map locations to bounce off as an arraylist
	public static int bounceInd = 0;
	public static MapLocation[] toRepel;
	public static RobotController robotController;

	public static void initialize(RobotController robotController) {
		Muckraker.robotController = robotController;
		toRepel = new MapLocation[300];
	}

	public static void processRound() throws GameActionException {
		//TODO: surround enemy politicians
		int start = Clock.getBytecodeNum();//remove line
		processECs();
		flagLogger.logBytecode(start, Clock.getBytecodeNum());//remove line
		int roundBegin = robotController.getRoundNum();//remove line
		start = Clock.getBytecodeNum();//remove line
		if(robotController.isReady()) {
			mapExplorationPathfind();
		}
		pathfindLogger.logBytecode(roundBegin, robotController.getRoundNum(), start, Clock.getBytecodeNum());//remove line
	}

	/**
	 * Communicates neutral ECs.
	 */
	public static void processECs() throws GameActionException {
		RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, Team.NEUTRAL);
		if(nearbyRobots.length>0) {
			RobotInfo robotInfo = nearbyRobots[FastRandom.nextInt(nearbyRobots.length)];
			robotController.setFlag(encodePrefix(robotInfo.influence, encodeLocation(robotInfo.location)));
		}else {
			nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam().opponent());
			for(int i = nearbyRobots.length; --i>=0; ) {
				RobotInfo robotInfo = nearbyRobots[i];
				if(robotInfo.type==RobotType.ENLIGHTENMENT_CENTER) {
					int influence = robotInfo.influence, flagInfluence = 1;
					for( ; ; flagInfluence++) {
						if(Math.pow(1.1, flagInfluence)>influence) {
							flagInfluence--;
							break;
						}
					}
					robotController.setFlag(encodePrefix(ENEMY_EC_PREFIX|flagInfluence, encodeLocation(robotInfo.location)));
				}
			}
		}
	}

	/**
	 * Pathfinding using muckraker bouncing.
	 */
	public static void mapExplorationPathfind() throws GameActionException {
		bounceInd = 0;
		MapLocation myLocation = robotController.getLocation();
		int leftBorder = getBoundary(robotController.onTheMap(myLocation.translate(-1, 0)),
				robotController.onTheMap(myLocation.translate(-2, 0)),
				robotController.onTheMap(myLocation.translate(-3, 0)),
				robotController.onTheMap(myLocation.translate(-4, 0)),
				robotController.onTheMap(myLocation.translate(-5, 0)));
		int rightBorder = getBoundary(robotController.onTheMap(myLocation.translate(1, 0)),
				robotController.onTheMap(myLocation.translate(2, 0)),
				robotController.onTheMap(myLocation.translate(3, 0)),
				robotController.onTheMap(myLocation.translate(4, 0)),
				robotController.onTheMap(myLocation.translate(5, 0)));
		int bottomBorder = getBoundary(robotController.onTheMap(myLocation.translate(0, -1)),
				robotController.onTheMap(myLocation.translate(0, -2)),
				robotController.onTheMap(myLocation.translate(0, -3)),
				robotController.onTheMap(myLocation.translate(0, -4)),
				robotController.onTheMap(myLocation.translate(0, -5)));
		int topBorder = getBoundary(robotController.onTheMap(myLocation.translate(0, 1)),
				robotController.onTheMap(myLocation.translate(0, 2)),
				robotController.onTheMap(myLocation.translate(0, 3)),
				robotController.onTheMap(myLocation.translate(0, 4)),
				robotController.onTheMap(myLocation.translate(0, 5)));
		leftBorder++;
		rightBorder++;
		bottomBorder++;
		topBorder++;
		if(leftBorder<5) {
			toRepel[bounceInd++] = myLocation.translate(-leftBorder, 0);
			toRepel[bounceInd++] = myLocation.translate(-leftBorder, 0);
		}
		if(rightBorder<5) {
			toRepel[bounceInd++] = myLocation.translate(rightBorder, 0);
			toRepel[bounceInd++] = myLocation.translate(rightBorder, 0);
		}
		if(bottomBorder<5) {
			toRepel[bounceInd++] = myLocation.translate(0, -bottomBorder);
			toRepel[bounceInd++] = myLocation.translate(0, -bottomBorder);
		}
		if(topBorder<5) {
			toRepel[bounceInd++] = myLocation.translate(0, topBorder);
			toRepel[bounceInd++] = myLocation.translate(0, topBorder);
		}
		RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
		for(int i = nearbyRobots.length; --i>=0; ) {
			RobotInfo robotInfo = nearbyRobots[i];
			switch(robotInfo.type) {
				case ENLIGHTENMENT_CENTER:
					toRepel[bounceInd++] = robotInfo.location;
				case MUCKRAKER:
					toRepel[bounceInd++] = robotInfo.location;
					break;
				case POLITICIAN:
					if(robotInfo.conviction<=10||robotController.getFlag(robotInfo.ID)==1) {
						toRepel[bounceInd++] = robotInfo.location;
					}
			}
		}
		for(int i = bounceInd; --i>=0; ) {
			MapLocation location = toRepel[i];
			Direction direction = location.directionTo(myLocation);
			double dist = location.distanceSquaredTo(myLocation);
			vx += 5*direction.dx/dist;
			vy += 5*direction.dy/dist;
		}
		if(robotController.isReady()) {
			Direction direction = myLocation.directionTo(
					myLocation.translate((int) Math.round(vx), (int) Math.round(vy)));
			Direction left = direction.rotateLeft();
			Direction right = direction.rotateRight();
			double cost0 = robotController.canMove(direction) ? robotController.sensePassability(robotController.adjacentLocation(direction)) : -1;
			double cost1 = robotController.canMove(left) ? robotController.sensePassability(robotController.adjacentLocation(left)) : -1;
			double cost2 = robotController.canMove(right) ? robotController.sensePassability(robotController.adjacentLocation(right)) : -1;
			Direction moveDirection = direction;
			double max = cost0;
			if(cost1>max) {
				max = cost1;
				moveDirection = left;
			}
			if(cost2>max) {
				moveDirection = right;
			}
			if(robotController.canMove(moveDirection)) {
				robotController.move(moveDirection);
			}
		}
	}

	/**
	 * arr must have length 5 and its prefix must be true and its suffix must be false
	 *
	 * @return The first value of arr that is false, or 5, if none exist
	 */
	private static int getBoundary(boolean... arr) {
		if(arr[4]) {
			return 5;
		}
		if(arr[3]) {
			return 4;
		}
		if(arr[2]) {
			return 3;
		}
		if(arr[1]) {
			return 2;
		}
		if(arr[0]) {
			return 1;
		}
		return 0;
	}
}
