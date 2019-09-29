package wumpus.journey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import wumpus.fol.Constant;
import wumpus.fol.Function;
import wumpus.fol.HornClause;
import wumpus.fol.HornLiteral;
import wumpus.fol.LiteralSymbols;
import wumpus.fol.Term;
import wumpus.fol.Unifier;
import wumpus.fol.Variable;


public class KnowledgeBase
{
	//Contains horn clauses that are the rules of the game.
	private HashSet<HornClause> rules;

	//contains facts.
	private HashSet<HornLiteral> facts;
	
	
	
	
	public KnowledgeBase()
	{
		rules = new HashSet<HornClause>();
		facts = new HashSet<HornLiteral>();
	}
	
	
	
	
	public void addFact(HornLiteral hl)
	{
		if(hl.isFact()) facts.add(hl);
	}
	
	
	
	
	public void addClause(HornClause hc)
	{
		if(hc.isDefiniteHornClause()) rules.add(hc);
	}
	
	
	
	
	public int numberOfRules()
	{
		return this.rules.size();
	}
	
	
	
	
	public int numberOfFacts()
	{
		return this.facts.size();
	}
		
	
	
	
	@Override
	public String toString()
	{
		String result = "";
		
		result+="FACTS :";
		
		for(HornLiteral hl : facts)
		{
			result+="\n\n\t"+hl.toString();
		}
		
		result+="\n\n\nRULES :";
		
		for(HornClause hc : rules)
		{
			result+="\n\n\t"+hc.toString();
		}
		
		result+="\n\n";
		
		return result;
	}
	
	
	
	
	
	

	
	
