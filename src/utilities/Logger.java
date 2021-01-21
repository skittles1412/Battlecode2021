package utilities;

/**
 * A logger class. Log levels:
 * <table>
 *     <tr>
 *         <td>Level</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>0</td>
 *         <td>Logs nothing</td>
 *     </tr>
 *     <tr>
 *         <td>1</td>
 *         <td>Logs bytecode warnings when the
 *         bytecode used exceeds limit-1000</td>
 *     </tr>
 *     <tr>
 *         <td>2</td>
 *         <td>Logs EC bytecode usages</td>
 *     </tr>
 *     <tr>
 *         <td>3</td>
 *         <td>Logs unit bytecode usages</td>
 *     </tr>
 *     <tr>
 *         <td>3</td>
 *         <td>Logs more specfic unit bytecode usages</td>
 *     </tr>
 * </table>
 */
public class Logger {
	public static final int LOG_LEVEL = Integer.parseInt(System.getProperty("bc.testing.logLevel", "0"));
	private final int logInterval, requiredLevel, bytecodeLimit;
	private final String defaultMessage;
	private int logCount = 0;

	public Logger(int logInterval, int requiredLevel, int bytecodeLimit) {
		this(logInterval, requiredLevel, bytecodeLimit, "");
	}

	public Logger(int logInterval, int requiredLevel, int bytecodeLimit, String defaultMessage) {
		this.logInterval = logInterval;
		this.requiredLevel = requiredLevel;
		this.bytecodeLimit = bytecodeLimit;
		this.defaultMessage = defaultMessage;
	}

	public void logBytecode(int bytecodeBegin, int bytecodeEnd) {
		logBytecode(bytecodeBegin, bytecodeEnd, defaultMessage);
	}

	public void logBytecode(int bytecodeBegin, int bytecodeEnd, String message) {
		logBytecode((bytecodeEnd+bytecodeLimit-bytecodeBegin)%bytecodeLimit, message);
	}

	public void logBytecode(int roundBegin, int roundEnd, int bytecodeBegin, int bytecodeEnd) {
		logBytecode(roundBegin, roundEnd, bytecodeBegin, bytecodeEnd, defaultMessage);
	}

	public void logBytecode(int roundBegin, int roundEnd, int bytecodeBegin, int bytecodeEnd, String message) {
		if(roundBegin!=roundEnd&&LOG_LEVEL>0) {
			System.out.printf("[WARN] %s: %d - overflowed into the next round\n", message, bytecodeLimit*(roundEnd-roundBegin)+bytecodeEnd-bytecodeBegin);
		}else {
			logBytecode(bytecodeLimit*(roundEnd-roundBegin)+bytecodeEnd-bytecodeBegin, message);
		}
	}

	public void logBytecode(int bytecodeUsed) {
		logBytecode(bytecodeUsed, defaultMessage);
	}

	public void logBytecode(int bytecodeUsed, String message) {
		if(LOG_LEVEL>0&&(bytecodeUsed>=bytecodeLimit-1000||LOG_LEVEL>=requiredLevel)&&++logCount==logInterval) {
			logCount = 0;
			System.out.println(message+": "+bytecodeUsed);
		}
	}
}
