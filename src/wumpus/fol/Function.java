package wumpus.fol;


public class Function extends Term
{
	
	private String function_name;
	
	
	private Term first;
	
	
	private Constant second;
	
	
	
	
	public Function()
	{
		this.function_name = "ADD";
		this.first = null;
		this.second = null;
	}
	
	
	
	
	public Function(String name , Term var ,  Constant value )
	{
		this.function_name = name;
		this.second = value;
		this.first = var;
	}
	
	
	
	
	public void setName(String s)
	{
		this.function_name = s;
	}
	
	
	
	
	public String getName()
	{
		return this.function_name;
	}
	
	
	
	
	public void setFirst(Term first)
	{
		this.first = first;
	}
	
	
	
	
	public Term getFirst()
	{
		return this.first;
	}
	
	
	
	
	public void setSecond(Constant con)
	{
		this.second = con;
	}
	
	
	
	
	public Term getSecond()
	{
		return this.second;
	}
	
	
	
	
	
	public int getAbsoluteNumber()
	{
	
		if(first instanceof Variable) return Integer.MIN_VALUE;
		
		else if(first instanceof Constant)
		{
			Constant t = (Constant)first;
			return second.getConstant() + t.getConstant();
		}
		
		else return Integer.MIN_VALUE;
	}
	
	
	
	
	@Override
	public boolean equals(Object ob1)
	{
		if(this.getClass() != ob1.getClass()) return false;
		
		Function f = (Function)ob1;
		
		return ( (f.function_name.equalsIgnoreCase(this.function_name)) && (f.first.equals(this.first)) && f.second.equals(this.second) );
	}
	
	
	
	
	@Override
	public String toString()
	{
		return ( this.function_name+"("+first.toString()+","+second.toString()+")" );
	}
	
	
	
	
	@Override
	public int hashCode()
	{
		return ( this.function_name.hashCode() + this.first.hashCode() + this.second.hashCode() );
	}


}