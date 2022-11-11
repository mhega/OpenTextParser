import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Optional;
import java.util.logging.Level;

public class TextParser extends JFrame
{
	/** 
	 * Text Parser V 4.9
	 * Author: Mohamed Hegazy
	 */
	private static final long serialVersionUID = 9206356051216703918L;
	private String version = "4.9";
	private static String getRelease()
	{
		return ModuleFactory.getRelease();
	}
	
	private static class Setting
	{		
		public static enum SettingValue
		{
			ENABLED, DISABLED;
		}
		public static final SettingValue DISABLED = SettingValue.DISABLED;
		public static final SettingValue ENABLED = SettingValue.ENABLED;
		public static final Setting AUTOSCROLLDOWN = new Setting(ENABLED, DISABLED);
		public static final Setting FILEREADSUPPORT = new Setting(ENABLED, DISABLED);
		public static final Setting MODULECANCELLATION = new Setting(ENABLED, DISABLED);
		private static Hashtable<Module, Hashtable<Setting, SettingValue> > moduleSettings;
		
		public final SettingValue[] validValues;
		private Setting(Setting.SettingValue... validValues)
		{
			moduleSettings = new Hashtable<Module, Hashtable<Setting, SettingValue> >();
			this.validValues = validValues;
		}
		
		public static void set(Module module, Setting setting, SettingValue value)
		{
			if(!(Arrays.asList(setting.validValues).contains(value)))
			{
				throw new TextParserException("Invalid setting value: "+value.toString());
			}
			else
			{
				moduleSettings.putIfAbsent(module, new Hashtable<Setting,SettingValue>());
				moduleSettings.get(module).put(setting, value);
			}
		}
		public static SettingValue get(Module module, Setting setting)
		{
			return moduleSettings.getOrDefault(module, new Hashtable<Setting, SettingValue>()).get(setting);
		}
		public static SettingValue get(Module module, Setting setting, SettingValue defaultValue)
		{
			if(!(Arrays.asList(setting.validValues).contains(defaultValue)))
				throw new TextParserException("Invalid setting value: "+defaultValue.toString());
			else
				return Optional.ofNullable(get(module, setting)).orElse(defaultValue);
		}
	}
	
	public static class Profile
	{
		public static enum ProfileFunctionality
		{
			ENABLEDISABLE, VISIBLEINVISIBLE;
		}
		private ProfileFunctionality profileFunctionality;
		public Profile()
		{
			this(ProfileFunctionality.ENABLEDISABLE);
		}
		public Profile(Profile.ProfileFunctionality functionality)
		{
			components = new Hashtable<Component, Boolean>();
			profileFunctionality = functionality;
		}
		
		private Hashtable<Component, Boolean> components; 
		public void addComponent(Component component)
		{
			components.put(component, component.isEnabled());
		}
		private boolean getSettingValue(Component component)
		{
			if(ProfileFunctionality.ENABLEDISABLE.equals(this.profileFunctionality))
				return component.isEnabled();
			else if(ProfileFunctionality.VISIBLEINVISIBLE.equals(this.profileFunctionality))
				return component.isVisible();
			else
			{
				throw new TextParserException("Unexpected Exception");
			}
		}
		public void disableAll()
		{
			Enumeration<Component> keys = components.keys();
			while(keys.hasMoreElements())
			{
				Component component = keys.nextElement();
				components.replace(component, getSettingValue(component));
				if(ProfileFunctionality.ENABLEDISABLE.equals(this.profileFunctionality))
					component.setEnabled(false);
				else if(ProfileFunctionality.VISIBLEINVISIBLE.equals(this.profileFunctionality))
					component.setVisible(false);
			}
		}
		public void reenableAll()
		{
			Enumeration<Component> keys = components.keys();
			while(keys.hasMoreElements())
			{
				Component component = keys.nextElement();
				if(ProfileFunctionality.ENABLEDISABLE.equals(this.profileFunctionality))
					component.setEnabled(components.get(component));
				else if(ProfileFunctionality.VISIBLEINVISIBLE.equals(this.profileFunctionality))
					component.setVisible(components.get(component));
				components.replace(component, component.isEnabled());
			}
			
		}
	}
	
