import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class AppLogger /*extends Logger*/ {
	/*For utilizing AppLogger:
	 * (1) Declare java.util.logging.Logger.
	 * 
	 * private Logger logger = null;
	 * 
	 * (2) Instantiate the Logger using AppLogger.getLogger method.
	 * 
	 * logger = AppLogger.getLogger("<<PanelName>>", "<<Directory path>>", "<<FileName>>.log", AppLogger.ONELINEFORMATTER);
	 * OR
	 * logger = AppLogger.getLogger("<<PanelName>>", "<<Directory path>>", "<<FileName>>.log");
	 * 
	 * Specify Directory and file as null if no file logging desited. You can later add TextAreaHandler in the below steps
	 * 
	 * ***Note: There is only one static logger  maintained by this class.
	 * Any subsequent run of the above getLogger methods will replace the logger with a new one, and will close any file handlers associated with the old.
	 * For logging to multiple files, see bullet (7).
	 * 
	 * (3) Instantiate new ProgressDialog. This extends JDialog, and will encapsulate all the needed components for the console Dialog box.
	 * getWindowAncestor method is used to retrieve the top level frame to be used as the owner of the dialog box.
	 * 
	 * ProgressDialog consoleDialog = new ProgressDialog((JFrame) SwingUtilities.getWindowAncestor(this),"Progress");
	 * JTextArea progressArea = consoleDialog.getTextArea();
	 * JButton displayConsoleDialogButton = consoleDialog.getProgressButton();
	 * 
	 * (4) Add the button to the proper panel. The button is invisible by default
	 * 
	 * (5) Pass the textArea from step 3 to the logger.
	 * AppLogger.setTextArea(progressArea);
	 * 
	 * (6) Use the logger. 
	 * 
	 * For logging the Teradata banner:
	 * logger.info(AppLogger.LOGOBANNERTEXTPARSER);
	 * 
	 * For logging informational level message:
	 * String message = ...
	 * logger.info(message);
	 * 
	 * For warning level message:
	 * logger.warning(message);
	 * 
	 * For logging an exception as SEVERE level:
	 * catch(IOException ioe) {
	 * 		logger.log(Level.SEVERE, "An Unexpected Exception Occurred", ioe);
	 * }
	 * 
	 * (7) For adding more File Handler(s) to the logger so multiple files can be logged into at the same time, use
	 * AppLogger.addFileHandler(String uniqueName, String filePath, String fileName).
	 * Always use this rather than logger.addHandler in order to hide the details of keeping track of the handler for later removal.
	 *
	 * (8) When the additional file logging is done, use AppLogger.removeFileHandler(String uniqueName).
	 * Pass the same uniqueName that was used to add the handler (7)
	 * 
	 * (9) For disassociating the TextArea handler previously set, use AppLogger.setTextArea(null) - just for the sake of removing overhead logging.
	 * Rerunning setTextArea(textArea) at any time will replace any old TextArea handler with the new one.
	 * 
	 * (10) For retrieving the same logger from  any other utility class e.g, SSHTunnel, or DirectoryZip, use the no-argument getLogger() method
	 * This will retrieve the logger instance that was created from the most recent panel that called this class.
	 * 
	 * Logger logger = AppLogger.getLogger()
	 * 
	 * (11) Make sure to close the logger by running AppLogger.closeLogger("<<PanelName>>");
	 * 
	 * */
	
	private static class TextAreaHandler extends StreamHandler
	{
		private JTextArea textArea = null;
		private void setTextArea(JTextArea textArea)
		{
			this.textArea = textArea;
		}
		
		public void publish(LogRecord record)
		{
			super.publish(record);
			if(textArea != null)
			{
				textArea.append(this.getFormatter().format(record));
				if(textArea.getParent().getParent() instanceof JScrollPane)
				{
					textArea.setCaretPosition(textArea.getDocument().getLength());
				}
			}
		}
	}
	public static boolean setTextArea(JTextArea textArea)
	{
		if(theLogger == null)
			return false;
		if(textArea == null) /*Designed so if a null is passed, then get rid of the text area handlers*/
		{
			return AppLogger.replaceTextAreaHandler(null);
		}
		TextAreaHandler taHandler = new TextAreaHandler();
		taHandler.setTextArea(textArea);
		try
		{
			taHandler.setFormatter(theLogger.getHandlers()[0].getFormatter());
		}
		catch(Exception e)
		{
			return false;
		}
		return AppLogger.replaceTextAreaHandler(taHandler);
	}
	private static boolean replaceTextAreaHandler(TextAreaHandler newHandler)
	{

		Handler[] existingHandlers = theLogger.getHandlers();
		for(int i=0;i<existingHandlers.length;i++)
		{
			if(existingHandlers[i] instanceof TextAreaHandler)
				theLogger.removeHandler(existingHandlers[i]);
		}
		if(newHandler == null)
			return true;
		theLogger.addHandler(newHandler);
		return true;
	}
	private static Logger theLogger = null;
	private static Logger dummy = null; /*Used for avoiding NullPointerException as a result of returning null Logger to the caller*/  

	public static final String LOGOBANNERTEXTPARSER = new Object()
	{
		public String readLogo()
		{
			BufferedReader buffer;
			StringBuffer result = new StringBuffer();
			try
			{
				buffer = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/logo.txt")));
				
				String line;
				while((line = buffer.readLine())!=null)
				{
					result.append(line+"\n");
				}
				buffer.close();
			}
			catch(Exception e)
			{
				return "";
			}
			
			
			return result.toString();
			
		}
	}.readLogo();
	
	
	public static final Level DISPLAY = new Level("DISPLAY",1800)
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	};
	
	public static final Formatter SIMPLEFORMATTER = new SimpleFormatter();
	public static final Formatter ONELINEFORMATTER = new Formatter()
	{
		public String padding(int r)
		{
			StringBuffer b = new StringBuffer();
			for(int i=0 ; i<r ; i++)
			{
				b.append(" ");
			}
			return b.toString();
		}
		public String format(LogRecord record)
		{
			String newLine = System.getProperty("line.separator");
			if(record.getLevel().equals(AppLogger.DISPLAY))
			{
				return record.getMessage().replaceFirst("::BANNER::", "").replaceAll("[\r\n]", newLine);
			}
			StringBuilder recordBuffer = new StringBuilder();
			recordBuffer.append("[").append(new Date(record.getMillis())).append("]");
			
			if(record.getLevel().equals(Level.INFO))
			{
				recordBuffer.append("             ");
			}
			else if(record.getLevel().equals(Level.WARNING))
			{
				recordBuffer.append(" --WARNING-- ");  
			}
			else if(record.getLevel().equals(Level.SEVERE) | record.getLevel().equals(Level.FINER))
			{
				recordBuffer.append(" ***Error*** ");
			}
			else
			{
				recordBuffer.append(record.getLevel().getLocalizedName());
			}
			recordBuffer.append(record.getMessage().replaceAll("[\r\n]", newLine+padding(43))+newLine);
			if(record.getThrown() != null)
			{
				try
				{
					StringWriter stringWriter = new StringWriter();
					PrintWriter printWriter = new PrintWriter(stringWriter);
					record.getThrown().printStackTrace(printWriter);
					printWriter.close();
					recordBuffer.append(stringWriter.toString());
				}
				catch(Exception e)
				{
					
				}
			}
			return recordBuffer.toString();
		}
	};
	/*private AppLogger(String name, String resourceBundleName)
	{
		super(name, resourceBundleName);
	}*/
	private static boolean replaceHandler(Handler newHandler)
	{
		if(newHandler == null)
			return false;
		Handler[] existingHandlers = theLogger.getHandlers();
		for(int i=0;i<existingHandlers.length;i++)
		{
			if(existingHandlers[i] != null && existingHandlers[i] instanceof FileHandler)
			{
				existingHandlers[i].close();
			}
			theLogger.removeHandler(existingHandlers[i]);
		}
		theLogger.addHandler(newHandler);
		return true;
		
	}
	private static Logger getSomeDummyLogger()
	{
		dummy = Logger.getAnonymousLogger();
		dummy.setFilter(new Filter()
		{
			public boolean isLoggable(LogRecord record)
			{
				return false;
			}
		});
		return dummy;
	}
	public static Logger getLogger(String panelName, String filePath, String fileName, Formatter formatter)
	{
		if(theLogger == null)
		{
			theLogger = Logger.getLogger(panelName);
		}
		FileHandler loggerHandler;
		try
		{
			if(filePath == null || fileName == null) //This is in case the logger is only needed for text area logging.
			{
				loggerHandler = new FileHandler();
				loggerHandler.setFilter(new Filter()
				{
					public boolean isLoggable(LogRecord record)
					{
						return false;
					}
				});
			}
			else
				loggerHandler = new FileHandler(filePath+"\\"+fileName,true);
		}
		catch(IOException ioe)
		{
			return getSomeDummyLogger();
		}
		loggerHandler.setFormatter(formatter);
		if(replaceHandler(loggerHandler))
		{
			return theLogger;
		}
		else
			return getSomeDummyLogger();
	}
	
	private static Hashtable<String, FileHandler> handlerTable = new Hashtable<String, FileHandler>();
	public static boolean addFileHandler(String uniqueName, String filePath, String fileName)
	{
		if(uniqueName == null || handlerTable.get(uniqueName) != null || theLogger == null)
		{
			return false;
		}
		FileHandler newHandler;
		try
		{
			newHandler = new FileHandler(filePath+"\\"+fileName,true);
			newHandler.setFormatter(theLogger.getHandlers()[0].getFormatter());
			theLogger.addHandler(newHandler);
			handlerTable.put(uniqueName, newHandler);
		}
		catch(Exception e)
		{
			AppLogger.getLogger().log(Level.SEVERE, "An Unexpected Exception Occurred", e);
			return false;
		}
		return true;
	}
	public static boolean removeFileHandler(String uniqueName)
	{
		if(uniqueName == null || handlerTable.get(uniqueName) == null || theLogger == null)
		{
			return false;
		}
		try
		{
			handlerTable.get(uniqueName).close();
			theLogger.removeHandler(handlerTable.get(uniqueName));
			handlerTable.remove(uniqueName);
		}
		catch(Exception e)
		{
			AppLogger.getLogger().log(Level.SEVERE, "An Unexpected Exception Occurred", e);
			return false;
		}
		return true;
	}
	public static boolean endLogger(String panelName)
	{
		if(theLogger == null || !panelName.equals(theLogger.getName()))
		{
			endDummyLogger();
			return false;
		}
		for(Handler h: theLogger.getHandlers())
		{
			if(h instanceof FileHandler)
			{
				h.flush();
				h.close();
			}
		}
		theLogger = null;
		handlerTable = new Hashtable<String, FileHandler>();
		endDummyLogger();
		return true;
	}
	public static boolean loggerExists()
	{
		return (theLogger != null) || (dummy != null);
	}
	private static void endDummyLogger()
	{
		if(dummy == null)
			return;
		for(Handler h: dummy.getHandlers())
		{
			h.flush();
			h.close();
		}
		dummy = null;
	}
	public static Logger getLogger(String panelName, String filePath, String fileName)
	{
		return getLogger(panelName, filePath, fileName, ONELINEFORMATTER);
	}
	public static Logger getLogger()
	{
		if(theLogger!=null)
		{
			return theLogger;
		}
		else
		{
			return getSomeDummyLogger();
		}
	}
}
