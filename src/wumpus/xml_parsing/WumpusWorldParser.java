package wumpus.xml_parsing;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class WumpusWorldParser
{
	/**
	 * The path of the xml file.
	 */
	private String file;
	
	

	public WumpusWorldParser(String file)
	{
		this.file = file;
	}
	
	

	public Object[] parse()
	{
		Object[] contents = new Object[2];
		
		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			WumpusWorldHandler handler = new WumpusWorldHandler();
			parser.parse(this.file, handler);
			
			contents[0] = handler.getBoard();
			contents[1] = handler.getPosition();
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return contents;
	}
}