package sprint_0;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;

public class EnlightenmentCenter {
	public static int vote = 2, lastVoteCount, lastSelfEmpower = 7;
	public static boolean voted = false;
	public static RobotController robotController;
	public static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
	public static final Direction[] CARDINAL_DIRECTIONS = Direction.cardinalDirections();
	public static void initialize(RobotController robotController) {
		EnlightenmentCenter.robotController = robotController;
		double[] val = new double[8];
		MapLocation myLocation = robotController.getLocation();
		for(int i = 8; --i>=0; ) {
			try {
				val[i] = robotController.sensePassability(myLocation.add(DIRECTIONS[i]));
			}catch(GameActionException ignored) {
			}
		}
		Arrays.sort(DIRECTIONS, Comparator.comparingDouble(o -> {
			return val[o.ordinal()];
		}));
		Arrays.sort(CARDINAL_DIRECTIONS, Comparator.comparingDouble(o -> {
			return val[o.ordinal()];
		}));
	}

	public static void processRound() throws GameActionException {
		if(voted&&robotController.getTeamVotes()==lastVoteCount) {
			vote += 2;
		}
		if(vote>2&&FastRandom.nextInt(15)==0) {
			vote -= 2;
		}
		if(++lastSelfEmpower>=10&&
				(robotController.getInfluence()/2-10)*(robotController.getEmpowerFactor(robotController.getTeam(), 11)-1)>=25) {
//			System.out.println(robotController.getRoundNum());
//			System.out.println(robotController.getEmpowerFactor(robotController.getTeam(), 11));
			if(build(CARDINAL_DIRECTIONS, RobotType.POLITICIAN, robotController.getInfluence()/2)!=0) {
				lastSelfEmpower = 0;
			}
		}else {
			build(DIRECTIONS, RobotType.MUCKRAKER, 1);
		}
		voted = false;
		if(robotController.getInfluence()>=Math.max(150, vote)&&robotController.getTeamVotes()<750
			/*&&FastRandom.nextInt(1500-robotController.getRoundNum())<(750-robotController.getTeamVotes())/0.7*/) {
			voted = true;
			lastVoteCount = robotController.getTeamVotes();
			robotController.bid(vote);
		}
	}

	/**
	 * returns the id of the robot built or 0 if none were built
	 */
	private static int build(Direction[] directions, RobotType type, int influence) throws GameActionException {
		//assume influence <= my influence
		if(robotController.isReady()) {
			for(int i = directions.length; --i>=0; ) {
				Direction direction = directions[i];
				if(robotController.canBuildRobot(type, direction, influence)) {
					robotController.buildRobot(type, direction, influence);
					return robotController.senseRobotAtLocation(robotController.getLocation().add(direction)).ID;
				}
			}
		}
		return 0;
	}
}
