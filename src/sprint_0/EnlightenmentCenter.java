package sprint_0;

import battlecode.common.*;
import sprint_0.FastRandom;

import java.util.Arrays;
import java.util.Comparator;

import static sprint_0.Constants.*;

public class EnlightenmentCenter {
	public static int vote = 2, lastVoteCount;
	public static boolean voted = false;
	public static RobotController robotController;
	public static final Direction[] sortedDirections = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};

	public static void initialize(RobotController robotController) {
		EnlightenmentCenter.robotController = robotController;
		double[] val = new double[8];
		MapLocation myLocation = robotController.getLocation();
		for(int i = 8; --i>=0; ) {
			try {
				val[i] = robotController.sensePassability(myLocation.add(sortedDirections[i]));
			}catch(GameActionException ignored) {
			}
		}
		Arrays.sort(sortedDirections, Comparator.comparingDouble(o -> {
			return val[o.ordinal()];
		}));
	}

	public static void processRound() throws GameActionException {
		if(voted&&robotController.getTeamVotes()==lastVoteCount) {
			vote += 2;
		}
		if(vote>2&&FastRandom.nextInt(15)==0) {
			vote -= 2;
		}
		build(RobotType.MUCKRAKER, 1);
		voted = false;
		if(robotController.getInfluence()>=Math.max(150, vote)&&robotController.getTeamVotes()<750
			/*&&FastRandom.nextInt(1500-robotController.getRoundNum())<(750-robotController.getTeamVotes())/0.7*/) {
			voted = true;
			lastVoteCount = robotController.getTeamVotes();
			robotController.bid(vote);
		}
	}

	/**
	 * returns the id of the robot built or 0 if none were built
	 */
	private static int build(RobotType type, int influence) throws GameActionException {
		//assume influence <= my influence
		if(robotController.isReady()) {
			for(int i = 8; --i>=0; ) {
				Direction direction = sortedDirections[i];
				if(robotController.canBuildRobot(type, direction, influence)) {
					robotController.buildRobot(type, direction, influence);
					return robotController.senseRobotAtLocation(robotController.getLocation().add(direction)).ID;
				}
			}
		}
		return 0;
	}
}
