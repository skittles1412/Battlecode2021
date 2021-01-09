package sprint_0;

import battlecode.common.Direction;

import java.util.Arrays;

public class Constants {
	public static final int MESSAGE_HASH = 1;
	public static final Direction[] DIRECTIONS = Direction.allDirections();
	public static final int[][] ENLIGHTENMENT_CENTER_SENSE = new int[][] {{0, 0}, {-1, 0}, {0, -1}, {0, 1}, {1, 0}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}, {-2, 0}, {0, -2}, {0, 2}, {2, 0}, {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}, {-2, -2}, {-2, 2}, {2, -2}, {2, 2}, {-3, 0}, {0, -3}, {0, 3}, {3, 0}, {-3, -1}, {-3, 1}, {-1, -3}, {-1, 3}, {1, -3}, {1, 3}, {3, -1}, {3, 1}, {-3, -2}, {-3, 2}, {-2, -3}, {-2, 3}, {2, -3}, {2, 3}, {3, -2}, {3, 2}, {-4, 0}, {0, -4}, {0, 4}, {4, 0}, {-4, -1}, {-4, 1}, {-1, -4}, {-1, 4}, {1, -4}, {1, 4}, {4, -1}, {4, 1}, {-3, -3}, {-3, 3}, {3, -3}, {3, 3}, {-4, -2}, {-4, 2}, {-2, -4}, {-2, 4}, {2, -4}, {2, 4}, {4, -2}, {4, 2}, {-5, 0}, {-4, -3}, {-4, 3}, {-3, -4}, {-3, 4}, {0, -5}, {0, 5}, {3, -4}, {3, 4}, {4, -3}, {4, 3}, {5, 0}, {-5, -1}, {-5, 1}, {-1, -5}, {-1, 5}, {1, -5}, {1, 5}, {5, -1}, {5, 1}, {-5, -2}, {-5, 2}, {-2, -5}, {-2, 5}, {2, -5}, {2, 5}, {5, -2}, {5, 2}, {-4, -4}, {-4, 4}, {4, -4}, {4, 4}, {-5, -3}, {-5, 3}, {-3, -5}, {-3, 5}, {3, -5}, {3, 5}, {5, -3}, {5, 3}, {-6, 0}, {0, -6}, {0, 6}, {6, 0}, {-6, -1}, {-6, 1}, {-1, -6}, {-1, 6}, {1, -6}, {1, 6}, {6, -1}, {6, 1}, {-6, -2}, {-6, 2}, {-2, -6}, {-2, 6}, {2, -6}, {2, 6}, {6, -2}, {6, 2}};
	public static final int[][] POLITICIAN_SENSE = Arrays.copyOfRange(ENLIGHTENMENT_CENTER_SENSE, 0, 81);
	public static final int[][] SLANDERER_SENSE = Arrays.copyOfRange(ENLIGHTENMENT_CENTER_SENSE, 0, 69);
	public static final int[][] MUCKRAKER_SENSE = Arrays.copyOfRange(ENLIGHTENMENT_CENTER_SENSE, 0, 97);
	//20 69
	//25 81
	//30 97
}
