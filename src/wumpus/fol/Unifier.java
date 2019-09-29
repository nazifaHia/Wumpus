package wumpus.fol;

import java.util.HashMap;
import java.util.Set;



public class Unifier
{
	private HashMap<Term , Term> theta;
	
	
	
	
	public Unifier()
	{
		theta = new HashMap<Term , Term>();
	}
	
	
	
	
	public void addSubstitution(Term key , Term subst)
	{
		theta.put(key , subst);
	}
	
	
	
	
	public boolean containsSubstitution(Term key)
	{
		return this.theta.containsKey(key);
	}
	
	
	
	
	public Term getSubstitution(Term key)
	{
		return this.theta.get(key);
	}
	
	
	
	
	public Set<Term> getKeys()
	{
		return this.theta.keySet();
	}
	
	
	
	
	@Override
	public String toString()
	{
		 String toReturn="";
		 
		 for(Term term : theta.keySet())
		 {
			 if(!toReturn.equals("")) toReturn+=" , ";
			 toReturn +=term.toString()+"/"+theta.get(term).toString();
		 }
		 
		 return "{ " + toReturn+" } ";
	}


}