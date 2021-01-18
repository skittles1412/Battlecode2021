package utilities;

import battlecode.common.MapLocation;

public class Communications {
	public static final int LOCATION_MOD = 128;//1<<7
	public static final int PREFIX_MUL = 16384;//1<<14

	/**
	 * @return the location, encoded into a 14 bit integer
	 */
	public static int encodeLocation(MapLocation location) {
		return ((location.x%LOCATION_MOD)*LOCATION_MOD)|(location.y%LOCATION_MOD);
	}

	/**
	 * @return the location represented by the last 14 bits of the provided message
	 */
	public static MapLocation decodeLocation(MapLocation location, int message) {
		//just decodeCoordinate inlined
		int x = location.x, y = location.y;
		int diff = (x-(message/LOCATION_MOD)%LOCATION_MOD)%LOCATION_MOD;
		if(diff<64) {
			x -= diff;
		}else {
			x += LOCATION_MOD-diff;
		}
		diff = (y-message%LOCATION_MOD)%LOCATION_MOD;
		if(diff<64) {
			y -= diff;
		}else {
			y += LOCATION_MOD-diff;
		}
		return new MapLocation(x, y);
	}

	/**
	 * @return the coordinate represented by the given location and the message
	 */
	public static int decodeCoordinate(int location, int message) {
		//location should be >= 10000 >= message
		int diff = (location-message)%LOCATION_MOD;
		if(diff<64) {//message is right of location
			return location-diff;
		}else {//message is left of location
			return location+LOCATION_MOD-diff;
		}
	}

	/**
	 * @return a message with prefix as the first 10 bits and message as the last 14 bits
	 */
	public static int encodePrefix(int prefix, int message) {
		return (prefix*PREFIX_MUL)|message;
	}

	/**
	 * @return the prefix (first 10 bits) of the given message
	 */
	public static int decodePrefix(int message) {
		return message/PREFIX_MUL;
	}

	/**
	 * @return the message (last 14 bits) of the given message
	 */
	public static int decodeMessage(int message) {
		return message%PREFIX_MUL;
	}
}
