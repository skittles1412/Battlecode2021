package sprint_0;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;

import static sprint_0.Constants.*;

public class EnlightenmentCenter implements Robot {
	private int ind = 0;
	private final int[] built;
	private final RobotController robotController;

	public EnlightenmentCenter(RobotController robotController) {
		this.robotController = robotController;
		built = new int[1500];
		System.out.println(Clock.getBytecodeNum());
		Arrays.sort(DIRECTIONS, Comparator.comparingDouble(o-> {
			try {
				return 1/robotController.sensePassability(robotController.getLocation().add(o));
			}catch(GameActionException e) {
				throw new RuntimeException(e);
			}
		}));
		System.out.println(Clock.getBytecodeNum());
	}

	@Override
	public void processRound() throws GameActionException {
		build(RobotType.MUCKRAKER, 1);
	}

	/**
	 * returns the id of the robot built or 0 if none were built
	 */
	private int build(RobotType type, int influence) throws GameActionException {
		//assume influence <= my influence
		if(robotController.isReady()) {
			for(Direction direction: DIRECTIONS) {
				if(robotController.canBuildRobot(type, direction, influence)) {
					robotController.buildRobot(type, direction, influence);
					return built[ind++] = robotController.senseRobotAtLocation(robotController.getLocation().add(direction)).ID;
				}
			}
		}
		return 0;
	}
}
