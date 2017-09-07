
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/*
 * I2C Communication with an Arduino
 * Raspberry is the Master, Arduino is the Slave
 *
 * See the Arduino sketch named RPi_I2C.ino
 * 
 * Wiring:
 * RasPI    Arduino
 * ----------------
 * GND #9   GND
 * SDA #3   SDA (or A4, before Rev3)
 * SLC #5   SLC (or A5, before Rev3)
 * 
 */
public class Arduino {
	public final static int ARDUINO_ADDRESS_1 = 0x04;
	public final static int ARDUINO_ADDRESS_2 = 0x05;

	private static boolean verbose = false;

	private I2CBus bus;
	private I2CDevice arduino;
	private static float delayBytes = 0.05f;
	private static float delayReads = 0.05f;
	private static float loopMinutes = 1f;

	public static void debugInfo(String msg) {
		if (verbose)
			System.out.println("Connected to bus. OK.");
	}

	public Arduino(int address) throws I2CFactory.UnsupportedBusNumberException {
		open(address);
	}

	public void open(int address) throws UnsupportedBusNumberException {
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPI
														// version
			debugInfo("Connected to bus. OK.");

			// Get device itself
			arduino = bus.getDevice(address);
			debugInfo("Connected to device. OK.");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void close() {
		try {
			this.bus.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/*
	 * methods readArduino, writeArduino This where the communication protocol
	 * would be implemented.
	 */
	public int readArduino() throws Exception {
		int r = arduino.read();
		return r;
	}

	public void writeArduino(byte b) throws Exception {
		arduino.write(b);
	}

	private static void delay(float d) // d in seconds.
	{
		try {
			Thread.sleep((long) (d * 1_000));
		} catch (Exception ex) {
		}
	}

	public String readState() {
		String answer = "";
/*		
		for (int i = 0; i < 3; i++) {
*/
			try {
				answer = readState(delayBytes);
				debugInfo("Successful read (delaySec=" + (delayBytes) + "sec). Answer=" + answer);
				return answer;
			} catch (Exception e) {
				System.out.println("IOException (delaySec=" + (delayBytes) + "sec).");
//				delayBytes = 2 * delayBytes;
 
			}
//		}
		return null;
	}

	public String readState(float delaySeconds) throws Exception {
		// Arduino vorwarnen dass gleich gelesen wird: '1'
		// byte b = '1';
		// writeArduino(b);
		// debugInfo("Wrote to Arduino: " + b);

		// jetzt solange lesen bis ein @ kommt
		String inputFromArduino = "";
		int read = 0;
		boolean weiterLesen = true;
		long millisStart = System.currentTimeMillis();
		while (weiterLesen) {
			read = readArduino();
			delay(delaySeconds); // verlangsamen, weil wenn zu schnell dann
									// kommen vereinzelt IOExceptions
			if (read == '@')
				weiterLesen = false;
			else
				inputFromArduino += Character.toString((char) read);
		}
		debugInfo("ReadALL (" + (System.currentTimeMillis() - millisStart) + "ms): " + inputFromArduino);
		return inputFromArduino;
	}

	public static void triggerRoomControl(String text) {
		try {
			URL url = new URL("http://10.0.0.9:8080/BiSuRo/trigger?room=1&info=" + text + "&action=setArduinoInfo");
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				debugInfo(inputLine);
			in.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length>0 && args[0]=="?") {
			System.out.println("EXIT.GURU - Raspberry Arduino Bridge");
			System.out.println("  Parameters: ");
			System.out.println("  <DelayAfterEachByteInSeconds> float default=0.05");
			System.out.println("  <DelayAfterEachReadInSeconds> float default=0.05");
			System.out.println("  <LoopForMinutes> float default=1");
			return;
		}
		if (args.length>0) delayBytes = Float.parseFloat(args[0]);
		if (args.length>1) delayReads = Float.parseFloat(args[1]);
		if (args.length>2) loopMinutes = Float.parseFloat(args[2]);

		System.out.println("Starting to poll Arduinos: delayBytes="+delayBytes+"sec, delayReads="+delayReads+"sec, loop="+loopMinutes+"min");
		
		Arduino sensor1 = new Arduino(ARDUINO_ADDRESS_1);
		Arduino sensor2 = new Arduino(ARDUINO_ADDRESS_2);

		long startMillis=System.currentTimeMillis();
		while ((System.currentTimeMillis()-startMillis)<(loopMinutes*60*1000)) {
			handleSensor(sensor1);
			handleSensor(sensor2);
		}
		System.out.println("Bye!");
	}

	public static void handleSensor(Arduino sensor1) {
		long millis1 = System.currentTimeMillis();
		String sensorState = sensor1.readState();
		long millis2 = System.currentTimeMillis();
		triggerRoomControl(sensorState);
		long millis3 = System.currentTimeMillis();
		System.out.println("--- " + sensorState + " (READ: "+(millis2-millis1)+" ms, TRIGGER: "+(millis3-millis2)+" ms, TOTAL: "+(millis3-millis1)+" ms)");
		delay(delayReads);
	}
}
