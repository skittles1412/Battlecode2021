package sprint_0;

import battlecode.common.MapLocation;

public class Communications {
	public static final int LOCATION_MOD = 128;
	public static final int LOCATION_TOT = 16384;//LOCATION_MOD squared

	public static int encodeLocation(MapLocation location) {
		return ((location.x%LOCATION_MOD)*LOCATION_MOD)|(location.y%LOCATION_MOD);
	}

	public static MapLocation decodeLocation(MapLocation location, int message) {
		return new MapLocation(decodeCoordinate(location.x, (message/LOCATION_MOD)%LOCATION_MOD), decodeCoordinate(location.y, message%LOCATION_MOD));
	}

	public static int decodeCoordinate(int coordinate, int message) {
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

	public static int encodePrefix(int prefix, int message) {
		return prefix*LOCATION_TOT|message;
	}

	public static int decodePrefix(int message) {
		return message/LOCATION_TOT;
	}

	public static int decodeMessage(int message) {
		return message%LOCATION_TOT;
	}
}
