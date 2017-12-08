import javafx.util.Duration;

public class VideoStopObserver implements Runnable{

	public VideoStopObserver() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		System.out.println("RESTART VIDEO");
		VideoPlayer.player.seek(Duration.ZERO);
		
	}

}
