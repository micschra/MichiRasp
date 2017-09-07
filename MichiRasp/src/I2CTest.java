
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
public class I2CTest {
	public final static int ARDUINO_ADDRESS_1 = 0x04;

	private static boolean verbose = true;

	private I2CBus bus;
	private I2CDevice arduino;

	public static void debugInfo(String msg) {
		if (verbose)
			System.out.println("Connected to bus. OK.");
	}

	public I2CTest(int address) throws I2CFactory.UnsupportedBusNumberException {
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

	public String readState(float delaySeconds) throws Exception {
		// Arduino vorwarnen dass gleich gelesen wird: '1'
		byte b = 'x'; //'1';
		writeArduino(b);
		//debugInfo("Wrote to Arduino: " + b);

		delay(delaySeconds); // verlangsamen, weil wenn zu schnell dann
		// kommen vereinzelt IOExceptions

		int read = 0;
		read = readArduino();
		String inputFromArduino = Character.toString((char) read);
		return inputFromArduino;
	}

	public static void main(String[] args) throws Exception {

		System.out.println("I2CTest");
		I2CTest sensor1 = new I2CTest(ARDUINO_ADDRESS_1);

		for (int i = 0; i < 100; i++) {
			String s = sensor1.readState(0.01f);
			System.out.print(s);
			//delay(0.01f);
		}
		System.out.println("Bye!");
	}

}
