package sprint_0;

import battlecode.common.*;

import static sprint_0.Constants.*;

public class EnlightenmentCenter implements Robot {
	private final RobotController robotController;

	public EnlightenmentCenter(RobotController robotController) {
		this.robotController = robotController;
	}

	@Override
	public void processRound() throws GameActionException {
		build(RobotType.MUCKRAKER, 1);
	}

	private void build(RobotType type, int influence) throws GameActionException {
		//assume influence <= my influence
		if(robotController.isReady()) {
			for(Direction direction: DIRECTIONS) {
				if(robotController.canBuildRobot(type, direction, influence)) {
					robotController.buildRobot(type, direction, influence);
					return;
				}
			}
		}
	}
}
