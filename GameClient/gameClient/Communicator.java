package gameClient;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;


public class Communicator implements Runnable{
	private GameController gameController;
	private DatagramSocket socket;
	InetAddress serverAddress = null;
	private String host;
	private int port;

	public Communicator(GameController gameController, String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		this.gameController = gameController;

		socket = new DatagramSocket();
		try {
			serverAddress = InetAddress.getByName(host);
		} catch (Exception e) {
			e.printStackTrace();
		}
		gameController.timeAdjusment = getAdjustment() - getLatency();
		notifyServer(Command.NEW_PLAYER, System.nanoTime() - gameController.timeAdjusment);
	}

	@Override
	public void run() {
		try {
			byte[] receiveData = new byte[5296];			
			DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(receivedPacket);
			String data = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

//			System.out.println(data);

			String[] dataList = data.split(",");
			Command cmd = Command.valueOf(dataList[0]);
			if(cmd == Command.PING) notifyServer(Command.PING);
			else gameController.updatePlayerMap(data);
		}catch (IOException e) {
			e.printStackTrace();;
		}
	}


	public void notifyServer(Command cmd, String data, long time) {
		data = cmd.toString() + "," + data + "," + time;

		byte[] sendData = data.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void notifyServer(Command cmd, long time) {
		String data = cmd.toString() + "," + time;
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
		String data = cmd.toString();
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


	public long getLatency() {
		long totalRTT = 0;
		InetAddress serverAddress = null;
		try {
			serverAddress = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 20; i++) {
			long clientSendTime = System.nanoTime();

			byte[] sendData = String.valueOf(Command.GET_SERVER_TIME).getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
			try {
				socket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}

			byte[] receiveData = new byte[32];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				socket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}

			long clientReceiveTime = System.nanoTime();
			long rtt = clientReceiveTime - clientSendTime;
			totalRTT += rtt;
		}

		// Calculate average latency
		long averageRTT = (long) totalRTT / 100;
		return (long) averageRTT / 2;
	}

	public long getAdjustment() {
		String data = Command.GET_SERVER_TIME.toString();
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

		byte[] receiveData = new byte[32];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		try {
			socket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String time = new String(receivePacket.getData(), 0, receivePacket.getLength());
		long serverTime = Long.valueOf(time.split(",")[1]);
		return System.nanoTime() - serverTime;

	}

	public void closeConnection() {

	}
}

