import java.awt.Dimension;

import javax.swing.JFrame;


public class Display{
	
	private JFrame frame;
	
	
	private String title;
	private int width,height;

	public Display(String title,int width,int height) {
		this.title=title;
		this.width=width;
		this.height=height;
		
		createDisplay();
	}
	
	

	public void createDisplay() {
		frame = new JFrame(title);
		frame.setPreferredSize(new Dimension(width,height));
		
		
	   
	
		Menu menu = new Menu(frame);
		
		frame.setJMenuBar(menu.getAppMenu());		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);		
		frame.pack();
		frame.setLocationRelativeTo(null);		
	}

	
	
	
	
	
	
	
	
	

}
