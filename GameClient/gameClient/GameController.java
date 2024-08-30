package gameClient;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;

import gameServer.Command;

public class GameController extends Thread{
	private GameFrame gameFrame;
	private Communicator communicator;
	private ClientPlayer me;
	private Player mirrorMe;
	private Image meImg, enemyImg, laserImg, missileImg;
	private Font gameNameFont, gameOverFont;

	private ConcurrentHashMap<Integer,Player> playerMap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, Projectile> projectileMap = new ConcurrentHashMap<>();
	public static final int GAME_WIDTH = 800, GAME_HEIGHT = 600;

	protected long timeAdjusment = 0;
	protected static final int FPS_PLAYER = 60, FPS_SERVER = 12;
	public static final int PLAYER_HITBOX_WIDTH = 42, PLAYER_HITBOX_HEIGHT = 84;

	private boolean gameRunning = true, dead = false;

	public GameController(String host, int port) throws IOException {
		this(host, port, GAME_WIDTH, GAME_HEIGHT);
	}
	public GameController(String host, int port, int gameWidth, int gameHeight) throws IOException {
		gameFrame = new GameFrame(gameWidth, gameHeight);
		loadImages();
		loadFonts();
		communicator = new Communicator(this, host, port);
		gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Call the shutdown method of the communicator to stop its thread
                communicator.closeConnection();

                // Optionally, you can wait for the thread to finish
                try {
                	communicator.join();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                // Dispose the frame and exit the application
                gameFrame.dispose();
                System.exit(0); // Ensure the application exits completely
            }
        });

	}
	

	public void update(long deltaTime) {
		int dx = me.getDirectionX(), dy = me.getDirectionY();

		if(gameFrame.keyDown.get("right")) me.setDirectionX(1);
		else if(gameFrame.keyDown.get("left")) me.setDirectionX(-1);
		else me.setDirectionX(0);

		if(gameFrame.keyDown.get("down")) me.setDirectionY(1);
		else if(gameFrame.keyDown.get("up")) me.setDirectionY(-1);
		else me.setDirectionY(0);

		if(me.getDirectionX() != dx || me.getDirectionY() != dy) {
			me.move(deltaTime);
			communicator.notifyServer(Command.MOVE, me.toString(), System.nanoTime() - timeAdjusment);
		}

		if(gameFrame.keyDown.get("space")) {
			String cmdString = me.tryToFire();
			if(cmdString != null) {
				Command cmd = Command.valueOf(cmdString);
				communicator.notifyServer(cmd, me.toString(), System.nanoTime() - timeAdjusment);
			}
		}

		if(mirrorMe.borderCollision()) {
			communicator.notifyServer(Command.DEAD);
			dead = true;
		}

		if(gameFrame.keyDown.get("esc") || gameFrame.keyDown.get("q")) {
			communicator.notifyServer(Command.DISCONNECT);
		}


	}

	public void updateAll(String[] dataList) {
		int playerID, dx, dy, xPos, yPos;
		long lastUpdateTime;
		for(int i = 1; i < dataList.length-1; i += 6 ) {
			playerID = Integer.valueOf(dataList[i]);
			dx = Integer.valueOf(dataList[i+1]);
			dy = Integer.valueOf(dataList[i+2]);
			xPos = Integer.valueOf(dataList[i+3]);
			yPos = Integer.valueOf(dataList[i+4]);
			lastUpdateTime = Long.valueOf(dataList[i+5]);

			Player updatedPlayer = playerMap.get(playerID);

			if (updatedPlayer != null) {
				updatedPlayer.update(dx, dy, xPos, yPos);
				updatedPlayer.lastUpdateTime = lastUpdateTime;

				double angle; 
				if(dx == 0 && dy == 0) angle = 0;
				else angle = Math.atan2(dy, dx) + Math.PI / 2;
				updatedPlayer.setSupposedAngle(angle);
			}
		}

	}
	public void updatePlayerMap(String data) {
		String[] dataList = data.split(",");

		Command cmd = Command.valueOf(dataList[0]);
		int playerID = Integer.valueOf(dataList[1]);
		int dx = Integer.valueOf(dataList[2]);
		int dy = Integer.valueOf(dataList[3]);
		int xPos = Integer.valueOf(dataList[4]);
		int yPos = Integer.valueOf(dataList[5]);
		//		long lastUpdateTime = Long.valueOf(dataList[6]);

		switch(cmd) {
		case LASER_FIRED: {			
			int projectileID = Integer.valueOf(dataList[6]);
			long lastUpdateTime = Long.valueOf(dataList[7]);
			Laser newLaser = new Laser(laserImg, xPos, yPos, dx, dy, projectileID);
			projectileMap.put(projectileID, newLaser);
			newLaser.lastUpdateTime = lastUpdateTime;
			break;
		}

		case MISSILE_FIRED: {
			int projectileID = Integer.valueOf(dataList[6]);
			long lastUpdateTime = Long.valueOf(dataList[7]);
			Missile newMissile =new Missile(missileImg, xPos, yPos, dx, dy, projectileID);
			projectileMap.put(projectileID, newMissile);
			newMissile.lastUpdateTime = lastUpdateTime;
			break;			
		}

		case HIT:
			int projectileID = Integer.valueOf(dataList[6]);
			projectileMap.remove(projectileID);
			break;


		case CONNECTED:
			me = new ClientPlayer(playerID, xPos, yPos, meImg);
			mirrorMe = new Player(playerID, xPos, yPos, meImg);
			playerMap.put(playerID, mirrorMe);
			this.start();
			break;

		case RECEIVE_ALL:
			for(int i = 1; i < dataList.length-1; i += 6 ) {
				playerID = Integer.valueOf(dataList[i]);
				xPos = Integer.valueOf(dataList[i+3]);
				yPos = Integer.valueOf(dataList[i+4]);

				playerMap.put(playerID, new Player(playerID,xPos, yPos, enemyImg));
			}
			break;

		case NEW_PLAYER:
			playerMap.put(playerID, new Player(playerID,xPos, yPos, enemyImg));
			break;

		case REMOVE:
			playerMap.remove(playerID);
			break;

		case DISCONNECT:
			communicator.closeConnection();
			System.exit(0);
		default:
		}
	}

	public void loadImages() {
		meImg = new ImageIcon(getClass().getResource("/ship_yellow.png")).getImage();
		enemyImg = new ImageIcon(getClass().getResource("/ship_red.png")).getImage();
		laserImg = new ImageIcon(getClass().getResource("/laser.png")).getImage();
		missileImg = new ImageIcon(getClass().getResource("/missile.png")).getImage();

		Image background = new ImageIcon(getClass().getResource("/background.jpg")).getImage();
		gameFrame.setBackground(background);
	}

	public void loadFonts() {
		try {
			String path = getClass().getResource("/droidlover.ttf").getFile();
			path = URLDecoder.decode(path, "utf-8");
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(path));

			gameNameFont = baseFont.deriveFont(30f);
			gameOverFont = baseFont.deriveFont(100f);

		} catch (Exception e) {
			gameNameFont = new Font("Serif", Font.PLAIN, 30);
			gameOverFont = new Font("Serif", Font.PLAIN, 100);
			e.printStackTrace();
		}
	}

	public void movePlayers() {
		for(Player player : playerMap.values()) {
			player.move((System.nanoTime() - timeAdjusment - player.lastUpdateTime));
			player.lastUpdateTime = System.nanoTime() - timeAdjusment;
		}
	}
	public void moveProjectiles() {
		for(Projectile projectile : projectileMap.values()) {
			projectile.move(System.nanoTime() - timeAdjusment - projectile.lastUpdateTime);
			projectile.lastUpdateTime = System.nanoTime() - timeAdjusment;
			if(projectile.borderCollision())  projectileMap.remove(projectile.getProjectileID());
		}
	}


	public void checkCollisions() {
		for(Player player : playerMap.values()) {
			if(player != mirrorMe) {
				if(mirrorMe.collision(player)) {
					//					System.out.println("Collision!!!");
				}
			}
		}
		for(Projectile projectile : projectileMap.values()) {
			if(mirrorMe.collision(projectile)) {

			}
		}
	}

	@Override
	public void run() {
		long lastPlayerUpdateTime = System.nanoTime();
		double delayClient = 1e9 / FPS_PLAYER;
		long lastServerUpdateTime = System.nanoTime();
		double delayServer = 1e9 / FPS_SERVER;

		//		long time = System.nanoTime();
		//		int countServer = 0, countPlayer = 0;
		while(gameRunning) {
			long deltaTimeClient = System.nanoTime() - lastPlayerUpdateTime;
			long deltaTimeServer = System.nanoTime() - lastServerUpdateTime;

			if(deltaTimeServer > delayServer) {
				if(!dead) {
					update(deltaTimeServer);
				} else if(gameFrame.keyDown.get("esc") || gameFrame.keyDown.get("q")) {
					communicator.notifyServer(Command.DISCONNECT, System.nanoTime());
				}
				lastServerUpdateTime = System.nanoTime();
				//				countServer++;
			}
			if(deltaTimeClient > delayClient) {
				gameFrame.write("The best game ever", 10, 40, Color.YELLOW, gameNameFont);
				if(dead) {
					gameFrame.write("You dead lol", Color.RED, gameOverFont);
				}
				movePlayers();
				moveProjectiles();
				//				checkCollisions();
				gameFrame.renderProjectiles(projectileMap);
				gameFrame.render(playerMap);
				lastPlayerUpdateTime = System.nanoTime();
				//				countPlayer++;
			}

			else {
				try {
					Thread.sleep((long) ((delayClient - deltaTimeClient) / 1e6));
				} catch (InterruptedException e) {}
			}
		}
		//		time = System.nanoTime() - time;
		//		System.out.println(countPlayer / (time / 1e9) +" -- " + countServer / (time / 1e9));
	}
}