	public class ModuleRegistrant
	{
		private JMenu modulesMenu;
		private ButtonGroup moduleButtonGroup;
		private Hashtable<String, JMenu> menus;
		private Hashtable<JMenu, JMenu> parents;
		
		public ModuleRegistrant(JMenu modulesMenu , ButtonGroup moduleButtonGroup)
		{
			menus = new Hashtable<String, JMenu>();
			parents = new Hashtable<JMenu, JMenu>();
			this.modulesMenu = modulesMenu;
			menus.put(modulesMenu.getText(), modulesMenu);
			this.moduleButtonGroup = moduleButtonGroup;
		}
		
		public void disableAutoScrollDown(Module module)
		{
			try
			{
				Setting.set(module, Setting.AUTOSCROLLDOWN, Setting.DISABLED);
			}
			catch(Exception e)
			{
				AppLogger.getLogger().log(Level.WARNING, "Unexpected Exception", e);
			}
		}
		
		public void enableFileReadSupport(Module module)
		{
			//Default for MODULECANCELLATION is ENABLED
			enableFileReadSupport(module, true);
		}
		
		public void enableFileReadSupport(Module module, boolean canBeCancelled)
		{
			try
			{
				Setting.set(module, Setting.FILEREADSUPPORT, Setting.ENABLED);
				//Default for MODULECANCELLATION is ENABLED
				Setting.set(module, Setting.MODULECANCELLATION, canBeCancelled?Setting.ENABLED:Setting.DISABLED);;
				if(module.equals(TextParser.this.replacerModule))
				{
					TextParser.this.open.setVisible(true);
				}
			}
			catch(Exception e)
			{
				AppLogger.getLogger().log(Level.WARNING, "Unexpected Exception", e);
			}
		}
		
		private boolean isFileReadSupportEnabled(Module module)
		{
			try
			{
				// Feature is disabled by default.
				return Setting.get(module, Setting.FILEREADSUPPORT, Setting.DISABLED) == Setting.ENABLED;
			}
			catch(Exception e)
			{
				TextParser.processException(e, Level.WARNING, false, null);
				return false;
			}
			
		}
		
		public void addSubmenu(String submenuName)
		{
			addSubmenu(submenuName, modulesMenu.getText());
		}
		public void addSubmenu(String submenuName, String parentMenuName)
		{
			JMenu parentMenuObject = menus.get(parentMenuName);
			if(parentMenuObject == null)
			{
				throw new TextParserException("INTERNAL : Attempting to add a submenu with a non-existing parent menu name..");
			}
			else if(menus.get(submenuName) != null)
			{
				throw new TextParserException("INTERNAL : Attempting to add a submenu with a name that already exists..");
			}
			else
			{
				JMenu submenuObject = new JMenu(submenuName);
				parentMenuObject.add(submenuObject);
				menus.put(submenuName, submenuObject);
				parents.put(submenuObject, parentMenuObject);
			}
		}
		
		private String getParentComponentName(JMenu leaf)
		{
			/*
			 * Retrieves chain of submenu names for title display*/
			JMenu parent = parents.get((JMenu)leaf);
			if(parent == null)
				return "";
					
			return getParentComponentName(parent) +" - "+ leaf.getText();
		}
		
