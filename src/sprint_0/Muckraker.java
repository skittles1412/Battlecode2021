package sprint_0;

import battlecode.common.*;
import utilities.FastRandom;
import utilities.Logger;

import static utilities.Communications.*;

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
	public static final Logger pathfindEnemyLogger
			= new Logger(LOG ? 200 : 0, 4, 15000, "Muckraker enemy");
	public static final Logger pathfindFriendlyLogger
			= new Logger(LOG ? 200 : 0, 4, 15000, "Muckraker friendly");
	//remove end
	//velocity for bouncing pathfinding
	public static double vx, vy;
	//map locations to bounce off as an arraylist
	public static int bounceInd = 0;
	public static MapLocation[] toBounce;
	public static MapLocation spawned;
	public static RobotController robotController;

	public static void initialize(RobotController robotController) {
		Muckraker.robotController = robotController;
		for(RobotInfo robotInfo: robotController.senseNearbyRobots(2, robotController.getTeam())) {
			if(robotInfo.type==RobotType.ENLIGHTENMENT_CENTER) {
				spawned = robotInfo.location;
				break;
			}
		}
		Direction direction = spawned.directionTo(robotController.getLocation());
		vx = direction.dx;
		vy = direction.dy;
		toBounce = new MapLocation[300];
	}

	public static void processRound() throws GameActionException {
		//TODO: surround enemy politicians
		int start = Clock.getBytecodeNum();//remove line
		processNeutralECs();
		flagLogger.logBytecode(start, Clock.getBytecodeNum());//remove line
		int roundBegin = robotController.getRoundNum();//remove line
		start = Clock.getBytecodeNum();//remove line
		if(robotController.isReady()) {
			if(robotController.getRoundNum()<=300) {
				RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam().opponent());
				for(int i = nearbyRobots.length; --i>=0; ) {
					if(nearbyRobots[i].type==RobotType.SLANDERER) {
						robotController.expose(nearbyRobots[i].ID);
						return;
					}
				}
				mapExplorationPathfind();
			}else {
				pathfind();
			}
		}
		pathfindLogger.logBytecode(roundBegin, robotController.getRoundNum(), start, Clock.getBytecodeNum());//remove line
	}

	/**
	 * Communicates neutral ECs.
	 */
	private static void processNeutralECs() throws GameActionException {
		RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, Team.NEUTRAL);
		if(nearbyRobots.length>0) {
			RobotInfo robotInfo = nearbyRobots[FastRandom.nextInt(nearbyRobots.length)];
			robotController.setFlag(encodePrefix(robotInfo.influence, encodeLocation(robotInfo.location)));
		}else {
			robotController.setFlag(0);
		}
	}

	/**
	 * Moves the robot.
	 * essentially assign a cost to each of the directions using a heuristic
	 * then move in the direction where the heuristic returns the smallest value
	 * unroll everything to save bytecode
	 */
	private static void pathfind() throws GameActionException {
		MapLocation location0 = robotController.adjacentLocation(Direction.NORTH);
		MapLocation location1 = robotController.adjacentLocation(Direction.NORTHEAST);
		MapLocation location2 = robotController.adjacentLocation(Direction.EAST);
		MapLocation location3 = robotController.adjacentLocation(Direction.SOUTHEAST);
		MapLocation location4 = robotController.adjacentLocation(Direction.SOUTH);
		MapLocation location5 = robotController.adjacentLocation(Direction.SOUTHWEST);
		MapLocation location6 = robotController.adjacentLocation(Direction.WEST);
		MapLocation location7 = robotController.adjacentLocation(Direction.NORTHWEST);
		MapLocation location8 = robotController.getLocation();
		double cost0 = robotController.canMove(Direction.NORTH) ? 2/robotController.sensePassability(location0) : 1e10;
		double cost1 = robotController.canMove(Direction.NORTHEAST) ? 2/robotController.sensePassability(location1) : 1e10;
		double cost2 = robotController.canMove(Direction.EAST) ? 2/robotController.sensePassability(location2) : 1e10;
		double cost3 = robotController.canMove(Direction.SOUTHEAST) ? 2/robotController.sensePassability(location3) : 1e10;
		double cost4 = robotController.canMove(Direction.SOUTH) ? 2/robotController.sensePassability(location4) : 1e10;
		double cost5 = robotController.canMove(Direction.SOUTHWEST) ? 2/robotController.sensePassability(location5) : 1e10;
		double cost6 = robotController.canMove(Direction.WEST) ? 2/robotController.sensePassability(location6) : 1e10;
		double cost7 = robotController.canMove(Direction.NORTHWEST) ? 2/robotController.sensePassability(location7) : 1e10;
		double cost8 = robotController.sensePassability(location8);

		int roundBegin = robotController.getRoundNum();//remove line
		int start = Clock.getBytecodeNum();//remove line
		//get attracted to EC and slanderers on enemy team
		RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam().opponent());
		int id = 0, influence = -1;
		for(int i = nearbyRobots.length; --i>=0; ) {
			RobotInfo robotInfo = nearbyRobots[i];
			MapLocation mapLocation = robotInfo.location;
			switch(robotInfo.type) {
				case ENLIGHTENMENT_CENTER:
					cost0 += 40*mapLocation.distanceSquaredTo(location0);
					cost1 += 40*mapLocation.distanceSquaredTo(location1);
					cost2 += 40*mapLocation.distanceSquaredTo(location2);
					cost3 += 40*mapLocation.distanceSquaredTo(location3);
					cost4 += 40*mapLocation.distanceSquaredTo(location4);
					cost5 += 40*mapLocation.distanceSquaredTo(location5);
					cost6 += 40*mapLocation.distanceSquaredTo(location6);
					cost7 += 40*mapLocation.distanceSquaredTo(location7);
					cost8 += 40*mapLocation.distanceSquaredTo(location8);
					break;
				case SLANDERER:
					if(location8.distanceSquaredTo(mapLocation)<=12&&robotInfo.influence>influence) {
						id = robotInfo.ID;
						influence = robotInfo.influence;
					}
					cost0 += 200*mapLocation.distanceSquaredTo(location0);
					cost1 += 200*mapLocation.distanceSquaredTo(location1);
					cost2 += 200*mapLocation.distanceSquaredTo(location2);
					cost3 += 200*mapLocation.distanceSquaredTo(location3);
					cost4 += 200*mapLocation.distanceSquaredTo(location4);
					cost5 += 200*mapLocation.distanceSquaredTo(location5);
					cost6 += 200*mapLocation.distanceSquaredTo(location6);
					cost7 += 200*mapLocation.distanceSquaredTo(location7);
					cost8 += 200*mapLocation.distanceSquaredTo(location8);
			}
		}
		if(id!=0) {
			robotController.expose(id);
			return;
		}
		pathfindEnemyLogger.logBytecode(roundBegin, robotController.getRoundNum(), start, Clock.getBytecodeNum());//remove line

		roundBegin = robotController.getRoundNum();//remove line
		start = Clock.getBytecodeNum();//remove line
		//repel from our ECs (for the sake of spawning) and muckrakers
		//get out of empower radius of politicians
		nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
		for(int i = nearbyRobots.length; --i>=0; ) {
			RobotInfo robotInfo = nearbyRobots[i];
			MapLocation mapLocation = robotInfo.location;
			switch(robotInfo.type) {
				case ENLIGHTENMENT_CENTER: {
					cost0 -= 3*mapLocation.distanceSquaredTo(location0);
					cost1 -= 3*mapLocation.distanceSquaredTo(location1);
					cost2 -= 3*mapLocation.distanceSquaredTo(location2);
					cost3 -= 3*mapLocation.distanceSquaredTo(location3);
					cost4 -= 3*mapLocation.distanceSquaredTo(location4);
					cost5 -= 3*mapLocation.distanceSquaredTo(location5);
					cost6 -= 3*mapLocation.distanceSquaredTo(location6);
					cost7 -= 3*mapLocation.distanceSquaredTo(location7);
					cost8 -= 3*mapLocation.distanceSquaredTo(location8);
					break;
				}
				case MUCKRAKER: {
//					int flag = robotController.getFlag(robotInfo.ID);
//					if(flag<PREFIX_MUL) {
//						cost0 += (flag-1)*mapLocation.distanceSquaredTo(location0);
//						cost1 += (flag-1)*mapLocation.distanceSquaredTo(location1);
//						cost2 += (flag-1)*mapLocation.distanceSquaredTo(location2);
//						cost3 += (flag-1)*mapLocation.distanceSquaredTo(location3);
//						cost4 += (flag-1)*mapLocation.distanceSquaredTo(location4);
//						cost5 += (flag-1)*mapLocation.distanceSquaredTo(location5);
//						cost6 += (flag-1)*mapLocation.distanceSquaredTo(location6);
//						cost7 += (flag-1)*mapLocation.distanceSquaredTo(location7);
//						cost8 += (flag-1)*mapLocation.distanceSquaredTo(location8);
//					}else {
					cost0 -= mapLocation.distanceSquaredTo(location0);
					cost1 -= mapLocation.distanceSquaredTo(location1);
					cost2 -= mapLocation.distanceSquaredTo(location2);
					cost3 -= mapLocation.distanceSquaredTo(location3);
					cost4 -= mapLocation.distanceSquaredTo(location4);
					cost5 -= mapLocation.distanceSquaredTo(location5);
					cost6 -= mapLocation.distanceSquaredTo(location6);
					cost7 -= mapLocation.distanceSquaredTo(location7);
					cost8 -= mapLocation.distanceSquaredTo(location8);
//					}
					break;
				}
				case POLITICIAN: {
					int flag = robotController.getFlag(robotInfo.ID);
					if(flag!=0) {
						flag++;
						cost0 += 100000*Math.max(0, flag-mapLocation.distanceSquaredTo(location0));
						cost1 += 100000*Math.max(0, flag-mapLocation.distanceSquaredTo(location1));
						cost2 += 100000*Math.max(0, flag-mapLocation.distanceSquaredTo(location2));
						cost3 += 100000*Math.max(0, flag-mapLocation.distanceSquaredTo(location3));
						cost4 += 100000*Math.max(0, flag-mapLocation.distanceSquaredTo(location4));
						cost5 += 100000*Math.max(0, flag-mapLocation.distanceSquaredTo(location5));
						cost6 += 100000*Math.max(0, flag-mapLocation.distanceSquaredTo(location6));
						cost7 += 100000*Math.max(0, flag-mapLocation.distanceSquaredTo(location7));
						cost8 += 100000*Math.max(0, flag-mapLocation.distanceSquaredTo(location8));
					}
				}
			}
		}
		pathfindFriendlyLogger.logBytecode(roundBegin, robotController.getRoundNum(), start, Clock.getBytecodeNum());//remove line

		//find next place to move
		Direction moveDirection = Direction.NORTH;
		double min = cost0;
		if(cost1<min) {
			moveDirection = Direction.NORTHEAST;
			min = cost1;
		}
		if(cost2<min) {
			moveDirection = Direction.EAST;
			min = cost2;
		}
		if(cost3<min) {
			moveDirection = Direction.SOUTHEAST;
			min = cost3;
		}
		if(cost4<min) {
			moveDirection = Direction.SOUTH;
			min = cost4;
		}
		if(cost5<min) {
			moveDirection = Direction.SOUTHWEST;
			min = cost5;
		}
		if(cost6<min) {
			moveDirection = Direction.WEST;
			min = cost6;
		}
		if(cost7<min) {
			moveDirection = Direction.NORTHWEST;
			min = cost7;
		}
		if(cost8<min) {
			return;
		}
		if(min<1e9) {
			robotController.move(moveDirection);
		}
	}

	/**
	 * Pathfinding using muckraker bouncing.
	 */
	private static void mapExplorationPathfind() throws GameActionException {
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
			toBounce[bounceInd++] = myLocation.translate(-leftBorder, 0);
		}
		if(rightBorder<5) {
			toBounce[bounceInd++] = myLocation.translate(rightBorder, 0);
		}
		if(bottomBorder<5) {
			toBounce[bounceInd++] = myLocation.translate(0, -bottomBorder);
		}
		if(topBorder<5) {
			toBounce[bounceInd++] = myLocation.translate(0, topBorder);
		}
		RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
		for(int i = nearbyRobots.length; --i>=0; ) {
			RobotInfo robotInfo = nearbyRobots[i];
			if(robotInfo.type==RobotType.MUCKRAKER&&robotInfo.location.distanceSquaredTo(spawned)>2) {
				toBounce[bounceInd++] = robotInfo.location;
			}
		}
		for(int i = bounceInd; --i>=0; ) {
			MapLocation location = toBounce[i];
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
