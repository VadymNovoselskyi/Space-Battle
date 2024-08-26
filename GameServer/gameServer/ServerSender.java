package gameServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSender {
	private DatagramSocket socket;
	private ExecutorService executor;

	public ServerSender(DatagramSocket socket) {
		this.socket = socket;
		this.executor = Executors.newFixedThreadPool(4); // Adjust the pool size based on your needs
	}

	public void updatePlayers(ConcurrentHashMap<String, Player> updatedPlayers, String address) {		
		String playerList = "";
		for(Player player : updatedPlayers.values()) {
			playerList += "," +player.toString();
		}
		String responseMessage = Command.UPDATE_ALL.toString() + "," + playerList.substring(1);
		
		executor.execute(() -> {
			InetAddress playerIP = null;
			try {
				playerIP = InetAddress.getByName(address.split(":")[0]);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			int playerPort = Integer.parseInt(address.split(":")[1]);
			
			byte[] buffer = responseMessage.getBytes();
			try {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, playerIP, playerPort);
				socket.send(packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public void notifyClient(Command cmd, String data, String address) {
		executor.execute(() -> {
			String responseMessage = cmd.toString() + "," + data;
			InetAddress playerIP = null;
			try {
				playerIP = InetAddress.getByName(address.split(":")[0]);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			int playerPort = Integer.parseInt(address.split(":")[1]);
			
			byte[] buffer = responseMessage.getBytes();
			try {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, playerIP, playerPort);
				socket.send(packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public void shutdown() {
		executor.shutdown();
	}
}
