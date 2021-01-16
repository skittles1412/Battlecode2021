package antimuckrush;

import battlecode.common.*;

public class Politician {
    public static RobotController robotController;
    private static MapLocation startLocation;
    private static int ecID;

    public static void initialize(RobotController robotController) {
        Politician.robotController = robotController;
        RobotInfo[] myRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
        for (int i=myRobots.length; --i >= 0; ) {
            if (myRobots[i].getType() == RobotType.ENLIGHTENMENT_CENTER) {
                startLocation = myRobots[i].getLocation();
                ecID = myRobots[i].getID();
                break;
            }
        }
    }

    public static void processRound() throws GameActionException {
        MapLocation curLocation = robotController.getLocation();
        final Team myTeam = robotController.getTeam(), opponent = robotController.getTeam().opponent();
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(curLocation, -1, opponent);
        //calculate flag before returning
        // set last 8 bits for each direction that an enemy is spotted
        if (!robotController.isReady()) {
            int flag = 0;
            for (int i = enemyRobots.length; --i >= 0; ) {
                flag |= 1 << indexOfDirections(curLocation.directionTo(enemyRobots[i].getLocation()));
            }
            robotController.setFlag(flag);
            return;
        }

        int ecFlag = robotController.getFlag(ecID);
        int bubbleSize = ecFlag >> 18;

        for (int i=0; i<enemyRobots.length; i++) { // need to go in order
            int dist = curLocation.distanceSquaredTo(enemyRobots[i].getLocation());
            if (startLocation.distanceSquaredTo(enemyRobots[i].getLocation()) > bubbleSize + 16) continue;
            if (dist <= 2) {
                robotController.empower(dist);
                return;
            } else {
                if (dist <= 9 && enemyRobots[i].getType() == RobotType.MUCKRAKER) {
                    RobotInfo[] inDanger = robotController.senseNearbyRobots(enemyRobots[i].getLocation(), 30, myTeam);
                    for (int j=inDanger.length; --j >= 0; ) {
                        if (inDanger[j].getType() == RobotType.SLANDERER) {
                            robotController.empower(dist);
                            return;
                        }
                    }
                }
                Direction dir = robotController.getLocation().directionTo(enemyRobots[i].getLocation());
                if (robotController.canMove(dir)) {
                    robotController.move(dir);
                    return;
                }
            }
        }

        double INF = 1e8;
        Direction dir = Direction.NORTH;
        double min = INF;

        boolean moveInwards = curLocation.distanceSquaredTo(startLocation) > bubbleSize;
        Direction inwards = curLocation.directionTo(startLocation);
        for (int i = 8; --i >= 0; ) {
            final Direction cur = Direction.allDirections()[i];
            MapLocation location = robotController.adjacentLocation(Direction.allDirections()[i]);
            double cost = robotController.canMove(cur) ? 1.6 / robotController.sensePassability(location) : INF;
            cost += FastRandom.nextInt(50) / 10.0;

            if (min > cost && (!moveInwards || cur.equals(inwards) || cur.equals(inwards.rotateLeft()) || cur.equals(inwards.rotateRight()))) {
                min = cost;
                dir = cur;
            }
        }

        if (min < INF) {
            robotController.move(dir);
        }
    }

    private static int indexOfDirections(Direction dir) {
        for (int i=9; --i>=0;) {
            if (Direction.allDirections()[i].equals(dir)) return i;
        }
        return -1;
    }
}
