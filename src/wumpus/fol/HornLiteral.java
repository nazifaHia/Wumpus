package wumpus.fol;

import java.util.ArrayList;

public class HornLiteral
{
	
	private String name;
	
	
	private boolean negation;
	
	
	private ArrayList<Term> terms;
	
	
	
	
	public HornLiteral()
	{
		name="";
		negation = true;
		terms = new ArrayList<Term>();
	}
	
	
	
	
	public HornLiteral(String Name , boolean Negation , Term term1)
	{
		name = Name;
		negation = Negation;
		terms = new ArrayList<Term>();
		terms.add(term1);
	}
	
	
	
	
	public HornLiteral(String Name , boolean Negation , Term term1 , Term term2)
	{
		name = Name;
		negation = Negation;
		terms = new ArrayList<Term>();
		terms.add(term1);
		terms.add(term2);
	}
	
	
	
	
	public void setName(String Name)
	{
		name = Name;
	}
	
	
	
	
	public String getName()
	{
		return name;
	}
	
	
	
	
	public void setNegation(boolean Negation)
	{
		negation = Negation;
	}
	
	
	
	
	public boolean getNegation()
	{
		return negation;
	}
	
	
	
	
	
	public void addTerm(Term term)
	{
		this.terms.add(term);
	}
	
	
	
	

	public ArrayList<Term> getTerms()
	{
		return this.terms;
	}
	
	
	
	
	
	public boolean isNegation(HornLiteral literal)
	{
		return (this.name.equals(literal.name) && this.terms.equals(literal.terms) && this.negation!=literal.negation);
	}
	
	
	
	
	
	public boolean isFact()
	{
		return negation;
	}
	
	
	
	
	@Override
	public String toString()
	{
		String Negation = (negation)?"":"not ";
		
		String args = "";
		
		for(Term term : terms)
		{
			if(!args.equals("")) args+=",";
			args+=term.toString();
		}
		
		return ( Negation+""+name+"("+args+")");
	}
	
	
	
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj.getClass()!=this.getClass()) return false;
		
		HornLiteral literal = (HornLiteral)obj;
		
		
		if(literal.name.equals(this.name) && literal.negation==this.negation && literal.terms.equals(this.terms)) return true;
		
		return false;
	}
	
	
	
	
	@Override
	public int hashCode()
	{
		int hash = this.name.hashCode();
		
		for(Term term : terms)
		{
			hash += term.hashCode();
		}
		
		return hash;
	}

}