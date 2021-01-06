package template;

import battlecode.common.*;
import static template.Constants.*;

public class RobotPlayer {
	public static void run(RobotController robotController) throws GameActionException {
		Robot robot;
		switch(robotController.getType()) {
			case ENLIGHTENMENT_CENTER:
				robot = new EnlightenmentCenter(robotController);
				break;
			case POLITICIAN:
				robot = new Politician(robotController);
				break;
			case SLANDERER:
				robot = new Slanderer(robotController);
				break;
			case MUCKRAKER:
				robot = new Muckraker(robotController);
				break;
			default:
				throw new IllegalArgumentException("Unexpected robot type");
		}
		while(true) {
			try {
				robot.processRound();
				Clock.yield();
			}catch(Exception e) {
				System.out.println(robotController.getType()+" Exception");
				e.printStackTrace();
			}
		}
	}
}
