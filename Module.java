import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.regex.*;

public abstract class Module
{
/*
 * 2 steps below are needed for defining and registering a new Module to the application:
 * 
 *  Step (1).  Step 1 is to be implemented in "Module.java"
 *  -------------------------------------------------------
 *  
 *  This step implements the logic of the desired module.
 *  See example code snippet below for line numbers.
 *  
 *  * Line 1. 		Instantiate a static final object of an anonymous subclass of the abstract class "Module".
 *  
 *  * Line 3. 		Override abstract method "replace" to define your own implementation of the module.
 *  
 *  * Lines 5-6		Add as many calls as needed to "listOfDefaultReplaceables.add()" function
 *  				for implementing replacement logic.
 *  				This can be achieved through specifying on-the-fly regexp-replacement pair as in line 5
 *  				or specifying a function (method) to be called for complex replacement logic as in line 6. 
 *  				In line 6, the syntax in setMethod (s -> replacementFunction(s)) is for creating a lambda function to encapsulate the custom created function.
 *  				Replacement is performed in the same order as calls are made to "add()".
 *  				i.e., the output of each replacement is fed as an input to the next replacement.
 *  
 *  * Lines 9-11	Define the body of the custom function(s) in line 6. 
 *  
 *  1.	public static final Module FILERTBLHDR = new Module()
 *  2.	{
 *  3.		protected void replace(String input)
 *  4.		{
 *  5.			listOfDefaultReplaceables.add(new Replaceable("<<Regular Expression pattern to look for>>","<<Replacement Text>>"));
 *  			AND/OR
 *  6.			listOfDefaultReplaceables.add(new Replaceable().setMethod(s -> replacementFunction(s)));
 *  			:::
 *  7.		}
 *  8.
 *  9.		private String replacementFunction(String input)
 *  10.		{
 *  			::::	//This is your own code. Do whatever is needed to transform the input String and return the result String back to the caller.
 *  11.		}
 *  12.	}
 *  
 *  
 *  Step (2).  Step 2 is to be implemented in "TextParser.java"
 *  -----------------------------------------------------------
 *  
 *  This step registers the previously created logic (Module object) to the GUI (TextParser)
 *  See example code snippet below for line numbers.
 *  
 * 	Line 3.			At the very end of the body of the "assemble" function, call registerModule function
 * 					to register the previously created Module instant to the application GUI.
 * 					
 * 					"reg" is an instance of "ModuleRegistrant" class
 * 					which is in charge of creating the GUI components of the module and linking them to the Module.
 * 					
 * 					Parameters of registerModule:
 * 
 * 						   Parameter Name		Type						
 * 						1. isDefaultModule	-	boolean (true/false)	-	whether this is the default module when the program loads.
 * 																			Specify only one default module.
 * 						2. module			-	Module					- 	The static "Module" object which was created in step 1.
 * 						3. moduleName		-	String					-	The name of the module as desired to be used in various parts of the GUI.
 * 						4. promptText		-	String					-	The prompt string to be initially displayed in the Paste area when the module is selected.
 * 						5. aboutText		-	String					-	A one-liner description of the module to be displayed in the "About" dialog box.
 * 
 *	1.	public void assemble()
 *	2.	{
 *			::::	//No changes needed in the "assemble" body other than adding the next line.
 *	3.		reg.registerModule(true, Module.FILERTBLHDR, "FilerTableHeader", "Please use CTRL+V to paste Filer text...", "Extracts table information from Table Headers");
 *	4.	}

 *
 *
 * 
 * */
	
	public static final Module CENTRIFYCLEANER = new Module()
	{
		protected void replace(String input)
		{
			listOfDefaultReplaceables.add(new Replaceable(("[ ]+[\r\n$]"),"\n"));
		}
		
	};

