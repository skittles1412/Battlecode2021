package sprint_0;

import battlecode.common.*;

import static sprint_0.Constants.*;

public class Muckraker implements Robot {
	private int enlightenmentCenterID;
	private final RobotController robotController;

	public Muckraker(RobotController robotController) {
		this.robotController = robotController;
		for(RobotInfo robotInfo: robotController.senseNearbyRobots(2)) {
			if(robotInfo.type==RobotType.ENLIGHTENMENT_CENTER) {
				if(robotInfo.team==robotController.getTeam()) {
					enlightenmentCenterID = robotInfo.ID;
				}else {
					enlightenmentCenterID = -1;
				}
				break;
			}
		}
	}

	@Override
	public void processRound() throws GameActionException {

	}
}
