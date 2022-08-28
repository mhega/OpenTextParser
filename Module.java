import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.*;

public abstract class Module
{

	public static final Module CENTRIFYCLEANER = new Module()
	{
		protected void replace(String input)
		{
			listOfDefaultReplaceables.add(new Replaceable(("[ ]+[\\s\r\n$]"),"\n"));
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
				listOfDefaultReplaceables.add(new Replaceable().setMethod("clearLastDoubleQuote"));
				listOfDefaultReplaceables.add(new Replaceable(memoryAddressRegexp,""));
				
			}

			listOfDefaultReplaceables.add(new Replaceable().setMethod("replaceAllWithNTimes"));
			listOfDefaultReplaceables.add(new Replaceable().setMethod("markCommentedLines"));

			if(!hasMemoryAddresses)
			{
				listOfDefaultReplaceables.add(new Replaceable().setMethod("clearNewLines"));
			}

			listOfDefaultReplaceables.add(new Replaceable(("\\\\n"),"\n"));
			listOfDefaultReplaceables.add(new Replaceable(("\\\\r"),"\n"));
			listOfDefaultReplaceables.add(new Replaceable(("\\\\t"),"\t"));
			listOfDefaultReplaceables.add(new Replaceable(("\\\\\""),"\""));
			listOfDefaultReplaceables.add(new Replaceable().setMethod("restoreCommentedLinesStatus"));


			//####################################################################################################################
		}
		
		//####################################################################################################################
		//####### (Note: All the methods that are being called from callMethod have to be public even though they are in #####
		//####### the same class. Using getMethod method failed to identify private classes                              #####
		//####################################################################################################################

		// Example:
		// public String exampleMethod(String input)
		// {
		//		//transform the input String object..
		//
		//		return input
		// }
		//

		
		public String markCommentedLines(String input)
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

		public String restoreCommentedLinesStatus(String input)
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
		
		public String parseComment(String input) //Added on 3-19: Since a commented line may be ending with unparsed \r or \n, which has been saved in commentStates
		{
			input=input.replaceAll("\\\\r|\\\\n","\n");
			return input;
		}

		public String clearLastDoubleQuote(String input) //Clears a possibly existing exessive double quote in SQL that contains memory addresses
		{
			input = input.replaceAll("\"([.][.][.])?+[\n ]*$","");
			input = input.replaceAll("\\\\$","\\\"");
			return input;
			
		}

		public String replaceAllWithNTimes(String input) //This method replaces the regular expression "'x' <repeats n times>" with n repetitions of character x
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
		
		public String clearNewLines(String input) //This method is only to be envoked if there are no memory addresses in the SQL
		{
			input = input.replaceAll("([\n]?)[ ]?(.{70,}?)\n[ ]?(?=\\S)","$1$2");
			return input;
		}
		
	};
	
	
	protected ArrayList<Replaceable> listOfDefaultReplaceables = new ArrayList<Replaceable>();
	
	protected abstract void replace(String input);

	
	protected String callMethod(String methodName, String input) throws Exception
	{
		String output = "";
		output = (String)(this.getClass().getMethod(methodName, Class.forName("java.lang.String")).invoke((Object)this, (Object)input));
		return output;
	}
	
	public String runReplacements(String input) throws Exception
	{
		
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
	protected String callMethodToReplace;

	public Replaceable(String regex, String replacement)
	{
		this.regex = regex;
		this.replacement = replacement;
		this.callMethodToReplace = "";
	}

	public Replaceable()
	{
		this.regex = "";
		this.replacement = "";
		this.callMethodToReplace = "";
	}
	
	public Replaceable setMethod(String methodName)
	{
		this.callMethodToReplace = methodName;
		return this;
	}
	
	protected boolean replaceViaCallMethod()
	{
		return !("".equals(callMethodToReplace));
	}
	
}
