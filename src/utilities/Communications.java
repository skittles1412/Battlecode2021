package utilities;

import battlecode.common.MapLocation;

public class Communications {
	public static final int LOCATION_BITS = 7;
	public static final int LOCATION_MOD = 1<<LOCATION_BITS;
	/**
	 * Returns an encoded location
	 */
	public static int encodeLocation(int x, int y) {
		return ((x%LOCATION_MOD)<<LOCATION_BITS)|(y%LOCATION_MOD);
	}
	/**
	 * Decodes the last bits of a location message, ignoring everything else.
	 *
	 * @param locationX The x coordinate of this bot
	 * @param locationY The y coordinate of this bot
	 */
	public static MapLocation decodeLocation(int locationX, int locationY, int message) {
		return new MapLocation(decodeCoordinate(locationX, (message >> LOCATION_BITS)%LOCATION_MOD), decodeCoordinate(locationY, message%LOCATION_MOD));
	}
	private static int decodeCoordinate(int coordinate, int message) {
		//coordinate should be >= 10000 >= message
		int diff = (coordinate-message)%LOCATION_MOD;
		if(diff<64) {//message is right of coordinate
			return coordinate-diff;
		}else if(diff>64) {//message is left of coordinate
			return coordinate+LOCATION_MOD-diff;
		}
		//invalid coordinates
		throw new IllegalArgumentException("There can't be two cells with distance greater than 64");
	}
}
