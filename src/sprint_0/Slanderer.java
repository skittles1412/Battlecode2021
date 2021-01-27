package sprint_0;

import battlecode.common.*;

public class Slanderer {
	public static MapLocation target;
	public static RobotController robotController;

	public static void initialize(RobotController robotController) {
		Slanderer.robotController = robotController;
		for(RobotInfo robotInfo: robotController.senseNearbyRobots(2, robotController.getTeam())) {
			if(robotInfo.type==RobotType.ENLIGHTENMENT_CENTER) {
				target = robotInfo.location;
				break;
			}
		}
	}

	public static void processRound() throws GameActionException {
		if(robotController.isReady()) {
			pathfind();
		}
	}

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
		double cost0 = robotController.canMove(Direction.NORTH) ? robotController.sensePassability(location0) : 1e10;
		double cost1 = robotController.canMove(Direction.NORTHEAST) ? robotController.sensePassability(location1) : 1e10;
		double cost2 = robotController.canMove(Direction.EAST) ? robotController.sensePassability(location2) : 1e10;
		double cost3 = robotController.canMove(Direction.SOUTHEAST) ? robotController.sensePassability(location3) : 1e10;
		double cost4 = robotController.canMove(Direction.SOUTH) ? robotController.sensePassability(location4) : 1e10;
		double cost5 = robotController.canMove(Direction.SOUTHWEST) ? robotController.sensePassability(location5) : 1e10;
		double cost6 = robotController.canMove(Direction.WEST) ? robotController.sensePassability(location6) : 1e10;
		double cost7 = robotController.canMove(Direction.NORTHWEST) ? robotController.sensePassability(location7) : 1e10;
		double cost8 = robotController.sensePassability(location8);
		int dist0 = target.distanceSquaredTo(location0);
		int dist1 = target.distanceSquaredTo(location1);
		int dist2 = target.distanceSquaredTo(location2);
		int dist3 = target.distanceSquaredTo(location3);
		int dist4 = target.distanceSquaredTo(location4);
		int dist5 = target.distanceSquaredTo(location5);
		int dist6 = target.distanceSquaredTo(location6);
		int dist7 = target.distanceSquaredTo(location7);
		int dist8 = target.distanceSquaredTo(location8);
		if(dist0<=2) {
			cost0 += 100*dist0;
		}
		if(dist1<=2) {
			cost1 += 100*dist1;
		}
		if(dist2<=2) {
			cost2 += 100*dist2;
		}
		if(dist3<=2) {
			cost3 += 100*dist3;
		}
		if(dist4<=2) {
			cost4 += 100*dist4;
		}
		if(dist5<=2) {
			cost5 += 100*dist5;
		}
		if(dist6<=2) {
			cost6 += 100*dist6;
		}
		if(dist7<=2) {
			cost7 += 100*dist7;
		}
		if(dist8<=2) {
			cost8 += 100*dist8;
		}
		cost0 += 30*((location0.x+location0.y)%2);
		cost1 += 30*((location1.x+location1.y)%2);
		cost2 += 30*((location2.x+location2.y)%2);
		cost3 += 30*((location3.x+location3.y)%2);
		cost4 += 30*((location4.x+location4.y)%2);
		cost5 += 30*((location5.x+location5.y)%2);
		cost6 += 30*((location6.x+location6.y)%2);
		cost7 += 30*((location7.x+location7.y)%2);
		cost8 += 30*((location8.x+location8.y)%2);
		if((robotController.getLocation().x+robotController.getLocation().y)%2!=0) {
			MapLocation foundLocation;
			{
				Direction direction = location8.directionTo(target).opposite();
				foundLocation = location8.translate(direction.dx*4, direction.dy*4);
			}
			for(int i = -4; i<=4; i++) {
				for(int j = -4; j<=4; j++) {
					if((i+j)%2==1) {
						MapLocation newLocation = location8.translate(i, j);
						RobotInfo robotInfo;
						if(target.distanceSquaredTo(newLocation)>2&&robotController.canSenseLocation(newLocation)
								&&((robotInfo = robotController.senseRobotAtLocation(newLocation))==null
								||(robotInfo.type!=RobotType.POLITICIAN&&robotInfo.type!=RobotType.ENLIGHTENMENT_CENTER))
								&&location8.distanceSquaredTo(newLocation)<location8.distanceSquaredTo(foundLocation)) {
							foundLocation = newLocation;
						}
					}
				}
			}
			cost0 += foundLocation.distanceSquaredTo(location0);
			cost1 += foundLocation.distanceSquaredTo(location1);
			cost2 += foundLocation.distanceSquaredTo(location2);
			cost3 += foundLocation.distanceSquaredTo(location3);
			cost4 += foundLocation.distanceSquaredTo(location4);
			cost5 += foundLocation.distanceSquaredTo(location5);
			cost6 += foundLocation.distanceSquaredTo(location6);
			cost7 += foundLocation.distanceSquaredTo(location7);
			cost8 += foundLocation.distanceSquaredTo(location8);
		}
		RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam().opponent());
		for(int i = nearbyRobots.length; --i>=0; ) {
			RobotInfo cur = nearbyRobots[i];
			if(cur.type==RobotType.MUCKRAKER) {
				MapLocation location = cur.location;
				cost0 -= 10000*location.distanceSquaredTo(location0);
				cost1 -= 10000*location.distanceSquaredTo(location1);
				cost2 -= 10000*location.distanceSquaredTo(location2);
				cost3 -= 10000*location.distanceSquaredTo(location3);
				cost4 -= 10000*location.distanceSquaredTo(location4);
				cost5 -= 10000*location.distanceSquaredTo(location5);
				cost6 -= 10000*location.distanceSquaredTo(location6);
				cost7 -= 10000*location.distanceSquaredTo(location7);
				cost8 -= 10000*location.distanceSquaredTo(location8);
			}
		}
		nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
		for(int i = nearbyRobots.length; --i>=0; ) {
			RobotInfo cur = nearbyRobots[i];
			if(cur.type==RobotType.POLITICIAN) {
				MapLocation location = cur.location;
				cost0 -= 1.5*Math.max(0, 2-location.distanceSquaredTo(location0));
				cost1 -= 1.5*Math.max(0, 2-location.distanceSquaredTo(location1));
				cost2 -= 1.5*Math.max(0, 2-location.distanceSquaredTo(location2));
				cost3 -= 1.5*Math.max(0, 2-location.distanceSquaredTo(location3));
				cost4 -= 1.5*Math.max(0, 2-location.distanceSquaredTo(location4));
				cost5 -= 1.5*Math.max(0, 2-location.distanceSquaredTo(location5));
				cost6 -= 1.5*Math.max(0, 2-location.distanceSquaredTo(location6));
				cost7 -= 1.5*Math.max(0, 2-location.distanceSquaredTo(location7));
				cost8 -= 1.5*Math.max(0, 2-location.distanceSquaredTo(location8));
			}
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
		if(cost8<=min) {
			return;
		}
		if(min<1e9) {
			robotController.move(moveDirection);
		}
	}
}
