package gameClient;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URLDecoder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;

public class GameController extends Thread{
	public static final int FRAME_WIDTH = 1080, FRAME_HEIGHT = 720, GAME_WIDTH = (int) (FRAME_WIDTH * Math.sqrt(2)), GAME_HEIGHT = (int) (FRAME_HEIGHT * Math.sqrt(2));
	public static final int PLAYER_HITBOX_WIDTH = 42, PLAYER_HITBOX_HEIGHT = 84;
	public static final int FPS_PLAYER = 60, FPS_SERVER = 12;
	private static final int DATA_FIELDS_COUNT = 9;

	ScheduledExecutorService executor;
	private GameFrame gameFrame;
	private Communicator communicator;
	private ClientPlayer me;
	private Player mirrorMe;
	private String name;
	protected long timeAdjusment = 0;

	private ConcurrentHashMap<Integer,Player> playerMap = new ConcurrentHashMap<>();
	private static ArrayList<Player> playerList = new ArrayList<>();
	private ConcurrentHashMap<Integer, Projectile> projectileMap = new ConcurrentHashMap<>();
	private static List<Explosion> explosionList = Collections.synchronizedList(new ArrayList<>());


	private static Image meImage, enemyImage, laserImage, missileImage, explosionImage;
	protected static Font gameNameFont, gameOverFont, playerNameFont, scoreboardFont;
	private boolean dead = false;

