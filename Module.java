import java.util.ArrayList;
import java.util.Hashtable;
import java.util.function.Function;

public abstract class Module implements Cloneable
{
	private boolean cloned = false;
	private void protect()
	{
		/*
		 * This is to protect the predefined reusable modules in ModuleFactory
		 *  from inadvertent changes to their states by ModuleFactory developers.
		 *  The intent is to always keep the predefined modules unchanged (as templates)
		 *  , so they will be safely shared (optionally registered multiple times to different menu items)
		 *  , while TextParser.ModuleRegistrant class will perform a deep-clone of the Module object
		 *   during every module registration and prior to every replacement cycle.
		 *   The deep-cloned copy of the Module can then be successfully changed
		 *   without throwing the following exception.
		 *  
		 *  List of methods that are protected against changes to un-cloned class instances:
		 *   1. setPromptDisplayMethod
		 *  
		 *  It is preferred to keep the Module class design at the least possible modifiability.
		 *  
		 *  It is also important to note that since the Module is cloned on-the-fly before every replacement cycle
		 *  , any Module instance variables that are defined within ModuleFactory or TextParser classes will not carry over to the clone copy.
		 *  This was done to safeguard the module by preventing instance variables that are created by ModuleFactory developer 
		 *  from carrying over to subsequent executions (replacements)
		 *  
		 *  Any new instance variables that need to be survive through cloning should be copied within the clone method.
		 *  
		 * */
		if(!cloned)
		{
			throw new TextParserException("INTERNAL: Attempting to modify a protected Module instance");
		}
	}
	public Object clone() throws CloneNotSupportedException
	{
		Module o = (Module)super.clone();
		o.displayMethod = this.displayMethod;
		o.listOfReplaceables = (ArrayList<Replaceable>)(this.listOfReplaceables);
		o.cloned = true;
		return o;
	}
	interface Displayable
	{
		/*
		 * Both Module and JFrame (Component) are passed to display method
		 * , since it controls both GUI and module logic.
		 * */
		public DataObjectTable display(Module module, java.awt.Component parent);
	}
	private Displayable displayMethod = null;
	public boolean isPromptDisplayEnabled()
	{
		if(displayMethod == null)
			return false;
		else
			return true;
	}
	
	public DataObjectTable getNewDataObjectTable()
	{
		return new DataObjectTable();
	}
	public class DataObjectTable extends Hashtable<String, Object>
	{
		/*
		 * Although this class is no more than a Hashtable
		 * , it is created for loose-coupled interaction with interfacing classes. 
		 */
		private static final long serialVersionUID = 5524123527653934470L;

		private DataObjectTable()
		{
			super();
		}
	}
	
	private ArrayList<Replaceable> listOfReplaceables;
	
	protected Replaceable addReplaceable(Replaceable replaceable)
	{
		listOfReplaceables.add(replaceable);
		return replaceable;
	}
	
	protected abstract void replace(String input, DataObjectTable dataObjectTable);
	
	public void setPromptDisplayMethod(Displayable  d)
	{
		protect();
		this.displayMethod = d;
	}
	public DataObjectTable display(java.awt.Component parent)
	{
		if(this.isPromptDisplayEnabled())
			return this.displayMethod.display(this, parent);
		else
			throw new TextParserException("INTERNAL: Attempt to invoke Display Method on a Module with no previous registration of a Display Method.");
	}

	
	protected String callMethod(Function<String,String> method, String input) throws Exception
	{
		String output = "";
		output = method.apply(input);
		return output;
	}
	
	public String runReplacements(String input, DataObjectTable dataObjectTable) throws Exception
	{
		protect();
		listOfReplaceables  = new ArrayList<Replaceable>();
		replace(input, dataObjectTable);
		Replaceable[] replaceables = listOfReplaceables.toArray(new Replaceable[0]);
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
	}

	public Replaceable()
	{
		this.regex = "";
		this.replacement = "";
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