		public Module registerModule(boolean isDefaultModule, Module module, String moduleName, String promptText, String aboutText)
		{
			return registerModule(modulesMenu.getText(), isDefaultModule, module, moduleName, promptText, aboutText);
		}
		public Module registerModule(String submenuName, boolean isDefaultModule, Module originalModule, String moduleName, String promptText, String aboutText)
		{
			Module module;
			try
			{
				module = (Module)(originalModule.clone());
			}
			catch(CloneNotSupportedException e)
			{
				throw new TextParserException("Internal Error..");
			}
			
			JMenu submenuObject = menus.get(submenuName);
			if(submenuObject ==null)
			{
				throw new TextParserException("INTERNAL : Attempting to register a module with a non-existing submenu name..");
			}
			JRadioButtonMenuItem radioButtonMenuItem = new JRadioButtonMenuItem(moduleName);
			submenuObject.add(radioButtonMenuItem);
			moduleButtonGroup.add(radioButtonMenuItem);			

			radioButtonMenuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					AppLogger.getLogger().info("Switching to "+moduleName);
					TextParser.this.replacerModule = module;
					TextParser.this.setTitle(TextParser.this.title+ getParentComponentName(submenuObject) +" - "+ moduleName);
					if (!"".equals(promptText))
						txt.setText(promptText);
					String crossModuleText = "Selected Module: "+moduleName+"<br>";
					TextParser.this.aboutLabel.setText(TextParser.this.aboutTextHeader+crossModuleText+aboutText+"<br>"+TextParser.this.aboutTextFooter);
					TextParser.this.open.setVisible(isFileReadSupportEnabled(module));
					TextParser.this.fileNameLabel.setText("");
					TextParser.this.fileNameLabel.setVisible(isFileReadSupportEnabled(module));
					TextParser.this.aboutDialog.repaint();
				}
			});
			
			if(isDefaultModule)
			{
				radioButtonMenuItem.doClick();
				TextParser.this.replacerModule = module;
			}
			return module;
		}
	}
 	
	public static void main(String args[])
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		 	new TextParser("TextParser");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
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
	private JPanel topPanel = new JPanel();
	private JLabel fileNameLabel;
	private JButton aboutOkButton = new JButton("OK");
	private JMenuItem copy = new JMenuItem("Copy");
	private JMenuItem paste = new JMenuItem("Paste");
	private JMenuItem open = new JMenuItem("Open");
	private JMenuItem cancel = new JMenuItem("Cancel");
	private Profile cancelProfile;
	private JFileChooser openChooser = new JFileChooser();
	private final JMenu mnModules = new JMenu("Modules");
	private ButtonGroup moduleButtonGroup = new ButtonGroup();
	private Module replacerModule;
	private ConsoleDialog consoleDialog;
	private Profile profile;
	private JLabel bottomLabel;
	private String moduleStatus;
	private SwingWorker<Void,Void> moduleWorker;
	
	public SwingWorker<Void,Void> getModuleWorker()
	{
		return moduleWorker;
	}
	
	
 	private static void processException(Exception e, Level level, boolean popup, Component parent)
 	{
 		AppLogger.getLogger().log(level, "An Unexpected Exception Occurred", e);
		if(popup)
		{
			if(e instanceof TextParserException)
				JOptionPane.showMessageDialog(parent,"Error Occurred! \""+e.getMessage()+"\". Please review Console for details","Error",JOptionPane.ERROR_MESSAGE);
			else
				JOptionPane.showMessageDialog(parent,"Exception Occurred! Please review Console for details","Error",JOptionPane.ERROR_MESSAGE);
		}
 	}
 	
 	private Object getInput()
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
 	
	private boolean isAutoScrollDownEnabled(Module module)
	{
		try
		{
			// Feature is enabled by default.
			return Setting.get(module, Setting.AUTOSCROLLDOWN, Setting.ENABLED) == Setting.ENABLED;
		}
		catch(Exception e)
		{
			TextParser.processException(e, Level.WARNING, false, null);
			return true;
		}
		
	}
	
	private boolean isModuleCancellationEnabled(Module module)
	{
		//Default for MODULECANCELLATION is ENABLED
		try
		{
			return Setting.get(module, Setting.MODULECANCELLATION, Setting.ENABLED) == Setting.ENABLED;
		}
		catch(Exception e)
		{
			TextParser.processException(e, Level.WARNING, false, null);
			return true;
		}
	}
	

 	private void executeReplacementProcess(BufferedReader inputReader)
 	{
 		Object input = inputReader==null?TextParser.this.getInput():inputReader;
 		
		if (input ==null)
				JOptionPane.showMessageDialog(this,"Clipboard empty, or invalid","Error",JOptionPane.ERROR_MESSAGE);
		else
		{		
			moduleWorker = new SwingWorker<Void,Void>()
			{
				public void tryToClose(Closeable reader)
				{
					try
					{
						if(reader!=null)
							reader.close();
					}
					catch(Exception e)
					{
						TextParser.processException(e, Level.WARNING, false, TextParser.this);
					}
				}
				public Void doInBackground()
				{
					try
					{
		 				Module.ModuleContext moduleContext = null;
		 				if(inputReader !=null
	 							&& TextParser.this.isModuleCancellationEnabled(replacerModule))
	 						cancelProfile.reenableAll();
		 				if(!replacerModule.isPromptDisplayEnabled()
		 						|| (moduleContext = replacerModule.display(TextParser.this))!= null)
		 				{
		 					if(moduleContext == null)
		 					{
		 						moduleContext = replacerModule.initContext();
		 					}
		 					profile.disableAll();
		 					fileNameLabel.setText("");
							TextParser.this.setStatus("Processing...");
		 					/*We are cloning this Module on the fly to safeguard the module by preventing instance variables that are created by ModuleFactory developer
 		 					 *  from carrying over to subsequent executions (replacements)*/
 							String result = ((Module)(TextParser.this.replacerModule.clone())).runReplacements(input, moduleContext);
 							TextParser.this.txt.setText(this.isCancelled()?"Execution Canceled!":result);
 							//AutoScrollDown defaults to Enabled
 		 					int caretPosition = TextParser.this.txt.getDocument().getLength();
 		 					if(!isAutoScrollDownEnabled(replacerModule))
 		 						caretPosition = 0;
 		 					
 		 					if(inputReader != null && !this.isCancelled())
 		 						fileNameLabel.setText((String)(inputReader.getClass().getMethod("getFileName").invoke(inputReader)));

 		 					TextParser.this.txt.setCaretPosition(caretPosition);
		 				}
					}
					catch(Exception e)
					{
						TextParser.processException(e, Level.SEVERE, true, TextParser.this);
					}
					finally
					{
						profile.reenableAll();
						if(inputReader !=null 
								&& TextParser.this.isModuleCancellationEnabled(replacerModule))
							cancelProfile.disableAll();
						TextParser.this.setStatus(null);
						tryToClose(inputReader);
					}
					return null;
				}
			};
			moduleWorker.addPropertyChangeListener(new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent pe)
				{
					if("state".equals(pe.getPropertyName())
							&& SwingWorker.StateValue.DONE.equals(pe.getNewValue())
							&& moduleWorker.isCancelled())
					{
						try
						{
							moduleWorker.getClass().getMethod("tryToClose", Closeable.class).invoke(moduleWorker, inputReader);
						}
						catch(Exception e)
						{
							AppLogger.getLogger().log(Level.WARNING, "Unexpected Exception Occurred. Execution could not be cancelled.", e);
						}
						
					}
				}
				
			});
			moduleWorker.execute();
			
		}
 	}
 	
 	private void setStatus(String status)
 	{
 		moduleStatus = status;
 		if(status == null)
 		{
 			bottomLabel.setText("");
 			bottomLabel.setVisible(false);
 		}
 		else
 		{
 	 		bottomLabel.setText(status);
 	 		bottomLabel.setVisible(true);
 		}
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
 	
	private void assemble()
	{	
		aboutTextHeader = "<html><div align='CENTER'>Text Parser&nbsp;&nbsp;V "+version+" R "+TextParser.getRelease()+"<br>"+
				"Performs text cleanup / transformation according to the selected module.<br><br>";
		aboutTextFooter = "<br>Created by Mohamed Hegazy</div></html>";
		aboutDialog = new JDialog(this,"Text Parser");
		aboutDialog.setModal(true);
		aboutLabel = new JLabel();
		profile = new Profile();
		cancelProfile = new Profile(Profile.ProfileFunctionality.VISIBLEINVISIBLE);
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
		moduleStatus = null;
		moduleWorker = null;
		
		fileNameLabel = new JLabel("");
		fileNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		bottomLabel = new JLabel("");
		bottomLabel.setHorizontalAlignment(SwingConstants.CENTER);
		bottomLabel.setVisible(false);
		
		consoleDialog = new ConsoleDialog((JFrame) SwingUtilities.getWindowAncestor(this),"Console");
		JTextArea consoleArea = consoleDialog.getTextArea();
		JMenuItem displayConsoleDialogItem = consoleDialog.getConsoleButton();
		displayConsoleDialogItem.setVisible(true);
		file.add(displayConsoleDialogItem);
		
		open.setVisible(false);
		fileNameLabel.setVisible(false);
		
		/* cancel MenuItem is made visible or invisible via a profile.
		 * So it should be initially made disabled */
		cancel.setVisible(false);
		
		edit.add(copy);
		edit.add(paste);
		edit.add(open);
		edit.add(cancel);
		file.setMnemonic(KeyEvent.VK_F);
		edit.setMnemonic(KeyEvent.VK_E);
		about.setMnemonic(KeyEvent.VK_A);
		copy.setMnemonic(KeyEvent.VK_C);
		paste.setMnemonic(KeyEvent.VK_P);


		mainMenu.add(file);
		mainMenu.add(edit);
		
		AppLogger.getLogger("TextParser", null, null, AppLogger.ONELINEFORMATTER);
		AppLogger.setTextArea(consoleArea);
		AppLogger.getLogger().info(AppLogger.LOGOBANNERTEXTPARSER);


		txt.setEditable(false);
		txt.setFont(Font.decode("Lucida Console"));
		txt.setText("Please use CTRL+V to paste text..");

		txt.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent ke)
			{
				if (ke.getKeyCode() == KeyEvent.VK_V && ke.isControlDown())
				{			
					if(moduleStatus == null)
						TextParser.this.executeReplacementProcess(null);
					else
						setStatus("PROCESSING...");
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
				TextParser.this.executeReplacementProcess(null);
			}
		});
		
		open.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				File workingDirectory = new File(System.getProperty("user.dir"));
				openChooser.setCurrentDirectory(workingDirectory);
				int returnVal = openChooser.showOpenDialog(TextParser.this);
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					File inputFile = openChooser.getSelectedFile();
					
					try
					{
						//BufferedReader inputReader = new BufferedReader(new FileReader(inputFile));
						@SuppressWarnings("resource")
						BufferedReader inputReader = new BufferedReader(new FileReader(inputFile))
						{
							private String fileName = "";
							@SuppressWarnings("unused")
							public String getFileName()
							{
								return fileName;
							}
							public BufferedReader setFileName(String fileName)
							{
								this.fileName = fileName;
								return this;
							}
							
						}.setFileName(inputFile.getName());
						TextParser.this.executeReplacementProcess(inputReader);
					}
					catch(IOException ioe)
					{
						TextParser.processException((TextParserException)(new TextParserException(ioe.getMessage()).initCause(ioe)), Level.SEVERE, true, TextParser.this);
					}
				}
			}
		});
		
		cancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				AppLogger.getLogger().log(Level.INFO,"Module Execution State: "+moduleWorker.getState().toString());
				if(SwingWorker.StateValue.STARTED.equals(moduleWorker.getState()))
				{
					AppLogger.getLogger().log(Level.INFO
							, "Trying to cancel. Cencellation "
							+(moduleWorker.cancel(true)?"successful. An exception may be thrown due to early stream closure.":"failed."));
					
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
		topPanel.setLayout(new BorderLayout());
		topPanel.add(mainMenu, BorderLayout.NORTH);
		topPanel.add(fileNameLabel, BorderLayout.SOUTH);
		contentPane.add(topPanel, BorderLayout.NORTH);
		
		mainMenu.add(mnModules);
		//profile.addComponent(edit);
		profile.addComponent(copy);
		profile.addComponent(paste);
		profile.addComponent(open);
		profile.addComponent(mnModules);
		profile.addComponent(about);
		cancelProfile.addComponent(cancel);

		contentPane.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(bottomLabel, BorderLayout.SOUTH);
		
		ModuleRegistrant reg = new ModuleRegistrant(mnModules,  moduleButtonGroup);
		ModuleFactory.register(reg);

		
	}

}