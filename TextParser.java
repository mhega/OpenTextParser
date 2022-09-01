import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.logging.Level;

public class TextParser extends JFrame
{
	/** 
	 * Text Parser V 2.6
	 * Author: Mohamed Hegazy
	 */
	private static final long serialVersionUID = 9206356051216703918L;
	private String version = "2.6";
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
					AppLogger.getLogger().info("Switching to "+moduleName);
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
	private ConsoleDialog consoleDialog;

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
 	
 	private void processException(Exception e)
 	{
 		AppLogger.getLogger().log(Level.SEVERE, "An Unexpected Exception Occurred", e);
		if(e instanceof TextParserException)
			JOptionPane.showMessageDialog(TextParser.this,"Error Occurred! \""+e.getMessage()+"\". Please review Console for details","Error",JOptionPane.ERROR_MESSAGE);
		else
			JOptionPane.showMessageDialog(TextParser.this,"Exception Occurred! Please review Console for details","Error",JOptionPane.ERROR_MESSAGE);
 	}
 	private class ConsoleDialog extends JDialog {
 		
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private JMenuItem openConsole = new JMenuItem("Console");
 		private JTextArea consoleTextArea = new JTextArea();
 		private JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);
 		public ConsoleDialog(Frame owner, String title)
 		{
 			super(owner,title);
 			openConsole.setVisible(false); //By default set as invisible until collection is executed.	
 			
 			consoleTextArea.setEditable(false);
 			consoleTextArea.setFont(Font.decode("Lucida Console"));
 			this.setModalityType(Dialog.ModalityType.valueOf("APPLICATION_MODAL"));
 			this.add(consoleScrollPane,BorderLayout.CENTER);
 			this.setSize(1200, 400);
 			
 			
 			openConsole.addActionListener(new ActionListener()
 			{
 				public void actionPerformed(ActionEvent ae)
 				{
 					ConsoleDialog.this.setLocationRelativeTo(TextParser.this.file);
 					ConsoleDialog.this.setVisible(true);
 				}
 			});
 		}
 		public JTextArea getTextArea()
 		{
 			return consoleTextArea;
 		}
 		public JMenuItem getConsoleButton()
 		{
 			return openConsole;
 		}
 	}
 	
	public void assemble()
	{	
		aboutTextHeader = "<html><div align='CENTER'>Text Parser&nbsp;&nbsp;"+version+"<br>"+
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
		
		consoleDialog = new ConsoleDialog((JFrame) SwingUtilities.getWindowAncestor(this),"Console");
		JTextArea consoleArea = consoleDialog.getTextArea();
		JMenuItem displayConsoleDialogItem = consoleDialog.getConsoleButton();
		displayConsoleDialogItem.setVisible(true);
		file.add(displayConsoleDialogItem);
		
		edit.add(copy);
		edit.add(paste);
		file.setMnemonic(KeyEvent.VK_F);
		edit.setMnemonic(KeyEvent.VK_E);
		about.setMnemonic(KeyEvent.VK_A);
		copy.setMnemonic(KeyEvent.VK_C);
		paste.setMnemonic(KeyEvent.VK_P);


		mainMenu.add(file);
		mainMenu.add(edit);
		
		AppLogger.getLogger("TextParser", null, null, AppLogger.ONELINEFORMATTER);
		AppLogger.setTextArea(consoleArea);


		txt.setEditable(false);
		txt.setFont(Font.decode("Lucida Console"));
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
					processException(e);
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
				aboutDialog.setLocationRelativeTo(TextParser.this.file);
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
					processException(e);
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
		reg.registerModule(false, Module.CENTRIFYCLEANER, "CentrifyTextParser", "Please use CTRL+V to paste CENTRIFY text...", "Cleans Text extracted from Centrify connections");
		reg.registerModule(true, Module.FILERTBLHDR, "FilerTableHeader", "Please use CTRL+V to paste Filer text...", "Extracts table information from Table Headers");

		
	}

}