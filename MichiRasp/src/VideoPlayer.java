
import java.io.File;
import java.util.Random;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class VideoPlayer extends Application implements Runnable {

	public VideoPlayer() {
		super();
		instance = this;
	}

	StackPane stack = new StackPane();
	public static VideoPlayer instance;
	public static MediaPlayer player;
	public static MediaView mediaView;
	private static Stage primaryStage;
	static int MAX_RANDOM_LENGTH_SEC = 12 * 60;
	static String currentFile = "";

	public MediaPlayer initPlayer(String fileName) {
		return initPlayer(fileName, Duration.ZERO, Duration.INDEFINITE, true);
	}

	public MediaPlayer initPlayer(String fileName, Duration start, Duration ende, boolean mute) {
		if (currentFile.equals(fileName)) {
			seek(start, ende);
		} else {
			playNewFile(fileName, start, ende, mute);
		}
		currentFile = fileName;
		return player;
	}

	private void playNewFile(String fileName, Duration start, Duration ende, boolean mute) {
		File f = new File(fileName);
		Media media = new Media(f.toURI().toString());
		if (player != null)
			player.stop();
		player = new MediaPlayer(media);
		player.setOnEndOfMedia(new Runnable() {
			public void run() {
				handleEndOfVideo();
			}
		});
		player.setStartTime(start);
		player.setStopTime(ende);

		System.out.println("media Duration " + media.getDuration());
		player.setMute(mute);

		if (mediaView == null) {
			mediaView = new MediaView(player);

			// 			mediaView.setEffect(null);


			// resizeable!
			DoubleProperty mvw = mediaView.fitWidthProperty();
			DoubleProperty mvh = mediaView.fitHeightProperty();
			mvw.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
			mvh.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
			mediaView.setPreserveRatio(true);

			primaryStage.setScene(new Scene(new Group(mediaView), 740, 408));
			primaryStage.centerOnScreen();
			primaryStage.setAlwaysOnTop(true);
			primaryStage.setResizable(true);
			primaryStage.setTitle("Title");
			primaryStage.show();
		} else {
			mediaView.setMediaPlayer(player);
		}

		player.play();
	}

	private void seek(Duration start, Duration ende) {
		player.setStartTime(start);
		player.setStopTime(ende);
		player.seek(start);
	}

	public void handleEndOfVideo() {
		playNewFile("vhs-kurz.mp4", Duration.ZERO, Duration.INDEFINITE, false);
	}

	public void start(Stage thePrimaryStage) throws Exception {
		primaryStage = thePrimaryStage;
		handleEndOfVideo();
	}

	public static void main(String[] args) {
		System.out.println("Starting VIDEO PLAYER");
		new Thread() {
			public void run() {
				try {
					Thread.sleep(9000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				instance.startKaiser();
			}
		}.start();

		Application.launch();
		System.out.println("Finished VIDEO PLAYER");
	}

	public void run() {
		Application.launch();
	}

	public void startKaiser() {
		System.out.println("startKaiser");
		playNewFile("Finale2.mp4", Duration.ZERO, Duration.INDEFINITE, false);
	}

}