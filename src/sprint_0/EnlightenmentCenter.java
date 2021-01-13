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
	public static final Direction[] sortedDirections = Direction.values();

	public static void initialize(RobotController robotController) {
		EnlightenmentCenter.robotController = robotController;
		Arrays.sort(sortedDirections, Comparator.comparingDouble(o -> {
			try {
				// -x and 1/x will still create the same ordering
				return -robotController.sensePassability(robotController.getLocation().add(o));
			}catch(GameActionException e) {
				throw new RuntimeException(e);
			}
		}));
	}

	public static void processRound() throws GameActionException {
		if(voted&&robotController.getTeamVotes()==lastVoteCount) {
			vote += 2;
		}
		if(vote>2&& FastRandom.nextInt(15)==0) {
			vote -= 2;
		}
		build(RobotType.MUCKRAKER, 1);
		voted = false;
		if(robotController.getInfluence()>=Math.max(50, vote)
				&&FastRandom.nextInt(1500-robotController.getRoundNum())<(750-robotController.getTeamVotes())/0.7) {
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
			for(Direction direction: sortedDirections) {
				if(robotController.canBuildRobot(type, direction, influence)) {
					robotController.buildRobot(type, direction, influence);
					return robotController.senseRobotAtLocation(robotController.getLocation().add(direction)).ID;
				}
			}
		}
		return 0;
	}
}