	public GameController(String host, int port, String name) throws IOException {
		this.name = name;
		executor = Executors.newScheduledThreadPool(3);
		gameFrame = new GameFrame(FRAME_WIDTH, FRAME_HEIGHT);
		loadImages();
		loadFonts();
		communicator = new Communicator(this, host, port, name);
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
			movePlayers();
			moveProjectiles();
			checkExplosions();

			gameFrame.renderBG(mirrorMe.getXPos(), mirrorMe.getYPos());
			gameFrame.write("THE BEST GAME EVER", 0.01, 0.02, Color.YELLOW, gameNameFont);
			if(dead) {
				gameFrame.write("YOU DEAD LOL", 0.5, 0.4, Color.RED, gameOverFont);
				gameFrame.write("To play again press 'Enter'", 0.5, 0.52, Color.RED, scoreboardFont);
			}
			int count = 0; 
			for(Player player : playerList) {
				count++;
				gameFrame.write(player.getName() +": " +player.getKillCount(), 0.98, count * 0.03, Color.GREEN, scoreboardFont);	
			}

			gameFrame.renderProjectiles(projectileMap);
			gameFrame.renderExplosions(explosionList);
			gameFrame.render(playerMap, mirrorMe);
		} catch (Exception e) {e.printStackTrace();}
	}

	public void updateServer() {
		try {
			if(!dead) {
				checkMovement((long)(1e9 / FPS_PLAYER));
				updatePlayerState((long)(1e9 / FPS_PLAYER));
			} 
			else if(gameFrame.keyDown.get(GameFrame.Key.ENTER)) {
				communicator.notifyServer(Command.REVIVE);
			}

			if(gameFrame.keyDown.get(GameFrame.Key.ESC) || gameFrame.keyDown.get(GameFrame.Key.Q)) {
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
	}

	public void updatePlayerMap(String data) {
		String[] dataList = data.split(",");

		Command cmd = Command.valueOf(dataList[0]);
		int id = Integer.parseInt(dataList[1]);
		String name = dataList[2];
		int xPos = Integer.parseInt(dataList[3]);
		int yPos = Integer.parseInt(dataList[4]);
		double angle = Double.parseDouble(dataList[5]);
		double supposedAngle = Double.parseDouble(dataList[6]);
		int killCount = Integer.parseInt(dataList[7]);
		boolean still = Boolean.parseBoolean(dataList[8]);
		long lastUpdateTime = Long.parseLong(dataList[9]);

		switch(cmd) {
		case UPDATE_ALL: {
			for(int i = 1; i < dataList.length-1; i += DATA_FIELDS_COUNT ) {
				id = Integer.parseInt(dataList[i]);
				xPos = Integer.parseInt(dataList[i+2]);
				yPos = Integer.parseInt(dataList[i+3]);
				angle = Double.parseDouble(dataList[i+4]);
				supposedAngle = Double.parseDouble(dataList[i+5]);
				killCount = Integer.parseInt(dataList[i+6]);
				still = Boolean.parseBoolean(dataList[i+7]);
				lastUpdateTime = Long.parseLong(dataList[i+8]);

				Player updatedPlayer = playerMap.get(id);

				if (updatedPlayer != null) {
					updatedPlayer.setStill(still);
					updatedPlayer.update(xPos, yPos, angle, supposedAngle);
					updatedPlayer.setKillCount(killCount);
					updatedPlayer.lastUpdateTime = lastUpdateTime;
				}
				else { 
					if(id == me.getPlayerID()) {
						updatedPlayer = new Player(id, xPos, yPos, angle, name, meImage);
						me = new ClientPlayer(id, xPos, yPos, angle, name, meImage);
						mirrorMe = updatedPlayer;
						dead = false;
						System.out.println(updatedPlayer.toString() +" " +playerMap.size());
					}
					else updatedPlayer = new Player(id, xPos, yPos, angle, name, enemyImage);

					playerMap.put(id, updatedPlayer);
					playerList.add(updatedPlayer);

					updatedPlayer.setKillCount(killCount);
					updatedPlayer.setStill(still);
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

		case HIT: {
			Projectile projectile = projectileMap.get(id);
			synchronized(explosionList) {
				double x = xPos+ projectile.getWidth() / 2 - Math.sin(angle) * (-projectile.getHeight() / 2) - Explosion.WIDTH / 2;
				double y = yPos + projectile.getHeight() / 2 + Math.cos(angle) * (-projectile.getHeight() / 2) - Explosion.HEIGHT / 2;
				Explosion explosion = new Explosion((int)x, (int)y, 1, explosionImage);
				explosion.setStartTime(System.nanoTime());
				explosionList.add(explosion);
			}
			projectileMap.remove(id);
			break;
		}

		case CONNECTED:
			me = new ClientPlayer(id, xPos, yPos, angle, this.name, meImage);
			mirrorMe = new Player(id, xPos, yPos, angle, this.name, meImage);
			mirrorMe.lastUpdateTime = lastUpdateTime;
			playerMap.put(id, mirrorMe);
			playerList.add(mirrorMe);
			this.start();
			break;

		case RECEIVE_ALL:
			for(int i = 1; i < dataList.length-1; i += DATA_FIELDS_COUNT ) {
				id = Integer.valueOf(dataList[i]);
				name = dataList[i+1];
				xPos = Integer.valueOf(dataList[i+2]);
				yPos = Integer.valueOf(dataList[i+3]);
				angle = Double.parseDouble(dataList[i+4]);
				killCount = Integer.parseInt(dataList[i+6]);
				still = Boolean.parseBoolean(dataList[i+7]);
				lastUpdateTime = Long.parseLong(dataList[i+8]);

				Player newPlayer = new Player(id, xPos, yPos, angle, name, enemyImage);
				playerMap.put(id, newPlayer);
				playerList.add(newPlayer);

				newPlayer.setKillCount(killCount);
				newPlayer.setStill(still);
				newPlayer.lastUpdateTime = lastUpdateTime;
			}
			break;

		case NEW_PLAYER:
			Player newPlayer = new Player(id, xPos, yPos, angle, name, enemyImage);
			playerMap.put(id, newPlayer);
			playerList.add(newPlayer);
			break;

		case REMOVE:
			if(id == mirrorMe.getPlayerID()) dead = true;
			playerList.remove(playerMap.get(id));
			playerMap.remove(id);
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

	public void checkExplosions() {
		long currentTime = System.nanoTime();
		synchronized(explosionList) {			
			Iterator<Explosion> iterator = explosionList.iterator();
			while (iterator.hasNext()) {
				Explosion explosion = iterator.next();
				if (explosion.getDuration() * 1e9 < currentTime - explosion.getStartTime()) iterator.remove();
			}
		}
	}


	public static void sortPlayers() {
	    int n = playerList.size();
	    boolean swapped;
	    for (int i = 0; i < n - 1; i++) {
	        swapped = false;
	        for (int j = 0; j < n - i - 1; j++) {
	            if (playerList.get(j).getKillCount() < playerList.get(j + 1).getKillCount()) {
	                Player temp = playerList.get(j);
	                playerList.set(j, playerList.get(j + 1));
	                playerList.set(j + 1, temp);
	                swapped = true;
	            }
	        }
	        if (!swapped) break;
	    }
	}


	public void loadImages() {
		meImage = new ImageIcon(getClass().getResource("/ship_yellow.png")).getImage();
		enemyImage = new ImageIcon(getClass().getResource("/ship_red.png")).getImage();
		laserImage = new ImageIcon(getClass().getResource("/laser.png")).getImage();
		missileImage = new ImageIcon(getClass().getResource("/missile.png")).getImage();
		explosionImage = new ImageIcon(getClass().getResource("/explosion.png")).getImage();

		Image background = new ImageIcon(getClass().getResource("/tile_bg.jpg")).getImage();
		gameFrame.setBackground(background);
	}

	public void loadFonts() {
		try {
			String path = getClass().getResource("/arcade_game.ttf").getFile();
			path = URLDecoder.decode(path, "utf-8");
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(path));

			gameNameFont = baseFont.deriveFont(24f);
			gameOverFont = baseFont.deriveFont(60f);
			playerNameFont = baseFont.deriveFont(20f);
			scoreboardFont = baseFont.deriveFont(14f);

		} catch (Exception e) {
			gameNameFont = new Font("Serif", Font.PLAIN, 24);
			gameOverFont = new Font("Serif", Font.PLAIN, 60);
			e.printStackTrace();
		}
	}
}