	public static final Module FILERTBLHDR = new Module()
	{
		protected void replace(String input)
		{
			listOfDefaultReplaceables.add(new Replaceable().setMethod(s -> processFiler(s)));
			listOfDefaultReplaceables.add(new Replaceable(("[ ]+[\r\n$]"),"\n"));
		}
		
		private String processFiler(String input)
		{
			//StringBuffer replacement = new StringBuffer();
			Record record = new Record();
			StringTokenizer tokenizer = new StringTokenizer(input, "\n");
			String currentToken = "";
			Pattern pDb = Pattern.compile("\\s*Database Name\\s+:\\s+\"(.*?)\"\\s*");
			Pattern pTb = Pattern.compile("\\s*Table Name\\s+:\\s+\"(.*?)\"\\s*");
			Pattern pFb = Pattern.compile("\\s*Protection\\s+:\\s+(.*?)$");
			/*
			 * The intent of this validation array here is to make sure
			 *  occurrences of DatabaseName, TableName, and Protection matches are in the expected order.
			 * The "next" counter is incremented within the tokenizer loop
			 *  and used to perform a mod operation with the array length for order validation.
			 * */
			Pattern[] validationArray  = new Pattern[]{pDb,pTb,pFb};
			int next = 0;
			Matcher currentMatcher = null;
			String currentDb = "";
			String currentTable = "";
			String currentProtection = "";
			while(tokenizer.hasMoreTokens())
			{
				currentToken = tokenizer.nextToken();

				if((currentMatcher = pDb.matcher(currentToken)).find())
				{
					if(pDb.equals(validationArray[next%validationArray.length]))
					{
						currentDb = currentMatcher.group(1);
						//replacement.append(currentMatcher.group(1));
						next++;
					}
					else
					{
						throw new TextParserException("Invalid Filer Text");
					}
				}
				else if((currentMatcher = pTb.matcher(currentToken)).find())
				{
					if(pTb.equals(validationArray[next%validationArray.length]))
					{
						currentTable = currentMatcher.group(1);
						//replacement.append("."+currentTable);
						next++;
					}
					else
					{
						throw new TextParserException("Invalid Filer Text");
					}
				}
				else if((currentMatcher = pFb.matcher(currentToken)).find())
				{
					if(pFb.equals(validationArray[next%validationArray.length]))
					{
						currentProtection = currentMatcher.group(1).trim();
						//replacement.append("  ("+currentProtection+")\n");
						record.addRecord(currentDb+"."+currentTable, currentProtection);
						currentDb = "";
						currentTable = "";
						currentProtection = "";
						next++;
					}
					else
					{
						throw new TextParserException("Invalid Filer Text");
					}
				}
			}
			if(!pDb.equals(validationArray[next%validationArray.length]))
				throw new TextParserException("Invalid or incomplete Filer Text");
			//return replacement.toString();
			return record.stringValue();
		
		}
	};
	
