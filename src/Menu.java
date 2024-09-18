import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Menu{
	
	private JMenuBar appMenu;
	private JMenu file;
	private JMenu help;
	
	private JFrame frame;
	private JPanel panel;
	private JLabel label;

	private JMenuItem start;
	private JMenuItem stop;
	private JMenuItem report;
	private JMenuItem add_host;
	private JMenuItem display;
	private JMenuItem exit;
	
	private JMenuItem info;
	
	private ProxyServer proxy;
	private ServerSocket welcomeSocket;
	private boolean flag;
	
	private Thread thread;
	
	public Menu(JFrame frame){
		this.frame=frame;
		proxy=new ProxyServer();
		frame.getContentPane().removeAll();
		createMenu();
		ActionListener();	
		
	}
	public void createMenu(){
		panel = new JPanel(new BorderLayout());
		label = new JLabel("Click start to run Proxy Server",JLabel.CENTER);
		Font font = new Font("Courier", Font.BOLD,18);
		label.setFont(font);
		panel.add(label,BorderLayout.CENTER);
	    frame.add(panel,BorderLayout.CENTER);
		appMenu = new JMenuBar();
		file = new JMenu("File");
		help = new JMenu("Help");		
		fileMenu();
		helpMenu();
		appMenu.add(file);
		appMenu.add(help);
		
	}
	
	public void fileMenu() {
		start = new JMenuItem("Start");
		stop = new JMenuItem("Stop");
		report = new JMenuItem("Report");
		add_host = new JMenuItem("Add host to filter");
		display = new JMenuItem("Display current filtered hosts");
		exit = new JMenuItem("Exit");
		file.add(start);
		file.add(stop);
		file.add(report);
		file.add(add_host);
		file.add(display);	
		file.add(exit);	
	}
	
	public void helpMenu() {
		info = new JMenuItem("Info");
		help.add(info);
		
	}
	
	
	public void ActionListener(){
		
		start.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(!flag) {
					label.setText("Proxy Server is Running...");
					label.setVerticalTextPosition(JLabel.BOTTOM);
					label.setHorizontalTextPosition(JLabel.CENTER);
					flag=true;
					thread = new Thread("start") {
						public void run() {
							try {							
								welcomeSocket = new ServerSocket(8080);
								while(thread.isAlive()){									
									Socket connectionSocket = welcomeSocket.accept();
									proxy.acceptClient(connectionSocket);									
								}
								
								
							} catch (IOException e1) {
								
								e1.printStackTrace();
							}
						}
					};		
					thread.start();
				}
				else {
					JOptionPane.showMessageDialog(frame,"Proxy server is already running!","Warning",JOptionPane.WARNING_MESSAGE);
				}
					
				
				
				
				
			}
			
		});
				
				
					
			
				
		
		
		stop.addActionListener(new ActionListener() {
			
			@SuppressWarnings("removal")
			@Override
			public void actionPerformed(ActionEvent e) {
				if(flag) {
					flag=false;
					thread.suspend();
					try {
						welcomeSocket.close();
					} catch (IOException e1) {					
						e1.printStackTrace();
					}
					label.setText("<html>Proxy Server is Stopped<br/>Click start to run Proxy Server</html>");
					label.setVerticalTextPosition(JLabel.BOTTOM);
					label.setHorizontalTextPosition(JLabel.CENTER);	
				}
				else {
					JOptionPane.showMessageDialog(frame,"Proxy server is not running!","Warning",JOptionPane.WARNING_MESSAGE);
				}
									
			}
			
		});
		
		report.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String client=JOptionPane.showInputDialog(frame,"Enter the ip address of the client");
				if(client!=null) {
					boolean find;
					find=proxy.searchClient(client);
					int index=proxy.clientIndex;
					if(find){
						proxy.clients.get(index).writeToTxt();
					}
					else {
						JOptionPane.showMessageDialog(frame,"Cannot find a client with the entered ip address","Warning",JOptionPane.WARNING_MESSAGE);
					}
				}
			}
			
		});		
		
		add_host.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String host=JOptionPane.showInputDialog(frame,"Enter the address of the host");
				if(host!=null) {
					proxy.addHostToFilter(host);	
				}								
			}
			
		});
		
		display.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {				
				if(proxy.getForbiddenAddresses().size()>0) {
					String addresses="----------Filtered Hosts----------\n";
		    		
		    		for(String s : proxy.getForbiddenAddresses())
		    		{
		    			addresses += s + "\n";
		    		}
		    		JOptionPane.showMessageDialog(frame, addresses);
				}
				else {
					JOptionPane.showMessageDialog(frame,"There is no filtered hosts","Warning",JOptionPane.WARNING_MESSAGE);
				}					
			}
			
		});
		
		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);				
			}
			
		});
		
		info.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "emir.senel1@std.yeditepe.edu.tr";
				JOptionPane.showMessageDialog(frame, s);
			}
			
		});
			
		
		
	}
	
	public JMenuBar getAppMenu() {
		return appMenu;
	}
	
	
	

}
