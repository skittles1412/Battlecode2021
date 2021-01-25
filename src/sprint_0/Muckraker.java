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
	}

	public static void processRound() throws GameActionException {
		//TODO: surround enemy politicians
		int start = Clock.getBytecodeNum();//remove line
		processNeutralECs();
		flagLogger.logBytecode(start, Clock.getBytecodeNum());//remove line
		int roundBegin = robotController.getRoundNum();//remove line
		start = Clock.getBytecodeNum();//remove line
		if(robotController.isReady()) {
			pathfind();
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
		double cost0 = robotController.canMove(Direction.NORTH) ? 1.5/robotController.sensePassability(location0) : 1e10;
		double cost1 = robotController.canMove(Direction.NORTHEAST) ? 1.5/robotController.sensePassability(location1) : 1e10;
		double cost2 = robotController.canMove(Direction.EAST) ? 1.5/robotController.sensePassability(location2) : 1e10;
		double cost3 = robotController.canMove(Direction.SOUTHEAST) ? 1.5/robotController.sensePassability(location3) : 1e10;
		double cost4 = robotController.canMove(Direction.SOUTH) ? 1.5/robotController.sensePassability(location4) : 1e10;
		double cost5 = robotController.canMove(Direction.SOUTHWEST) ? 1.5/robotController.sensePassability(location5) : 1e10;
		double cost6 = robotController.canMove(Direction.WEST) ? 1.5/robotController.sensePassability(location6) : 1e10;
		double cost7 = robotController.canMove(Direction.NORTHWEST) ? 1.5/robotController.sensePassability(location7) : 1e10;
		double cost8 = robotController.sensePassability(location8);

		//explore the map more in early rounds
		if(robotController.getRoundNum()<250) {
			int cdist = spawned.distanceSquaredTo(location8);
			if(spawned.distanceSquaredTo(location0)<cdist) {
				cost0 += 10;
			}
			if(spawned.distanceSquaredTo(location1)<cdist) {
				cost1 += 10;
			}
			if(spawned.distanceSquaredTo(location2)<cdist) {
				cost2 += 10;
			}
			if(spawned.distanceSquaredTo(location3)<cdist) {
				cost3 += 10;
			}
			if(spawned.distanceSquaredTo(location4)<cdist) {
				cost4 += 10;
			}
			if(spawned.distanceSquaredTo(location5)<cdist) {
				cost5 += 10;
			}
			if(spawned.distanceSquaredTo(location6)<cdist) {
				cost6 += 10;
			}
			if(spawned.distanceSquaredTo(location7)<cdist) {
				cost7 += 10;
			}
		}

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
					cost0 += 25*mapLocation.distanceSquaredTo(location0);
					cost1 += 25*mapLocation.distanceSquaredTo(location1);
					cost2 += 25*mapLocation.distanceSquaredTo(location2);
					cost3 += 25*mapLocation.distanceSquaredTo(location3);
					cost4 += 25*mapLocation.distanceSquaredTo(location4);
					cost5 += 25*mapLocation.distanceSquaredTo(location5);
					cost6 += 25*mapLocation.distanceSquaredTo(location6);
					cost7 += 25*mapLocation.distanceSquaredTo(location7);
					cost8 += 25*mapLocation.distanceSquaredTo(location8);
					break;
				case SLANDERER:
					if(location8.distanceSquaredTo(mapLocation)<=12&&robotInfo.influence>influence) {
						id = robotInfo.ID;
						influence = robotInfo.influence;
					}
					cost0 += 60*mapLocation.distanceSquaredTo(location0);
					cost1 += 60*mapLocation.distanceSquaredTo(location1);
					cost2 += 60*mapLocation.distanceSquaredTo(location2);
					cost3 += 60*mapLocation.distanceSquaredTo(location3);
					cost4 += 60*mapLocation.distanceSquaredTo(location4);
					cost5 += 60*mapLocation.distanceSquaredTo(location5);
					cost6 += 60*mapLocation.distanceSquaredTo(location6);
					cost7 += 60*mapLocation.distanceSquaredTo(location7);
					cost8 += 60*mapLocation.distanceSquaredTo(location8);
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
						cost0 += 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location0));
						cost1 += 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location1));
						cost2 += 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location2));
						cost3 += 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location3));
						cost4 += 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location4));
						cost5 += 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location5));
						cost6 += 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location6));
						cost7 += 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location7));
						cost8 += 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location8));
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
}
