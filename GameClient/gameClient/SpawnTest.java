package gameClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SpawnTest {
	private DatagramSocket socket;
	InetAddress serverAddress = null;
	private String host;
	private int port;
	
	public SpawnTest(String host, int port) {
		this.host = host;
		this.port = port;
		
		try {
			socket = new DatagramSocket();
			serverAddress = InetAddress.getByName(host);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String data = Command.NEW_PLAYER + "," + System.nanoTime();

		byte[] sendData = data.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
