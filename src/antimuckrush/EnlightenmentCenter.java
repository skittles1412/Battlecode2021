package antimuckrush;

import battlecode.common.*;
import sprint_0.FastRandom;

import java.util.Arrays;
import java.util.Comparator;

public class EnlightenmentCenter {
    public static int vote = 2, lastVoteCount;
    public static boolean voted = false;
    public static RobotController robotController;
    public static final Direction[] sortedDirections = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
    private static int lastPoliSpawn = 0;

    public static void initialize(RobotController robotController) {
        EnlightenmentCenter.robotController = robotController;
        double[] val = new double[8];
        MapLocation myLocation = robotController.getLocation();
        for (int i = 8; --i >= 0; ) {
            try {
                val[i] = robotController.sensePassability(myLocation.add(sortedDirections[i]));
            } catch (GameActionException ignored) {
            }
        }
        Arrays.sort(sortedDirections, Comparator.comparingDouble(o -> val[o.ordinal()]));
    }

    public static void processRound() throws GameActionException {
        RobotInfo[] nearbyRobots = robotController.senseNearbyRobots(-1, robotController.getTeam().opponent());
        if (nearbyRobots.length >= 8 && robotController.getRoundNum() - lastPoliSpawn >= 1 + (nearbyRobots.length / 8)) {
            lastPoliSpawn = robotController.getRoundNum();

            int influenceSum = 0;
            for (int i = nearbyRobots.length; --i >= 0; ) {
                influenceSum += nearbyRobots[i].conviction + 1;
            }
            build(RobotType.POLITICIAN, influenceSum + 10);
        } else {
            build(RobotType.MUCKRAKER, 1);
        }
        vote();
    }

    /**
     * returns the id of the robot built or 0 if none were built
     */
    private static int build(RobotType type, int influence) throws GameActionException {
        //assume influence <= my influence
        if (robotController.isReady()) {
            for (int i = 8; --i >= 0; ) {
                Direction direction = sortedDirections[i];
                if (robotController.canBuildRobot(type, direction, influence)) {
                    robotController.buildRobot(type, direction, influence);
                    return robotController.senseRobotAtLocation(robotController.getLocation().add(direction)).ID;
                }
            }
        }
        return 0;
    }

    private static void vote() throws GameActionException {
        if (voted && robotController.getTeamVotes() == lastVoteCount) {
            vote += 2;
        }
        if (vote > 2 && FastRandom.nextInt(15) == 0) {
            vote -= 2;
        }
        build(RobotType.MUCKRAKER, 1);
        voted = false;
        if (robotController.getInfluence() >= Math.max(50, vote) && robotController.getTeamVotes() < 750
            /*&&FastRandom.nextInt(1500-robotController.getRoundNum())<(750-robotController.getTeamVotes())/0.7*/) {
            voted = true;
            lastVoteCount = robotController.getTeamVotes();
            robotController.bid(vote);
        }
    }
}
