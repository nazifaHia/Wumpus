package wumpus.xml_parsing;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import wumpus.journey.Position;


public class WumpusWorldHandler extends DefaultHandler 
{
	/**
	 * The board of the game.
	 */
	private String[][] board;
	
	/**
	 * Indicates if we are parsing through a "WumpusWorld" tag.
	 */
	private boolean parsingWorld;
	
	/**
	 * Indicates if we are parsing through a "BoardSize" tag.
	 */
	private boolean parsingSize;
	
	/**
	 * Indicates if we are parsing through a "AgentPosition" tag.
	 */
	private boolean parsingAgentPos;
		
	/**
	 * Indicates if we are parsing through a "row" tag.
	 */
	private boolean parsingRow;
	
	/**
	 * Indicates if we are parsing through a "col" tag.
	 */
	private boolean parsingCol;
	
	/**
	 * The row on the board of an object.
	 */
	private int row;
	
	/**
	 * The column on the board of an object.
	 */
	private int col;
	
	/**
	 * The size of the world.
	 */
	private int size;
	
	/**
	 * The agent's initial Position.
	 */
	private Position pos;
	
	
	/**
	 * Constructor.
	 */
	public WumpusWorldHandler()
	{
		this.parsingAgentPos = false;
		this.parsingSize = false;
		this.parsingWorld = false;
		
		this.row = -1;
		this.col = -1;
		
		this.pos = new Position();
	}
	
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		if(qName.equalsIgnoreCase("wumpusworld"))
		{
			this.parsingWorld = true;
		}
		
		else if(qName.equalsIgnoreCase("boardsize"))
		{
			this.parsingSize = true;
		}
		
		else if(qName.equalsIgnoreCase("agentposition"))
		{
			this.parsingAgentPos = true;
		}
		
		else if(qName.equalsIgnoreCase("wumpusposition"))
		{
			// Nothing to do.
		}
		
		else if(qName.equalsIgnoreCase("pitposition"))
		{
			// Nothing to do.
		}
		
		else if(qName.equalsIgnoreCase("goldposition"))
		{
			// Nothing to do.
		}
		
		else if(qName.equalsIgnoreCase("row"))
		{
			this.parsingRow = true;
		}
		
		else if(qName.equalsIgnoreCase("col"))
		{
			this.parsingCol = true;
		}
	}
	
	
	@Override
	public void characters(char ch[], int start, int length) throws SAXException
	{
		String s = new String(ch, start, length).trim();
		
		if(s.length() > 0)
		{
			if(this.parsingWorld && this.parsingRow)
			{
				this.row = Integer.parseInt(s);
				
				if(this.parsingAgentPos)
				{
					this.pos.setRow(row);
				}
			}
			
			else if(this.parsingWorld && this.parsingCol)
			{
				this.col = Integer.parseInt(s);
				
				if(this.parsingAgentPos)
				{
					this.pos.setColumn(col);
				}
			}
			
			else if(this.parsingWorld && this.parsingSize)
			{
				this.size = Integer.parseInt(s);
			}
		}
	}
	
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if(qName.equalsIgnoreCase("wumpusworld"))
		{
			this.parsingWorld = false;
		}
		
		else if(qName.equalsIgnoreCase("boardsize"))
		{
			this.parsingSize = false;
			this.initializeBoard();
		}
		
		else if(qName.equalsIgnoreCase("agentposition"))
		{
			this.parsingAgentPos = false;
		}
		
		else if(qName.equalsIgnoreCase("wumpusposition"))
		{			
			if(board[row][col].equals("")) board[row][col] += "W";
			else board[row][col]+= (",W" );
		}
		
		else if(qName.equalsIgnoreCase("goldposition"))
		{
			if(board[row][col].equals("")) board[row][col] += "G";
			else board[row][col]+= (",G" );
		}
		
		else if(qName.equalsIgnoreCase("pitposition"))
		{			
			if(board[row][col].equals("")) board[row][col] += "P";
			else board[row][col]+= (",P" );
		}
		
		else if(qName.equalsIgnoreCase("row"))
		{
			this.parsingRow = false;
		}
		
		else if(qName.equalsIgnoreCase("col"))
		{
			this.parsingCol = false;
		}
	}
	
	
	/**
	 * Initializes the board.
	 */
	private void initializeBoard()
	{
		this.board = new String[this.size][this.size];
		
		for(int i = 0; i < this.size; i++)
		{
			for(int j = 0; j < this.size; j++)
			{
				this.board[i][j] = "";
			}
		}
	}
	
	
	/**
	 * @return board The game Board.
	 */
	public String[][] getBoard()
	{
		return this.board;
	}
	
	
	/**
	 * @return pos The agent's initial position.
	 */
	public Position getPosition()
	{
		return this.pos;
	}
}