package template;

import battlecode.common.MapLocation;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static template.Communicator.*;

class CommunicatiorTest {
	@Test
	void locationTest() {
		int n = 20000;
		for(int i = n; i<n+128; i++) {
			for(int j = n; j<n+128; j++) {
				for(int k = -63; k<=63; k++) {
					for(int l = -63; l<=63; l++) {
						int x = i+k, y = j+l;
						assertEquals(new MapLocation(x, y), decodeLocation(i, j, encodeLocation(x, y)));
					}
				}
			}
		}
	}
}