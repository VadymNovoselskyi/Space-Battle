package gameClient;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;

public class Explosion {
	public static final int WIDTH = 26, HEIGHT = 26;
	public static final double FADE_OPACITY = 0.08;
	
	private int xPos, yPos, duration;
	private long startTime;
	private Image image;
	
	private float alpha;
	
	public Explosion(int xPos, int yPos, int duration, Image image) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.duration = duration;
		this.image = image;
	}
	
	public void draw(Graphics2D g) {
		alpha += (System.nanoTime() - startTime < (duration*1e9) / 2) ? FADE_OPACITY : -FADE_OPACITY;
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		
		g.drawImage(image, xPos, yPos, null);
	}


	public int getDuration() {
		return duration;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
}
