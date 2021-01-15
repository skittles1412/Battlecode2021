package sprint_0;

import battlecode.common.*;

import static sprint_0.Constants.*;
import static sprint_0.Communications.*;

public class Muckraker {
	//	public static final int[][] MUCKRAKER_SENSE = {{0, 0}, {-1, 0}, {0, -1}, {0, 1}, {1, 0}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}, {-2, 0}, {0, -2}, {0, 2}, {2, 0}, {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}, {-2, -2}, {-2, 2}, {2, -2}, {2, 2}, {-3, 0}, {0, -3}, {0, 3}, {3, 0}, {-3, -1}, {-3, 1}, {-1, -3}, {-1, 3}, {1, -3}, {1, 3}, {3, -1}, {3, 1}, {-3, -2}, {-3, 2}, {-2, -3}, {-2, 3}, {2, -3}, {2, 3}, {3, -2}, {3, 2}, {-4, 0}, {0, -4}, {0, 4}, {4, 0}, {-4, -1}, {-4, 1}, {-1, -4}, {-1, 4}, {1, -4}, {1, 4}, {4, -1}, {4, 1}, {-3, -3}, {-3, 3}, {3, -3}, {3, 3}, {-4, -2}, {-4, 2}, {-2, -4}, {-2, 4}, {2, -4}, {2, 4}, {4, -2}, {4, 2}, {-5, 0}, {-4, -3}, {-4, 3}, {-3, -4}, {-3, 4}, {0, -5}, {0, 5}, {3, -4}, {3, 4}, {4, -3}, {4, 3}, {5, 0}, {-5, -1}, {-5, 1}, {-1, -5}, {-1, 5}, {1, -5}, {1, 5}, {5, -1}, {5, 1}, {-5, -2}, {-5, 2}, {-2, -5}, {-2, 5}, {2, -5}, {2, 5}, {5, -2}, {5, 2}};
	public static boolean communicated = false;
	public static RobotController robotController;

	public static void initialize(RobotController robotController) {
		Muckraker.robotController = robotController;
	}

	public static void processPathfindingFlag() throws GameActionException {
		int flag = 0;
		MapLocation myLocation = robotController.getLocation();
		RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam().opponent());
		//30 is sensing radius
		for(int i = nearbyRobots.length; --i>=0; ) {
			RobotInfo robotInfo = nearbyRobots[i];
			MapLocation mapLocation = robotInfo.location;
			switch(robotInfo.type) {
				case ENLIGHTENMENT_CENTER:
					//250+25*(30-...)
					flag += 25*(40-myLocation.distanceSquaredTo(mapLocation));
					break;
				case SLANDERER:
					//50+10*(30-...)
					flag += 10*(35-myLocation.distanceSquaredTo(mapLocation));
			}
		}
		robotController.setFlag(flag);
	}

	public static void processRound() throws GameActionException {
		//calculate flag before returning
		//flag is basically a heuristic of the current position
		//it allows other muckrakers to come here despite of the repelling
		//basically a heuristic getting muckrakers to come near opponent EC and slanderers
		//larger flag = better
		//for muckrakers a flag with prefix 0 means a pathfinding hint and an EC comm otherwise
		//to ensure that an EC comm will always have the 23rd bit on
		if(communicated) {
			processPathfindingFlag();
			communicated = false;
		}
		if(!robotController.isReady()) {
			processPathfindingFlag();
			return;
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

			//send neutral EC locations
			RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, Team.NEUTRAL);
			if(nearbyRobots.length>0) {
				RobotInfo robotInfo = nearbyRobots[0];
				robotController.setFlag(encodePrefix(512|robotInfo.influence, encodeLocation(robotInfo.location)));
				communicated = true;
			}

			//get attracted to EC and slanderers on enemy team
			nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam().opponent());
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
						cost0 += 10*mapLocation.distanceSquaredTo(location0);
						cost1 += 10*mapLocation.distanceSquaredTo(location1);
						cost2 += 10*mapLocation.distanceSquaredTo(location2);
						cost3 += 10*mapLocation.distanceSquaredTo(location3);
						cost4 += 10*mapLocation.distanceSquaredTo(location4);
						cost5 += 10*mapLocation.distanceSquaredTo(location5);
						cost6 += 10*mapLocation.distanceSquaredTo(location6);
						cost7 += 10*mapLocation.distanceSquaredTo(location7);
						cost8 += 10*mapLocation.distanceSquaredTo(location8);
				}
			}
			if(id!=0) {
				robotController.expose(id);
				return;
			}

			//repel from our ECs (for the sake of spawning) and muckrakers
			//get out of empower radius of politicians
			nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
			for(int i = nearbyRobots.length; --i>=0; ) {
				RobotInfo robotInfo = nearbyRobots[i];
				MapLocation mapLocation = robotInfo.location;
				switch(robotInfo.type) {
					case ENLIGHTENMENT_CENTER: {
//						for(int j = 8; --j>=0; ) {
//							cost[j] -= 7*mapLocation.distanceSquaredTo(locations[j]);
//						}
						cost0 -= 2.5*mapLocation.distanceSquaredTo(location0);
						cost1 -= 2.5*mapLocation.distanceSquaredTo(location1);
						cost2 -= 2.5*mapLocation.distanceSquaredTo(location2);
						cost3 -= 2.5*mapLocation.distanceSquaredTo(location3);
						cost4 -= 2.5*mapLocation.distanceSquaredTo(location4);
						cost5 -= 2.5*mapLocation.distanceSquaredTo(location5);
						cost6 -= 2.5*mapLocation.distanceSquaredTo(location6);
						cost7 -= 2.5*mapLocation.distanceSquaredTo(location7);
						cost8 -= 2.5*mapLocation.distanceSquaredTo(location8);
						break;
					}
					case MUCKRAKER: {
//						for(int j = 8; --j>=0; ) {
//							cost[j] -= 3*mapLocation.distanceSquaredTo(locations[j]);
//						}
						int flag = robotController.getFlag(robotInfo.ID);
						if(decodePrefix(flag)==0) {
							cost0 -= mapLocation.distanceSquaredTo(location0)+flag;
							cost1 -= mapLocation.distanceSquaredTo(location1)+flag;
							cost2 -= mapLocation.distanceSquaredTo(location2)+flag;
							cost3 -= mapLocation.distanceSquaredTo(location3)+flag;
							cost4 -= mapLocation.distanceSquaredTo(location4)+flag;
							cost5 -= mapLocation.distanceSquaredTo(location5)+flag;
							cost6 -= mapLocation.distanceSquaredTo(location6)+flag;
							cost7 -= mapLocation.distanceSquaredTo(location7)+flag;
							cost8 -= mapLocation.distanceSquaredTo(location8)+flag;
						}
						break;
					}
					case POLITICIAN: {
						int flag = robotController.getFlag(robotInfo.ID)+1;
						if(flag!=0) {
							cost0 -= 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location0));
							cost1 -= 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location1));
							cost2 -= 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location2));
							cost3 -= 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location3));
							cost4 -= 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location4));
							cost5 -= 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location5));
							cost6 -= 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location6));
							cost7 -= 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location7));
							cost8 -= 1000*Math.max(0, flag-mapLocation.distanceSquaredTo(location8));
						}
					}
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
			if(cost8<min) {
				return;
			}
			if(min<1e9) {
				robotController.move(moveDirection);
			}
		}
	}
}
