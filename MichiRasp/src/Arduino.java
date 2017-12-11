
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	public final static int ARDUINO_ADDRESS_1 = 0x05;

	private static boolean verbose = false;

	private I2CBus bus;
	private I2CDevice arduino;
	private int address;
	private static float delayBytes = 0.05f;
	private static float delayReads = 0.05f;
	private static float loopMinutes = 1f;
	private static String serverIP = "10.0.0.9:8080";
	private static String roomNr = "2";
	private static Arduino[] sensors;
	private static int reopenSensorsFrequency = 100;
	public static VideoPlayer videoPlayer = null;

	
	
	public static Arduino[] getSensors() {
		return sensors;
	}

	private int successfulRead = 0;

	public int getSuccessfulRead() {
		return successfulRead;
	}

	public int increaseSuccessfulRead() {
		return ++successfulRead;
	}

	public void setSuccessfulRead(int successfulRead) {
		this.successfulRead = successfulRead;
	}

	public static void debugInfo(String msg) {
		if (verbose)
			System.out.println(msg);
	}

	public Arduino(int address) throws I2CFactory.UnsupportedBusNumberException {
		this.address = address;
		open(address);
		successfulRead = 0;
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

	public String[] readState() {
		String[] answer;
		try {
			answer = readState(delayBytes);
			debugInfo("Successful read (delaySec=" + (delayBytes) + "sec). Answer=" + answer);
			return answer;
		} catch (Exception e) {
			debugInfo("Arduino Read (" + getAddress() + "): " + e);
		}
		return null;
	}

	public String[] readState(float delaySeconds) throws Exception {
		// jetzt solange lesen bis ein @ kommt
		String inputFromArduino[] = new String[100];
		int subID = 1;
		int read = 0;
		inputFromArduino[1] = "";
		boolean weiterLesen = true;
		long millisStart = System.currentTimeMillis();
		while (weiterLesen) {
			read = readArduino();
			delay(delaySeconds); // verlangsamen, weil wenn zu schnell dann
									// kommen vereinzelt IOExceptions
			if (read == '@')
				weiterLesen = false;
			else {
				if (read == '#') {
					debugInfo("ReadState: " + inputFromArduino[subID]);
					subID++;
					inputFromArduino[subID] = "";
				} else
					inputFromArduino[subID] += Character.toString((char) read);
			}
		}
		debugInfo("ReadState (" + (System.currentTimeMillis() - millisStart) + "ms): "
				+ sensorStateArray2String(inputFromArduino));
		return inputFromArduino;
	}

	public static void triggerRoomControl(String text) {
		String urlString = "http://" + serverIP + "/BiSuRo/trigger?room=" + roomNr + "&info=" + text
				+ "&action=setArduinoInfo";
		if (text.length()<40){
			URLTrigger ut = new URLTrigger(urlString, sensors);
			ut.callAsynchron();
		}
		else {
			debugInfo("Text von Arduino verdächtig lang ("+text.length()+"): '"+text.substring(0,5)+"...' Trigger nicht aufgerufen.");
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0 || (args.length > 0 && args[0] == "?")) {
			System.out.println("EXIT.GURU - Raspberry Arduino Bridge");
			System.out.println("  Parameters: ");
			System.out.println("  <ServerIP:port> String ");
			System.out.println("  <roomNr> int default=2");
			System.out.println("  <DelayAfterEachByteInSeconds> float default=0.05");
			System.out.println("  <DelayAfterEachReadInSeconds> float default=0.05");
			System.out.println("  <LoopForMinutes> float default=1 (0 for endless)");
			return;
		}

		// start VIDEO
		videoPlayer = new VideoPlayer();
		Thread videoThread = new Thread(videoPlayer);

		System.out.println("Starting Videoplayer");
		videoThread.start();
		
		/* TEST KAISER 
		System.out.println("Sleeping 10 sec");
		Thread.sleep(10000);
		System.out.println("Change to Kaiser");
		videoPlayer.startKaiser();
		System.out.println("Sleeping 60 sec");
		Thread.sleep(60000);
		System.out.println("Change to Kaiser");
		videoPlayer.startKaiser();
		*/
		
		serverIP = args[0];
		if (args.length > 1)
			roomNr = args[1];
		if (args.length > 2)
			delayBytes = Float.parseFloat(args[2]);
		if (args.length > 3)
			delayReads = Float.parseFloat(args[3]);
		if (args.length > 4)
			loopMinutes = Float.parseFloat(args[4]);

		sensors = new Arduino[50];

		System.out.println("Starting to poll Arduinos: roomNr=" + roomNr + ", server=" + serverIP + ", delayBytes="
				+ delayBytes + "sec, delayReads=" + delayReads + "sec, loop=" + loopMinutes + "min");
		
		openSensors();

		long startMillis = System.currentTimeMillis();
		boolean loopOn = true;
		int counter = 0;
		while (loopOn) {
			for (int i = 0; i < sensors.length; i++) {
				if (sensors[i] == null)
					continue;
				String[] answer = handleSensor(sensors[i]);

			}
			loopOn = (loopMinutes == 0) || ((System.currentTimeMillis() - startMillis) < (loopMinutes * 60 * 1000));
			if (reopenSensorsFrequency>0 && ++counter>reopenSensorsFrequency) {
				counter=0;
				openSensors();
				System.out.println("REOPENING SENSORS");
			}
		}
		System.out.println("Bye!");
	}

	private static void openSensors() throws UnsupportedBusNumberException {
		reopenSensor(ARDUINO_ADDRESS_1);
		//reopenSensor(ARDUINO_ADDRESS_2);
	}

	private static void reopenSensor(int id) throws UnsupportedBusNumberException {
		debugInfo("Reopen Sensor " + id);
		if (sensors[id] != null)
			sensors[id].close();
		sensors[id] = new Arduino(id);

	}

	public static String[] handleSensor(Arduino sensor) {
		if (sensor == null)
			return null;
		System.out.print(formatNow()+"-(" + sensor.getAddress() + ")");
		long millis1 = System.currentTimeMillis();
		String sensorState[] = sensor.readState(); // array of strings.. each string is a Arduino substatus
		long millis2 = System.currentTimeMillis();
		long millis3 = 0;
		if (sensorState != null) {
			sensor.increaseSuccessfulRead();
			for (int i = 0; i < sensorState.length; i++) {
				if (sensorState[i] != null && sensorState[i].trim().length() > 0) {
					triggerRoomControl(sensorState[i]);
				}
			}
			millis3 = System.currentTimeMillis();
			System.out.println("- success#" + sensor.getSuccessfulRead() + " - " + sensorStateArray2String(sensorState)
					+ " (R: " + (millis2 - millis1) + "ms, T: " + (millis3 - millis2) + "ms)");
		} else {
			// I2C error?
			millis3 = System.currentTimeMillis();
			System.out.println("- failure! - " + sensorStateArray2String(sensorState) + " (R: " + (millis2 - millis1)
					+ "ms, T: " + (millis3 - millis2) + "ms)");
		}

		delay(delayReads);
		return sensorState;
	}

	public int getAddress() {
		// TODO Auto-generated method stub
		return address;
	}

	public static String sensorStateArray2String(String[] sensorState) {
		String s = "{";
		boolean schonWas = false;
		if (sensorState != null) {
			for (int i = 0; i < sensorState.length; i++) {
				if (sensorState[i] != null && sensorState[i].trim().length() > 0) {
					if (schonWas)
						s += ", ";
					s += "SubID=" + i + ": " + (sensorState[i].length()>40?"?NOISE?":sensorState[i]) + " ";
					schonWas = true;
				}
			}
		}
		return s + "}";
	}
	
	public static String formatNow() {
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(d);
	}
}
