package gameClient;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import gameServer.Command;


public class Communicator extends Thread{
	private DatagramSocket socket;
	private String host;
	private int port;


	private GameController gameController;

	private volatile boolean  quit = false;

	public Communicator(GameController gameController, String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		this.gameController = gameController;

		this.start();
	}

	public void notifyServer(Command cmd, String data) {
		data = (cmd.toString() + "," + data);
		InetAddress serverAddress = null;
		try {
			serverAddress = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		byte[] sendData = data.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void notifyServer(Command cmd) {
		notifyServer(cmd, "");
	}


	@Override
	public void run() {
		try {
			socket = new DatagramSocket();
			notifyServer(Command.NEW_PLAYER);

			byte[] receiveData = new byte[64];
			while(!quit) {
				DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
				socket.receive(receivedPacket);
				String data = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
				
//				System.out.println(data);
				
				String[] dataList = data.split(",");
				Command cmd = Command.valueOf(dataList[0]);
				if(cmd != Command.UPDATE_ALL) gameController.updatePlayerMap(data);
				else gameController.updateAll(dataList);
			}
		}catch (IOException e) {
			closeConnection();
		}
	}

	public void closeConnection() {
		quit = true;
	}
}

