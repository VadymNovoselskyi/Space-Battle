package gameServer;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class PacketProcessor implements Runnable {
	private DatagramPacket packet;

	public PacketProcessor(DatagramPacket packet) {
		this.packet = packet;
	}

	@Override
	public void run() {
		int length = packet.getLength();
		String message = new String(packet.getData(), 0, length);

		InetAddress playerIP = packet.getAddress();
		int playerPort = packet.getPort();
		String playerAddress = playerIP.getHostAddress() + ":" + playerPort;

		System.out.println("Received from player: " + message +" Address: " +playerAddress);
		if(!Server.deadPlayerMap.containsKey(playerAddress)) {
			exeCommand(message, playerAddress);
		} else {
			String[] dataList = message.split(",");
			Command cmd = Command.valueOf(dataList[0]);
			if(cmd.equals(Command.DISCONNECT)) {
				disconnect(playerAddress);
			}
		}
	}

	public void exeCommand(String data, String playerAddress) {
		String[] dataList = data.split(",");
		Command cmd = Command.valueOf(dataList[0]);

		switch(cmd) {
		case NEW_PLAYER:
			Player newPlayer = new Player(Server.playerID, (int)(Math.random()*700), (int)(Math.random()*500), 10);


			Server.notifyClient(Command.CONNECTED, newPlayer.toString(), playerAddress);
			if(Server.alivePlayerMap.size() > 0) {
				String playerList = "";
				for (Player player : Server.alivePlayerMap.values()) playerList += "," +player;
				Server.notifyClient(Command.RECEIVE_ALL, playerList.substring(1), playerAddress);
			}
			
			Server.alivePlayerMap.put(playerAddress, newPlayer);
			Server.notifyAllButThis(cmd, newPlayer.toString(), playerAddress);

			System.out.println("PlayerID: " + Server.playerID + " Connected to server");
			Server.playerAddresses.put(playerAddress, Server.playerID);
			Server.playerID++;
			break;

		case MOVE:
			int dx = Integer.valueOf(dataList[2]);
			int dy = Integer.valueOf(dataList[3]);
			int xPos = Integer.valueOf(dataList[4]);
			int yPos = Integer.valueOf(dataList[5]);

			Player movedPlayer = Server.alivePlayerMap.get(playerAddress);
			movedPlayer.update(dx, dy, xPos, yPos);
			Server.updatedPlayers.put(playerAddress, movedPlayer);
			break;

		case HIT:
			break;

		case DEAD:
			Player deadPlayer = Server.alivePlayerMap.get(playerAddress);
			Server.alivePlayerMap.remove(playerAddress);
			Server.deadPlayerMap.put(playerAddress, deadPlayer);

			Server.notifyAllClients(Command.REMOVE, deadPlayer.toString());
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
		Player player = Server.alivePlayerMap.get(playerAddress);
		if(player == null) {
			player = Server.deadPlayerMap.get(playerAddress);
		}

		Server.notifyClient(Command.DISCONNECT, player.toString(), playerAddress);
		if(!player.isDead()) {
			Server.alivePlayerMap.remove(playerAddress);
			Server.notifyAllClients(Command.REMOVE, player.toString());
		} else {
			Server.deadPlayerMap.remove(playerAddress);
		}
		Server.playerAddresses.remove(playerAddress);
		System.out.println("Disconnecting " +player.getID());
	}

}
