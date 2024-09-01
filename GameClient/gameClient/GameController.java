package gameClient;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URLDecoder;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;


public class GameController extends Thread{
	public static final int GAME_WIDTH = 800, GAME_HEIGHT = 600;
	public static final int PLAYER_HITBOX_WIDTH = 42, PLAYER_HITBOX_HEIGHT = 84;
	public static final int FPS_PLAYER = 60, FPS_SERVER = 10;
	private static final int DATA_FIELDS_COUNT = 7;

	ScheduledExecutorService executor;
	private GameFrame gameFrame;
	private Communicator communicator;
	private ClientPlayer me;
	private Player mirrorMe;
	protected long timeAdjusment = 0;

	private ConcurrentHashMap<Integer,Player> playerMap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, Projectile> projectileMap = new ConcurrentHashMap<>();

	private Image meImage, enemyImage, laserImage, missileImage;
	private Font gameNameFont, gameOverFont;
	private boolean dead = false;

	public GameController(String host, int port) throws IOException {
		executor = Executors.newScheduledThreadPool(3);
		gameFrame = new GameFrame(GAME_WIDTH, GAME_HEIGHT);
		loadImages();
		loadFonts();
		communicator = new Communicator(this, host, port);
		executor.scheduleWithFixedDelay(communicator, 0, 1, TimeUnit.MILLISECONDS);

		gameFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				executor.shutdown();
				gameFrame.dispose();
				System.exit(0);
			}
		});
	}


	@Override
	public void run() {
		Runnable clientTask = () -> updateClient();
		Runnable serverTask = () -> updateServer();

		executor.scheduleAtFixedRate(clientTask, 0, 1000 / FPS_PLAYER, TimeUnit.MILLISECONDS);
		executor.scheduleAtFixedRate(serverTask, 0, 1000 / FPS_SERVER, TimeUnit.MILLISECONDS);
	}


	public void updateClient() {
		try {			
			gameFrame.write("THE BEST GAME EVER", 10, 40, Color.YELLOW, gameNameFont);
			if(dead) {
				gameFrame.write("YOU DEAD LOL", Color.RED, gameOverFont);
			}
			movePlayers();
			moveProjectiles();
			gameFrame.renderProjectiles(projectileMap);
			gameFrame.render(playerMap);
		} catch (Exception e) {e.printStackTrace();}
	}

	public void updateServer() {
		try {
			if(!dead) {
				checkMovement((long)(1e9 / FPS_PLAYER));
				updatePlayerState((long)(1e9 / FPS_PLAYER));
			} 
			else if(gameFrame.keyDown.get(GameFrame.Key.ESC) || gameFrame.keyDown.get(GameFrame.Key.Q)) {
				communicator.notifyServer(Command.DISCONNECT);
				executor.shutdown();
				gameFrame.dispose();
				System.exit(0);
				
			}
		} catch (Exception e) {e.printStackTrace();}
	}


	public void checkMovement(long deltaTime) {
		int dxBefore = me.getDirectionX(), dyBefore = me.getDirectionY();

		if(gameFrame.keyDown.get(GameFrame.Key.RIGHT)) me.setDirectionX(1);
		else if(gameFrame.keyDown.get(GameFrame.Key.LEFT)) me.setDirectionX(-1);
		else me.setDirectionX(0);

		if(gameFrame.keyDown.get(GameFrame.Key.DOWN)) me.setDirectionY(1);
		else if(gameFrame.keyDown.get(GameFrame.Key.UP)) me.setDirectionY(-1);
		else me.setDirectionY(0);

		if(me.getDirectionX() != dxBefore || me.getDirectionY() != dyBefore) {
			me.move(deltaTime);
			communicator.notifyServer(Command.MOVE, me.toString(), System.nanoTime() - timeAdjusment);
		}		
	}

	public void updatePlayerState(long deltaTime) {
		if(gameFrame.keyDown.get(GameFrame.Key.SPACE)) {
			String cmdString = me.tryToFire();
			if(cmdString != null) {
				Command cmd = Command.valueOf(cmdString);
				communicator.notifyServer(cmd, me.toString() +","+ String.valueOf(mirrorMe.getAngle()), System.nanoTime() - timeAdjusment);
			}
		}

		if(mirrorMe.borderCollision()) {
			System.out.println(mirrorMe.toString());
			communicator.notifyServer(Command.DEAD);
			dead = true;
		}

		if(gameFrame.keyDown.get(GameFrame.Key.ESC) || gameFrame.keyDown.get(GameFrame.Key.Q)) {
			communicator.notifyServer(Command.DISCONNECT);
			executor.shutdown();
			gameFrame.dispose();
			System.exit(0);
		}
	}

	public void updatePlayerMap(String data) {
		String[] dataList = data.split(",");

		Command cmd = Command.valueOf(dataList[0]);
		int playerID = Integer.parseInt(dataList[1]);
		int xPos = Integer.parseInt(dataList[2]);
		int yPos = Integer.parseInt(dataList[3]);
		double angle = Double.parseDouble(dataList[4]);
		double supposedAngle = Double.parseDouble(dataList[5]);
		boolean still = Boolean.parseBoolean(dataList[6]);
		long lastUpdateTime = Long.parseLong(dataList[7]);

		switch(cmd) {
		case UPDATE_ALL: {
			for(int i = 1; i < dataList.length-1; i += DATA_FIELDS_COUNT ) {
				playerID = Integer.parseInt(dataList[i]);
				xPos = Integer.parseInt(dataList[i+1]);
				yPos = Integer.parseInt(dataList[i+2]);
				angle = Double.parseDouble(dataList[i+3]);
				supposedAngle = Double.parseDouble(dataList[i+4]);
				still = Boolean.parseBoolean(dataList[i+5]);
				lastUpdateTime = Long.parseLong(dataList[i+6]);

				Player updatedPlayer = playerMap.get(playerID);

				if (updatedPlayer != null) {
					updatedPlayer.setStill(still);
					updatedPlayer.update(xPos, yPos, supposedAngle);
					updatedPlayer.angle = angle;
					updatedPlayer.lastUpdateTime = lastUpdateTime;
				}
			}
			break;
		}
		case LASER_FIRED: {			
			int projectileID = Integer.parseInt(dataList[1]);
			Laser newLaser = new Laser(projectileID, xPos, yPos, angle, laserImage);
			projectileMap.put(projectileID, newLaser);
			newLaser.lastUpdateTime = lastUpdateTime;
			break;
		}

		case MISSILE_FIRED: {
			int projectileID = Integer.parseInt(dataList[1]);
			Missile newMissile = new Missile(projectileID, xPos, yPos, angle, missileImage);
			projectileMap.put(projectileID, newMissile);
			newMissile.lastUpdateTime = lastUpdateTime;
			break;			
		}

		case HIT:
			int projectileID = Integer.parseInt(dataList[1]);
			projectileMap.remove(projectileID);
			break;


		case CONNECTED:
			me = new ClientPlayer(playerID, xPos, yPos, meImage);
			mirrorMe = new Player(playerID, xPos, yPos, meImage);
			mirrorMe.lastUpdateTime = lastUpdateTime;
			playerMap.put(playerID, mirrorMe);
			this.start();
			break;

		case RECEIVE_ALL:
			for(int i = 1; i < dataList.length-1; i += DATA_FIELDS_COUNT ) {
				playerID = Integer.valueOf(dataList[i]);
				xPos = Integer.valueOf(dataList[i+1]);
				yPos = Integer.valueOf(dataList[i+2]);
				angle = Double.parseDouble(dataList[i+3]);
				still = Boolean.parseBoolean(dataList[i+5]);
				lastUpdateTime = Long.parseLong(dataList[i+6]);

				Player newPlayer = new Player(playerID, xPos, yPos, enemyImage);
				playerMap.put(playerID, newPlayer);

				newPlayer.setSupposedAngle(angle);
				newPlayer.setStill(still);
				newPlayer.lastUpdateTime = lastUpdateTime;
			}
			break;

		case NEW_PLAYER:
			playerMap.put(playerID, new Player(playerID,xPos, yPos, enemyImage));
			break;

		case REMOVE:
			playerMap.remove(playerID);
			break;

		case DISCONNECT:
			executor.shutdown();
			gameFrame.dispose();
			System.exit(0);
		default:
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



	public void loadImages() {
		meImage = new ImageIcon(getClass().getResource("/ship_yellow.png")).getImage();
		enemyImage = new ImageIcon(getClass().getResource("/ship_red.png")).getImage();
		laserImage = new ImageIcon(getClass().getResource("/laser.png")).getImage();
		missileImage = new ImageIcon(getClass().getResource("/missile.png")).getImage();

		Image background = new ImageIcon(getClass().getResource("/background.jpg")).getImage();
		gameFrame.setBackground(background);
	}

	public void loadFonts() {
		try {
			String path = getClass().getResource("/arcade_game.ttf").getFile();
			path = URLDecoder.decode(path, "utf-8");
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(path));

			gameNameFont = baseFont.deriveFont(24f);
			gameOverFont = baseFont.deriveFont(60f);

		} catch (Exception e) {
			gameNameFont = new Font("Serif", Font.PLAIN, 24);
			gameOverFont = new Font("Serif", Font.PLAIN, 60);
			e.printStackTrace();
		}
	}
}

