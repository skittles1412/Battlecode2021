package sprint_0;

// https://stackoverflow.com/questions/1640258/need-a-fast-random-generator-for-c
public class FastRandom {
	static long x = (long) (Math.random()*0xffffffffL)&0xffffffffL;
	public static int nextInt() {
		x = (214013*x+2531011);
		return (int) (x >> 16)&0x7FFF;
	}
	public static int nextInt(int bound) {
		final int ret = nextInt()%bound;
		if(ret<0) return ret+bound;
		return ret;
	}
}
