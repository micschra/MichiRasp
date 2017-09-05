
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

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
	public final static int ARDUINO_ADDRESS = 0x04; // See RPi_I2C.ino. Must be
													// in sync.
	private static boolean verbose = true;

	private I2CBus bus;
	private I2CDevice arduino;

	public Arduino() throws I2CFactory.UnsupportedBusNumberException {
		this(ARDUINO_ADDRESS);
	}

	public Arduino(int address) throws I2CFactory.UnsupportedBusNumberException {
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPI
														// version
			if (verbose)
				System.out.println("Connected to bus. OK.");

			// Get device itself
			arduino = bus.getDevice(address);
			if (verbose)
				System.out.println("Connected to device. OK.");
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

	public static void main(String[] args) throws Exception {
		final NumberFormat NF = new DecimalFormat("##00.00");
		Arduino sensor = new Arduino();
		int read = 0;

		for (int i = 0; i < 3; i++) {
			String inputFromArduino = "";
			int retries = 0;
			boolean weiterLesen=true;
			long millisStart = System.currentTimeMillis();
			while (weiterLesen) {
				try{
					read = sensor.readArduino();
					delay(0.006f); // verlangsamen, weil wenn zu schnell (<0.05) dann kommen vereinzelt IOExceptions
				}
				catch (IOException e) {
					if (retries<20) {
						System.out.println("IOException: delay and retry "+ ++retries);
						delay(0.05f);
						continue;
					}
					else {
						System.out.println("IOException: give up!");
						throw e;
					}
				}
				inputFromArduino += Character.toString((char)read);
				System.out.println("Read: " + Character.toString((char)read));
				weiterLesen = read!='@';
				//delay(0.01f);
			}

			System.out.println("ReadALL ("+(System.currentTimeMillis()-millisStart)+"ms): " + inputFromArduino);

			byte b = (byte) '1';
			sensor.writeArduino(b);
			System.out.println("Wrote to Arduino: " + b);
			delay(3);
		}
		System.out.println("Bye!");
	}
}
