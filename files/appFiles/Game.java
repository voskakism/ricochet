import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Game extends JPanel implements ActionListener, KeyListener
{
	JFrame f;
	int width;
	int height;
	
	Timer t;
	Random r;
	boolean paused;
	
	private static final int xBezel = 7;  // total vertical bezels' thickness in pixels
	private static final int yBezel = 29; // total horizontal bezels' thickness in pixels
	private static final int ballDiameter = 40;
	
	int redComponent;
	int x;
	int y;
	double velx;
	double vely;
	double vel;
	
	int foodX;
	int foodY;
	int foodVal;
	
	int poisonX;
	int poisonY;
	double poisonChance;
	boolean poisonOnScreen;
	
	BufferedImage skull;
	
	long score;
	
	Clip bumpClip;
	Clip pickUpClip;
	Clip gameOverClip;
	
	public Game(int width, int height, JFrame f)
	{
		this.f = f;
		paused = false;
		r = new Random();
		score = 0;
		this.width = width;
		this.height = height;
		t = new Timer(5, this);
		redComponent = 0;
		x = 0;
		y = 0;
		velx = 0;
		vely = 0;
		vel = 0;
		skull = loadImage("images/skull.png");
		poisonOnScreen = false;
		newFood();
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		
		try{
			File bumpSound = new File("audio/bump.wav");
			File pickUpSound = new File("audio/accept.wav");
			File gameOverSound = new File("audio/scream.wav");
		
			AudioInputStream aisBump = AudioSystem.getAudioInputStream(bumpSound);
			AudioInputStream aisPickUp = AudioSystem.getAudioInputStream(pickUpSound);
			AudioInputStream aisGameOver = AudioSystem.getAudioInputStream(gameOverSound);
			
			bumpClip = AudioSystem.getClip();
			pickUpClip = AudioSystem.getClip();
			gameOverClip = AudioSystem.getClip();
			
			bumpClip.open(aisBump);
			pickUpClip.open(aisPickUp);
			gameOverClip.open(aisGameOver);
			
		} catch(UnsupportedAudioFileException | IOException | LineUnavailableException e){
			e.printStackTrace();
		}
		t.start();
	}
	
	private BufferedImage loadImage(String fileName)
	{
		BufferedImage img = null;
		try{
			img = ImageIO.read(new File(fileName));
		} catch(IOException e){
			e.printStackTrace();
		}
		return img;
	}
	
	public void newFood()
	{
		foodVal = (r.nextInt(10) + 1) * 50;
		foodX = r.nextInt(width - xBezel - foodVal);
		foodY = r.nextInt(height - yBezel - foodVal);
		
		poisonChance = r.nextInt(100);
		if(poisonChance < 30){
			poisonOnScreen = true;
			do{
				poisonX = r.nextInt(width - xBezel - 80);
				poisonY = r.nextInt(height - yBezel - 80);
			} while(((poisonX + 80) > foodX) && (poisonX < (foodX + foodVal)) && ((poisonY + 80) > foodY) && (poisonY < (foodY + foodVal)));
		} else{
			poisonOnScreen = false;
			poisonX = -100;
			poisonY = -100;
		}
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		
		vel = Math.sqrt(Math.pow(velx, 2) + Math.pow(vely, 2));
		redComponent = (int)(vel * 20 + 0.5);
		if(redComponent > 255) redComponent = 255;
		g2.setPaint(new Color(redComponent, 0, 0));
		g2.fill(new Ellipse2D.Double(x, y, ballDiameter, ballDiameter));
		g2.setColor(Color.GREEN);
		g2.fill(new Rectangle2D.Double(foodX, foodY, foodVal, foodVal));
		g2.setColor(Color.BLACK);
		g2.fill(new Rectangle2D.Double(poisonX, poisonY, 80, 80));
		if(poisonOnScreen){
			g2.drawImage(skull, null, poisonX, poisonY);
		}
		g2.setFont(new Font("Courier New", Font.BOLD, 20));
		if(score > 0){
			g2.drawString("Score: " + Long.toString(score), 15, 40);
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		// Collision Detection
		// Food
		if(((x + 20) > foodX) && ((x + 20) < (foodX + foodVal))){
			if(((y + 20) > foodY) && ((y + 20) < (foodY + foodVal))){
				playSound(pickUpClip);
				score += foodVal;
				newFood();
			}
		}
		
		// Poison
		if(((x + 20) > poisonX) && ((x + 20) < (poisonX + 80))){
			if(((y + 20) > poisonY) && ((y + 20) < (poisonY + 80))){
				playSound(gameOverClip);
				t.stop();
				JOptionPane.showMessageDialog(null, "Your Score: " + score, "Game Over", JOptionPane.ERROR_MESSAGE);
				WindowEvent we = new WindowEvent(f, WindowEvent.WINDOW_CLOSING);
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(we);
				/*f.setVisible(false);
				f.dispose();*/
				//System.exit(0);
			}
		}
		
		if(x < 0){
			velx *= (-1);
			playSound(bumpClip);
		}
		if(x > (width - xBezel - ballDiameter)){
			velx *= (-1);
			playSound(bumpClip);
		}
		if(y < 0){
			vely *= (-1);
			playSound(bumpClip);
		}
		if(y > (height - yBezel - ballDiameter)){
			vely *= (-1);
			playSound(bumpClip);
		}
		x += velx;
		y += vely;
		repaint();
	}
	
	public void up()
	{
		vely -= 2;
	}
	
	public void down()
	{
		vely += 2;
	}
	
	public void left()
	{
		velx -= 2;
	}
	
	public void right()
	{
		velx += 2;
	}
	
	public void stopBall()
	{
		velx = 0;
		vely = 0;
	}
	
	public void togglePause()
	{
		paused = !paused;
		if(paused){
			t.stop();
		} else{
			t.start();
		}
	}
	
	public void keyPressed(KeyEvent e)
	{
		int code = e.getKeyCode();
		if(!paused){
			if(code == KeyEvent.VK_UP){
				up();
			}
			if(code == KeyEvent.VK_DOWN){
				down();
			}
			if(code == KeyEvent.VK_LEFT){
				left();
			}
			if(code == KeyEvent.VK_RIGHT){
				right();
			}
			if(code == KeyEvent.VK_SPACE){
				stopBall();
			}
		}
		if(code == KeyEvent.VK_P){
			togglePause();
		}
	}
	
	public void keyTyped(KeyEvent e){}
	public void keyReleased(KeyEvent e){}
	
	public void playSound(Clip clip)
	{
		if(clip == gameOverClip){
			clip.start();
		} else{
			if(clip.isRunning()){
				clip.stop();
			}
			clip.setFramePosition(0);
			clip.start();
		}
	}
}