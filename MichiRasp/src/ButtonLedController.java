
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class ButtonLedController {

	private enum ButtonState {
		DOWN, UP
	}

	// private static final Logger log =
	// LoggerFactory.getLogger(ButtonLedController.class);

	private final GpioController gpio = GpioFactory.getInstance();

	private  GpioPinDigitalOutput led = null;
	private  GpioPinDigitalInput button = null;

	private boolean buttonPressed = false;
	private boolean isRunning = false;
	private ButtonState state = ButtonState.UP;
	private ExecutorService executor;
	private Future<?> loop;

	public ButtonLedController(Pin ledPin, Pin buttonPin) {
		super();
		configure(ledPin, buttonPin);
	}

	public void start() {
		isRunning = true;
		createLoop();
		System.out.println("Controller has been started.");
	}

	public void stop() {
		isRunning = false;
		while (!loop.isDone()) {
			System.out.println("Waiting for runner to stop...");
		}
		executor.shutdown();
		System.out.println("Controller has been stopped.");
	}

	private void configure(Pin ledPin, Pin buttonPin) {
		led = gpio.provisionDigitalOutputPin(ledPin, "led", PinState.LOW);
		button = gpio.provisionDigitalInputPin(buttonPin, "button", PinPullResistance.PULL_DOWN);
		led.setShutdownOptions(true, PinState.LOW);
		addButtonListener();
	}

	private void addButtonListener() {
		button.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				handleButtonPressed(event);
			}
		});
	}

	private void createLoop() {
		executor = Executors.newSingleThreadExecutor();
		loop = executor.submit(new Runnable() {
			@Override
			public void run() {
				while (isRunning) {
					if (buttonPressed) {
						led.toggle();
						buttonPressed = false;
					}
				}
			}
		});
	}

	private void handleButtonPressed(GpioPinDigitalStateChangeEvent event) {
		if (event.getState().isHigh()) {
			state = ButtonState.DOWN;
		} else if (event.getState().isLow() && state == ButtonState.DOWN) {
			buttonPressed = true;
			state = ButtonState.UP;
		}
	}
}