
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

	StackPane stack = new StackPane();
	public static MediaPlayer player;

	public void start(Stage primaryStage) throws Exception {


		primaryStage.setScene(new Scene(new Group(new MediaView(player)), 740, 408));
		primaryStage.centerOnScreen();
		primaryStage.setAlwaysOnTop(true);
		primaryStage.setResizable(true);
		primaryStage.setTitle("Title");
		primaryStage.show();

		System.out.println("START VIDEO");
		player.play();
	}

	public static void main(String[] args) {
		Media media = new Media("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
		player = new MediaPlayer(media);
		player.setStartTime(Duration.ZERO);
		player.setStopTime(new Duration(5000));
		player.setOnEndOfMedia(new VideoStopObserver());
//????
		MediaView mv = new MediaView(player);
		DoubleProperty mvw = mv.fitWidthProperty();
		DoubleProperty mvh = mv.fitHeightProperty();
		mvw.bind(Bindings.selectDouble(mv.sceneProperty(), "width"));
		mvh.bind(Bindings.selectDouble(mv.sceneProperty(), "height"));
		mv.setPreserveRatio(true);		
		
		Application.launch();
	}

	public void run() {
		main(null);
	}

}