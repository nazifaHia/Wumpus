package wumpus.fol;


public class Variable extends Term
{
	private String var_name;
	
	
	
	
	public Variable()
	{
		var_name="";
	}
	
	
	
	
	public Variable(String variable)
	{
		var_name=variable;
	}
	
	
	
	
	public void setVariable(String var)
	{
		var_name=var;
	}
	
	
	
	
	public String getVariable()
	{
		return var_name;
	}
	
	
	
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj.getClass()!=this.getClass()) return false;
		
		Variable var=(Variable)obj;
		
		if(var.var_name.equals(this.var_name)) return true;
		
		return false;
	}
	
	
	
	
	@Override
	public String toString()
	{
		return var_name;
	}
	
	
	
	
	@Override
	public int hashCode()
	{
		return this.var_name.hashCode();
	}


}