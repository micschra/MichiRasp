import java.io.FileInputStream;

import javazoom.jl.player.Player;

public class AudioPlayer {

	public AudioPlayer() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			FileInputStream fis = new FileInputStream("C:\\Users\\OEM\\Downloads\\Tuer.mp3");
			Player player = new Player(fis);
			System.out.println("Song playing");
			player.play(150);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
