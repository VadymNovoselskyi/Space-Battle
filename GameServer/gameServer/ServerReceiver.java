package gameServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerReceiver implements Runnable {
    private DatagramSocket socket;
    private ExecutorService threadPool;

    public ServerReceiver(DatagramSocket socket) {
        this.socket = socket;
        this.threadPool = Executors.newFixedThreadPool(4); // Adjust pool size as needed
    }

    @Override
    public void run() {
    	System.out.println("Listening for messages on port: " +socket.getLocalPort());
        try {
            while (true) {
                byte[] buffer = new byte[32];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Submit the packet processing task to the thread pool
                threadPool.submit(new PacketProcessor(packet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