	public static final Module SQLCLEANER = new Module()
	{
		String memoryAddressRegexp = "([\"]?+([.][.][.])?+[ ]*?\n)?+0[xX][0-9a-fA-F]+?:[ ]*[\"]?+";
		Hashtable<Double,String> commentStates;
		boolean hasMemoryAddresses(String input)
		{
			return Pattern.compile(memoryAddressRegexp).matcher(input).find();
		}
		protected void replace(String input)
		{
			//####################################################################################################################
			//####### (Note: The tool is very sensitive to order of replacements. i.e, order of Replaceables in the array) #######
			//####################################################################################################################
			

			boolean hasMemoryAddresses = hasMemoryAddresses(input);
			if(hasMemoryAddresses)
			{
				listOfDefaultReplaceables.add(new Replaceable().setMethod(s -> clearLastDoubleQuote(s)));
				listOfDefaultReplaceables.add(new Replaceable(memoryAddressRegexp,""));
				
			}

			listOfDefaultReplaceables.add(new Replaceable().setMethod(s -> replaceAllWithNTimes(s)));
			listOfDefaultReplaceables.add(new Replaceable().setMethod(s -> markCommentedLines(s)));

			if(!hasMemoryAddresses)
			{
				listOfDefaultReplaceables.add(new Replaceable().setMethod(s -> clearNewLines(s)));
			}

			listOfDefaultReplaceables.add(new Replaceable(("\\\\n"),"\n"));
			listOfDefaultReplaceables.add(new Replaceable(("\\\\r"),"\n"));
			listOfDefaultReplaceables.add(new Replaceable(("\\\\t"),"\t"));
			listOfDefaultReplaceables.add(new Replaceable(("\\\\\""),"\""));
			listOfDefaultReplaceables.add(new Replaceable().setMethod(s -> restoreCommentedLinesStatus(s)));


			//####################################################################################################################
		}

		
		private String markCommentedLines(String input)
		{
			commentStates = new Hashtable<java.lang.Double, String>();
			double mark;
			String occurance;
			Matcher p = Pattern.compile("--.*?(\\\\r|\\\\n|\n)").matcher(input); //.*? is used for the expression to select the smallest match. Regexps by default pick the largest match
			
			
			while(p.find())
			{
				mark = Math.random();
				occurance = input.substring(p.start(),p.end());
				commentStates.put(new Double(mark),occurance);
			}
			Enumeration<Double> keys = commentStates.keys();
			while(keys.hasMoreElements()) //Replace all occurances of comments in the string with the corresponding random marks.
			//Had to do that in a separate loop, as altering the input in the same loop above messed up with the whole thing. So had to make sure the string is unchanged during teh loop execution 
			{
				Double d = (Double)(keys.nextElement());
				String s = (String)(commentStates.get(d));
				input=input.replace((String)s, Double.toString(d)); //Using replace() not replaceAll() since we are dealing with literal sting here. replace() is new in JDK1.5
				//Here dealing with the string (comment) as a literal string (not regular expression) is important since we don't know what's in there
			}
			return input;	
		}

		private String restoreCommentedLinesStatus(String input)
		{
			
			Enumeration<Double> keys = commentStates.keys();
			while(keys.hasMoreElements())
			{
				
				Double d = (Double)(keys.nextElement());
				String s = (String)(commentStates.get(d));
				
				input=input.replace(Double.toString(d),parseComment((String)(s)));
				//replaceAll() may treat the random mark in the string as regexp, and therefore treat the decimal dot as any character: Not a big deal, but not what we expect to happen
			}
			
			return input;
		}
		
		private String parseComment(String input) //Added on 3-19: Since a commented line may be ending with unparsed \r or \n, which has been saved in commentStates
		{
			input=input.replaceAll("\\\\r|\\\\n","\n");
			return input;
		}

		private String clearLastDoubleQuote(String input) //Clears a possibly existing exessive double quote in SQL that contains memory addresses
		{
			input = input.replaceAll("\"([.][.][.])?+[\n ]*$","");
			input = input.replaceAll("\\\\$","\\\"");
			return input;
			
		}

		private String replaceAllWithNTimes(String input) //This method replaces the regular expression "'x' <repeats n times>" with n repetitions of character x
		{
			int numOfRepeats = -1;
			Hashtable<String, StringBuffer> theTable = new Hashtable<String, StringBuffer>();
			String letterToRepeat = "";
			StringBuffer replacement= new StringBuffer("");
			String occurrance = "";
			String occurrance2 = "";
			StringBuffer replacement2 = new StringBuffer("");
			Matcher m = Pattern.compile(("(\")?(,)?\\s*('(.)'\\s+<repeats\\s+(\\d++)\\s+times>)(,\\s+\")?+")).matcher(input);

			//During the following code, it was not possible to do on the fly replacement inside the first loop which checks for pattern matches and compose the replacement string
			//That would screw it. The only way to do that was to store the occurances of the matches, and the corresponding replacement strings into a Hashtable, and then do the replacements separately in the while next loop

			while(m.find())
			{
				replacement = new StringBuffer("");
				try
				{
					numOfRepeats = Integer.parseInt(m.group(5));
				}
				catch (NumberFormatException nfe)
				{
					throw nfe;
				}
				letterToRepeat = m.group(4);
				for (int i=0; i<numOfRepeats; i++)
				{
					replacement.append(letterToRepeat);
				}
				occurrance = input.substring(m.start(),m.end());
				theTable.put(occurrance, replacement);
			}
			Enumeration<String> theTableKeys = theTable.keys();
			while(theTableKeys.hasMoreElements())
			{
				occurrance2 = (String)(theTableKeys.nextElement());
				replacement2 = (StringBuffer)(theTable.get(occurrance2));
				input=input.replace(occurrance2, replacement2);
			}
			return input;
		}
		
		private String clearNewLines(String input) //This method is only to be envoked if there are no memory addresses in the SQL
		{
			input = input.replaceAll("([\n]?)[ ]?(.{70,}?)\n[ ]?(?=\\S)","$1$2");
			return input;
		}
		
	};
	
	
	protected ArrayList<Replaceable> listOfDefaultReplaceables;
	
	protected abstract void replace(String input);

	
	protected String callMethod(Function<String,String> method, String input) throws Exception
	{
		String output = "";
		//output = (String)(this.getClass().getMethod(methodName, Class.forName("java.lang.String")).invoke((Object)this, (Object)input));
		output = method.apply(input);
		return output;
	}
	
	public String runReplacements(String input) throws Exception
	{
		listOfDefaultReplaceables  = new ArrayList<Replaceable>();
		replace(input);
		Replaceable[] replaceables = listOfDefaultReplaceables.toArray(new Replaceable[0]);
		for (int i=0 ; i<replaceables.length ; i++) 
		{
			if(replaceables[i].replaceViaCallMethod())
			{
				input = callMethod(replaceables[i].callMethodToReplace,input);
			}
			else
			{
				input = input.replaceAll(replaceables[i].regex, replaceables[i].replacement);	
			}
		}

		return input;
	}
	
}
class Replaceable
{
	protected String regex;
	protected String replacement;
	protected Function<String,String> callMethodToReplace = null;

	public Replaceable(String regex, String replacement)
	{
		this.regex = regex;
		this.replacement = replacement;
		//this.callMethodToReplace = "";
	}

	public Replaceable()
	{
		this.regex = "";
		this.replacement = "";
		//this.callMethodToReplace = "";
	}
	
	public Replaceable setMethod(Function<String,String> method)
	{
		this.callMethodToReplace = method;
		return this;
	}
	
	protected boolean replaceViaCallMethod()
	{
		return !(callMethodToReplace == null);
	}
	
}

class TextParserException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4310496290737841066L;
	
	public TextParserException(String message)
	{
		super(message);
	}
	
}
