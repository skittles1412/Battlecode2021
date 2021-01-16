package antimuckrush;

import battlecode.common.*;

public class Muckraker {
    //	public static final int[][] MUCKRAKER_SENSE = {{0, 0}, {-1, 0}, {0, -1}, {0, 1}, {1, 0}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}, {-2, 0}, {0, -2}, {0, 2}, {2, 0}, {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}, {-2, -2}, {-2, 2}, {2, -2}, {2, 2}, {-3, 0}, {0, -3}, {0, 3}, {3, 0}, {-3, -1}, {-3, 1}, {-1, -3}, {-1, 3}, {1, -3}, {1, 3}, {3, -1}, {3, 1}, {-3, -2}, {-3, 2}, {-2, -3}, {-2, 3}, {2, -3}, {2, 3}, {3, -2}, {3, 2}, {-4, 0}, {0, -4}, {0, 4}, {4, 0}, {-4, -1}, {-4, 1}, {-1, -4}, {-1, 4}, {1, -4}, {1, 4}, {4, -1}, {4, 1}, {-3, -3}, {-3, 3}, {3, -3}, {3, 3}, {-4, -2}, {-4, 2}, {-2, -4}, {-2, 4}, {2, -4}, {2, 4}, {4, -2}, {4, 2}, {-5, 0}, {-4, -3}, {-4, 3}, {-3, -4}, {-3, 4}, {0, -5}, {0, 5}, {3, -4}, {3, 4}, {4, -3}, {4, 3}, {5, 0}, {-5, -1}, {-5, 1}, {-1, -5}, {-1, 5}, {1, -5}, {1, 5}, {5, -1}, {5, 1}, {-5, -2}, {-5, 2}, {-2, -5}, {-2, 5}, {2, -5}, {2, 5}, {5, -2}, {5, 2}};
    public static RobotController robotController;

    public static void initialize(RobotController robotController) {
        Muckraker.robotController = robotController;
    }

    public static void processRound() throws GameActionException {
        //calculate flag before returning
        if (!robotController.isReady()) {
            int flag = 0;
            MapLocation myLocation = robotController.getLocation();
            RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam().opponent());
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

        //path find
        //essentially assign a cost to each of the directions using a heuristic
        //then move in the direction where the heuristic returns the smallest value
        {
            MapLocation[] locations = new MapLocation[9];
            double[] costs = new double[9];
            double INF = 1e8;
            for (int i = 8; --i >= 0; ) {
                final Direction dir = Direction.allDirections()[i];
                locations[i] = robotController.adjacentLocation(Direction.allDirections()[i]);
                costs[i] = robotController.canMove(dir) ? 1.6 / robotController.sensePassability(locations[i]) : INF;
            }
            locations[8] = robotController.getLocation();
            costs[8] = robotController.sensePassability(locations[8]);
            RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam().opponent());
            int id = 0, influence = -1;
            for (int i = nearbyRobots.length; --i >= 0; ) {
                RobotInfo robotInfo = nearbyRobots[i];
                MapLocation mapLocation = robotInfo.getLocation();
                switch (robotInfo.type) {
                    case ENLIGHTENMENT_CENTER:
                        for (int j = 9; --j >= 0; ) {
                            costs[j] += 100 * mapLocation.distanceSquaredTo(locations[j]);
                        }
                        break;
                    case SLANDERER:
                        if (locations[8].distanceSquaredTo(mapLocation) <= 12 && robotInfo.influence > influence) {
                            id = robotInfo.ID;
                            influence = robotInfo.influence;
                        }
                }
            }
            if (id != 0) {
                robotController.expose(id);
                return;
            }
            nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
            for (int i = nearbyRobots.length; --i >= 0; ) {
                RobotInfo robotInfo = nearbyRobots[i];
                MapLocation mapLocation = robotInfo.location;
                switch (robotInfo.type) {
                    case ENLIGHTENMENT_CENTER:
                        for (int j = 8; --j >= 0; ) {
                            costs[j] -= 2.3 * mapLocation.distanceSquaredTo(locations[j]);
                        }
                        break;
                    case MUCKRAKER:
                        int flag = robotController.getFlag(robotInfo.ID);
                        for (int j = 8; --j >= 0; ) {
                            costs[j] -= mapLocation.distanceSquaredTo(locations[j]) + flag;
                        }
                        break;
                }
            }
            //find next place to move
            Direction moveDirection = Direction.NORTH;
            double min = costs[0];
            for (int i = 9; --i > 0; ) {
                if (min > costs[i]) {
                    min = costs[i];
                    moveDirection = Direction.allDirections()[i];
                }
            }
            if (min < INF && !moveDirection.equals(Direction.CENTER)) {
                robotController.move(moveDirection);
            }
        }
    }
}