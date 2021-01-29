package eco;

import battlecode.common.*;
import utilities.Logger;

import static utilities.Communications.decodeLocation;
import static utilities.Communications.encodeLocation;

/**
 * <p><dt><strong>Communications:</strong></dt>
 * <dd>The politician sets its flag to the radius
 * it'll empower to, and tells nearby muckrakers
 * to get out of that radius.</dd></p>
 */
public class Politician {
	//how many times have I tried to empower
	public static int tries = 0;
	//min radius I should be at from the EC
	public static int radius = 40;
	//how many times have I visited this tile
	public static double[] visited;
	public static MapLocation spawned, target;
	public static RobotController robotController;

	public static void initialize(RobotController robotController) throws GameActionException {
		Politician.robotController = robotController;
		Muckraker.initialize(robotController);
		spawned = robotController.getLocation();
		if(Slanderer.spawned!=null) {
			spawned = Slanderer.spawned;
		}
		for(RobotInfo robotInfo: robotController.senseNearbyRobots(2, robotController.getTeam())) {
			if(robotInfo.type==RobotType.ENLIGHTENMENT_CENTER) {
				spawned = robotInfo.location;
				break;
			}
		}
		//find EC that spawned me
		for(RobotInfo robotInfo: robotController.senseNearbyRobots(2, robotController.getTeam())) {
			if(robotInfo.type==RobotType.ENLIGHTENMENT_CENTER) {
				int flag = robotController.getFlag(robotInfo.ID);
				if(flag!=0) {
					int prefix = flag/EnlightenmentCenter.EC_PREFIX_MUL;
					if(robotInfo.getLocation().add(EnlightenmentCenter.directions[prefix-1]).equals(robotController.getLocation())) {
						target = decodeLocation(robotController.getLocation(), flag);
						break;
					}
				}
			}
		}
		visited = new double[16384];
	}

	public static void processRound() throws GameActionException {
		Muckraker.processECs();
		if(robotController.isReady()) {
			if(robotController.getConviction()<=15) {
				robotController.setFlag(1);
				Muckraker.mapExplorationPathfind();
			}else if(target==null) {
				robotController.setFlag(0);
				int conviction = (int) Math.floor((robotController.getConviction()-10)
						*robotController.getEmpowerFactor(robotController.getTeam(), 0));
				int max = 0, radius = 0;
				for(int i = 9; --i>0; ) {
					RobotInfo[] affectedRobots = robotController.senseNearbyRobots(i, robotController.getTeam().opponent());
					if(affectedRobots.length>0) {
						int damage = conviction/affectedRobots.length;
						int cnt = 0;
						for(int j = affectedRobots.length; --j>=0; ) {
							if(affectedRobots[j].conviction<damage) {
								cnt++;
							}
						}
						if(cnt>max) {
							max = cnt;
							radius = i;
						}
					}
				}
				if(max>1||(max==1&&spawned.distanceSquaredTo(robotController.getLocation())<=30)) {
					robotController.empower(radius);
					return;
				}else if(max==1) {
					RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
					for(int i = nearbyRobots.length; --i>=0; ) {
						if(nearbyRobots[i].type==RobotType.POLITICIAN&&robotController.getFlag(nearbyRobots[i].ID)==2) {
							robotController.empower(radius);
							return;
						}
					}
				}
				protectPathfind();
			}else {
				robotController.setFlag(3);
				int dist = robotController.getLocation().distanceSquaredTo(target);
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
				targetPathfind();
			}
		}
	}

	private static boolean validDistance(int distance) {
		return distance>=radius&&distance<=radius+10;
	}

	private static void protectPathfind() throws GameActionException {
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
		double cost8 = 2/robotController.sensePassability(location8);
		int dist0 = spawned.distanceSquaredTo(location0);
		int dist1 = spawned.distanceSquaredTo(location1);
		int dist2 = spawned.distanceSquaredTo(location2);
		int dist3 = spawned.distanceSquaredTo(location3);
		int dist4 = spawned.distanceSquaredTo(location4);
		int dist5 = spawned.distanceSquaredTo(location5);
		int dist6 = spawned.distanceSquaredTo(location6);
		int dist7 = spawned.distanceSquaredTo(location7);
		int dist8 = spawned.distanceSquaredTo(location8);
		if(spawned.distanceSquaredTo(location8.add(spawned.directionTo(location8)))<radius) {
			if(spawned.distanceSquaredTo(location0)<dist8) {
				cost0 += 10;
			}
			if(spawned.distanceSquaredTo(location1)<dist8) {
				cost1 += 10;
			}
			if(spawned.distanceSquaredTo(location2)<dist8) {
				cost2 += 10;
			}
			if(spawned.distanceSquaredTo(location3)<dist8) {
				cost3 += 10;
			}
			if(spawned.distanceSquaredTo(location4)<dist8) {
				cost4 += 10;
			}
			if(spawned.distanceSquaredTo(location5)<dist8) {
				cost5 += 10;
			}
			if(spawned.distanceSquaredTo(location6)<dist8) {
				cost6 += 10;
			}
			if(spawned.distanceSquaredTo(location7)<dist8) {
				cost7 += 10;
			}
		}
		if(!validDistance(dist0)) {
			cost0 += 10;
		}
		if(!validDistance(dist1)) {
			cost1 += 10;
		}
		if(!validDistance(dist2)) {
			cost2 += 10;
		}
		if(!validDistance(dist3)) {
			cost3 += 10;
		}
		if(!validDistance(dist4)) {
			cost4 += 10;
		}
		if(!validDistance(dist5)) {
			cost5 += 10;
		}
		if(!validDistance(dist6)) {
			cost6 += 10;
		}
		if(!validDistance(dist7)) {
			cost7 += 10;
		}
		if(!validDistance(dist8)) {
			cost8 += 10;
		}

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

	private static void targetPathfind() throws GameActionException {
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