	/*
	 * returns : 
	 * 1 if question is true.
	 * -1 if question is false.
	 * 0 if can not answer the question.
	 */
	public int Ask(HornLiteral literal , int size)
	{
		this.UpdateKnowledgeBase(size);
		
		if(this.OK(literal)) return -1;
		
		if(this.knownFact(literal)) return 1;
		
		return 0;
	}
	
	
	
	
	//checks if the literal is an already known fact.
	private boolean knownFact(HornLiteral literal)
	{
		for(HornLiteral lit : facts)
		{
			if(lit.equals(literal)) return true;
		}
		
		return false;
	}
	
	
	
	
	//checks if the square is an ok square.
	private boolean OK(HornLiteral literal)
	{	
		String litName = (literal.getName().equals(LiteralSymbols.wumpus))?LiteralSymbols.wumpusfree:LiteralSymbols.pitfree;
		Constant const1 = (Constant)literal.getTerms().get(0);
		Constant const2 = (Constant)literal.getTerms().get(1);
		
		int row = const1.getConstant();
		int col = const2.getConstant();
		
		HornLiteral lit1 = new HornLiteral(LiteralSymbols.ok,true,new Constant(row),new Constant(col));
		HornLiteral lit2 = new HornLiteral(litName,true,new Constant(row),new Constant(col));
		
		for(HornLiteral fact : facts)
		{
			if(fact.equals(lit1) || fact.equals(lit2)) return true;
		}
		
		return false;
	}
	
	
	
	
	//updates the knowledge base with new inferences.
	private void UpdateKnowledgeBase(int size)
	{
		String[] toAsk = new String[]{LiteralSymbols.wumpus , LiteralSymbols.pit};
		
		for(String temp : toAsk)
		{
			for(int i=0; i<size; i++)
			{
				for(int j=0; j<size; j++)
				{
					HornLiteral literal = new HornLiteral(temp , true , new Constant(i) , new Constant(j));
					this.ask(literal);
				}
			}
		}
	}
	
	
	
	
	//checks if there is a pit or a wumpus in the specified position.
	private void ask(HornLiteral literal)
	{
		String toLook = (literal.getName().equals(LiteralSymbols.wumpus))?LiteralSymbols.stench:LiteralSymbols.breeze;
		String litName = (literal.getName().equals(LiteralSymbols.wumpus))?LiteralSymbols.wumpusfree:LiteralSymbols.pitfree;
		
		Constant const1 = (Constant)literal.getTerms().get(0);
		Constant const2 = (Constant)literal.getTerms().get(1);
		
		int row = const1.getConstant();
		int col = const2.getConstant();
		
		
		
		//Stench/Breeze(row+1 , col).
		HornLiteral lit1 = new HornLiteral(toLook , true , new Constant(row+1) , new Constant(col));
		
		//Stench/Breeze(row-1 , col).
		HornLiteral lit2 = new HornLiteral(toLook , true , new Constant(row-1) , new Constant(col));
		
		//Stench/Breeze(row , col+1).
		HornLiteral lit3 = new HornLiteral(toLook , true , new Constant(row)   , new Constant(col+1));
		
		//Stench/Breeze(row , col-1).
		HornLiteral lit4 = new HornLiteral(toLook , true , new Constant(row)   , new Constant(col-1));
		
		
		
		//InBounds(row+2 , col)
		HornLiteral in1 = new HornLiteral(LiteralSymbols.inbounds , true , new Constant(row+2) , new Constant(col));
		
		//InBounds(row-2 , col)
		HornLiteral in2 = new HornLiteral(LiteralSymbols.inbounds , true , new Constant(row-2) , new Constant(col));
				
		//InBounds(row , col+2)
		HornLiteral in3 = new HornLiteral(LiteralSymbols.inbounds , true , new Constant(row)   , new Constant(col+2));
		
		//InBounds(row , col-2)
		HornLiteral in4 = new HornLiteral(LiteralSymbols.inbounds , true , new Constant(row)   , new Constant(col-2));
		
		
		
		//Center_Square(row , col).
		HornLiteral cs  = new HornLiteral(LiteralSymbols.cs  , true , new Constant(row) , new Constant(col));
		
		//Top_Square(row , col).
		HornLiteral ts  = new HornLiteral(LiteralSymbols.ts  , true , new Constant(row) , new Constant(col));
		
		//Bottom_Square(row , col).
		HornLiteral bs  = new HornLiteral(LiteralSymbols.bs  , true , new Constant(row) , new Constant(col));
		
		//Left_Square(row , col).
		HornLiteral ls  = new HornLiteral(LiteralSymbols.ls  , true , new Constant(row) , new Constant(col));
		
		//Right_Square(row , col).
		HornLiteral rs  = new HornLiteral(LiteralSymbols.rs  , true , new Constant(row) , new Constant(col));
		
		//Top_Left_Square(row , col).
		HornLiteral tls = new HornLiteral(LiteralSymbols.tls , true , new Constant(row) , new Constant(col));
		
		//Top_Right_Square(row , col).
		HornLiteral trs = new HornLiteral(LiteralSymbols.trs , true , new Constant(row) , new Constant(col));
		
		//Bottom_Left_Square(row , col).
		HornLiteral bls = new HornLiteral(LiteralSymbols.bls , true , new Constant(row) , new Constant(col));
		
		//Bottom_Right_Square(row , col).
		HornLiteral brs = new HornLiteral(LiteralSymbols.brs , true , new Constant(row) , new Constant(col));
		
		
		
		
		//center squares.
		if(this.knownFact(cs))
		{
			// WumpusFree/PitFree(row-2 , col).
			HornLiteral temp_literal_1 = new HornLiteral(litName , true , new Constant(row-2) ,  new Constant(col));
			
			// WumpusFree/PitFree(row-1 , col-1).
			HornLiteral temp_literal_2 = new HornLiteral(litName , true , new Constant(row-1) ,  new Constant(col-1));
			
			// WumpusFree/PitFree(row , col-2).
			HornLiteral temp_literal_3 = new HornLiteral(litName , true , new Constant(row)   ,  new Constant(col-2));
			
			// WumpusFree/PitFree(row+1 , col-1).
			HornLiteral temp_literal_4 = new HornLiteral(litName , true , new Constant(row+1) ,  new Constant(col-1));
			
			// WumpusFree/PitFree(row+2 , col).
			HornLiteral temp_literal_5 = new HornLiteral(litName , true , new Constant(row+2) ,  new Constant(col));
			
			// WumpusFree/PitFree(row+1 , col+1).
			HornLiteral temp_literal_6 = new HornLiteral(litName , true , new Constant(row+1) ,  new Constant(col+1));
			
			// WumpusFree/PitFree(row , col+2).
			HornLiteral temp_literal_7 = new HornLiteral(litName , true , new Constant(row)   ,  new Constant(col+2));
			
			// WumpusFree/PitFree(row-1 , col+1).
			HornLiteral temp_literal_8 = new HornLiteral(litName , true , new Constant(row-1) ,  new Constant(col+1));
					
			if(this.knownFact(lit1))
			{
				if(this.knownFact(in1))
				{
					if(this.knownFact(temp_literal_4) && this.knownFact(temp_literal_5) && this.knownFact(temp_literal_6))
					{
						this.addFact(literal);
					}
				}
						
				else
				{
					if(this.knownFact(temp_literal_4) && this.knownFact(temp_literal_6))
					{
						this.addFact(literal);
					}
				}
			}
					
					
			if(this.knownFact(lit2))
			{
				if(this.knownFact(in2))
				{
					if(this.knownFact(temp_literal_1) && this.knownFact(temp_literal_2) && this.knownFact(temp_literal_8))
					{
						this.addFact(literal);
					}
				}
						
				else
				{
					if(this.knownFact(temp_literal_2) && this.knownFact(temp_literal_8))
					{
						this.addFact(literal);
					}
				}
			}
					
					
			if(this.knownFact(lit3))
			{
				if(this.knownFact(in3))
				{
					if(this.knownFact(temp_literal_6) && this.knownFact(temp_literal_7) && this.knownFact(temp_literal_8))
					{
						this.addFact(literal);
					}

				}
						
				else
				{
					if(this.knownFact(temp_literal_6) && this.knownFact(temp_literal_8))
					{
						this.addFact(literal);
					}
				}
			}
					
					
			if(this.knownFact(lit4))
			{
				if(this.knownFact(in4))
				{
					if(this.knownFact(temp_literal_2) && this.knownFact(temp_literal_3) && this.knownFact(temp_literal_4))
					{
						this.addFact(literal);
					}
				}
						
				else
				{
					if(this.knownFact(temp_literal_2) && this.knownFact(temp_literal_4))
					{
						this.addFact(literal);
					}
				}
			}
		}
		
		
		
		
		//left squares , except corners.
		else if(this.knownFact(ls))
		{
			// WumpusFree/PitFree(row-2 , col).
			HornLiteral temp_literal_1 = new HornLiteral(litName , true , new Constant(row-2)   , new Constant(col));
			
			// WumpusFree/PitFree(row-1 , col+1).
			HornLiteral temp_literal_2 = new HornLiteral(litName , true , new Constant(row-1) , new Constant(col+1));
			
			// WumpusFree/PitFree(row , col+2).
			HornLiteral temp_literal_3 = new HornLiteral(litName , true , new Constant(row) , new Constant(col+2));
			
			// WumpusFree/PitFree(row+1 , col+1).
			HornLiteral temp_literal_4 = new HornLiteral(litName , true , new Constant(row+1) , new Constant(col+1));
			
			// WumpusFree/PitFree(row+2 , col).
			HornLiteral temp_literal_5 = new HornLiteral(litName , true , new Constant(row+2) , new Constant(col));
					
			if(this.knownFact(lit2))
			{
				if(this.knownFact(in2))
				{
					if(this.knownFact(temp_literal_1) && this.knownFact(temp_literal_2))
					{
						this.addFact(literal);
					}
				}
						
				else
				{
					if(this.knownFact(temp_literal_2))
					{
						this.addFact(literal);
					}
				}
			}
					
					
			if(this.knownFact(lit1))
			{
				if(this.knownFact(in1)	)
				{
					if(this.knownFact(temp_literal_4) && this.knownFact(temp_literal_5))
					{
						this.addFact(literal);
					}
				}
						
				else
				{
					if(this.knownFact(temp_literal_4))
					{
						this.addFact(literal);
					}
				}
			}
					
					
			if(this.knownFact(lit3))
			{
				if(this.knownFact(temp_literal_2) && this.knownFact(temp_literal_3) && this.knownFact(temp_literal_4))
				{
					this.addFact(literal);
				}
			}
		}
		
		
		
		
		//right squares , except corners.
		else if(this.knownFact(rs))
		{
			// WumpusFree/PitFree(row+2 , col).
			HornLiteral temp_literal_1 = new HornLiteral(litName , true , new Constant(row+2)   , new Constant(col));
			
			// WumpusFree/PitFree(row-1 , col-1).
			HornLiteral temp_literal_2 = new HornLiteral(litName , true , new Constant(row-1) , new Constant(col-1));
			
			// WumpusFree/PitFree(row , col-2).
			HornLiteral temp_literal_3 = new HornLiteral(litName , true , new Constant(row) , new Constant(col-2));
			
			// WumpusFree/PitFree(row+1 , col-1).
			HornLiteral temp_literal_4 = new HornLiteral(litName , true , new Constant(row+1) , new Constant(col-1));
			
			// WumpusFree/PitFree(row-2 , col).
			HornLiteral temp_literal_5 = new HornLiteral(litName , true , new Constant(row-2) , new Constant(col));
					
			if(this.knownFact(lit2))
			{
				if(this.knownFact(in2))
				{
					if(this.knownFact(temp_literal_5) && this.knownFact(temp_literal_2))
					{
						this.addFact(literal);
					}
				}
						
				else
				{
					if(this.knownFact(temp_literal_2))
					{
						this.addFact(literal);
					}
				}
			}
					
			
			if(this.knownFact(lit1))
			{
				if(this.knownFact(in1))
				{
					if(this.knownFact(temp_literal_4) && this.knownFact(temp_literal_1))
					{
						this.addFact(literal);
					}
				}
						
				else
				{
					if(this.knownFact(temp_literal_4))
					{
						this.addFact(literal);
					}
				}
			}
					
					
			if(this.knownFact(lit4))
			{
				if(this.knownFact(temp_literal_2) && this.knownFact(temp_literal_3) && this.knownFact(temp_literal_4))
				{
					this.addFact(literal);
				}
			}
		}
		
		
		
		
		//top squares , except corners.
		else if(this.knownFact(ts))
		{
			// WumpusFree/PitFree(row , col-2).
			HornLiteral temp_literal_1 = new HornLiteral(litName , true , new Constant(row)   , new Constant(col-2));
			
			// WumpusFree/PitFree(row+1 , col-1).
			HornLiteral temp_literal_2 = new HornLiteral(litName , true , new Constant(row+1) , new Constant(col-1));
			
			// WumpusFree/PitFree(row+2 , col).
			HornLiteral temp_literal_3 = new HornLiteral(litName , true , new Constant(row+2) , new Constant(col));
			
			// WumpusFree/PitFree(row+1 , col+1).
			HornLiteral temp_literal_4 = new HornLiteral(litName , true , new Constant(row+1) , new Constant(col+1));
			
			// WumpusFree/PitFree(row , col+2).
			HornLiteral temp_literal_5 = new HornLiteral(litName , true , new Constant(row) , new Constant(col+2));
					
			if(this.knownFact(lit1))
			{
				if(this.knownFact(temp_literal_2) && this.knownFact(temp_literal_3) && this.knownFact(temp_literal_4))
				{
					this.addFact(literal);
				}
			}
					
					
			if(this.knownFact(lit3))
			{
				if(this.knownFact(in3))
				{
					if(this.knownFact(temp_literal_4) && this.knownFact(temp_literal_5))
					{
						this.addFact(literal);
					}
				}
						
				else
				{
					if(this.knownFact(temp_literal_4))
					{
						this.addFact(literal);
					}
				}
			}
					
					
			if(this.knownFact(lit4))
			{
				if(this.knownFact(in4))
				{
					if(this.knownFact(temp_literal_1) && this.knownFact(temp_literal_2))
					{
						this.addFact(literal);
					}
				}
						
				else
				{
					if(this.knownFact(temp_literal_2))
					{
						this.addFact(literal);
					}
				}
			}
		}
		
		
		
		
		//bottom squares , except corners.
		else if(this.knownFact(bs))
		{
			// WumpusFree/PitFree(row , col-2).
			HornLiteral temp_literal_1 = new HornLiteral(litName , true , new Constant(row)   , new Constant(col-2));
			
			// WumpusFree/PitFree(row-1 , col-1).
			HornLiteral temp_literal_2 = new HornLiteral(litName , true , new Constant(row-1) , new Constant(col-1));
			
			// WumpusFree/PitFree(row-2 , col).
			HornLiteral temp_literal_3 = new HornLiteral(litName , true, new Constant(row-2) , new Constant(col));
			
			// WumpusFree/PitFree(row-1 , col+1).
			HornLiteral temp_literal_4 = new HornLiteral(litName , true , new Constant(row-1) , new Constant(col+1));
			
			// WumpusFree/PitFree(row , col+2).
			HornLiteral temp_literal_5 = new HornLiteral(litName , true , new Constant(row) , new Constant(col+2));
					
			if(this.knownFact(lit2))
			{
				if(this.knownFact(temp_literal_2) && this.knownFact(temp_literal_3) && this.knownFact(temp_literal_4))
				{
					this.addFact(literal);
				}
			}
					
					
			if(this.knownFact(lit3))
			{
				if(this.knownFact(in3))
				{
					if(this.knownFact(temp_literal_4) && this.knownFact(temp_literal_5))
					{
						this.addFact(literal);
					}
				}
						
				else
				{
					if(this.knownFact(temp_literal_4))
					{
						this.addFact(literal);
					}
				}
			}
					
					
			if(this.knownFact(lit4))
			{
				if(this.knownFact(in4))
				{
					if(this.knownFact(temp_literal_1) && this.knownFact(temp_literal_2))
					{
						this.addFact(literal);
					}
				}
						
				else
				{
					if(this.knownFact(temp_literal_2))
					{
						this.addFact(literal);
					}
				}
			}
		}
		
		
		
		
		//top left square.
		else if(this.knownFact(tls))
		{
			// WumpusFree/PitFree(row , col+2).
			HornLiteral temp_literal_1 = new HornLiteral(litName , true , new Constant(row)   , new Constant(col+2));
			
			// WumpusFree/PitFree(row+1 , col+1).
			HornLiteral temp_literal_2 = new HornLiteral(litName , true , new Constant(row+1) , new Constant(col+1));
			
			// WumpusFree/PitFree(row+2 , col).
			HornLiteral temp_literal_3 = new HornLiteral(litName , true , new Constant(row+2) , new Constant(col));
					
			if(this.knownFact(lit1))
			{	
				if(this.knownFact(temp_literal_2) && this.knownFact(temp_literal_3))
				{
					this.addFact(literal);
				}
			}
					
			if(this.knownFact(lit3))
			{
				if(this.knownFact(temp_literal_1) && this.knownFact(temp_literal_2))
				{
					this.addFact(literal);
				}
			}
		}
		
		
		
		
		//top right square.
		else if(this.knownFact(trs))
		{
			// WumpusFree/PitFree(row , col-2).
			HornLiteral temp_literal_1 = new HornLiteral(litName , true , new Constant(row)   , new Constant(col-2));
			
			// WumpusFree/PitFree(row+1 , col-1).
			HornLiteral temp_literal_2 = new HornLiteral(litName , true , new Constant(row+1) , new Constant(col-1));
			
			// WumpusFree/PitFree(row+2 , col).
			HornLiteral temp_literal_3 = new HornLiteral(litName , true , new Constant(row+2) , new Constant(col));	
					
			if(this.knownFact(lit1))
			{	
				if(this.knownFact(temp_literal_2) && this.knownFact(temp_literal_3))
				{
					this.addFact(literal);
				}
			}
					
			if(this.knownFact(lit4))
			{
				if(this.knownFact(temp_literal_1) && this.knownFact(temp_literal_2))
				{
					this.addFact(literal);
				}
			}
		}
		
		
		
		
		//bottom left square.
		else if(this.knownFact(bls))
		{
			// WumpusFree/PitFree(row , col+2).
			HornLiteral temp_literal_1 = new HornLiteral(litName , true , new Constant(row)   , new Constant(col+2));
			
			// WumpusFree/PitFree(row-1 , col+1).
			HornLiteral temp_literal_2 = new HornLiteral(litName , true , new Constant(row-1) , new Constant(col+1));
			
			// WumpusFree/PitFree(row-2 , col).
			HornLiteral temp_literal_3 = new HornLiteral(litName , true , new Constant(row-2) , new Constant(col));
					
			if(this.knownFact(lit2))
			{	
				if(this.knownFact(temp_literal_2) && this.knownFact(temp_literal_3))
				{
					this.addFact(literal);
				}
			}
					
			if(this.knownFact(lit3))
			{
				if(this.knownFact(temp_literal_1) && this.knownFact(temp_literal_2))
				{
					this.addFact(literal);
				}
			}
		}
		
		
		
		
		//bottom right square.
		else if(this.knownFact(brs))
		{
			// WumpusFree/PitFree(row , col-2).
			HornLiteral temp_literal_1 = new HornLiteral(litName , true , new Constant(row)   , new Constant(col-2));
			
			// WumpusFree/PitFree(row-1 , col-1).
			HornLiteral temp_literal_2 = new HornLiteral(litName , true , new Constant(row-1) , new Constant(col-1));
			
			// WumpusFree/PitFree(row-2 , col).
			HornLiteral temp_literal_3 = new HornLiteral(litName , true , new Constant(row-2) , new Constant(col));
					
			if(this.knownFact(lit2))
			{	
				if(this.knownFact(temp_literal_2) && this.knownFact(temp_literal_3))
				{
					this.addFact(literal);
				}
			}
					
			if(this.knownFact(lit4))
			{
				if(this.knownFact(temp_literal_1) && this.knownFact(temp_literal_2))
				{
					this.addFact(literal);
				}
			}
		}
	}
	
	
	
	
	//returns the wumpus position , or null if we don't know it yet.
	public Position WumpusPosition()
	{
		for(HornLiteral lit : facts)
		{
			HornLiteral literal = lit;
			
			if(literal.getName().equals(LiteralSymbols.wumpus) && literal.getNegation())
			{
				Position position = new Position();
				
				Constant row = (Constant)literal.getTerms().get(0);
				Constant col = (Constant)literal.getTerms().get(1);
				
				position.setRow(row.getConstant());
				position.setColumn(col.getConstant());
				
				return position;
			}
		}
		
		return null;
	}
	
	
	
	
	//tells to the KB that wumpus is dead. We remove it from the KB and the stenches too.
	public void WumpusIsDead()
	{
		HashSet<HornLiteral> temp = new HashSet<HornLiteral>();
		
		for(HornLiteral literal : facts)
		{
			if((!literal.getName().equals(LiteralSymbols.wumpus) || !literal.getNegation()) && !literal.getName().equals(LiteralSymbols.stench))
			{
				temp.add(literal);
			}
		}
		
		facts.clear();
		facts.addAll(temp);
	}
	
	
	
	
	
	
	//------------------------------UNIFICATION ALGORITHMS AND HELPER FUNCTIONS------------------------------//
	
