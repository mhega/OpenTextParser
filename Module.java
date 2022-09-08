import java.util.ArrayList;
import java.util.Hashtable;
import java.util.function.Function;

public abstract class Module implements Cloneable
{
	private boolean cloned = false;
	private void protect()
	{
		if(!cloned)
		{
			throw new TextParserException("INTERNAL: Attempting to modify a protected Module instance");
		}
	}
	public Object clone() throws CloneNotSupportedException
	{
		Module o = (Module)super.clone();
		o.displayMethod = this.displayMethod;
		o.listOfDefaultReplaceables = (ArrayList<Replaceable>)(this.listOfDefaultReplaceables);
		o.cloned = true;
		return o;
	}
	interface Displayable
	{
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
		/**
		 * 
		 */
		private static final long serialVersionUID = 5524123527653934470L;

		private DataObjectTable()
		{
			super();
		}
	}
	
	protected ArrayList<Replaceable> listOfDefaultReplaceables;
	
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
		listOfDefaultReplaceables  = new ArrayList<Replaceable>();
		replace(input, dataObjectTable);
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
