package antimuckrush;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class RobotPlayer {
    public static void run(RobotController robotController) throws GameActionException {
        switch (robotController.getType()) {
            case ENLIGHTENMENT_CENTER:
                EnlightenmentCenter.initialize(robotController);
                while (true) {
                    try {
                        EnlightenmentCenter.processRound();
                        Clock.yield();
                    } catch (Exception e) {
                        System.out.println(robotController.getType() + " Exception");
                        e.printStackTrace();
                    }
                }
            case SLANDERER:
                Slanderer.initialize(robotController);
                while (robotController.getType() == RobotType.SLANDERER) {
                    try {
                        Slanderer.processRound();
                        Clock.yield();
                    } catch (Exception e) {
                        System.out.println(robotController.getType() + " Exception");
                        e.printStackTrace();
                    }
                }
            case POLITICIAN:
                Politician.initialize(robotController);
                while (true) {
                    try {
                        Politician.processRound();
                        Clock.yield();
                    } catch (Exception e) {
                        System.out.println(robotController.getType() + " Exception");
                        e.printStackTrace();
                    }
                }
            case MUCKRAKER:
                Muckraker.initialize(robotController);
                while (true) {
                    try {
                        Muckraker.processRound();
                        Clock.yield();
                    } catch (Exception e) {
                        System.out.println(robotController.getType() + " Exception");
                        e.printStackTrace();
                    }
                }
            default:
                throw new IllegalArgumentException("Unexpected robot type");
        }
    }
}
