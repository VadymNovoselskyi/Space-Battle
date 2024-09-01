package gameServer;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class ServerReceiver implements Runnable {
	private DatagramSocket socket;

	public ServerReceiver(DatagramSocket socket) {
		this.socket = socket;
		System.out.println("Listening for messages on port: " +socket.getLocalPort());
	}

	@Override
	public void run() {
		//receive
		byte[] buffer = new byte[128];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		try {
			socket.receive(packet);
		} catch (Exception e) {e.printStackTrace();}

		//handle the package
		String message = new String(packet.getData(), 0, packet.getLength());
		String playerAddress = getPlayerAddress(packet);

//		System.out.println("Received from player: " + message +" Address: " +playerAddress);

		//start proccesing
		try {
			if(!Server.deadPlayersMap.containsKey(playerAddress)) {
				executeCommand(message, playerAddress);
			} 
			else if (Server.deadPlayersMap.containsKey(playerAddress)) {
				handleDeadPlayerCommand(message, playerAddress);
			}
		} catch (Exception e) {e.printStackTrace();}
	}

	public void executeCommand(String data, String playerAddress) {
		String[] dataList = data.split(",");
		Command cmd = Command.valueOf(dataList[0]);

		switch(cmd) {
		case NEW_PLAYER:
			System.out.println("PlayerID: " + Server.playerID + " Connected to server");
			Player newPlayer = new Player(Server.playerID, (int)(Math.random()*500 + 150), (int)(Math.random()*400 + 100));
			newPlayer.lastUpdateTime = Long.parseLong(dataList[1]);
			newPlayer.lastPingTime = Long.parseLong(dataList[1]);
			newPlayer.setPlayerAddress(playerAddress);
			Server.playerID++;

			Server.notifyClient(Command.CONNECTED, newPlayer.toString(), playerAddress);
			if(!Server.alivePlayersMap.isEmpty()) {
				String playerList = "";
				for (Player player : Server.alivePlayersMap.values()) playerList += "," +player;
				Server.notifyClient(Command.RECEIVE_ALL, playerList.substring(1), playerAddress);
			}

			Server.alivePlayersMap.put(playerAddress, newPlayer);
			Server.playerAddresses.put(newPlayer, playerAddress);
			Server.notifyAllButThis(cmd, newPlayer.toString(), playerAddress);
			break;

		case MOVE: {			
			double supposedAngle = Double.parseDouble(dataList[4]);
			boolean still = Boolean.parseBoolean(dataList[5]);
			long updateTime = Long.parseLong(dataList[6]);

			Player movedPlayer = Server.alivePlayersMap.get(playerAddress);
			if(still) movedPlayer.setStill(true);
			else {
				movedPlayer.setStill(false);
				handlePlayerMovementUpdate(playerAddress, updateTime, supposedAngle);
			}

			Server.updatedPlayers.put(playerAddress, movedPlayer);
			break;
		}

		case FIRE_LASER: {
			int playerID = Integer.parseInt(dataList[1]);
			double supposedAngle = Double.parseDouble(dataList[4]);
			double angle = Double.parseDouble(dataList[6]);
			long updateTime = Long.parseLong(dataList[7]);

			Player firedPlayer = Server.alivePlayersMap.get(playerAddress);
			handlePlayerMovementUpdate(playerAddress, updateTime, supposedAngle);


			double x = firedPlayer.getxPos() + Player.HITBOX_WIDTH / 2 - Math.sin(angle) * (-Player.HITBOX_HEIGHT / 2)  - Laser.WIDTH / 2;
			double y = firedPlayer.getyPos() + Player.HITBOX_HEIGHT / 2 + Math.cos(angle) * (-Player.HITBOX_HEIGHT / 2) - Laser.HEIGHT / 2;
			Laser newLaser = new Laser(playerID, Server.projectileID++, x, y, angle);
			Server.projectilesList.add(newLaser);

			Server.notifyAllClients(Command.LASER_FIRED, newLaser.toString());
			break;			
		}

		case FIRE_MISSILE: {			
			int playerID = Integer.parseInt(dataList[1]);
			double supposedAngle = Double.parseDouble(dataList[4]);
			double angle = Double.parseDouble(dataList[6]);
			long updateTime = Long.parseLong(dataList[7]);

			Player firedPlayer = Server.alivePlayersMap.get(playerAddress);;
			handlePlayerMovementUpdate(playerAddress, updateTime, supposedAngle);


			double x = firedPlayer.getxPos()+ Player.HITBOX_WIDTH / 2 - Math.sin(angle) * (-Player.HITBOX_HEIGHT / 2) - Missile.WIDTH / 2;
			double y = firedPlayer.getyPos() + Player.HITBOX_HEIGHT / 2 + Math.cos(angle) * (-Player.HITBOX_HEIGHT / 2) - Missile.HEIGHT / 2;
			Missile newMissile = new Missile(playerID, Server.projectileID++, x, y, angle);
			Server.projectilesList.add(newMissile);

			Server.notifyAllClients(Command.MISSILE_FIRED, newMissile.toString());
			break;
		}

		case GET_SERVER_TIME:
			Server.notifyClient(Command.GET_SERVER_TIME, String.valueOf(System.nanoTime()), playerAddress);
			break;

		case PING:
			Server.alivePlayersMap.get(playerAddress).lastPingTime = System.nanoTime();
			break;

		case DEAD:
			Player deadPlayer = Server.alivePlayersMap.get(playerAddress);
			Server.alivePlayersMap.remove(playerAddress);
			Server.deadPlayersMap.put(playerAddress, deadPlayer);

			Server.notifyAllClients(Command.REMOVE, deadPlayer.toString());
			System.out.println("PlayerID: " + deadPlayer.getID() + " DIED");
			deadPlayer.setDead(true);
			break;

		case DISCONNECT:
			Player disconnectingPlayer = Server.alivePlayersMap.get(playerAddress);
			if(disconnectingPlayer == null) disconnectingPlayer = Server.deadPlayersMap.get(playerAddress);
			disconnectPlayer(playerAddress);
			Server.playerAddresses.remove(disconnectingPlayer);
			break;

		default:
			break;
		}
	}

	public void handlePlayerMovementUpdate(String playerAddress, long updateTime, double angle) {
		Player movedPlayer = Server.alivePlayersMap.get(playerAddress);
		if(movedPlayer == null) return;

		movedPlayer.move(updateTime - movedPlayer.lastUpdateTime);
		movedPlayer.updateSupposedAngle(angle);
		movedPlayer.move(System.nanoTime() - updateTime);
		movedPlayer.lastUpdateTime = System.nanoTime();
	}

	public static void disconnectPlayer(String playerAddress) {
		Player player = Server.alivePlayersMap.get(playerAddress);
		System.out.println("Disconnecting " +playerAddress);

		if(player == null) {
			player = Server.deadPlayersMap.get(playerAddress);
			Server.deadPlayersMap.remove(playerAddress);
		}
		else {			
			Server.alivePlayersMap.remove(playerAddress);
			Server.notifyAllButThis(Command.REMOVE, player.toString(), playerAddress);
		}
	}

	private String getPlayerAddress(DatagramPacket packet) {
		InetAddress playerIP = packet.getAddress();
		int playerPort = packet.getPort();
		return playerIP.getHostAddress() + ":" + playerPort;
	}

	private void handleDeadPlayerCommand(String message, String playerAddress) {
		try {
			String[] dataList = message.split(",");
			Command cmd = Command.valueOf(dataList[0]);

			switch (cmd) {
			case PING:
				Server.deadPlayersMap.get(playerAddress).lastPingTime = System.nanoTime();
				break;
			case DISCONNECT:
				disconnectPlayer(playerAddress);
				break;
			default:
				break;
			}
		}catch (Exception e) {e.printStackTrace();}
	}

}
