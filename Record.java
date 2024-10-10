import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Record extends InfoRecord{
	public Record()
	{
		super();
	}
	public Record(int indentLength, char block, String topTitle, int titleMaxLength)
	{
		super(indentLength, block, topTitle, titleMaxLength);
	}
	public void addRecord(String left, String right)
	{
		setRecord(left,'-',right);
	}
	public void addRecord(String left, char separator, String right)
	{
		setRecord(left, separator,right);
	}
}

class TextFrame extends InfoRecord {
	public TextFrame(String text, char block)
	{
		super(10,block,"",10);
		setRecord("",' ', text);
	}
	public TextFrame(String text)
	{
		this(text,'\u2592');
	}
}

class Underline {
	private String text;
	private char block;
	private String underline(String text, char block)
	{
		StringBuilder underline = new StringBuilder();
		underline.append(text+"\n");
		for (int i=0;i<text.length();i++)
		{
			underline.append(block);
		}
		return underline.toString();	
	}
	public Underline(String text, char block)
	{
		this.text = text;
		this.block = block;
	}
	public Underline(String text)
	{
		this.text = text;
		this.block = '*';
	}
	public String stringValue()
	{
		return new TextFrame(underline(text,block),' ').stringValue();
	}

}

class Analyze3Record extends InfoRecord {
	public Analyze3Record(int userInput1, String userInput2)
	{
		super(5,'\u2592'," Rebuild Details ",50);
		setRecord("",' ',"");
		setRecord("Number Of Integer Values",Integer.toString(userInput1));
		setRecord("Input String",userInput2);
		setRecord("",' ',"");
	}
}


abstract class InfoRecord {
	private StringBuffer stringValueBuffer = new StringBuffer("");
	private String topTitle;
	private  int titleMaxLength;
	private  int indentLength;
	private  char block;
	private int padding = 0; //4+value.length()+(2*titleMaxLength)-title.length()-topTitle.length
	private StringBuffer appendRightBlocks(StringBuffer input)
	{
		Pattern p = null;
		Matcher currentMatcher;
		StringBuffer tempBuffer = null;
		p = Pattern.compile("[\r\n][ ]*(["+block+"].*)");
		currentMatcher = p.matcher(input);
		tempBuffer = new StringBuffer();
		while(currentMatcher.find())
		{
			currentMatcher.appendReplacement(tempBuffer,currentMatcher.group(0)+padding((2*padding+topTitle.length())-(currentMatcher.group(1).replace("$","").replace("\\\\","\\").length())-1,' ')+block);
		}
		currentMatcher.appendTail(tempBuffer);
		return tempBuffer;
	}
	
	protected void setRecord(String title, String value)
	{
		setRecord(title,':',value);
	}
	protected void setRecord(String title, char separator, String value)
	{
		try
		{
			String[] tokens = value.replaceAll("\t", " ").split("[\n\r]");
			for (int i=0 ; i< tokens.length ; i++)
			{
				stringValueBuffer.append("\n");
				stringValueBuffer.append(padding(indentLength,' '));
				stringValueBuffer.append(block);
				stringValueBuffer.append(padding(titleMaxLength-title.length(),' '));
				stringValueBuffer.append(i==0?title:padding(title.length(),' '));
				stringValueBuffer.append(i==0&&!title.equals("")?" "+separator+" ":"   ");
				stringValueBuffer.append(Matcher.quoteReplacement(tokens[i]));
				calcPadding(title, tokens[i]);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private void calcPadding(String title, String value)
	{
		int newPadding = (4+value.length()+(2*titleMaxLength)-title.length()-topTitle.length())/2;
		padding = padding<newPadding?newPadding:padding;
	}
	private String padding(int r, char x)
	{
		StringBuffer b = new StringBuffer();
		for(int i=0 ; i<r ; i++)
		{
			b.append(x);
		}
		return b.toString();
	}
	private String header()
	{
		return 	"\n"+padding(indentLength,' ')+
				padding(padding, block)+
				topTitle+
				padding(padding, block);
	}
	private String footer()
	{
		return "\n"+padding(indentLength,' ')+
				padding((2*padding)+topTitle.length(), block)+
				"\n\n";
	}
	protected InfoRecord()
	{
		indentLength = 5;
		block = ' ';
		topTitle = "";
		titleMaxLength = 50;
	}
	protected InfoRecord(int indentLength, char block, String topTitle, int titleMaxLength)
	{
		this.indentLength = indentLength;
		this.block = block;
		this.topTitle = topTitle;
		this.titleMaxLength = titleMaxLength;
	}
	public String stringValue()
	{
		String returnValue = "";
		returnValue+=header();
		try
		{
			returnValue+=appendRightBlocks(stringValueBuffer);
		}
		catch(Exception e)
		{}
		returnValue+=footer();
		return returnValue;
	}
}
