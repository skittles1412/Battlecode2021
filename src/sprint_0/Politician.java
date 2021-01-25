package sprint_0;

import battlecode.common.*;
import utilities.Logger;

import static utilities.Communications.*;

/**
 * <p><dt><strong>Communications:</strong></dt>
 * <dd>The politician sets its flag to the radius
 * it'll empower to, and tells nearby muckrakers
 * to get out of that radius.</dd></p>
 */
public class Politician {
	//remove begin
	public static final boolean LOG = true;
	public static final Logger initializationLogger
			= new Logger(LOG ? 1 : 0, 3, 15000, "Politician initialization");
	public static final Logger pathfindingLogger
			= new Logger(LOG ? 25 : 0, 3, 15000, "Politician pathfinding");
	//remove end
	//how many times have I tried to empower
	public static int tries = 0;
	//how many times have I visited this tile
	public static double[] visited;
	public static boolean antiSurround;
	public static MapLocation target;
	public static RobotController robotController;
	public static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};

	public static void initializeSlanderer() {
		target = Slanderer.target;
		robotController = Slanderer.robotController;
		visited = new double[16384];
	}

	public static void initialize(RobotController robotController) throws GameActionException {
		if(Slanderer.target!=null) {
			initializeSlanderer();
			return;
		}
		int roundBegin = robotController.getRoundNum();//remove line
		Politician.robotController = robotController;
		//find EC that spawned me
		loop:
		{
			for(RobotInfo robotInfo: robotController.senseNearbyRobots(2, robotController.getTeam())) {
				if(robotInfo.type==RobotType.ENLIGHTENMENT_CENTER) {
					int flag = robotController.getFlag(robotInfo.ID);
					if(flag!=0) {
						int prefix = flag/EnlightenmentCenter.EC_PREFIX_MUL;
						if(robotInfo.getLocation().add(DIRECTIONS[prefix-1]).equals(robotController.getLocation())) {
							if(flag%EnlightenmentCenter.EC_PREFIX_MUL==EnlightenmentCenter.ANTI_SURROUND_PREFIX) {
								antiSurround = true;
							}else {
								target = decodeLocation(robotController.getLocation(), flag);
							}
							break loop;
						}
					}
				}
			}
			antiSurround = true;
		}
		visited = new double[16384];
		initializationLogger.logBytecode(roundBegin, robotController.getRoundNum(), 0, Clock.getBytecodeNum());//remove line
	}

	public static void processRound() throws GameActionException {
		if(antiSurround) {
			processSurround();
			return;
		}
		//calculate flags
		int dist = robotController.getLocation().distanceSquaredTo(target);
		if(dist<=9&&robotController.getCooldownTurns()<4) {
			robotController.setFlag(dist);
		}else {
			robotController.setFlag(0);
		}
		if(!robotController.isReady()) {
			return;
		}
		//empower
		//if dist == 1 it will blow up after waiting 5 rounds
		if(dist<=9) {
			RobotInfo robotInfo = robotController.senseRobotAtLocation(target);
			if(robotInfo.team==robotController.getTeam()) {
				if((dist==1&&++tries==5)||robotController.senseNearbyRobots(dist, robotController.getTeam()).length<=1) {
					robotController.empower(dist);
				}
			}else {
				if((dist==1&&++tries==5)||robotInfo.influence+1<=
						((robotController.getConviction()-10)*robotController.getEmpowerFactor(robotController.getTeam(), 0))
								/robotController.senseNearbyRobots(dist).length) {
					robotController.empower(dist);
				}
			}
		}
		int roundBegin = robotController.getRoundNum();//remove line
		int start = Clock.getBytecodeNum();//remove line
		pathfind();
		pathfindingLogger.logBytecode(roundBegin, robotController.getRoundNum(), start, Clock.getBytecodeNum());//remove line
	}

	private static void processSurround() throws GameActionException {
		if(!robotController.isReady()) {
			return;
		}
		//TODO: maybe account for non 1 influence mucks
		for(int i = 1; i<=9; i++) {
			if(((robotController.getConviction()-10)*robotController.getEmpowerFactor(robotController.getTeam(), 0))
					/Math.max(1, robotController.senseNearbyRobots(i).length)<2) {
				robotController.empower(i-1);
			}
		}
		robotController.empower(9);
	}

	private static void pathfind() throws GameActionException {
		//TODO: maybe improve pathfinding; add bug nav?
		MapLocation location0 = robotController.adjacentLocation(Direction.NORTH);
		MapLocation location1 = robotController.adjacentLocation(Direction.NORTHEAST);
		MapLocation location2 = robotController.adjacentLocation(Direction.EAST);
		MapLocation location3 = robotController.adjacentLocation(Direction.SOUTHEAST);
		MapLocation location4 = robotController.adjacentLocation(Direction.SOUTH);
		MapLocation location5 = robotController.adjacentLocation(Direction.SOUTHWEST);
		MapLocation location6 = robotController.adjacentLocation(Direction.WEST);
		MapLocation location7 = robotController.adjacentLocation(Direction.NORTHWEST);
		MapLocation location8 = robotController.getLocation();
		double cost0 = robotController.canMove(Direction.NORTH) ? robotController.sensePassability(location0) : 1e10;
		double cost1 = robotController.canMove(Direction.NORTHEAST) ? robotController.sensePassability(location1) : 1e10;
		double cost2 = robotController.canMove(Direction.EAST) ? robotController.sensePassability(location2) : 1e10;
		double cost3 = robotController.canMove(Direction.SOUTHEAST) ? robotController.sensePassability(location3) : 1e10;
		double cost4 = robotController.canMove(Direction.SOUTH) ? robotController.sensePassability(location4) : 1e10;
		double cost5 = robotController.canMove(Direction.SOUTHWEST) ? robotController.sensePassability(location5) : 1e10;
		double cost6 = robotController.canMove(Direction.WEST) ? robotController.sensePassability(location6) : 1e10;
		double cost7 = robotController.canMove(Direction.NORTHWEST) ? robotController.sensePassability(location7) : 1e10;
		double cost8 = robotController.sensePassability(location8);
		int encoded8 = encodeLocation(location8);
		cost0 += visited[encodeLocation(location0)];
		cost1 += visited[encodeLocation(location1)];
		cost2 += visited[encodeLocation(location2)];
		cost3 += visited[encodeLocation(location3)];
		cost4 += visited[encodeLocation(location4)];
		cost5 += visited[encodeLocation(location5)];
		cost6 += visited[encodeLocation(location6)];
		cost7 += visited[encodeLocation(location7)];
		cost8 += visited[encoded8];
		visited[encoded8] = Math.pow(Math.sqrt(visited[encoded8]*3)+1, 2)/3;
		int dist0 = target.distanceSquaredTo(location0);
		int dist1 = target.distanceSquaredTo(location1);
		int dist2 = target.distanceSquaredTo(location2);
		int dist3 = target.distanceSquaredTo(location3);
		int dist4 = target.distanceSquaredTo(location4);
		int dist5 = target.distanceSquaredTo(location5);
		int dist6 = target.distanceSquaredTo(location6);
		int dist7 = target.distanceSquaredTo(location7);
		int dist8 = target.distanceSquaredTo(location8);
		cost0 += 1.5*dist0;
		cost1 += 1.5*dist1;
		cost2 += 1.5*dist2;
		cost3 += 1.5*dist3;
		cost4 += 1.5*dist4;
		cost5 += 1.5*dist5;
		cost6 += 1.5*dist6;
		cost7 += 1.5*dist7;
		cost8 += 1.5*dist8;
		//avoid possible targets which would reduce empower effectiveness
		//800 bytecode but probably more efficient than a for loop
		cost0 += robotController.senseNearbyRobots(dist0).length;
		cost1 += robotController.senseNearbyRobots(dist1).length;
		cost2 += robotController.senseNearbyRobots(dist2).length;
		cost3 += robotController.senseNearbyRobots(dist3).length;
		cost4 += robotController.senseNearbyRobots(dist4).length;
		cost5 += robotController.senseNearbyRobots(dist5).length;
		cost6 += robotController.senseNearbyRobots(dist6).length;
		cost7 += robotController.senseNearbyRobots(dist7).length;
		cost8 += robotController.senseNearbyRobots(dist8).length;
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
