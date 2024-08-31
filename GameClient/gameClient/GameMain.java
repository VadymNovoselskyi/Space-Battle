package gameClient;

import java.io.IOException;

public class GameMain {
	private int port = 9864;
//	private String host = "localhost";
	private String host = "192.168.1.21";

	public GameMain() {
		//host = JOptionPane.showInputDialog("Server Adress");
		try {
			new GameController(host, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new GameMain();
	}
}
