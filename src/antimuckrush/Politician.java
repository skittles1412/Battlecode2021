package antimuckrush;

import battlecode.common.*;

public class Politician {
    public static RobotController robotController;

    public static void initialize(RobotController robotController) {
        Politician.robotController = robotController;
    }

    public static void processRound() throws GameActionException {
        RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(robotController.getLocation(), -1, robotController.getTeam().opponent());
        //calculate flag before returning
        if (!robotController.isReady()) {
            int flag = 0;
            MapLocation myLocation = robotController.getLocation();
            for (int i = nearbyRobots.length; --i >= 0; ) {
                RobotInfo robotInfo = nearbyRobots[i];
                MapLocation mapLocation = robotInfo.location;
                if (robotInfo.type == RobotType.ENLIGHTENMENT_CENTER) {
                    flag += 35 - myLocation.distanceSquaredTo(mapLocation);
                }
            }
            robotController.setFlag(flag * 100);
            return;
        }

        int sum = 0;
        for (RobotInfo nearbyRobot : nearbyRobots) {
            sum += nearbyRobot.getConviction() + (nearbyRobot.getType() == RobotType.MUCKRAKER ? 2 : 0);
            final int dist = robotController.getLocation().distanceSquaredTo(nearbyRobot.getLocation());
            if (dist > 9) break;
            if (nearbyRobot.getType() == RobotType.ENLIGHTENMENT_CENTER || sum >= 8) {
                robotController.empower(dist);
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
            MapLocation location8 = robotController.getLocation();
            double cost0 = robotController.canMove(Direction.NORTH) ? 1.6 / robotController.sensePassability(location0) : 1e10;
            double cost1 = robotController.canMove(Direction.NORTHEAST) ? 1.6 / robotController.sensePassability(location1) : 1e10;
            double cost2 = robotController.canMove(Direction.EAST) ? 1.6 / robotController.sensePassability(location2) : 1e10;
            double cost3 = robotController.canMove(Direction.SOUTHEAST) ? 1.6 / robotController.sensePassability(location3) : 1e10;
            double cost4 = robotController.canMove(Direction.SOUTH) ? 1.6 / robotController.sensePassability(location4) : 1e10;
            double cost5 = robotController.canMove(Direction.SOUTHWEST) ? 1.6 / robotController.sensePassability(location5) : 1e10;
            double cost6 = robotController.canMove(Direction.WEST) ? 1.6 / robotController.sensePassability(location6) : 1e10;
            double cost7 = robotController.canMove(Direction.NORTHWEST) ? 1.6 / robotController.sensePassability(location7) : 1e10;
            double cost8 = robotController.sensePassability(location8);
            int id = 0, influence = -1;
            for (int i = nearbyRobots.length; --i >= 0; ) {
                RobotInfo robotInfo = nearbyRobots[i];
                MapLocation mapLocation = robotInfo.location;
                switch (robotInfo.type) {
                    case ENLIGHTENMENT_CENTER:
                        cost0 += 100 * mapLocation.distanceSquaredTo(location0);
                        cost1 += 100 * mapLocation.distanceSquaredTo(location1);
                        cost2 += 100 * mapLocation.distanceSquaredTo(location2);
                        cost3 += 100 * mapLocation.distanceSquaredTo(location3);
                        cost4 += 100 * mapLocation.distanceSquaredTo(location4);
                        cost5 += 100 * mapLocation.distanceSquaredTo(location5);
                        cost6 += 100 * mapLocation.distanceSquaredTo(location6);
                        cost7 += 100 * mapLocation.distanceSquaredTo(location7);
                        cost8 += 100 * mapLocation.distanceSquaredTo(location8);
                        break;
                    case SLANDERER:
                        if (location8.distanceSquaredTo(mapLocation) <= 12 && robotInfo.influence > influence) {
                            id = robotInfo.ID;
                            influence = robotInfo.influence;
                        }
                }
            }
            nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
            for (int i = nearbyRobots.length; --i >= 0; ) {
                RobotInfo robotInfo = nearbyRobots[i];
                MapLocation mapLocation = robotInfo.location;
                switch (robotInfo.type) {
                    case ENLIGHTENMENT_CENTER:
//						for(int j = 8; --j>=0; ) {
//							cost[j] -= 7*mapLocation.distanceSquaredTo(locations[j]);
//						}
                        cost0 -= 2.3 * mapLocation.distanceSquaredTo(location0);
                        cost1 -= 2.3 * mapLocation.distanceSquaredTo(location1);
                        cost2 -= 2.3 * mapLocation.distanceSquaredTo(location2);
                        cost3 -= 2.3 * mapLocation.distanceSquaredTo(location3);
                        cost4 -= 2.3 * mapLocation.distanceSquaredTo(location4);
                        cost5 -= 2.3 * mapLocation.distanceSquaredTo(location5);
                        cost6 -= 2.3 * mapLocation.distanceSquaredTo(location6);
                        cost7 -= 2.3 * mapLocation.distanceSquaredTo(location7);
                        cost8 -= 2.3 * mapLocation.distanceSquaredTo(location8);
                        break;
                    case MUCKRAKER:
//						for(int j = 8; --j>=0; ) {
//							cost[j] -= 3*mapLocation.distanceSquaredTo(locations[j]);
//						}
                        int flag = robotController.getFlag(robotInfo.ID);
                        cost0 -= mapLocation.distanceSquaredTo(location0) + flag;
                        cost1 -= mapLocation.distanceSquaredTo(location1) + flag;
                        cost2 -= mapLocation.distanceSquaredTo(location2) + flag;
                        cost3 -= mapLocation.distanceSquaredTo(location3) + flag;
                        cost4 -= mapLocation.distanceSquaredTo(location4) + flag;
                        cost5 -= mapLocation.distanceSquaredTo(location5) + flag;
                        cost6 -= mapLocation.distanceSquaredTo(location6) + flag;
                        cost7 -= mapLocation.distanceSquaredTo(location7) + flag;
                        cost8 -= mapLocation.distanceSquaredTo(location8) + flag;
                        break;
                }
            }
            //find next place to move
            Direction moveDirection = Direction.NORTH;
            double min = cost0;
            if (cost1 < min) {
                moveDirection = Direction.NORTHEAST;
                min = cost1;
            }
            if (cost2 < min) {
                moveDirection = Direction.EAST;
                min = cost2;
            }
            if (cost3 < min) {
                moveDirection = Direction.SOUTHEAST;
                min = cost3;
            }
            if (cost4 < min) {
                moveDirection = Direction.SOUTH;
                min = cost4;
            }
            if (cost5 < min) {
                moveDirection = Direction.SOUTHWEST;
                min = cost5;
            }
            if (cost6 < min) {
                moveDirection = Direction.WEST;
                min = cost6;
            }
            if (cost7 < min) {
                moveDirection = Direction.NORTHWEST;
                min = cost7;
            }
            if (cost8 < min) {
                return;
            }
            if (min < 1e9) {
                robotController.move(moveDirection);
            }
        }
    }
}
