
public class TextFrame extends InfoRecord {
	public TextFrame(String text, char block, int width)
	{
		super(10,block,"",((width-text.length())/2)+text.length()+(width%2==0?text.length()%2:0));
		setRecord(text, "");
	}
	public TextFrame(String text, int width)
	{
		this(text,'\u2592',width);
	}
	public TextFrame(String text)
	{
		this(text,text.length()-(text.length() % 100)+100);
	}
}