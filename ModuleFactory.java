import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.AbstractMap;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ModuleFactory
{
	
	
	/*
	 * Author: Mohamed Hegazy
	 * Refer to README.MD for instructions on utilizing ModuleFactory to build new Modules.
	 * Please update the release upon every change to ModuleFactory.
	 * */
	
	public static String getRelease ()
	{
		return "13";
	}
	
	private static class Modules
	{
		
		public static final Module SAMPLEANALYZER1 = new Module()
		{
			protected void replace(Module.ModuleContext moduleCtx)
			{
				addReplaceable(new Replaceable(("(\\s{3,}|\\s+\\d+[)])"),"\n$1"));
			}
		};
		public static final Module SAMPLEANALYZER2 = new Module()
		{
			protected void replace(Module.ModuleContext moduleCtx)
			{
				addReplaceable(new Replaceable().setMethod(c->doAnalyze(c)));
				addReplaceable(new Replaceable(("[ ]+[\r\n$]"),"\n"));
			}
			private String doAnalyze(Module.ModuleContext moduleCtx)
			{
				Pattern somePattern = Pattern.compile("^((?!\\s).)*?[a-zA-Z]");
				//:::
				StringBuilder resultBuffer = new StringBuilder();
				
				Consumer<BufferedReader> processReader = (BufferedReader reader)->{
					try
					{
						//::
						Matcher someMatcher = null;
						//::
						resultBuffer.append("Just a demo");
						
					}
					catch(Exception e)
					{
						AppLogger.getLogger().log(Level.SEVERE, "Unexpected Error Occurred", e);
					}
				};
		
				BufferedReader reader = moduleCtx.getReader();
				if(reader == null)
				{
					BufferedReader stringReader = new BufferedReader(new StringReader(moduleCtx.getInputString()));
					processReader.accept(stringReader);
				}
				else
				{
					processReader.accept(reader);
				}
				
				
				return resultBuffer.toString();
			}
		};

		
		public static final Module SAMPLEANALYZER3  = new Module()
		{
			HashSet<Integer> userInput1 = null;
			String userInput2 = "";
			//:::
			protected void replace(Module.ModuleContext moduleCtx)
			{
				@SuppressWarnings("unchecked")
				HashSet<Integer> userInput1 = (HashSet<Integer>)moduleCtx.get("intList");
				String userInput2 = (String)moduleCtx.get("dataSelection");
				this.userInput1 = userInput1;
				this.userInput2 = userInput2;
				//:::
				addReplaceable(new Replaceable().setMethod(c -> resultHeader(c)));
			}
			
			private String resultHeader(Module.ModuleContext moduleCtx)
			{
				Analyze3Record record = new Analyze3Record(userInput1.size(),userInput2);
				return record.stringValue()+"\n\n"
						//+moduleCtx.getInputString()
						;
			}
			
		};
		
		
	}
	
	private static class Displayables
	{
		private static final Module.Displayable SAMPLEDISPLAYABLE = (Module module, TextParser parent)->{
			JTextField integerListField = new JTextField();
			Object[] choiceList = {"","First Choice","Second Choice"};
			JComboBox<Object> dropDownList = new JComboBox<Object>(choiceList);
			Object[] message = {"Enter Integer, Integer List and/or Integer Range: ",integerListField,"Pick One Choice:", dropDownList};
			HashSet<Integer> intList = null;
			Object dataSelection = choiceList[0];
			Function<String, HashSet<Integer>> parseList = s -> {
				Matcher m = Pattern.compile("((\\d+\\s*)(-(\\s*\\d+\\s*))?)(,(\\s*\\d+\\s*)(-(\\s*\\d+))?)*").matcher(s.trim());
				if(!m.matches())
				{
					AppLogger.getLogger().log(Level.WARNING, "Invalid Text: "+s);
					return null;
				}

				HashSet<Integer> result = new HashSet<Integer>();
				StringTokenizer commaTokenizer = new StringTokenizer(s,",");
				Function<Integer, Boolean> validateValue = (Integer value) ->{
					int upperLimit = 3000;
					if(value > upperLimit)
					{
						AppLogger.getLogger().log(Level.WARNING, "Too Large Integer Value: "+value);
						return false;
					}
					return true;
				};
				while(commaTokenizer.hasMoreTokens())
				{
					String  currentToken = commaTokenizer.nextToken().trim();
					StringTokenizer dashTokenizer = new StringTokenizer(currentToken, "-");
					if(dashTokenizer.countTokens() > 2 )
					{
						AppLogger.getLogger().log(Level.WARNING, "Invalid Range");
						return null;
					}
					else if(dashTokenizer.countTokens() == 1)
					{
						Integer nextInt = Integer.valueOf(dashTokenizer.nextToken().trim());
						if(!validateValue.apply(nextInt))
							return null;
						result.add(nextInt);
					}
					else
					{
						int start = Integer.parseInt(dashTokenizer.nextToken().trim());
						int end = Integer.parseInt(dashTokenizer.nextToken().trim());
						if(!validateValue.apply(start) || !validateValue.apply(end))
							return null;
						
						for (int i=start;i<=end;i++)
						{
							Integer nextInt = Integer.valueOf(i);
							result.add(nextInt);
						}
						
					}
				}
				return result;
			};
			
			while(true)
			{
				try
				{
					int response = JOptionPane.showConfirmDialog(parent, message, "Title", JOptionPane.OK_CANCEL_OPTION);
					if(response != JOptionPane.OK_OPTION)
					{
						return null;
					}
					intList = parseList.apply(integerListField.getText());
					if(intList == null)
					{
						if(parent.getModuleWorker().isCancelled())
							return null;
						else
							throw new TextParserException("We failed to parse the input text");
					}
					dataSelection = dropDownList.getSelectedItem();
					if("".equals(dataSelection))
					{
						AppLogger.getLogger().log(Level.WARNING, "Data Copy Selection is required");
						throw new TextParserException("Data Copy Selection is required");
					}
					
				}
				catch(Exception e)
				{
					AppLogger.getLogger().log(Level.WARNING, e.getMessage());
					continue;
				}

				Module.ModuleContext moduleCtx = module.initContext();
				moduleCtx.put("intList", intList);
				moduleCtx.put("dataSelection",dataSelection);
				return moduleCtx;
			}
		};
	}
	
	public static void register(TextParser.ModuleRegistrant moduleRegistrant)
	{	
		moduleRegistrant.addSubmenu("Menu1");
		moduleRegistrant.registerModule("Menu1",
				false
				, ModuleFactory.Modules.SAMPLEANALYZER1
				, "Sample Analyzer 1"
				, "Please use CTRL+V or Edit menu to paste Sample text..."
				, "Anzlyzes input text data");
		
		moduleRegistrant.addSubmenu("Menu2");
		moduleRegistrant.registerModule("Menu2",
				false
				, ModuleFactory.Modules.SAMPLEANALYZER2
				, "Sample Analyzer 2"
				, "Please use CTRL+V or Edit menu to paste Sample text..."
				, "Analyzes input text data");
		
		Module sampleAnalyzer3 = moduleRegistrant.registerModule("Menu2",
				false
				, ModuleFactory.Modules.SAMPLEANALYZER3
				, "Sample Analyzer 3"
				, "Please use CTRL+V or Edit menu to paste sample text..."
				, "Analyzes input text data");

		
		sampleAnalyzer3.setPromptDisplayMethod(ModuleFactory.Displayables.SAMPLEDISPLAYABLE);
		moduleRegistrant.enableFileReadSupport(sampleAnalyzer3);
		moduleRegistrant.disableAutoScrollDown(sampleAnalyzer3);
		
		
		
		
	}
}