package gameClient;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;


public class GameFrame extends JFrame implements KeyListener{
	public HashMap<String, Boolean> keyDown = new HashMap<>();

	private Canvas gameCanvas;
	private BufferStrategy backBuffer;
	private Image background = null;

	private Dimension canvasDimension;
	private int width, height;

	public GameFrame(int width, int height) {
		this.width = width;
		this.height = height;
		canvasDimension= new Dimension(width, height);
		addKeyListener(this);

		createWindow();

		keyDown.put("left", false);
		keyDown.put("right", false);
		keyDown.put("up", false);
		keyDown.put("down", false);
		keyDown.put("esc", false);
		keyDown.put("q", false);
		keyDown.put("space", false);
	}

	public void createWindow() {
		gameCanvas = new Canvas();
		gameCanvas.setSize(canvasDimension);
		gameCanvas.setFocusable(false);

		this.add(gameCanvas);
		this.pack();
		this.setMinimumSize(canvasDimension);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		gameCanvas.createBufferStrategy(2);
		backBuffer = gameCanvas.getBufferStrategy();
	}

	public synchronized void render(ConcurrentHashMap<Integer, Player> playerMap) {
		Graphics2D g = (Graphics2D)backBuffer.getDrawGraphics();
		for (Player player : playerMap.values()) {
			player.draw(g);
		}
		backBuffer.show();
	}
	public synchronized void renderProjectiles(ConcurrentHashMap<Integer, Projectile> projectileMap) {
		Graphics2D g = (Graphics2D)backBuffer.getDrawGraphics();
		for (Projectile player : projectileMap.values()) {
			player.draw(g);
		}
	}
	
	public void write(String text, int x, int y, Color color, Font font) {
		Graphics2D g = (Graphics2D) backBuffer.getDrawGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		g.drawImage(background, 0, 0, width, height, gameCanvas);

		g.setFont(font);
		g.setColor(color);
		g.drawString(text, x, y);
	}
	public void write(String text, Color color, Font font) {
		Graphics2D g = (Graphics2D) backBuffer.getDrawGraphics();
		g.setFont(font);
		g.setColor(color);
		FontMetrics fontMetrics = g.getFontMetrics(font);
		int x = (width - fontMetrics.stringWidth(text)) / 2;
		int y = ((height - fontMetrics.getHeight()) / 2) + fontMetrics.getAscent();

		g.drawString(text, x, y);
		g.dispose();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		if(key == KeyEvent.VK_LEFT) {
			keyDown.put("left", true);
		} else if(key == KeyEvent.VK_RIGHT) {
			keyDown.put("right", true);
		} else if(key == KeyEvent.VK_UP) {
			keyDown.put("up", true);
		} else if(key == KeyEvent.VK_DOWN) {
			keyDown.put("down", true);
		} else if(key == KeyEvent.VK_ESCAPE) {
			keyDown.put("esc", true);
		} else if(key == KeyEvent.VK_Q) {
			keyDown.put("q", true);
		} else if(key == KeyEvent.VK_SPACE) {
			keyDown.put("space", true);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();

		if(key == KeyEvent.VK_LEFT) {
			keyDown.put("left", false);
		} else if(key == KeyEvent.VK_RIGHT) {
			keyDown.put("right", false);
		} else if(key == KeyEvent.VK_UP) {
			keyDown.put("up", false);
		} else if(key == KeyEvent.VK_DOWN) {
			keyDown.put("down", false);
		} else if(key == KeyEvent.VK_SPACE) {
			keyDown.put("space", false);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	public void setBackground(Image background) {
		this.background = background;
	}

	public Dimension getCanvasDimension() {
		return canvasDimension;
	}
}
