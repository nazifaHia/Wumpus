package wumpus.journey;



public class Position
{
	//the vertical position.
	private int row;
	
	//the horizontal position
	private int col;
	
	/*
	 * The direction of the agent.
	 * 1 = Agent is looking to the North.
	 * 2 = Agent is looking to the East.
	 * 3 = Agent is looking to the South.
	 * 4 = Agent is looking to the West.
	 */
	private int direction;
	
	
	
	
	public Position()
	{
		this.row = -1;
		this.col = -1;
		this.direction = 1;
	}
	
	
	
	
	public Position(int r , int c , int d)
	{
		row=r;
		col=c;
		direction=d;
	}
	
	
	
	
	public void setRow(int r)
	{
		row=r;
	}
	
	
	
	
	public int getRow()
	{
		return row;
	}
	
	
	
	
	public void setColumn(int col)
	{
		this.col=col;
	}
	
	
	
	
	public int getColumn()
	{
		return col;
	}
	
	
	
	
	public void setDirection(int d)
	{
		direction =d;
	}
	
	
	
	
	public int getDirection()
	{
		return direction;
	}
	
	
	
	
	@Override
	public String toString()
	{
		return ("row="+String.valueOf(row)+",column="+String.valueOf(col)+",direction="+String.valueOf(direction));
	}
	
	
	
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj.getClass()!=this.getClass()) return false;
		
		Position p = (Position)obj;
		
		if(p.col==this.col && p.row==this.row) return true;
		
		return false;
	}


}