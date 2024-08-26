package gameServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class GameServer {
	private DatagramSocket socket;
	private static HashMap<String, Integer> playerAddresses = new HashMap<>();
	private int port, playerID = 1;

	public static HashMap<String, Player> deadPlayerMap = new HashMap<>();
	public static HashMap<String, Player> alivePlayerMap = new HashMap<>();

	public GameServer(int port) throws Exception {
		this.port = port;
		socket = new DatagramSocket(port);

		byte[] receiveData = new byte[32];
		while (true) {
			DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(receivedPacket);

			InetAddress playerIP = receivedPacket.getAddress();
			int playerPort = receivedPacket.getPort();
			String message = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
			String playerAddress = playerIP.getHostAddress() + ":" + playerPort;

			//			System.out.println("Received from player " + playerAddresses.get(playerAddress) + ": " + message +". Address: " +playerAddress);
			if(!deadPlayerMap.containsKey(playerAddress)) {
				exeCommand(message, playerAddress);
			} else {
				String[] dataList = message.split(",");
				Command cmd = Command.valueOf(dataList[0]);
				if(cmd.equals(Command.DISCONNECT)) {
					disconnect(playerAddress);
				}
			}
		}
	}

	public void exeCommand(String data, String playerAddress) {
		String[] dataList = data.split(",");
		Command cmd = Command.valueOf(dataList[0]);

		switch(cmd) {
		case NEW_PLAYER:
			Player newPlayer = new Player(playerID, (int)(Math.random()*700), (int)(Math.random()*500), 10);

			alivePlayerMap.put(playerAddress, newPlayer);

			notifyClient(Command.CONNECTED, newPlayer.toString(), playerAddress);
			notifyAllButThis(cmd, newPlayer.toString(), playerAddress);
			
			if(alivePlayerMap.size() > 1) {
				String playerList = "";
				Player requestingPlayer = alivePlayerMap.get(playerAddress);
				for (Player player : alivePlayerMap.values()) {
					if(player != requestingPlayer) {
						playerList += "," +player;
					}
				}
				// playerList.substring(1) Tar bort 1:a ","
				notifyClient(Command.RECEIVE_ALL, playerList.substring(1), playerAddress);
			}

			System.out.println("PlayerID: " + playerID + " Connected to server");
			playerAddresses.put(playerAddress, playerID);
			playerID++;
			break;

		case MOVE:
			int xPos = Integer.valueOf(dataList[2]);
			int yPos = Integer.valueOf(dataList[3]);

			Player movedPlayer = alivePlayerMap.get(playerAddress);
			movedPlayer.update(xPos, yPos);
			notifyAllButThis(cmd, movedPlayer.toString(), playerAddress);
			break;

		case HIT:
			break;
		case DEAD:
			Player deadPlayer = alivePlayerMap.get(playerAddress);
			alivePlayerMap.remove(playerAddress);
			deadPlayerMap.put(playerAddress, deadPlayer);

			notifyAllClients(Command.REMOVE, deadPlayer.toString());
			System.out.println("PlayerID: " + deadPlayer.getID() + " DIED");
			deadPlayer.setDead(true);
			break;

		case DISCONNECT:
			disconnect(playerAddress);
			break;

		default:
			break;
		}
	}

	public void disconnect(String playerAddress) {
		Player player = alivePlayerMap.get(playerAddress);
		if(player == null) {
			player = deadPlayerMap.get(playerAddress);
		}

		notifyClient(Command.DISCONNECT, player.toString(), playerAddress);
		if(!player.isDead()) {
			alivePlayerMap.remove(playerAddress);
			notifyAllClients(Command.REMOVE, player.toString());
		} else {
			deadPlayerMap.remove(playerAddress);
		}
		playerAddresses.remove(playerAddress);
		System.out.println("Disconnecting " +player.getID());
	}

	public void notifyClient(Command cmd, String data, String playerAddress) {
		String responseMessage = cmd.toString() + "," + data;
		notifyClient(responseMessage, playerAddress);
	}
	public void notifyClient(String data, String playerAddress) {
		byte[] sendData = data.getBytes();

		InetAddress playerIP = null;
		try {
			playerIP = InetAddress.getByName(playerAddress.split(":")[0]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int playerPort = Integer.parseInt(playerAddress.split(":")[1]);

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, playerIP, playerPort);
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void notifyAllClients(Command cmd, String data) {
		data = cmd.toString() + "," + data;
		for(String playerAddress : playerAddresses.keySet()) {
			notifyClient(data, playerAddress);
		}
	}

	public void notifyAllButThis(Command cmd, String data, String ignoreAddres) {
		data = cmd.toString() + "," + data;
		for(String playerAddress : playerAddresses.keySet()) {
			if(!playerAddress.equals(ignoreAddres)) {
				notifyClient(data, playerAddress);
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("Server is up and running!");
		try {
			GameServer server = new GameServer(9001);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
