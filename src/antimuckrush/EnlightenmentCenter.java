package antimuckrush;

import battlecode.common.*;
import utilities.FastRandom;

import java.util.Arrays;
import java.util.Comparator;

public class EnlightenmentCenter {
	public static int vote = 2, lastVoteCount;
	public static boolean voted = false;
	public static RobotController robotController;
	public static final Direction[] sortedDirections = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
	private static int lastPoliSpawn = 0;

	public static void initialize(RobotController robotController) throws GameActionException {
		EnlightenmentCenter.robotController = robotController;
		double[] val = new double[8];
		MapLocation myLocation = robotController.getLocation();
		for(int i = 8; --i>=0; ) {
			try {
				val[i] = robotController.sensePassability(myLocation.add(sortedDirections[i]));
			}catch(GameActionException ignored) {
			}
		}
		Arrays.sort(sortedDirections, Comparator.comparingDouble(o -> val[o.ordinal()]));
		robotController.setFlag(50<<18);
	}

	public static void processRound() throws GameActionException {
		MapLocation myLocation = robotController.getLocation();
		RobotInfo[] enemyRobots = robotController.senseNearbyRobots(myLocation, -1, robotController.getTeam().opponent());

		if(enemyRobots.length>20) {
			robotController.resign();
			return;
		}

		RobotInfo[] myRobots = robotController.senseNearbyRobots(-1, robotController.getTeam());
		int poliCount = 0, slandererCount = 0;
		int flag = robotController.getFlag(robotController.getID());
		for(int i = myRobots.length; --i>=0; ) {
			if(myRobots[i].getType()==RobotType.POLITICIAN) poliCount++;
			else if(myRobots[i].getType()==RobotType.SLANDERER) slandererCount++;
			flag |= robotController.getFlag(myRobots[i].getID());
		}
		flag &= (1<<18)-1;
		final int bubbleSize = Math.max(32, poliCount);
		flag |= bubbleSize<<18;
		robotController.setFlag(flag);
		int nearestEnemy = enemyRobots.length>0 ? myLocation.distanceSquaredTo(enemyRobots[0].getLocation()) : 1001001;
//        int avgEnemy = 10;
//        if (enemyRobots.length > 0) {
//            avgEnemy = 0;
//            for (int i = enemyRobots.length; --i >= 0;) {
//                avgEnemy += enemyRobots[i].getConviction();
//            }
//            avgEnemy /= enemyRobots.length;
//            avgEnemy++;
//        }
//        if (muckCount < 6 && build(RobotType.MUCKRAKER, 1) > 0) {
//            muckCount++;
//        } else
		if(robotController.getRoundNum()<=8&&nearestEnemy>bubbleSize) {
			build(RobotType.SLANDERER, 63);
		}else if(poliCount<8) {
			build(RobotType.POLITICIAN, 16);
		}else if(slandererCount<Math.sqrt(bubbleSize)&&nearestEnemy>bubbleSize) {
			build(RobotType.SLANDERER, 63);
		}else {
			build(RobotType.POLITICIAN, 16);

//            if (enemyRobots.length >= 8 && robotController.getRoundNum() - lastPoliSpawn >= 1 + (enemyRobots.length / 8)) {
//                lastPoliSpawn = robotController.getRoundNum();
//
//                int influenceSum = 0;
//                for (int i = enemyRobots.length; --i >= 0; ) {
//                    influenceSum += enemyRobots[i].conviction + 1;
//                }
//                build(RobotType.POLITICIAN, influenceSum + 10);
//            } else if (20 <= robotController.getRoundNum() && robotController.getRoundNum() <= 200) {
//                build(RobotType.MUCKRAKER, 1);
//            } else {
//                build(RobotType.SLANDERER, 63);
//            }
		}
		if(robotController.getRoundNum()>=150||(robotController.getRoundNum()>=50&&robotController.getRoundNum()%10==0)) {
			vote();
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

	private static void vote() throws GameActionException {
		if(voted&&robotController.getTeamVotes()==lastVoteCount) {
			vote += 2;
		}
		if(vote>2&&FastRandom.nextInt(15)==0) {
			vote -= 2;
		}
		voted = false;
		if(robotController.getInfluence()>=Math.max(350, vote)&&robotController.getTeamVotes()<750
			/*&&FastRandom.nextInt(1500-robotController.getRoundNum())<(750-robotController.getTeamVotes())/0.7*/) {
			voted = true;
			lastVoteCount = robotController.getTeamVotes();
			robotController.bid(vote);
		}
	}
}
