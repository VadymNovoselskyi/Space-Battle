package duoGame;


import javax.swing.JFrame;
import java.awt.Graphics2D;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Color;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class GameFrame extends JFrame implements KeyListener{
	public enum Key {
		A, D, W, S, SPACE,
		LEFT, RIGHT, UP, DOWN, SHIFT,
		ESC
	}
	public EnumMap<Key, Boolean> keyDown = new EnumMap<>(Key.class);

	private Canvas gameCanvas;
	private BufferStrategy backBuffer;
	private Image background = null;

	private Dimension canvasDimension;
	private int width, height;

	public GameFrame(int width, int height) {;
	this.width = width;
	this.height = height;
	canvasDimension= new Dimension(width, height);
	createWindow();

	addKeyListener(this);
	for (Key key : Key.values()) {
		keyDown.put(key, false);
	}
	}

	public void createWindow() {
		gameCanvas = new Canvas();
		gameCanvas.setSize(canvasDimension);
		gameCanvas.setFocusable(false);

		this.add(gameCanvas);
		this.pack();
		this.setMinimumSize(canvasDimension);
		this.setResizable(false);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Important to control the closing behavior

		gameCanvas.createBufferStrategy(2);
		backBuffer = gameCanvas.getBufferStrategy();
	}

	public void renderBG() {		
		Graphics2D g = (Graphics2D)backBuffer.getDrawGraphics();
		g.drawImage(background, 0, 0, width, height, gameCanvas);
	}
	public void render(Player player) {
		Graphics2D g = (Graphics2D)backBuffer.getDrawGraphics();
		player.draw(g);
		g.dispose();
	}
	public void renderProjectiles(HashMap<Projectile, Player> projectileMap) {
		Graphics2D g = (Graphics2D)backBuffer.getDrawGraphics();
		projectileMap.keySet().forEach(projectile -> projectile.draw(g));
		g.dispose();
	}
	public void renderExplosions(ArrayList<Explosion> explosionList) {
		Graphics2D g = (Graphics2D)backBuffer.getDrawGraphics();
		explosionList.forEach(explosion -> explosion.draw(g));
		g.dispose();
	}

	public void write(String text, int x, int y, Color color, Font font) {
		Graphics2D g = (Graphics2D) backBuffer.getDrawGraphics();
		drawText(g, text, x, y, color, font);
		g.dispose();
		backBuffer.show();
	}
	public void write(String text, Color color, Font font) {
		Graphics2D g = (Graphics2D) backBuffer.getDrawGraphics();
		FontMetrics fontMetrics = g.getFontMetrics(font);
		int x = (width - fontMetrics.stringWidth(text)) / 2;
		int y = ((height - fontMetrics.getHeight()) / 2) + fontMetrics.getAscent();
		drawText(g, text, x, y, color, font);
		g.dispose();
	}

	private void drawText(Graphics2D g, String text, int x, int y, Color color, Font font) {
		g.setFont(font);
		g.setColor(color);
		g.drawString(text, x, y);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		setKeyState(e, true);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		setKeyState(e, false);
	}

	private void setKeyState(KeyEvent e, boolean state) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT -> keyDown.put(Key.LEFT, state);
		case KeyEvent.VK_RIGHT -> keyDown.put(Key.RIGHT, state);
		case KeyEvent.VK_UP -> keyDown.put(Key.UP, state);
		case KeyEvent.VK_DOWN -> keyDown.put(Key.DOWN, state);
		case KeyEvent.VK_SHIFT -> keyDown.put(Key.SHIFT, state);
		
		case KeyEvent.VK_A -> keyDown.put(Key.A, state);
		case KeyEvent.VK_D-> keyDown.put(Key.D, state);
		case KeyEvent.VK_W-> keyDown.put(Key.W, state);
		case KeyEvent.VK_S-> keyDown.put(Key.S, state);
		case KeyEvent.VK_SPACE -> keyDown.put(Key.SPACE, state);
		
		
		case KeyEvent.VK_ESCAPE -> keyDown.put(Key.ESC, state);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}


	public void setBackground(Image background) {
		this.background = background;
	}
}
