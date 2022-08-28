import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;

public class TextParser extends JFrame
{
	
	private class ModuleRegistrant
	{
		private JMenu modulesMenu;
		private ButtonGroup moduleButtonGroup;
		
		public ModuleRegistrant(JMenu modulesMenu , ButtonGroup moduleButtonGroup)
		{
			this.modulesMenu = modulesMenu;
			this.moduleButtonGroup = moduleButtonGroup;
		}
		public void registerModule(boolean isDefaultModule, Module module, String moduleName, String promptText, String aboutText)
		{
			JRadioButtonMenuItem radioButtonMenuItem = new JRadioButtonMenuItem(moduleName);
			modulesMenu.add(radioButtonMenuItem);
			moduleButtonGroup.add(radioButtonMenuItem);			
			

			radioButtonMenuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					TextParser.this.replacerModule = module;
					TextParser.this.setTitle(TextParser.this.title+" - "+ moduleName);
					if (!"".equals(promptText))
						txt.setText(promptText);
					String crossModuleText = "Selected Module: "+moduleName+"<br>";
					TextParser.this.aboutLabel.setText(TextParser.this.aboutTextHeader+crossModuleText+aboutText+"<br>"+TextParser.this.aboutTextFooter);
					TextParser.this.aboutDialog.repaint();
				}
			});
			
			if(isDefaultModule)
			{
				radioButtonMenuItem.doClick();
				TextParser.this.replacerModule = module;
			}
		}
	}
	public static void main(String args[])
	{
		new TextParser("TextParser");
	}

	public TextParser(String name)
	{
		super(name);
		this.title = name;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(800,600);
		this.assemble();
		this.setVisible(true);
	}
	
	private String title = "";
	private String aboutTextHeader;
	private String aboutTextFooter;
	private JMenuBar mainMenu = new JMenuBar();
	private JMenu file = new JMenu("File");
	private JMenu edit = new JMenu("Edit");
	private JMenuItem exit = new JMenuItem("Exit");
	private JMenuItem about = new JMenuItem("About");
	private JScrollPane scrollPane;
	private JTextArea txt = new JTextArea();
	private JDialog aboutDialog;
	private JLabel aboutLabel;
	private JPanel aboutOkPanel = new JPanel();
	private JButton aboutOkButton = new JButton("OK");
	private JMenuItem copy = new JMenuItem("Copy");
	private JMenuItem paste = new JMenuItem("Paste");
	private final JMenu mnModules = new JMenu("Modules");
	private ButtonGroup moduleButtonGroup = new ButtonGroup();
	private Module replacerModule;

 	private String getClipboard()
	{
    		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

    		try 
    		{
        		if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) 
        		{
            			String text = (String)t.getTransferData(DataFlavor.stringFlavor);
            			return text;
        		}
    		}
		catch (UnsupportedFlavorException ufe)
		{ufe.printStackTrace();}
		catch (IOException ioe) 
		{ioe.printStackTrace();}
    		return null;
	}
		
	public void assemble()
	{	
		aboutTextHeader = "<html><div align='CENTER'>Text Parser&nbsp;&nbsp;1.0<br>"+
				"Performs text cleanup / transformation according to the selected module.<br><br>";
		aboutTextFooter = "<br>Created by Mohamed Hegazy</div></html>";
		aboutDialog = new JDialog(this,"Text Parser");
		aboutDialog.setModal(true);
		aboutLabel = new JLabel();
		aboutLabel.setHorizontalAlignment(SwingConstants.CENTER);
		aboutLabel.setVerticalAlignment(SwingConstants.CENTER);
		aboutDialog.getContentPane().add(aboutLabel);
		aboutOkButton.setMaximumSize(new Dimension(25,25));
		aboutOkPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		aboutOkPanel.add(aboutOkButton);
		aboutDialog.getContentPane().add(aboutOkPanel,BorderLayout.SOUTH);
		aboutDialog.setSize(450,250);
		scrollPane = new JScrollPane(txt);
		file.add(about);
		file.add(exit);
		edit.add(copy);
		edit.add(paste);
		file.setMnemonic(KeyEvent.VK_F);
		edit.setMnemonic(KeyEvent.VK_E);
		about.setMnemonic(KeyEvent.VK_A);
		copy.setMnemonic(KeyEvent.VK_C);
		paste.setMnemonic(KeyEvent.VK_P);
		mainMenu.add(file);
		mainMenu.add(edit);
		

		txt.setEditable(false);
		txt.setText("Please use CTRL+V to paste text..");

		txt.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent ke)
			{
				String clipBoardContents;
				try
				{
					if (ke.getKeyCode() == KeyEvent.VK_V && ke.isControlDown())
					{
						clipBoardContents = TextParser.this.getClipboard();
						if (clipBoardContents ==null)
							JOptionPane.showMessageDialog(TextParser.this,"Clipboard empty, or invalid","Error",JOptionPane.ERROR_MESSAGE);
						else
							TextParser.this.txt.setText(replacerModule.runReplacements(clipBoardContents));
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(TextParser.this,"Error Occurred! Please consult the developer","Error",JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		exit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				System.exit(0);
			}
		});
		
		about.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				aboutDialog.setVisible(true);
			}
		});

		copy.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				TextParser.this.txt.selectAll();
				TextParser.this.txt.copy();
			}
		});



		paste.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				String clipBoardContents = TextParser.this.getClipboard();
				try
				{
					if (clipBoardContents ==null)
						JOptionPane.showMessageDialog(TextParser.this,"Clipboard empty, or invalid","Error",JOptionPane.ERROR_MESSAGE);
					else
						TextParser.this.txt.setText(replacerModule.runReplacements(clipBoardContents));
				}
				catch(Exception e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(TextParser.this,"Error Occurred! Please consult the developer","Error",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		aboutOkButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				aboutDialog.dispose();
			}
		});
		
		getContentPane().setLayout(new BorderLayout());
		Container contentPane = this.getContentPane();
		contentPane.add(mainMenu, BorderLayout.NORTH);
		
		mainMenu.add(mnModules);
		
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		ModuleRegistrant reg = new ModuleRegistrant(mnModules,  moduleButtonGroup);
		reg.registerModule(false, Module.SQLCLEANER, "SQLCleaner", "Please use CTRL+V to paste SQL text..", "Cleans SQL extracted from Teradata Database dumps");
		reg.registerModule(true, Module.CENTRIFYCLEANER, "CentrifyTextParser", "Please use CTRL+V to paste CENTRIFY text...", "Cleans Text extracted from Centrify connections");
		
		
	}

}