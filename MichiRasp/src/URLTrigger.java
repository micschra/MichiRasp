import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class URLTrigger implements Runnable {

	private static boolean isCurrentlyRunning = false;

	private String urlString;
	private Arduino[] sensors;

	public URLTrigger(String url, Arduino[] sensors) {
		this.urlString = url;
		this.sensors = sensors;
	}

	public void callAsynchron() {
		Thread t = new Thread(this);
		t.start();

	}

	@Override
	public void run() {

		try {
			int retryCounter = 0;
			while (isCurrentlyRunning) {
				Thread.sleep(10);
				if (retryCounter++ > 100) {
					System.out.println("URLTrigger: gave up waiting for thread sync...");
					return;
				}

			}
			isCurrentlyRunning = true;

			// System.out.println("Start trigger: " + urlString);

			URL url = new URL(urlString);
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (!inputLine.equals("NOOP")) {
					System.out.println("BEFEHL ERHALTEN: " + inputLine);
					String[] befehl = inputLine.split("-");
					int ardSubID = Integer.parseInt(befehl[1]);
					int command = Integer.parseInt(befehl[2]);
					byte b = (byte) ((ardSubID << 4) + command);
					if ("ARD".equals(befehl[0])) {
						for (int i = 0; i < sensors.length; i++) {
							Arduino ard = sensors[i];
							if (ard == null)
								continue;
							ard.writeArduino(b);
						}
					}
					else if ("RAS".equals(befehl[0])){
						int raspberryNr = Integer.parseInt(befehl[1]);
						String befehlTyp = befehl[2]; // z.b. Audio
						String befehlDetail = befehl[3]; 
						if (raspberryNr==1 && "Audio".equalsIgnoreCase(befehlTyp) && "1".equals(befehlDetail)) {
							AudioPlayer.playDoor();
						}
						if (raspberryNr==1 && "Video".equalsIgnoreCase(befehlTyp) && "1".equals(befehlDetail)) {
							Arduino.videoPlayer.startKaiser();
						}
					}
					else {
						System.out.println("UNBEKANNTER BEFEHL VON TRIGGER Servlet: "+inputLine);
					}
						

				}
			}
			in.close();
			// System.out.println("TRIGGER SUCCESS! (" + urlString + "): " );
		} catch (Exception e) {
			System.out.println("TRIGGER: (" + urlString + "): " + e);
		} finally {
			isCurrentlyRunning = false;
		}

	}
}
