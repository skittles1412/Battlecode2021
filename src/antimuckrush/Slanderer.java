package antimuckrush;

import battlecode.common.*;

import java.util.Arrays;

public class Slanderer {
    public static RobotController robotController;
    private static MapLocation startLocation;

    public static void initialize(RobotController robotController) {
        Slanderer.robotController = robotController;
        RobotInfo[] myRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
        for (int i=myRobots.length; --i >= 0; ) {
            if (myRobots[i].getType() == RobotType.ENLIGHTENMENT_CENTER) {
                startLocation = myRobots[i].getLocation();
                break;
            }
        }
    }

    public static void processRound() throws GameActionException {
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam().opponent());
        RobotInfo[] myRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
        MapLocation curLocation = robotController.getLocation();
        if (!robotController.isReady()) {
            int flag = robotController.getFlag(robotController.getID());
            for (int i = enemyRobots.length; --i >= 0; ) {
                flag |= 1 << indexOfDirections(curLocation.directionTo(enemyRobots[i].getLocation()));
            }
            for (int i = myRobots.length; --i >= 0; ) {
                flag |= robotController.getFlag(myRobots[i].getID());
            }
            robotController.setFlag(flag);
            return;
        }

        int[] dirScore = new int[8];
//        for (int i = enemyRobots.length; --i >= 0; ) {
//            if (enemyRobots[i].getType() != RobotType.MUCKRAKER) continue;
//
//            Direction dir = curLocation.directionTo(enemyRobots[i].getLocation());
//            for (int j = 8; --j >= 0; ) {
//                if (dir.equals(Direction.allDirections()[j])) {
//                    dirScore[j] = 2;
//                } else if (dir.rotateLeft().equals(Direction.allDirections()[j])) {
//                    dirScore[j] = 2;
//                } else if (dir.rotateRight().equals(Direction.allDirections()[j])) {
//                    dirScore[j] = 2;
//                } else if (dir.rotateLeft().rotateLeft().equals(Direction.allDirections()[j])) {
//                    dirScore[j] = Math.max(dirScore[j], 1);
//                } else if (dir.rotateRight().rotateRight().equals(Direction.allDirections()[j])) {
//                    dirScore[j] = Math.max(dirScore[j], 1);
//                }
//            }
//        }
//        for (int i = myRobots.length; --i >= 0; ) {
//            switch (myRobots[i].getType()) {
//                case MUCKRAKER:
//                    break;
//                default:
//                    if (robotController.canGetFlag(myRobots[i].getID())) {
//                        int flag = robotController.getFlag(myRobots[i].getID());
//                        for (int j = 8; --j >= 0; ) {
//                            if ((flag & (1 << j)) > 0) {
//                                dirScore[(j + 6) % 8] = Math.max(dirScore[(j + 6) % 8], 1);
//                                dirScore[(j + 7) % 8] = 2;
//                                dirScore[j % 8] = 2;
//                                dirScore[(j + 1) % 8] = 2;
//                                dirScore[(j + 2) % 8] = Math.max(dirScore[(j + 2) % 8], 1);
//                            }
//                        }
//                    }
//            }
//        }

        if (enemyRobots.length > 0 || FastRandom.nextInt(4) == 0) {
            double INF = 1e8;
//            int minScore = 10;
            boolean moveInwards = curLocation.distanceSquaredTo(startLocation) > 4;
            Direction inwards = curLocation.directionTo(startLocation);
            Direction moveDirection = Direction.CENTER;
            double min = INF;
            for (int i = 8; --i >= 0; ) {
                final Direction cur = Direction.allDirections()[i];
                if (robotController.canMove(cur)) {
                    MapLocation location = robotController.adjacentLocation(Direction.allDirections()[i]);
                    final double cost = (1.6 / robotController.sensePassability(location)) + FastRandom.nextInt(50) / 10.0;
                    if (min > cost && (!moveInwards || cur.equals(inwards) || cur.equals(inwards.rotateLeft()) || cur.equals(inwards.rotateRight()))) {
                        min = cost;
                        moveDirection = cur;
                    }
                }
            }
            if (min + 1 < INF) {
                robotController.move(moveDirection);
            }
        }
    }

    private static int indexOfDirections(Direction dir) {
        for (int i = 9; --i >= 0; ) {
            if (Direction.allDirections()[i].equals(dir)) return i;
        }
        return -1;
    }
}
