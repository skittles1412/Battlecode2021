package utilities;

import java.util.*;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class IntHashMapTest {
	@RepeatedTest(100)
	void stressTest() {
		int n = (int) 5e3, q = (int) 1e5, bound = n/100;
		Random rand = new Random();
		HashMap<Integer, Integer> correct = new HashMap<>(n);
		IntHashMap test = new IntHashMap(n);
		for(int i = 0; i<n; i++) {
			if(rand.nextBoolean()) {
				if(rand.nextBoolean()) {
					int k = rand.nextInt(bound);
					correct.remove(k);
					test.remove(k);
				}else {
					int k = rand.nextInt(bound), v = rand.nextInt(bound);
					correct.put(k, v);
					test.put(k, v);
				}
			}else {
				int count = 0;
				for(IntHashMap.Entry e: test) {
					assertEquals(correct.get(e.key), e.value);
					count++;
				}
				assertEquals(correct.size(), count);
				assertEquals(correct.size(), test.size());
			}
		}
	}
}