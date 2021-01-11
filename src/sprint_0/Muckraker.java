package sprint_0;

import battlecode.common.*;

import static sprint_0.Constants.*;

public class Muckraker {
	//	public static final int[][] MUCKRAKER_SENSE = {{0, 0}, {-1, 0}, {0, -1}, {0, 1}, {1, 0}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}, {-2, 0}, {0, -2}, {0, 2}, {2, 0}, {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}, {-2, -2}, {-2, 2}, {2, -2}, {2, 2}, {-3, 0}, {0, -3}, {0, 3}, {3, 0}, {-3, -1}, {-3, 1}, {-1, -3}, {-1, 3}, {1, -3}, {1, 3}, {3, -1}, {3, 1}, {-3, -2}, {-3, 2}, {-2, -3}, {-2, 3}, {2, -3}, {2, 3}, {3, -2}, {3, 2}, {-4, 0}, {0, -4}, {0, 4}, {4, 0}, {-4, -1}, {-4, 1}, {-1, -4}, {-1, 4}, {1, -4}, {1, 4}, {4, -1}, {4, 1}, {-3, -3}, {-3, 3}, {3, -3}, {3, 3}, {-4, -2}, {-4, 2}, {-2, -4}, {-2, 4}, {2, -4}, {2, 4}, {4, -2}, {4, 2}, {-5, 0}, {-4, -3}, {-4, 3}, {-3, -4}, {-3, 4}, {0, -5}, {0, 5}, {3, -4}, {3, 4}, {4, -3}, {4, 3}, {5, 0}, {-5, -1}, {-5, 1}, {-1, -5}, {-1, 5}, {1, -5}, {1, 5}, {5, -1}, {5, 1}, {-5, -2}, {-5, 2}, {-2, -5}, {-2, 5}, {2, -5}, {2, 5}, {5, -2}, {5, 2}};
	public static RobotController robotController;

	public static void initialize(RobotController robotController) {
		Muckraker.robotController = robotController;
	}

	public static void processRound() throws GameActionException {
		if(!robotController.isReady()) {
			return;
		}

		//expose slanderers
		{
			int id = 0, influence = -1;
			for(RobotInfo robotInfo: robotController.senseNearbyRobots(12, robotController.getTeam().opponent())) {
				if(robotInfo.type==RobotType.SLANDERER&&robotInfo.influence>influence) {
					id = robotInfo.ID;
					influence = robotInfo.influence;
				}
			}
			if(id!=0) {
				robotController.expose(id);
				return;
			}
		}

		//path find
		//essentially assign a cost to each of the directions using a heuristic
		//then move in the direction where the heuristic returns the smallest value
		//unroll everything to save bytecode
		{
			MapLocation location0 = robotController.adjacentLocation(Direction.NORTH);
			MapLocation location1 = robotController.adjacentLocation(Direction.NORTHEAST);
			MapLocation location2 = robotController.adjacentLocation(Direction.EAST);
			MapLocation location3 = robotController.adjacentLocation(Direction.SOUTHEAST);
			MapLocation location4 = robotController.adjacentLocation(Direction.SOUTH);
			MapLocation location5 = robotController.adjacentLocation(Direction.SOUTHWEST);
			MapLocation location6 = robotController.adjacentLocation(Direction.WEST);
			MapLocation location7 = robotController.adjacentLocation(Direction.NORTHWEST);
			double cost0 = robotController.canMove(Direction.NORTH) ? 1.6/robotController.sensePassability(location0) : 1e9;
			double cost1 = robotController.canMove(Direction.NORTHEAST) ? 1.6/robotController.sensePassability(location1) : 1e9;
			double cost2 = robotController.canMove(Direction.EAST) ? 1.6/robotController.sensePassability(location2) : 1e9;
			double cost3 = robotController.canMove(Direction.SOUTHEAST) ? 1.6/robotController.sensePassability(location3) : 1e9;
			double cost4 = robotController.canMove(Direction.SOUTH) ? 1.6/robotController.sensePassability(location4) : 1e9;
			double cost5 = robotController.canMove(Direction.SOUTHWEST) ? 1.6/robotController.sensePassability(location5) : 1e9;
			double cost6 = robotController.canMove(Direction.WEST) ? 1.6/robotController.sensePassability(location6) : 1e9;
			double cost7 = robotController.canMove(Direction.NORTHWEST) ? 1.6/robotController.sensePassability(location7) : 1e9;
			RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
			for(int i = nearbyRobots.length; --i>=0; ) {
				RobotInfo robotInfo = nearbyRobots[i];
				MapLocation mapLocation = robotInfo.location;
				switch(robotInfo.type) {
					case ENLIGHTENMENT_CENTER:
//						for(int j = 8; --j>=0; ) {
//							cost[j] -= 7*mapLocation.distanceSquaredTo(locations[j]);
//						}
						cost0 -= 2.3*mapLocation.distanceSquaredTo(location0);
						cost1 -= 2.3*mapLocation.distanceSquaredTo(location1);
						cost2 -= 2.3*mapLocation.distanceSquaredTo(location2);
						cost3 -= 2.3*mapLocation.distanceSquaredTo(location3);
						cost4 -= 2.3*mapLocation.distanceSquaredTo(location4);
						cost5 -= 2.3*mapLocation.distanceSquaredTo(location5);
						cost6 -= 2.3*mapLocation.distanceSquaredTo(location6);
						cost7 -= 2.3*mapLocation.distanceSquaredTo(location7);
						break;
					case MUCKRAKER:
//						for(int j = 8; --j>=0; ) {
//							cost[j] -= 3*mapLocation.distanceSquaredTo(locations[j]);
//						}
						cost0 -= mapLocation.distanceSquaredTo(location0);
						cost1 -= mapLocation.distanceSquaredTo(location1);
						cost2 -= mapLocation.distanceSquaredTo(location2);
						cost3 -= mapLocation.distanceSquaredTo(location3);
						cost4 -= mapLocation.distanceSquaredTo(location4);
						cost5 -= mapLocation.distanceSquaredTo(location5);
						cost6 -= mapLocation.distanceSquaredTo(location6);
						cost7 -= mapLocation.distanceSquaredTo(location7);
						break;
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
			}
			if(robotController.canMove(moveDirection)) {
				robotController.move(moveDirection);
			}
		}
	}
}
