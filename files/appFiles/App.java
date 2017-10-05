import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

public class App
{
	public static void main(String[] args)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int)screenSize.getWidth();
		int height = (int)screenSize.getHeight();
		JFrame f = new JFrame();
		f.setSize(width, height);
		Game g = new Game(width, height, f);
		f.add(g);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		f.setVisible(true);
	}
}