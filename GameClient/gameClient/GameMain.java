package gameClient;

import java.io.IOException;

public class GameMain {
	private GameController gameController;
//	private String host = "localhost";
	private int port = 9864;
	private String host = "192.168.1.21";

	public GameMain() {
		//host = JOptionPane.showInputDialog("Server Adress");
		try {
			gameController = new GameController(host, port, 800, 600);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new GameMain();
	}
}
