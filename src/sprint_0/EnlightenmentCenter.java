package sprint_0;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;

import static sprint_0.Constants.*;

public class EnlightenmentCenter {
	public static int ind = 0;
	public static RobotController robotController;
	public static final Direction[] sortedDirections = Direction.values();

	public static void initialize(RobotController robotController) {
		EnlightenmentCenter.robotController = robotController;
		Arrays.sort(sortedDirections, Comparator.comparingDouble(o -> {
			try {
				// -x and 1/x will still create the same ordering
				return -robotController.sensePassability(robotController.getLocation().add(o));
			} catch(GameActionException e) {
				throw new RuntimeException(e);
			}
		}));
	}

	public static void processRound() throws GameActionException {
		build(RobotType.MUCKRAKER, 1);
		if (robotController.getInfluence() >= 10) {
			robotController.bid(2);
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
