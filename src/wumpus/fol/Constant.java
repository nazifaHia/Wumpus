package wumpus.fol;


public class Constant extends Term
{	
	private int constant;
	
	
	
	
	public Constant()
	{
		constant = 0;
	}
	
	
	
	
	public Constant(int constant)
	{
		this.constant = constant;
	}
	
	
	
	
	public void setConstant(int constant)
	{
		this.constant = constant;
	}
	
	
	
	
	public int getConstant()
	{
		return this.constant;
	}
	
	
	
	
	@Override
	public boolean equals(Object ob)
	{
		if(ob.getClass() != this.getClass()) return false;
		
		Constant t = (Constant)ob;
		
		return t.constant == this.constant;
	}
	
	
	
	
	@Override
	public String toString()
	{
		return String.valueOf(this.constant);
	}
	
	
	
	
	@Override
	public int hashCode()
	{
		return this.toString().hashCode();
	}
}