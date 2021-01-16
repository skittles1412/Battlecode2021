package sprint_0;

import battlecode.common.*;

import static sprint_0.Communications.*;

//TODO: Account for converted politicians/slanderers
public class Politician {
	public static int tries = 0;
	public static RobotController robotController;
	public static MapLocation target;
	public static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};

	public static void initialize(RobotController robotController) throws GameActionException {
		Politician.robotController = robotController;
		for(RobotInfo robotInfo: robotController.senseNearbyRobots(2, robotController.getTeam())) {
			if(robotInfo.type==RobotType.ENLIGHTENMENT_CENTER) {
				int flag = robotController.getFlag(robotInfo.ID);
				if(flag!=0) {
					int prefix = decodePrefix(flag);
					if(robotInfo.getLocation().add(DIRECTIONS[prefix-1]).equals(robotController.getLocation())) {
						target = decodeLocation(robotController.getLocation(), flag);
					}
				}
			}
		}
	}

	public static void processRound() throws GameActionException {
		//calculate flags
		int dist = robotController.getLocation().distanceSquaredTo(target);
		if(!robotController.isReady()) {
			if(dist<=9&&robotController.getCooldownTurns()<2) {
				robotController.setFlag(dist);
			}
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
				if((dist==1&&++tries==5)||robotInfo.influence+1<=(robotController.getInfluence()-10)/robotController.senseNearbyRobots(dist).length) {
					robotController.empower(dist);
				}
			}
		}
		pathfind();
	}

	public static void pathfind() throws GameActionException {
		//TODO: maybe improve pathfinding; add bug nav?
		MapLocation location0 = robotController.adjacentLocation(Direction.NORTH);
		MapLocation location1 = robotController.adjacentLocation(Direction.NORTHEAST);
		MapLocation location2 = robotController.adjacentLocation(Direction.EAST);
		MapLocation location3 = robotController.adjacentLocation(Direction.SOUTHEAST);
		MapLocation location4 = robotController.adjacentLocation(Direction.SOUTH);
		MapLocation location5 = robotController.adjacentLocation(Direction.SOUTHWEST);
		MapLocation location6 = robotController.adjacentLocation(Direction.WEST);
		MapLocation location7 = robotController.adjacentLocation(Direction.NORTHWEST);
		double cost0 = robotController.canMove(Direction.NORTH) ? robotController.sensePassability(location0) : 1e10;
		double cost1 = robotController.canMove(Direction.NORTHEAST) ? robotController.sensePassability(location1) : 1e10;
		double cost2 = robotController.canMove(Direction.EAST) ? robotController.sensePassability(location2) : 1e10;
		double cost3 = robotController.canMove(Direction.SOUTHEAST) ? robotController.sensePassability(location3) : 1e10;
		double cost4 = robotController.canMove(Direction.SOUTH) ? robotController.sensePassability(location4) : 1e10;
		double cost5 = robotController.canMove(Direction.SOUTHWEST) ? robotController.sensePassability(location5) : 1e10;
		double cost6 = robotController.canMove(Direction.WEST) ? robotController.sensePassability(location6) : 1e10;
		double cost7 = robotController.canMove(Direction.NORTHWEST) ? robotController.sensePassability(location7) : 1e10;
		cost0 += 1.5*target.distanceSquaredTo(location0);
		cost1 += 1.5*target.distanceSquaredTo(location1);
		cost2 += 1.5*target.distanceSquaredTo(location2);
		cost3 += 1.5*target.distanceSquaredTo(location3);
		cost4 += 1.5*target.distanceSquaredTo(location4);
		cost5 += 1.5*target.distanceSquaredTo(location5);
		cost6 += 1.5*target.distanceSquaredTo(location6);
		cost7 += 1.5*target.distanceSquaredTo(location7);
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
		if(min<1e9) {
			robotController.move(moveDirection);
		}
	}
}