 	public Unifier UNIFY(Object ob1 , Object ob2 , Unifier theta)
	{
		if  (theta==null)  return null;
			
		else if(ob1.equals(ob2)) return theta;
			
		else if(ob1 instanceof Variable) return UNIFY_VAR((Variable)ob1 , (Term)ob2 , theta);
			
		else if(ob2 instanceof Variable) return UNIFY_VAR((Variable)ob2 , (Term)ob1 , theta);
			
		else if(ob1 instanceof HornLiteral && ob2 instanceof HornLiteral)
		{
			HornLiteral lit1 = (HornLiteral)ob1;
			HornLiteral lit2 = (HornLiteral)ob2;
				
			ArrayList<Term> xList = new ArrayList<Term>(lit1.getTerms());
			ArrayList<Term> yList = new ArrayList<Term>(lit2.getTerms());
				
			return UNIFY(xList , yList , UNIFY_OPS(lit1.getName() , lit2.getName() , theta));
		}
			
		else if(ob1 instanceof List && ob2 instanceof List)
		{
			@SuppressWarnings("unchecked")
			List<Term> list1 = (List<Term>)ob1;
				
			@SuppressWarnings("unchecked")
			List<Term> list2 = (List<Term>)ob2;
				
			if(list1.size()!=list2.size()) return null;
				
			else if(list1.size() == 0 && list2.size()==0) return theta;
				
			else if(list1.size()==1 && list2.size()==1) return UNIFY(list1.get(0) , list2.get(0) , theta);
				
			else return UNIFY(list1.subList(1,list1.size()) , list2.subList(1,list1.size()) , UNIFY( list1.get(0) , list2.get(0) , theta) );
		}
			
		else return null;
	}
		
		
		
		
	private Unifier UNIFY_OPS(String x , String y , Unifier theta)
	{
		if(theta == null) return null;
		
		else if(x.equals(y)) return theta;
			
		else return null;
	}
		
		
		
		
	private Unifier UNIFY_VAR(Variable var , Term x , Unifier theta)
	{
			
		if(theta.containsSubstitution(var))
		{
			return UNIFY(theta.getSubstitution(var) , x , theta);
		}
			
			
			
		else if(theta.containsSubstitution(x))
		{
			return UNIFY(var , theta.getSubstitution(x) , theta);
		}
			
			
			
		else if(OCCUR_CHECK(var , x , theta))
		{
			return null;
		}
			
			
			
		else
		{
			return cascadeSubstitution(theta , var , x);
		}
	}
		
		
		
		
	//this method checks if variable var exists in clause x.
	private boolean OCCUR_CHECK(Variable var , Term x , Unifier theta)
	{
		if(var.equals(x)) return true;
		
		else if(theta.containsSubstitution(x))
		{
			return OCCUR_CHECK(var , theta.getSubstitution(x) , theta);
		}
			
		else if(x instanceof Function)
		{
			Function f = (Function)x;
				
			Term ob1 = f.getFirst();
			Term ob2 = f.getSecond();
				
			if( OCCUR_CHECK(var , ob1 , theta) || OCCUR_CHECK(var , ob2 , theta)) return true;
		}
			
		return false;
	}
		
		
		
		
	private Unifier cascadeSubstitution(Unifier theta , Variable var , Term x)
	{
		theta.addSubstitution(var , x);
			
		//update the variables into the functions if nessesary.
		for(Term t : theta.getKeys())
		{
			Term term = theta.getSubstitution(t);
				
			if(term instanceof Function)
			{
				if(theta.containsSubstitution(((Function) term).getFirst()))
				{
					((Function) term).setFirst(theta.getSubstitution(((Function) term).getFirst()));
				}
			}
		}
			
		return theta;
	}


}