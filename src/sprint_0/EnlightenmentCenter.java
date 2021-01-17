package sprint_0;

import battlecode.common.*;

import java.util.*;

import static sprint_0.Communications.*;

public class EnlightenmentCenter {
	public static int vote = 2, lastVoteCount, lastVoteRound = -1, lastCleaned = 0, selfEmpowerID = 0, spawnInd = 0, nextFlag = 0;
	public static int[] spawned;
	public static boolean voted = false;
	public static RobotController robotController;
	public static HashMap<MapLocation, Integer> processedECs, toProcessECs;
	public static MapLocation myLocation;
	public static final Direction[] CARDINAL_DIRECTIONS = Direction.cardinalDirections();
	public static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
	public static void initialize(RobotController robotController) throws GameActionException {
		EnlightenmentCenter.robotController = robotController;
		spawned = new int[750];
		double[] val = new double[8];
		myLocation = robotController.getLocation();
		processedECs = new HashMap<>();
		toProcessECs = new HashMap<>();
		for(int i = 8; --i>=0; ) {
			MapLocation newLocation = myLocation.add(DIRECTIONS[i]);
			if(robotController.onTheMap(newLocation)) {
				val[i] = robotController.sensePassability(newLocation);
			}
		}
		Arrays.sort(DIRECTIONS, Comparator.comparingDouble(o -> val[o.ordinal()]));
		Arrays.sort(CARDINAL_DIRECTIONS, Comparator.comparingDouble(o -> val[o.ordinal()]));
	}

	public static void processRound() throws GameActionException {
		robotController.setFlag(nextFlag);
		nextFlag = 0;
		vote();
		//process comms
		//TODO: bytecode optimize this currently takes 27k bytecode worst case
		if(!robotController.isReady()) {
			if(robotController.getRoundNum()-lastCleaned>=100) {//update spawned and remove nonexistent robots
				int nSpawnInd = 0;
				int[] newSpawned = new int[750];
				for(int i = 0; i<spawnInd; i++) {
					if(robotController.canGetFlag(spawned[i])) {
						int flag = robotController.getFlag(newSpawned[nSpawnInd++] = spawned[i]);
						int prefix = decodePrefix(flag);
						if(prefix!=0) {
							int influence = prefix-512;
							if(influence<=300) {
								MapLocation location = decodeLocation(myLocation, flag);
								Integer assignee = processedECs.get(location);
								if(assignee==null) {
									toProcessECs.put(location, influence);
								}else if(!robotController.canGetFlag(assignee)) {
									processedECs.remove(location);
									toProcessECs.put(location, influence);
								}
							}
						}
					}
				}
				spawned = newSpawned;
				spawnInd = nSpawnInd;
				lastCleaned = robotController.getRoundNum();
			}else {
				for(int i = 0; i<spawnInd; i++) {
					if(robotController.canGetFlag(spawned[i])) {
						int flag = robotController.getFlag(spawned[i]);
						int prefix = decodePrefix(flag);
						if(prefix!=0) {
							int influence = prefix-512;
							if(influence<=300) {
								MapLocation location = decodeLocation(myLocation, flag);
								Integer assignee = processedECs.get(location);
								if(assignee==null) {
									toProcessECs.put(location, influence);
								}else if(!robotController.canGetFlag(assignee)) {
									processedECs.remove(location);
									toProcessECs.put(location, influence);
								}
							}
						}
					}
				}
			}
		}
		vote();
		//process spawning
		//TODO: Don't spawn in future self empowering places
		if(robotController.isReady()) {
			if(!attackEC()) {
				if(!robotController.canGetFlag(selfEmpowerID)&&robotController.getInfluence()<=9e7&&
						(robotController.getInfluence()/2-10)*(robotController.getEmpowerFactor(robotController.getTeam(), 11)-1)>=25) {//self empower
					Direction buildDirection;
					if((buildDirection = build(CARDINAL_DIRECTIONS, RobotType.POLITICIAN, robotController.getInfluence()/2))!=null) {
						selfEmpowerID = spawned[spawnInd-1];
						nextFlag = encodePrefix(buildDirection.ordinal()+1, encodeLocation(myLocation));
					}
				}else {
					build(DIRECTIONS, RobotType.MUCKRAKER, 1);
				}
			}
		}
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
		if(robotController.getRoundNum()!=lastVoteRound) {
			if(voted&&robotController.getTeamVotes()==lastVoteCount) {
				vote += 2;
			}
			if(vote>2&&FastRandom.nextInt(15)==0) {
				vote -= 2;
			}
			vote = Math.max(vote, robotController.getInfluence()/75000);
			voted = false;
			if(robotController.getTeamVotes()<=750) {
				if(robotController.getInfluence()>=Math.max(350, vote)
					/*&&FastRandom.nextInt(1500-robotController.getRoundNum())<(750-robotController.getTeamVotes())/0.7*/) {
					voted = true;
					lastVoteCount = robotController.getTeamVotes();
					robotController.bid(vote);
				}else if(robotController.getInfluence()>=100) {
					robotController.bid(1);
				}
			}
			lastVoteRound = robotController.getRoundNum();
		}
	}

	/**
	 * Returns true if a politician was built to attack an EC
	 */
	private static boolean attackEC() throws GameActionException {
		if(!toProcessECs.isEmpty()) {
			MapLocation location = null;
			int influence = Integer.MAX_VALUE;
			for(Map.Entry<MapLocation, Integer> target: toProcessECs.entrySet()) {
				if(target.getValue()<influence) {
					location = target.getKey();
					influence = target.getValue();
				}
			}
			int spawn = influence+11;
			if(robotController.getInfluence()-spawn>=100) {
				Direction buildDirection;
				if((buildDirection = build(DIRECTIONS, RobotType.POLITICIAN, spawn))!=null) {
					nextFlag = encodePrefix(buildDirection.ordinal()+1, encodeLocation(location));
					toProcessECs.remove(location);
					processedECs.put(location, spawned[spawnInd-1]);
					return true;
				}
			}
		}
		return false;
	}
}
