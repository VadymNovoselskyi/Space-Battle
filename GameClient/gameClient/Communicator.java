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

	public void notifyServer(Command cmd, String data, long time) {
		data = cmd.toString() + "," + data + "," + time;
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


	@Override
	public void run() {
		try {
			socket = new DatagramSocket();
			GameController.timeAdjusment = getAdjustment() - getLatency();
			System.out.println(GameController.timeAdjusment);
			notifyServer(Command.NEW_PLAYER, System.nanoTime() + GameController.timeAdjusment);

			byte[] receiveData = new byte[128];
			while(!quit) {
				DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
				socket.receive(receivedPacket);
				String data = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
				
				System.out.println(data);
				
				String[] dataList = data.split(",");
				Command cmd = Command.valueOf(dataList[0]);
				if(cmd != Command.UPDATE_ALL) gameController.updatePlayerMap(data);
				else gameController.updateAll(dataList);
			}
		}catch (IOException e) {
			closeConnection();
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
        for (int i = 0; i < 100; i++) {
            long clientSendTime = System.nanoTime();

            byte[] sendData = String.valueOf(Command.GET_SERVER_TIME).getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
            try {
				socket.send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            byte[] receiveData = new byte[32];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
				socket.receive(receivePacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            long clientReceiveTime = System.nanoTime();
            long rtt = clientReceiveTime - clientSendTime;
            totalRTT += rtt;
        }

        // Calculate average latency
        long averageRTT = (int) totalRTT / 100;
        return (int) averageRTT / 2;
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
		quit = true;
	}
}

