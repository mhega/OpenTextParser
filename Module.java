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
		o.dataObjectTable = (Hashtable<String,Object>)(this.dataObjectTable.clone());
		o.displayMethod = this.displayMethod;
		o.listOfDefaultReplaceables = (ArrayList<Replaceable>)(this.listOfDefaultReplaceables);
		o.cloned = true;
		return o;
	}
	interface Displayable
	{
		public void display(Module module, java.awt.Component parent);
	}
	private Displayable displayMethod = null;
	public boolean isPromptDisplayEnabled()
	{
		if(displayMethod == null)
			return false;
		else
			return true;
	}
	
	private Hashtable<String, Object> dataObjectTable = new Hashtable<String, Object>();
	public void setDataObject(String name, Object data)
	{
		protect();
		dataObjectTable.put(name, data);
	}
	
	private void deleteDataObjects()
	{
		dataObjectTable.clear();
	}
	
	public Object getDataObject(String name)
	{
		return dataObjectTable.get(name);
	}
	protected ArrayList<Replaceable> listOfDefaultReplaceables;
	
	protected abstract void replace(String input);
	
	public void setPromptDisplayMethod(Displayable  d)
	{
		protect();
		this.displayMethod = d;
	}
	public void display(java.awt.Component parent)
	{
		//if(this.isPromptDisplayEnabled())
		this.displayMethod.display(this, parent);
	}

	
	protected String callMethod(Function<String,String> method, String input) throws Exception
	{
		String output = "";
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
		
		this.deleteDataObjects();
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
