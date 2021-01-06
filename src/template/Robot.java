package template;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public interface Robot {
	void processRound() throws GameActionException;
}
