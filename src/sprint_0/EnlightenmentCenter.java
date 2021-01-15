package sprint_0;

import battlecode.common.*;

import java.util.*;

import static sprint_0.Communications.*;

public class EnlightenmentCenter {
	public static int vote = 2, lastVoteCount, lastSelfEmpower = 7, spawnInd = 0, nextFlag = 0;
	public static int[] spawned;
	public static boolean voted = false;
	public static RobotController robotController;
	public static HashSet<MapLocation> processedECs;
	public static TreeMap<MapLocation, Integer> toProcessECs;
	public static MapLocation myLocation;
	public static final Direction[] CARDINAL_DIRECTIONS = Direction.cardinalDirections();
	public static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
	public static void initialize(RobotController robotController) {
		EnlightenmentCenter.robotController = robotController;
		spawned = new int[750];
		double[] val = new double[8];
		myLocation = robotController.getLocation();
		processedECs = new HashSet<>();
		toProcessECs = new TreeMap<>(Comparator.comparingInt(o -> myLocation.distanceSquaredTo(o)));
		for(int i = 8; --i>=0; ) {
			try {
				val[i] = robotController.sensePassability(myLocation.add(DIRECTIONS[i]));
			}catch(GameActionException ignored) {
			}
		}
		Arrays.sort(DIRECTIONS, Comparator.comparingDouble(o -> val[o.ordinal()]));
		Arrays.sort(CARDINAL_DIRECTIONS, Comparator.comparingDouble(o -> val[o.ordinal()]));
	}

	public static void processRound() throws GameActionException {
		robotController.setFlag(nextFlag);
		nextFlag = 0;
		//process comms
		//TODO: bytecode optimize this currently takes 27k bytecode worst case
		if(!robotController.isReady()) {
			for(int i = 0; i<spawnInd; i++) {
				int cur = spawned[i];
				if(cur!=0) {
					if(robotController.canGetFlag(cur)) {
						int flag = robotController.getFlag(cur);
						int prefix = decodePrefix(flag);
						if(prefix!=0) {
							int influence = prefix-512;
							if(influence<=300) {
								MapLocation location = decodeLocation(myLocation, flag);
								if(!processedECs.contains(location)) {
									toProcessECs.put(location, influence);
								}
							}
						}
					}else {
						spawned[i] = 0;
					}
				}
			}
		}
		//process spawning
		if(robotController.isReady()) {
			if(robotController.getInfluence()>=411&&!toProcessECs.isEmpty()) {//attack ec
				Map.Entry<MapLocation, Integer> target = toProcessECs.firstEntry();
				Direction buildDirection;
				if((buildDirection = build(DIRECTIONS, RobotType.POLITICIAN, target.getValue()+11))!=null) {
					nextFlag = encodePrefix(buildDirection.ordinal()+1, encodeLocation(target.getKey()));
					toProcessECs.pollFirstEntry();
					processedECs.add(target.getKey());
				}
			}else if(++lastSelfEmpower>=10&&robotController.getInfluence()<=9e7&&
					(robotController.getInfluence()/2-10)*(robotController.getEmpowerFactor(robotController.getTeam(), 11)-1)>=25) {//self empower
				Direction buildDirection;
				if((buildDirection = build(CARDINAL_DIRECTIONS, RobotType.POLITICIAN, robotController.getInfluence()/2))!=null) {
					lastSelfEmpower = 0;
					nextFlag = encodePrefix(buildDirection.ordinal()+1, encodeLocation(myLocation));
				}
			}else {
				build(DIRECTIONS, RobotType.MUCKRAKER, 1);
			}
		}
		vote();
	}

	/**
	 * returns the direction of the robot built or null if none were built
	 */
	private static Direction build(Direction[] directions, RobotType type, int influence) throws GameActionException {
		//assume influence <= myInfluence and cooldown < 1
		for(int i = directions.length; --i>=0; ) {
			Direction direction = directions[i];
			if(robotController.canBuildRobot(type, direction, influence)) {
				robotController.buildRobot(type, direction, influence);
				spawned[spawnInd++] = robotController.senseRobotAtLocation(myLocation.add(direction)).ID;
				return direction;
			}
		}
		return null;
	}

	private static void vote() throws GameActionException {
		if(voted&&robotController.getTeamVotes()==lastVoteCount) {
			vote += 2;
		}
		if(vote>2&&FastRandom.nextInt(15)==0) {
			vote -= 2;
		}
		voted = false;
		if(robotController.getInfluence()>=Math.max(400, vote)&&robotController.getTeamVotes()<750
			/*&&FastRandom.nextInt(1500-robotController.getRoundNum())<(750-robotController.getTeamVotes())/0.7*/) {
			voted = true;
			lastVoteCount = robotController.getTeamVotes();
			robotController.bid(vote);
		}
	}
}
