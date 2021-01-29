package eco;

import battlecode.common.*;
import utilities.Communications;
import utilities.FastRandom;
import utilities.IntHashMap;
import utilities.Logger;

import static utilities.Communications.decodePrefix;
import static utilities.Communications.encodeLocation;

/**
 * <p><dt><strong>Bidding:</strong></dt>
 * <dd>Adaptive bidding. Increase bid by 2 on losses and decrease by 2 with 1/15 chance.
 * Only bid when >= 350 influence. Otherwise there's a 50% chance of choosing to bid
 * a random number from 1-3, and a 50% chance to not bid at all</dd></p>
 * <dd>The EC sends information to units it has just spawned.
 * The first four bits indicates the direction the unit which shall receive the command is.
 * An extra bit is used in order to differentiate with 0.
 * The last 20 bits describes the unit's command</dd></p>
 */
public class EnlightenmentCenter {
	//remove begin
	public static final boolean LOG = true;
	public static final Logger initializationLogger
			= new Logger(LOG ? 1 : 0, 2, 20000, "EC initialization");
	public static final Logger communicationLogger
			= new Logger(LOG ? 25 : 0, 2, 20000, "EC communication");
	public static final Logger voteLogger
			= new Logger(LOG ? 25 : 0, 2, 20000, "EC voting");
	public static final Logger attackLogger
			= new Logger(LOG ? 1 : 0, 2, 20000, "EC attacking");
	//remove end
	//to multiply by prefix
	public static final int EC_PREFIX_MUL = 1048576;//1<<20
	//optimal influence to spawn slanderers
	public static final int[] SLANDERER_INFLUENCE = {21, 41, 63, 85, 107, 130, 154, 178, 203, 228, 255, 282, 310, 339, 368, 399, 431, 463, 497, 532, 568, 605, 643, 683, 724, 766, 810, 855, 902, 949};
	//did I vote last round?
	public static boolean voted = false;
	//next vote, last amount of votes on my team, last time I tried to vote, how much influence should I have before voting
	public static int vote = 2, lastVoteCount, lastVoteRound = -1, minVoteInfluence = 600;
	//next build order
	public static int buildOrder = 0;
	//flag to set to next round
	public static int nextFlag = 0;
	//minimum amount of influence that I should have
	public static int minInfluence = 0;
	//last time I tried to clean up spawned, the pointer of spawned (used as an arraylist)
	public static int lastCleaned = 0, spawnInd = 0;
	public static int[] spawned;
	public static RobotController robotController;
	//attacked neutral ECs, to attack neutral ECs
	public static IntHashMap processedECs, toProcessECs;
	public static int myLocationFlag;
	public static MapLocation myLocation;
	//order to spawn robots
	public static Direction[] cardinalDirections = Direction.cardinalDirections();
	public static Direction[] directions = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};

	public static void initialize(RobotController robotController) throws GameActionException {
		EnlightenmentCenter.robotController = robotController;
		spawned = new int[750];
		myLocationFlag = encodeLocation(myLocation = robotController.getLocation());
		processedECs = new IntHashMap(17);
		toProcessECs = new IntHashMap(17);
		//sort build directions
		double[] val = new double[8];
		for(int i = 8; --i>=0; ) {
			MapLocation newLocation = myLocation.add(directions[i]);
			if(robotController.onTheMap(newLocation)) {
				val[i] = robotController.sensePassability(newLocation);
			}
		}
		for(int i = 7; --i>=0; ) {
			double cur = val[directions[i].ordinal()];
			for(int j = i; j<7&&cur>val[directions[j+1].ordinal()]; j++) {
				Direction tmp = directions[j];
				directions[j] = directions[j+1];
				directions[j+1] = tmp;
			}
		}
		for(int i = 3; --i>=0; ) {
			double cur = val[cardinalDirections[i].ordinal()];
			for(int j = i; j<3&&cur>val[cardinalDirections[j+1].ordinal()]; j++) {
				Direction tmp = cardinalDirections[j];
				cardinalDirections[j] = cardinalDirections[j+1];
				cardinalDirections[j+1] = tmp;
			}
		}
		initializationLogger.logBytecode(Clock.getBytecodeNum());//remove line
	}

	public static void processRound() throws GameActionException {
		robotController.setFlag(nextFlag);
		nextFlag = 0;
//		vote();
		//process comms
		if(!robotController.isReady()) {
			int round = robotController.getRoundNum();
			if(round<50) {
				minInfluence = 10;
			}else if(round<100) {
				minInfluence = 30;
			}else if(round<200) {
				minInfluence = 50;
			}else if(round<300) {
				minInfluence = 70;
			}else {
				minInfluence = 100;
			}
			if(round>=1430) {
				minVoteInfluence = 100;
			}
			int start = Clock.getBytecodeNum();//remove line
			if(robotController.getRoundNum()-lastCleaned>=100) {
				cleanUpCommunications();
				lastCleaned = robotController.getRoundNum();
			}else {
				processCommunications();
			}
			communicationLogger.logBytecode(round, robotController.getRoundNum(), start, Clock.getBytecodeNum());//remove line
		}
//		vote();
		boolean defend = false;
		for(RobotInfo robotInfo: robotController.senseNearbyRobots(-1, robotController.getTeam().opponent())) {
			if(robotInfo.type==RobotType.MUCKRAKER) {
				defend = true;
				break;
			}
		}
		//process spawning
		if(robotController.isReady()&&!attackEC()) {
			if(robotController.getRoundNum()<=35&&buildOrder%2!=0) {
				build(directions, RobotType.MUCKRAKER, 1);
				return;
			}
			//SPPSMSPPSPM
			if(buildOrder==11) {
				buildOrder = 1;
			}
			switch(buildOrder++) {
				case 0:
				case 2:
				case 5:
				case 8:
					if(buildSlanderer(defend)) {
						break;
					}
				case 1:
				case 3:
				case 6:
				case 7:
				case 9:
					if(buildPolitician(defend)) {
						break;
					}
				default:
					build(directions, RobotType.MUCKRAKER, 1);
			}
		}
	}

	/**
	 * Builds a robot with the specified type and influence using one of the
	 * supplied directions. The directions are processed in reverse order
	 *
	 * @return the direction of the robot built or null if none were built
	 */
	private static Direction build(Direction[] directions, RobotType type, int influence) throws GameActionException {
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

	/**
	 * @return the largest optimal slanderer influence less than or equal to
	 * the provided influence, or 0, if none exist
	 */
	private static int getSlandererInfluence(int influence) {
		int prev = 0;
		for(int i: SLANDERER_INFLUENCE) {
			if(i>influence) {
				return prev;
			}
			prev = i;
		}
		return prev;
	}

	/**
	 * @return true if a slanderer was built
	 */
	private static boolean buildSlanderer(boolean defend) throws GameActionException {
		if(defend) {
			return false;
		}
		build(directions, RobotType.SLANDERER, getSlandererInfluence(robotController.getInfluence()));
		return true;
	}

	/**
	 * @return true if a slanderer was built
	 */
	private static boolean buildPolitician(boolean defend) throws GameActionException {
		if(defend||FastRandom.nextInt(10)!=0) {
			return build(directions, RobotType.POLITICIAN, 20)!=null;
		}
		return build(directions, RobotType.POLITICIAN, 1)!=null;
	}

	private static void vote() throws GameActionException {
		if(robotController.getRoundNum()!=lastVoteRound) {
			int start = 0;//remove line
			if(voted&&robotController.getTeamVotes()==lastVoteCount) {
				vote += 2;
			}
			if(vote>2&&FastRandom.nextInt(15)==0) {
				vote -= 2;
			}
			vote = Math.max(vote, robotController.getInfluence()/75000);
			voted = false;
			if(robotController.getTeamVotes()<=750) {
				if(robotController.getInfluence()>=Math.max(minVoteInfluence, vote)
					/*&&FastRandom.nextInt(1500-robotController.getRoundNum())<(750-robotController.getTeamVotes())/0.7*/) {
					voted = true;
					lastVoteCount = robotController.getTeamVotes();
					robotController.bid(vote);
				}else if(robotController.getInfluence()>=minInfluence&&FastRandom.nextInt(2)==0) {
					robotController.bid(FastRandom.nextInt(3)+1);
				}
			}
			lastVoteRound = robotController.getRoundNum();
			voteLogger.logBytecode(Clock.getBytecodeNum()-start);//remove line
		}
	}

	/**
	 * Builds a politician to attack the neutral EC found so far
	 * with the lowest influence. Will only attack if it leaves
	 * this EC with 100 influence
	 *
	 * @return true if a politician was built to attack an EC
	 */
	private static boolean attackEC() throws GameActionException {
		if(!toProcessECs.isEmpty()) {
			int start = Clock.getBytecodeNum();//remove line
			int location = 0;
			int influence = Integer.MAX_VALUE;
			for(IntHashMap.Entry target: toProcessECs) {
				if(target.value<influence) {
					location = target.key;
					influence = target.value;
				}
			}
			int spawn = influence+61;
			if(robotController.getInfluence()-spawn>=minInfluence) {
				Direction buildDirection;
				if((buildDirection = build(directions, RobotType.POLITICIAN, spawn))!=null) {
					nextFlag = ((buildDirection.ordinal()+1)*EC_PREFIX_MUL)|location;
					toProcessECs.remove(location);
					processedECs.put(location, spawned[spawnInd-1]);
					return true;
				}
			}
			attackLogger.logBytecode(Clock.getBytecodeNum()-start);//remove line
		}
		return false;
	}

	/**
	 * Processes communications, adding new ECs if necessary
	 */
	private static void processCommunications() throws GameActionException {
		for(int i = 0; i<spawnInd; i++) {
			if(robotController.canGetFlag(spawned[i])) {
				int flag = robotController.getFlag(spawned[i]);
				if(flag!=0) {
					int influence = decodePrefix(flag);
					if(influence>=50) {
						if((influence&Muckraker.ENEMY_EC_PREFIX)>0) {
							influence = (int) Math.ceil(Math.pow(1.1, influence-Muckraker.ENEMY_EC_PREFIX)*1.3);
						}
						flag %= Communications.PREFIX_MUL;
						int assignee = processedECs.get(flag);
						if(assignee==0) {
							toProcessECs.put(flag, influence);
						}else if(!robotController.canGetFlag(assignee)) {
							processedECs.remove(flag);
							toProcessECs.put(flag, influence);
						}
					}
				}
			}
		}
	}

	/**
	 * Processes communications and deletes robots that no longer exist
	 *
	 * @see #processCommunications()
	 */
	private static void cleanUpCommunications() throws GameActionException {
		int nSpawnInd = 0;
		int[] newSpawned = new int[750];
		for(int i = 0; i<spawnInd; i++) {
			if(robotController.canGetFlag(spawned[i])) {
				int flag = robotController.getFlag(newSpawned[nSpawnInd++] = spawned[i]);
				if(flag!=0) {
					int influence = decodePrefix(flag);
					if(influence>=50) {
						if((influence&Muckraker.ENEMY_EC_PREFIX)>0) {
							influence = (int) Math.ceil(Math.pow(1.1, influence-Muckraker.ENEMY_EC_PREFIX)*1.3);
						}
						flag %= Communications.PREFIX_MUL;
						int assignee = processedECs.get(flag);
						if(assignee==0) {
							toProcessECs.put(flag, influence);
						}else if(!robotController.canGetFlag(assignee)) {
							processedECs.remove(flag);
							toProcessECs.put(flag, influence);
						}
					}
				}
			}
		}
		spawned = newSpawned;
		spawnInd = nSpawnInd;
	}
}
