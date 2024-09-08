package gameClient;

import javax.swing.JOptionPane;

public class GameMain {
	private static int port = 9001;
	private static String host = "localhost";
	private String name;
//	private String host = "192.168.1.21";

	public GameMain() {
		name = JOptionPane.showInputDialog("Dispaly name (max 16 chars): ");
		if(name.length() > 10) name = name.substring(0, 9);
		try {
			new GameController(host, port, name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new GameMain();
	}
}
