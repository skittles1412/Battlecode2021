package sprint_0;

import battlecode.common.*;

import static sprint_0.Constants.*;

public class Politician {
	public static int tries = 0;
	public static RobotController robotController;

	public static void initialize(RobotController robotController) {
		Politician.robotController = robotController;
	}

	public static void processRound() throws GameActionException {
		//TODO: Implement more logic later (slanderers converting, attacking neutral ECs, listening to an EC, etc)
		if(!robotController.isReady()) {
			if(robotController.getCooldownTurns()<=2) {
				robotController.setFlag(1);
			}
			return;
		}
		//blow up on the 5th try
		if(++tries==5||robotController.senseNearbyRobots(1, robotController.getTeam()).length>0) {
			robotController.empower(1);
		}
	}
}
