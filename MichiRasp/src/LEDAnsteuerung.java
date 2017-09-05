import com.pi4j.io.gpio.RaspiPin;

public class LEDAnsteuerung {


	public static void main(String[] args) throws InterruptedException {
		System.out.println("MichiRasp Sample ... started for 15 sec.");
		ButtonLedController blc = new ButtonLedController(RaspiPin.GPIO_29, RaspiPin.GPIO_22);
		blc.start();
		Thread.sleep(15000);
		blc.stop();
		System.out.println("MichiRasp Sample ... exit.");
	}
}
