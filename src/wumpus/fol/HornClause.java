package wumpus.fol;

import java.util.ArrayList;


public class HornClause
{
	private ArrayList<HornLiteral> myList;
	
	private int number_of_literals;
	
	
	
	public HornClause()
	{
		myList = new ArrayList<HornLiteral>();
		this.number_of_literals=0;
	}
	
	
	
	
	public void addLiteral(HornLiteral lit)
	{
		myList.add(lit);
		this.number_of_literals++;
	}
	
	
	
	
	public int getNumberOfLiterals()
	{
		return this.number_of_literals;
	}
	
	
	
	
	public HornLiteral getLiteralAt(int i)
	{
		return myList.get(i);
	}
	
	
	
	

	public boolean isDefiniteHornClause()
	{
		int number_of_false_literals = 0;
		int number_of_true_literals = 0;
		
		for(HornLiteral literal : myList)
		{
			if(literal.getNegation()) number_of_true_literals++;
			else number_of_false_literals++;
		}
		
		return (number_of_true_literals==1 && number_of_false_literals==this.myList.size()-1);
	}
	
	
	
	
	@Override
	public String toString()
	{
		String hypothesis = "";
		String inference = "";
		
		for(HornLiteral literal : myList)
		{
			if(!literal.getNegation())
			{
				if(!hypothesis.equals("")) hypothesis+=" ^ ";
				literal.setNegation(true);
				hypothesis +=literal.toString();
				literal.setNegation(false);
			}
			
			else
			{
				inference += literal.toString();
			}
		}
		
		return hypothesis+" => "+inference;
	}

}