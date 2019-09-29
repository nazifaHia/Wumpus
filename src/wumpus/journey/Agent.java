package wumpus.journey;

import java.util.ArrayList;
import java.util.Random;

import wumpus.fol.Constant;
import wumpus.fol.Function;
import wumpus.fol.FunctionSymbols;
import wumpus.fol.HornClause;
import wumpus.fol.HornLiteral;
import wumpus.fol.LiteralSymbols;
import wumpus.fol.Variable;


public class Agent 
{
	//The current position of the agent.
	private Position current_position;
	
	//The agent's previous position.
	private Position previous_position;
	
	//The world that the agent has discovered.
	private String[][] world_explored;
	
	//The original game board.
	private GameBoard gb;
	
	//The agent's knowledgeBase.
	private KnowledgeBase KB;
	
	//the number of times that we tolerate the agent to go back to a visited square;
	private final int backMoves = 30;
	
	//the number of times the agent went to a visited square.
	private int backTimes;
	
	/*
	 * The following variables will help us to kill the wumpus.
	 */
	
	//if the agent has still the arrow.
	private boolean hasArrow;
	
	//if wumpus is still alive.
	private boolean wumpusAlive;
	
	//the position of the wumpus.
	private Position wumpusPosition;
	
	
	
	
	//constuctor.
 	public Agent(String fileName)
	{
		this.gb = new GameBoard(fileName);
		this.current_position = gb.getInitialPosition();
		this.previous_position = new Position();
		this.InitializeBoard(gb.getSize());
		this.KB = new KnowledgeBase();
		this.backTimes = 0;
		this.AddMapFacts();
		this.AddRules();
		this.hasArrow = true;
		this.wumpusAlive = true;
		this.wumpusPosition = new Position();
	}
	
	
	
	
	//the agent starts its journey to find the gold
	public void FindGold()
	{	
		world_explored[this.current_position.getRow()][this.current_position.getColumn()] = gb.GetData(this.current_position.getRow(),this.current_position.getColumn())+",A"+String.valueOf(this.current_position.getDirection());
		this.printBoard();
		this.AddFacts();
		if(GameOver()) return;
		
		while(true)
		{		
			//if the agent knows where wumpus is , he will try to kill it.
			if(this.CanKillWumpus()) this.HuntWumpus();
			
			//make the safest move.
			Position toDo = this.MakeMove();
			
			//update the current map.
			this.UpdateWorld(toDo);
			
			//print the board.
			this.printBoard();
			
			//adds new facts to the the knowledge base.
			this.AddFacts();
			
			//if the agent found the gold , or fall into a pit , or got killed by the wumpus , end the game.
			if(this.GameOver()) break;
		}
	}
	
	
	
	
	

	
	//Initializes the KnowledgeBase.
	private void InitializeBoard(int size)
	{
		world_explored = new String[size][size];
		
		for(int i=0; i<size; i++)
		{
			for(int j=0; j<size; j++)
			{
				 world_explored[i][j]="-";
			}
		}
	}
	
	
	
	
	//prints the board that the agent has explored.
	private void printBoard() 
	{
		  
		 for(int i=0; i< world_explored.length; i++)
		  {
		     System.out.print("\t"+String.valueOf(i));
		  }
		   
		  System.out.println();
		  
		  for(int i=0; i< world_explored.length; i++)
		  {
			  System.out.print("\t*");
		  }
		   
		  System.out.println("\n");
		  
	      for (int row = 0; row < world_explored.length; row++) 
	      {
	    	 System.out.print(String.valueOf(row)+"*");
	         
	    	 for (int col = 0; col <  world_explored.length; col++) 
	         {
	             System.out.print("\t" + world_explored[row][col]); // print each of the cells
	         }
	         
	    	  System.out.print("\t*"+String.valueOf(row));
	    	 
	          System.out.println("\n");
	      }
	      
	      
	      for(int i=0; i< world_explored.length; i++)
		  {
			  System.out.print("\t*");
		  }
	      
	      System.out.println();
		  
		  for(int i=0; i< world_explored.length; i++)
		  {
			  System.out.print("\t"+String.valueOf(i));
		  }
		   
		  System.out.println("\n\n\n\n\n");    
	}
	
	
	
	
	
	private ArrayList<Position> getChildren()
	{
		
		Position right = new Position(current_position.getRow() , current_position.getColumn()+1 , 2);
		Position left  = new Position(current_position.getRow() , current_position.getColumn()-1 , 4);
		Position up    = new Position(current_position.getRow()-1 , current_position.getColumn() , 1);
		Position down  = new Position(current_position.getRow()+1 , current_position.getColumn() , 3);
		
		ArrayList<Position> moves = new ArrayList<Position>();
		
		if(isValidMove(right)) moves.add(right);
		if(isValidMove(left)) moves.add(left);
		if(isValidMove(up)) moves.add(up);
		if(isValidMove(down)) moves.add(down);
		
		return moves;
	}
	
	
	
	
	//checks if a move that the agent can do is valid.
	private boolean isValidMove(Position pos)
	{
		int row = pos.getRow();
		int column = pos.getColumn();
		
		if(row > world_explored.length-1 || row < 0) return false;
		
		if(column > world_explored.length-1 || column < 0) return false;
		
		return true;
	}
	
	
	
	
	//checks if the game has ended.
	private boolean GameOver()
	{	
		if(world_explored[current_position.getRow()][current_position.getColumn()].contains("W"))
		{
			System.out.println("Agent got killed by the mighty WUMPUS. GAME IS OVER!");
			return true;
		}
			
		if(world_explored[current_position.getRow()][current_position.getColumn()].contains("P"))
		{
			System.out.println("Agent fall into a pit. GAME IS OVER!");
			return true;
		}
		
		if(world_explored[current_position.getRow()][current_position.getColumn()].contains("G"))
		{
			System.out.println("Agent found the gold. Congratulations!!!");
			return true;
		}
			
		return false;
	}
	
	
	
	
	//updates the world that the agent has discovered.
	private void UpdateWorld(Position toDo)
	{
		//agent leaves the current square.
		String current = world_explored[current_position.getRow()][current_position.getColumn()];
		
		world_explored[current_position.getRow()][current_position.getColumn()]=current.substring(0 , current.length()-3);
		if(!current.contains("V")) world_explored[current_position.getRow()][current_position.getColumn()]+=",V";
		
		String next = world_explored[toDo.getRow()][toDo.getColumn()];
		if(next.equals("-")) world_explored[toDo.getRow()][toDo.getColumn()] = gb.GetData(toDo.getRow(),toDo.getColumn());
			
		world_explored[toDo.getRow()][toDo.getColumn()]+=",A"+String.valueOf(toDo.getDirection());
		
		//if we didn't change square , but we only change our direction.
		if(!toDo.equals(this.current_position))
		{
			this.previous_position.setRow(this.current_position.getRow());
			this.previous_position.setColumn(this.current_position.getColumn());
			this.previous_position.setDirection(this.current_position.getDirection());
			this.current_position.setRow(toDo.getRow());
			this.current_position.setColumn(toDo.getColumn());
		}
	
		this.current_position.setDirection(toDo.getDirection());
	}

	
	
	
	//add facts to the knowledge base.
	private void AddFacts()
	{
		int row = this.current_position.getRow();
		int col = this.current_position.getColumn();
		int size = this.world_explored.length;
		
		
		if(this.world_explored[row][col].contains("OK"))
		{
			//add the fact : OK(row , col).
			KB.addFact(new HornLiteral(LiteralSymbols.ok , true , new Constant(row) , new Constant(col)));
			
			//add the facts : not Wumpus(row,col) , not Wumpus(row+1,col) , not Wumpus(row-1,col) , not Wumpus(row,col+1) , not Wumpus(row,col-1).
			KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(row) , new Constant(col)));
			
			int temp_row = row+1;
			if(temp_row<size) KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(temp_row) , new Constant(col)));
			
			temp_row = row-1;
			if(temp_row>=0) KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(temp_row) , new Constant(col)));
			
			int temp_col = col+1;
			if(temp_col<size) KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(row) , new Constant(temp_col)));
			
			temp_col = col-1;
			if(temp_col>=0) KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(row) , new Constant(temp_col)));
			
			
			//now add the facts : not Pit(row,col) ,  not Pit(row+1,col) , not Pit(row-1,col) , not Pit(row,col+1) , not Pit(row,col-1).
			KB.addFact(new HornLiteral(LiteralSymbols.pitfree , true , new Constant(row) , new Constant(col)));
			
			temp_row = row+1;
			if(temp_row<size) KB.addFact(new HornLiteral(LiteralSymbols.pitfree , true , new Constant(temp_row) , new Constant(col)));
			
			temp_row = row-1;
			if(temp_row>=0) KB.addFact(new HornLiteral(LiteralSymbols.pitfree , true , new Constant(temp_row) , new Constant(col)));
			
			temp_col = col+1;
			if(temp_col<size) KB.addFact(new HornLiteral(LiteralSymbols.pitfree, true , new Constant(row) , new Constant(temp_col)));
			
			temp_col = col-1;
			if(temp_col>=0) KB.addFact(new HornLiteral(LiteralSymbols.pitfree , true , new Constant(row) , new Constant(temp_col)));
		}
		
		
		else if(this.world_explored[row][col].contains("S") && this.world_explored[row][col].contains("B"))
		{
			//add the facts : Stench(row , col) , Breeze(row,col).
			KB.addFact(new HornLiteral(LiteralSymbols.stench , true , new Constant(row) , new Constant(col)));
			KB.addFact(new HornLiteral(LiteralSymbols.breeze , true , new Constant(row) , new Constant(col)));
			
			//add the facts : not Wumpus(row,col) , not Pit(row,col)
			KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(row) , new Constant(col)));
			KB.addFact(new HornLiteral(LiteralSymbols.pitfree , true , new Constant(row) , new Constant(col)));
		}
		
		
		else if(this.world_explored[row][col].contains("S"))
		{
			//add the fact : Stench(row , col).
			KB.addFact(new HornLiteral(LiteralSymbols.stench , true , new Constant(row) , new Constant(col)));
			
			//now add the facts : not Wumpus(row,col) , not Pit(row,col) , not Pit(row+1,col) , not Pit(row-1,col) , not Pit(row,col+1) , not Pit(row,col-1).
			KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(row) , new Constant(col)));
			KB.addFact(new HornLiteral(LiteralSymbols.pitfree , true , new Constant(row) , new Constant(col)));
			
			int temp_row = row+1;
			if(temp_row<size) KB.addFact(new HornLiteral(LiteralSymbols.pitfree , true , new Constant(temp_row) , new Constant(col)));
			
			temp_row = row-1;
			if(temp_row>=0) KB.addFact(new HornLiteral(LiteralSymbols.pitfree , true , new Constant(temp_row) , new Constant(col)));
			
			int temp_col = col+1;
			if(temp_col<size) KB.addFact(new HornLiteral(LiteralSymbols.pitfree, true , new Constant(row) , new Constant(temp_col)));
			
			temp_col = col-1;
			if(temp_col>=0) KB.addFact(new HornLiteral(LiteralSymbols.pitfree , true , new Constant(row) , new Constant(temp_col)));
		}
		
		
		else if(this.world_explored[row][col].contains("B"))
		{
			//add the fact : Breeze(row , col).
			KB.addFact(new HornLiteral(LiteralSymbols.breeze , true , new Constant(row) , new Constant(col)));
			
			//add the facts : not Wumpus(row,col) , not Pit(row,col) , not Wumpus(row+1,col) , not Wumpus(row-1,col) , not Wumpus(row,col+1) , not Wumpus(row,col-1).
			KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(row) , new Constant(col)));
			KB.addFact(new HornLiteral(LiteralSymbols.pitfree , true , new Constant(row) , new Constant(col)));
			
			int temp_row = row+1;
			if(temp_row<size) KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(temp_row) , new Constant(col)));
			
			temp_row = row-1;
			if(temp_row>=0) KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(temp_row) , new Constant(col)));
			
			int temp_col = col+1;
			if(temp_col<size) KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(row) , new Constant(temp_col)));
			
			temp_col = col-1;
			if(temp_col>=0) KB.addFact(new HornLiteral(LiteralSymbols.wumpusfree , true , new Constant(row) , new Constant(temp_col)));
		}
	}
	
	
	
	
	//makes the most suitable move.
	private Position MakeMove()
	{
		Random rand = new Random();
		
		//All the moves that the agent can do from the current square.
		ArrayList<Position> all_moves  = getChildren();
		
		//The best moves that the agent can do.
		ArrayList<Position> best_moves = new ArrayList<Position>();
		
		//in this string we keep what the current square contains.
		String current = this.world_explored[this.current_position.getRow()][this.current_position.getColumn()];
		
		
		
		
		//if the square is OK...
		if(current.contains("OK"))
		{
			for(Position pos : all_moves)
			{
				if(!this.Visited(pos)) best_moves.add(pos);
			}
			
			//if there are "best" moves return one of them randomly.
			if(best_moves.size()>0) 
			{
				this.backTimes = 0;
				return best_moves.get(rand.nextInt(best_moves.size()));
			}
			
			//else do a move by going back to a visited square...
			Position temp = all_moves.get(rand.nextInt(all_moves.size()));
			
			//agent goes back to a visited square but not to the one that came from...
			while(temp.equals(this.previous_position))
			{
				temp = all_moves.get(rand.nextInt(all_moves.size()));
			}
			
			this.backTimes++;
			return temp;
		}
		
		
		
		//if agent feels both breeze and stench in current square.
		else if(current.contains("S") && current.contains("B"))
		{
			//we will keep all the moves that we are sure that they don't contain wumpus.
			ArrayList<Position> safe_positions = new ArrayList<Position>();
			
			//all the "dangerous" positions will be stored there.
			ArrayList<Position> risk_positions = new ArrayList<Position>();
			
			//positions that contain pit or wumpus will be stored there.
			ArrayList<Position> death_positions = new ArrayList<Position>();
			
			for(Position pos : all_moves)
			{
				//ask if there is a pit in each one of children squares.
				int answerP = KB.Ask( new HornLiteral(LiteralSymbols.pit , true , new Constant(pos.getRow()) , new Constant(pos.getColumn())) , this.gb.getSize());
				
				//ask if there is a wumpus in each one of children squares.
				int answerW = KB.Ask( new HornLiteral(LiteralSymbols.wumpus , true , new Constant(pos.getRow()) , new Constant(pos.getColumn())), this.gb.getSize());
				
				if(answerP == -1 && answerW == -1)     safe_positions.add(pos);
				
				else if(answerP == -1 && answerW == 0) risk_positions.add(pos);
					
				else if(answerP == -1 && answerW == 1) death_positions.add(pos);
				
				else if(answerP == 0 && answerW == -1) risk_positions.add(pos);
					
				else if(answerP == 0 && answerW == 0)  risk_positions.add(pos);
							
				else if(answerP == 0 && answerW == 1)  death_positions.add(pos);
								
				else if(answerP == 1 && answerW == -1) death_positions.add(pos);
									
				else if(answerP == 1 && answerW == 0)  death_positions.add(pos);
				
				else risk_positions.add(pos);
			}
			
			//from the above moves keep the ones that we have not visited(if any)!
			ArrayList<Position> temp_pos = new ArrayList<Position>(safe_positions);
			for(Position pos : temp_pos)
			{
				if(!this.Visited(pos)) 
				{
					best_moves.add(pos);
					safe_positions.remove(pos);
				}
			}
			
			//if there are "best" moves return one of them randomly.
			if(best_moves.size()>0) 
			{
				this.backTimes = 0;
				return best_moves.get(rand.nextInt(best_moves.size()));
			}
			
			/*
			 * if we have not exceeded the number of times that we can go back
			 * or if we have exceeded it and the only solution is death , we go to an already visited square.
			 */
			else if( (safe_positions.size() > 0 && this.backTimes < this.backMoves) || (safe_positions.size() > 0 && this.backTimes >= this.backMoves && risk_positions.isEmpty() && death_positions.size()>0) )
			{
				this.backTimes++;
				Position temp = safe_positions.get(rand.nextInt(safe_positions.size()));
				
				//if there is only one safe position , go there
				if(safe_positions.size()==1) return temp;
				
				//else go to the one that you did not came from...
				while(temp.equals(this.previous_position))
				{
					temp = safe_positions.get(rand.nextInt(safe_positions.size()));
				}
				
				return temp;
			}
			
			//return an undefined move.
			else if(risk_positions.size()>0)
			{
				this.backTimes = 0;
				System.out.println("I could not make an inference , so i did a random move...");
				return risk_positions.get(rand.nextInt(risk_positions.size()));
			}
		    
			//unavoidably, return a move that leads to the wumpus.
			this.backTimes = 0;
			return death_positions.get(rand.nextInt(death_positions.size()));
		}
		
		
		
		
		//if there is Stench in the current square...
		else if(current.contains("S"))
		{
			//we will keep all the moves that we are sure that they don't contain wumpus.
			ArrayList<Position> safe_positions = new ArrayList<Position>();
			
			//all the "dangerous" positions will be stored there.
			ArrayList<Position> risk_positions = new ArrayList<Position>();
			
			//positions that contain pit or wumpus will be stored there.
			ArrayList<Position> death_positions = new ArrayList<Position>();
			
			for(Position pos : all_moves)
			{
				//ask if there is a Wumpus in each one of children squares.
				int answer = KB.Ask( new HornLiteral(LiteralSymbols.wumpus , true , new Constant(pos.getRow()) , new Constant(pos.getColumn())), this.gb.getSize());
				
				if(answer == -1 ) safe_positions.add(pos);
				
				else if(answer == 1) death_positions.add(pos);
				
				else risk_positions.add(pos);
			}
			
			//from the above moves keep the ones that we have not visited(if any)!
			ArrayList<Position> temp_pos = new ArrayList<Position>(safe_positions);
			for(Position pos : temp_pos)
			{
				if(!this.Visited(pos)) 
				{
					best_moves.add(pos);
					safe_positions.remove(pos);
				}
			}
			
			//if there are "best" moves return one of them randomly.
			if(best_moves.size()>0) 
			{
				this.backTimes = 0;
				return best_moves.get(rand.nextInt(best_moves.size()));
			}
			
			/*
			 * if we have not exceeded the number of times that we can go back
			 * or if we have exceeded it and the only solution is death , we go to an already visited square.
			 */
			else if( (safe_positions.size() > 0 && this.backTimes < this.backMoves) || (safe_positions.size() > 0 && this.backTimes >= this.backMoves && risk_positions.isEmpty() && death_positions.size()>0) )
			{
				this.backTimes++;
				Position temp = safe_positions.get(rand.nextInt(safe_positions.size()));
				
				//if there is only one safe position , go there
				if(safe_positions.size()==1) return temp;
				
				//else go to the one that you did not came from...
				while(temp.equals(this.previous_position))
				{
					temp = safe_positions.get(rand.nextInt(safe_positions.size()));
				}
				
				return temp;
			}
			
			//return an undefined move.
			else if(risk_positions.size()>0)
			{
				this.backTimes = 0;
				System.out.println("I could not make an inference , so i did a random move...");
				return risk_positions.get(rand.nextInt(risk_positions.size()));
			}
		    
			//unavoidably, return a move that leads to the wumpus.
			this.backTimes = 0;
			return death_positions.get(rand.nextInt(death_positions.size()));
		}
		
		
		
		
		//if there is Breeze in the current square...
		else if(current.contains("B"))
		{
		    //we will keep all the moves that we are sure that they don't contain wumpus.
			ArrayList<Position> safe_positions = new ArrayList<Position>();
			
			//all the "dangerous" positions will be stored there.
			ArrayList<Position> risk_positions = new ArrayList<Position>();
			
			//positions that contain pit or wumpus will be stored there.
			ArrayList<Position> death_positions = new ArrayList<Position>();
			
			for(Position pos : all_moves)
			{
				//ask if there is a Pit in each one of children squares.
				int answer = KB.Ask( new HornLiteral(LiteralSymbols.pit , true , new Constant(pos.getRow()) , new Constant(pos.getColumn())), this.gb.getSize());
				
				if(answer == -1 ) safe_positions.add(pos);
				
				else if(answer == 1) death_positions.add(pos);
				
				else risk_positions.add(pos);
			}
			
			//from the above moves keep the ones that we have not visited(if any)!
			ArrayList<Position> temp_pos = new ArrayList<Position>(safe_positions);
			for(Position pos : temp_pos)
			{
				if(!this.Visited(pos)) 
				{
					best_moves.add(pos);
					safe_positions.remove(pos);
				}
			}
			
			//if there are "best" moves return one of them randomly.
			if(best_moves.size()>0) 
			{
				this.backTimes = 0;
				return best_moves.get(rand.nextInt(best_moves.size()));
			}
			
			/*
			 * if we have not exceeded the number of times that we can go back
			 * or if we have exceeded it and the only solution is death , we go to an already visited square.
			 */
			else if( (safe_positions.size() > 0 && this.backTimes < this.backMoves) || (safe_positions.size() > 0 && this.backTimes >= this.backMoves && risk_positions.isEmpty() && death_positions.size()>0) )
			{
				this.backTimes++;
				Position temp = safe_positions.get(rand.nextInt(safe_positions.size()));
				
				//if there is only one safe position , go there
				if(safe_positions.size()==1) return temp;
				
				//else go to the one that you did not came from...
				while(temp.equals(this.previous_position))
				{
					temp = safe_positions.get(rand.nextInt(safe_positions.size()));
				}
				
				return temp;
			}
			
			//return an undefined move.
			else if(risk_positions.size()>0)
			{
				this.backTimes = 0;
				System.out.println("I could not make an inference , so i did a random move...");
				return risk_positions.get(rand.nextInt(risk_positions.size()));
			}
			
			//unavoidably, return a move that leads to the wumpus.
			this.backTimes = 0;
			return death_positions.get(rand.nextInt(death_positions.size()));
		}
		
		return null;
	}
	
	
	
	
	//checks if the square given as argument is visited.
	private boolean Visited(Position pos)
	{
		if(this.world_explored[pos.getRow()][pos.getColumn()].contains("V")) return true;
		
		return false;
	}
	
	
	
	
	
	
	//------------------------------Adding facts that regard the current world------------------------------//
	
	//adds the "tags" of each square.
	private void AddMapFacts()
	{
		this.AddInboundSquares();
		this.AddCorners();
		this.AddEdges();
		this.AddCenterSquares();
	}
	
	
	
	
	//adds to the knowledge base the squares that are in the map.
	private void AddInboundSquares()
	{
		for(int row=0; row<this.world_explored.length; row++)
		{
			for(int col=0; col<this.world_explored.length; col++)
			{
				//InBounds(row,col).
				HornLiteral fact = new HornLiteral(LiteralSymbols.inbounds,true,new Constant(row) , new Constant(col));
				
				this.KB.addFact(fact);
			}
		}
	}
	
	
	
	
	//adds the facts that regard corner squares.
	private void AddCorners()
	{
		//Top_Left_Square(0 , 0).
		HornLiteral fact1 = new HornLiteral(LiteralSymbols.tls,true , new Constant(0),new Constant(0));
		
		//Top_Right_Square(0 , size-1).
		HornLiteral fact2 = new HornLiteral(LiteralSymbols.trs,true , new Constant(0),new Constant(this.world_explored.length-1));
		
		//Bottom_Left_Square(size-1 , 0).
		HornLiteral fact3 = new HornLiteral(LiteralSymbols.bls,true , new Constant(this.world_explored.length-1),new Constant(0));
		
		//Bottom_Right_Square(size-1 , size-1).
		HornLiteral fact4 = new HornLiteral(LiteralSymbols.brs,true , new Constant(this.world_explored.length-1),new Constant(this.world_explored.length-1));
	
		this.KB.addFact(fact1);
		this.KB.addFact(fact2);
		this.KB.addFact(fact3);
		this.KB.addFact(fact4);
	}
	
	
	
	
	//adds the facts that regard edge squares.
	private void AddEdges()
	{
		HornLiteral fact;
		
		//top squares(not corners).
		for(int col=1; col<this.world_explored.length-1; col++)
		{
			fact = new HornLiteral(LiteralSymbols.ts,true , new Constant(0),new Constant(col));
			this.KB.addFact(fact);
		}
		
		//bottom squares(not corners).
		for(int col=1; col<this.world_explored.length-1; col++)
		{
			fact = new HornLiteral(LiteralSymbols.bs,true , new Constant(this.world_explored.length-1),new Constant(col));
			this.KB.addFact(fact);
		}
		
		//right squares(not corners).
		for(int row=1; row<this.world_explored.length-1; row++)
		{
			fact = new HornLiteral(LiteralSymbols.rs,true , new Constant(row),new Constant(this.world_explored.length-1));
			this.KB.addFact(fact);
		}
		
		
		//left squares(not corners).
		for(int row=1; row<this.world_explored.length-1; row++)
		{
			fact = new HornLiteral(LiteralSymbols.ls,true , new Constant(0),new Constant(row));
			this.KB.addFact(fact);
		}
	}
	
	
	
	
	//adds the facts that regard the center squares.
	private void AddCenterSquares()
	{
		for(int row=1; row<this.world_explored.length-1; row++)
		{
			for(int col=1; col<this.world_explored.length-1; col++)
			{
				HornLiteral fact = new HornLiteral(LiteralSymbols.cs , true , new Constant(row) , new Constant(col));
				this.KB.addFact(fact);
			}
		}
	}
	
	
	
	
	
	
	//------------------------------Adding the rules of the game------------------------------//
	
	//adds the rules of the game to the knowledge base.
	private void AddRules()
	{
		this.addWumpusRules();
		this.addPitRules();
	}	
	
	
	
	
	//add the rules that regard the wumpus.
	private void addWumpusRules()
	{
		this.addWumpusCenterRules();
		this.addWumpusEdgeRules();
		this.addWumpusCornerRules();
	}
	
	
	
	
	//add the wumpus rules for the center squares.
	private void addWumpusCenterRules()
	{
		HornClause rule; 
		HornLiteral lit1;
		HornLiteral lit2;
		HornLiteral lit3;
		HornLiteral lit4;
		HornLiteral lit5;
		HornLiteral lit6;
		HornLiteral lit7;
		
		
		/*
		 * 	Center_Square(row,col) ^ Stench(Add(row,1),col) ^ InBounds(Add(row,2)) ^ WumpusFree(Add(row,1),Sub(col,1)) ^ WumpusFree(Add(row,2),col) ^ WumpusFree(Add(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)),new Variable("col"));
		lit6 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Stench(Add(row,1),col) ^ OutOfBounds(Add(row,2)) ^ WumpusFree(Add(row,1),Sub(col,1)) ^ WumpusFree(Add(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Stench(Sub(row,1),col) ^ InBounds(Sub(row,2)) ^ WumpusFree(Sub(row,2),col) ^ WumpusFree(Sub(row,1),Sub(col,1)) ^ WumpusFree(Sub(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)),new Variable("col"));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Stench(Sub(row,1),col) ^ OutOfBounds(Sub(row,2)) ^ WumpusFree(Sub(row,1),Sub(col,1)) ^ WumpusFree(Sub(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Stench(row,Add(col,1)) ^ InBounds(Add(col,2)) ^ WumpusFree(Add(row,1),Add(col,1)) ^ WumpusFree(row,Add(col,2)) ^ WumpusFree(Sub(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false ,new Variable("row"),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit6 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Stench(row,Add(col,1)) ^ OutOfBounds(Add(col,2)) ^ WumpusFree(Add(row,1),Add(col,1)) ^ WumpusFree(Sub(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false ,new Variable("row"),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 *Center_Square(row,col) ^ Stench(row,Sub(col,1)) ^ InBounds(Sub(col,2)) ^ WumpusFree(Sub(row,1),Sub(col,1)) ^ WumpusFree(row,Sub(col,2)) ^ WumpusFree(Add(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false ,new Variable("row"),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit6 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 *Center_Square(row,col) ^ Stench(row,Sub(col,1)) ^ OutOfBounds(Sub(col,2)) ^ WumpusFree(Sub(row,1),Sub(col,1)) ^ WumpusFree(Add(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false ,new Variable("row"),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
	}
	
	
	
	
	//add the wumpus rules for the top,bottom,right and left squares , but not corners.
	private void addWumpusEdgeRules()
	{
		HornClause rule; 
		HornLiteral lit1;
		HornLiteral lit2;
		HornLiteral lit3;
		HornLiteral lit4;
		HornLiteral lit5;
		HornLiteral lit6;
		
		
		/*
		 * 	Left_Square(row,col) ^ Stench(Sub(row,1),col) ^ InBounds(Sub(row,2)) ^ WumpusFree(Sub(row,2),col) ^ WumpusFree(Sub(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ls,false,new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Left_Square(row,col) ^ Stench(Sub(row,1),col) ^ OutOfBounds(Sub(row,2)) ^ WumpusFree(Sub(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ls,false,new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Left_Square(row,col) ^ Stench(Add(row,1),col) ^ InBounds(Add(row,2)) ^ WumpusFree(Add(row,2),col) ^ WumpusFree(Add(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ls,false,new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Left_Square(row,col) ^ Stench(Add(row,1),col) ^ OutOfBounds(Add(row,2)) ^ WumpusFree(Add(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ls,false,new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Left_Square(row,col) ^ Stench(row,Add(col,1)) ^ WumpusFree(Sub(row,1),Add(col,1)) ^ WumpusFree(row,Add(col,2)) ^ WumpusFree(Add(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ls,false,new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		
		
		
		
		
		/*
		 * Right_Square(row,col) ^ Stench(Sub(row,1),col) ^ InBounds(Sub(row,2),col) ^ WumpusFree(Sub(row,1),Sub(col,1)) ^ WumpusFree(Sub(row,2),col) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.rs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Right_Square(row,col) ^ Stench(Sub(row,1),col) ^ OutOfBounds(Sub(row,2),col) ^ WumpusFree(Sub(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.rs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Right_Square(row,col) ^ Stench(Add(row,1),col) ^ InBounds(Add(row,2),col) ^ WumpusFree(Add(row,1),Sub(col,1)) ^ WumpusFree(Add(row,2),col) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.rs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Right_Square(row,col) ^ Stench(Add(row,1),col) ^ OutOfBounds(Add(row,2),col) ^ WumpusFree(Add(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.rs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Right_Square(row,col) ^ Stench(row,Sub(col,1)) ^ WumpusFree(Sub(row,1),Sub(col,1)) ^ WumpusFree(row,Sub(col,2)) ^ WumpusFree(Add(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.rs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false,new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		
		
		
		
		
		/*
		 * Top_Square(row,col) ^ Stench(Add(row,1),col) ^ WumpusFree(Add(row,1),Sub(col,1)) ^ WumpusFree(Add(row,2),col) ^ WumpusFree(Add(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ts,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
	
		/*
		 * Top_Square(row,col) ^ Stench(row,Add(col,1)) ^ InBounds(Add(col,2)) ^ WumpusFree(Add(row,1),Add(col,1)) ^ WumpusFree(row,Add(col,2)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ts,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Top_Square(row,col) ^ Stench(row,Add(col,1)) ^ OutOfBounds(Add(col,2)) ^ WumpusFree(Add(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ts,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Top_Square(row,col) ^ Stench(row,Sub(col,1)) ^ InBounds(Sub(col,2)) ^ WumpusFree(row,Sub(col,2)) ^ WumpusFree(Add(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ts,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Top_Square(row,col) ^ Stench(row,Sub(col,1)) ^ OutOfBounds(Sub(col,2)) ^ WumpusFree(Add(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ts,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		
		
		
		
		
		/*
		 * Bottom_Square(row,col) ^ Stench(Sub(row,1),col) ^ WumpusFree(Sub(row,1),Sub(col,1)) ^ WumpusFree(Sub(row,2),col) ^ WumpusFree(Sub(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false , new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Square(row,col) ^ Stench(row,Add(col,1)) ^ InBounds(Add(col,2)) ^ WumpusFree(row,Add(col,2)) ^ WumpusFree(Sub(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Square(row,col) ^ Stench(row,Add(col,1)) ^ OutOfBounds(Add(col,2)) ^ WumpusFree(Sub(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Square(row,col) ^ Stench(row,Sub(col,1)) ^ InBounds(Sub(col,2)) ^ WumpusFree(row,Sub(col,2)) ^ WumpusFree(Sub(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Square(row,col) ^ Stench(row,Sub(col,1)) ^ OutOfBounds(Sub(col,2)) ^ WumpusFree(Sub(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.wumpus,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
	}
	
	
	
	
	//add the wumpus rules for the corner squares.
	private void addWumpusCornerRules()
	{
		HornClause rule; 
		HornLiteral lit1;
		HornLiteral lit2;
		HornLiteral lit3;
		HornLiteral lit4;
		HornLiteral lit5;
		
		/*
		 * Top_Left_Square(row,col) ^ Stench(Add(row,1),col) ^ WumpusFree(Add(row,2),col) ^ WumpusFree(Add(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.tls,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)),new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 * Top_Left_Square(row,col) ^ Stench(row,Add(col,1)) ^ WumpusFree(row,Add(col,2)) ^ WumpusFree(Add(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.tls,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 * Top_Right_Square(row,col) ^ Stench(Add(row,1),col) ^ WumpusFree(Add(row,2),col) ^ WumpusFree(Add(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.trs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)),new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 * Top_Right_Square(row,col) ^ Stench(row,Sub(col,1)) ^ WumpusFree(row,Sub(col,2)) ^ WumpusFree(Add(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.trs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Left_Square(row,col) ^ Stench(Sub(row,1),col) ^ WumpusFree(Sub(row,2),col) ^ WumpusFree(Sub(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bls,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)),new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Left_Square(row,col) ^ Stench(row,Add(col,1)) ^ WumpusFree(row,Add(col,2)) ^ WumpusFree(Sub(row,1),Add(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bls,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		
		/*
		 * Bottom_Right_Square(row,col) ^ Stench(Sub(row,1),col) ^ WumpusFree(Sub(row,2),col) ^ WumpusFree(Sub(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.brs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)),new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Right_Square(row,col) ^ Stench(row,Sub(col,1)) ^ WumpusFree(row,Sub(col,2)) ^ WumpusFree(Sub(row,1),Sub(col,1)) => Wumpus(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.brs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.stench,false , new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.wumpusfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.wumpus,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
	}
	
	
	
	
	//add the rules that regard the pits.
	private void addPitRules()
	{
		this.addPitCenterRules();
		this.addPitEdgeRules();
		this.addPitCornerRules();
	}
	
	
	
	
	//add the pit rules for the center squares.
	private void addPitCenterRules()
	{
		HornClause rule; 
		HornLiteral lit1;
		HornLiteral lit2;
		HornLiteral lit3;
		HornLiteral lit4;
		HornLiteral lit5;
		HornLiteral lit6;
		HornLiteral lit7;
		
		
		/*
		 * Center_Square(row,col) ^ Breeze(Add(row,1),col) ^ InBounds(Add(row,2)) ^ PitFree(Add(row,1),Sub(col,1)) ^ PitFree(Add(row,2),col) ^ PitFree(Add(row,1),Add(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)),new Variable("col"));
		lit6 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Breeze(Add(row,1),col) ^ OutOfBounds(Add(row,2)) ^ PitFree(Add(row,1),Sub(col,1)) ^ PitFree(Add(row,1),Add(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Breeze(Sub(row,1),col) ^ InBounds(Sub(row,2)) ^ PitFree(Sub(row,2),col) ^ PitFree(Sub(row,1),Sub(col,1)) ^ PitFree(Sub(row,1),Add(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)),new Variable("col"));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Breeze(Sub(row,1),col) ^ OutOfBounds(Sub(row,2)) ^ PitFree(Sub(row,1),Sub(col,1)) ^ PitFree(Sub(row,1),Add(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Breeze(row,Add(col,1)) ^ InBounds(Add(col,2)) ^ PitFree(Add(row,1),Add(col,1)) ^ PitFree(row,Add(col,2)) ^ PitFree(Sub(row,1),Add(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false ,new Variable("row"),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit6 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Breeze(row,Add(col,1)) ^ OutOfBounds(Add(col,2)) ^ PitFree(Add(row,1),Add(col,1)) ^ PitFree(Sub(row,1),Add(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false ,new Variable("row"),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Breeze(row,Sub(col,1)) ^ InBounds(Sub(col,2)) ^ PitFree(Sub(row,1),Sub(col,1)) ^ PitFree(row,Sub(col,2)) ^ PitFree(Add(row,1),Sub(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false ,new Variable("row"),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false,new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit6 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
		
		/*
		 * Center_Square(row,col) ^ Breeze(row,Sub(col,1)) ^ OutOfBounds(Sub(col,2)) ^ PitFree(Sub(row,1),Sub(col,1)) ^ PitFree(Add(row,1),Sub(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.cs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false ,new Variable("row"),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit7 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
		rule.addLiteral(lit7);
		
		this.KB.addClause(rule);
	}
	
	
	
	
	//add the pit rules for the top,bottom,right and left squares , but not corners.
	private void addPitEdgeRules()
	{
		HornClause rule; 
		HornLiteral lit1;
		HornLiteral lit2;
		HornLiteral lit3;
		HornLiteral lit4;
		HornLiteral lit5;
		HornLiteral lit6;
		
		
		/*
		 * 	Left_Square(row,col) ^ breeze(Sub(row,1),col) ^ InBounds(Sub(row,2)) ^ pitFree(Sub(row,2),col) ^ pitFree(Sub(row,1),Add(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ls,false,new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Left_Square(row,col) ^ breeze(Sub(row,1),col) ^ OutOfBounds(Sub(row,2)) ^ pitFree(Sub(row,1),Add(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ls,false,new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Left_Square(row,col) ^ breeze(Add(row,1),col) ^ InBounds(Add(row,2)) ^ pitFree(Add(row,2),col) ^ pitFree(Add(row,1),Add(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ls,false,new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Left_Square(row,col) ^ breeze(Add(row,1),col) ^ OutOfBounds(Add(row,2)) ^ pitFree(Add(row,1),Add(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ls,false,new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Left_Square(row,col) ^ breeze(row,Add(col,1)) ^ pitFree(Sub(row,1),Add(col,1)) ^ pitFree(row,Add(col,2)) ^ pitFree(Add(row,1),Add(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ls,false,new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		
		
		
		
		
		/*
		 * Right_Square(row,col) ^ breeze(Sub(row,1),col) ^ InBounds(Sub(row,2),col) ^ pitFree(Sub(row,1),Sub(col,1)) ^ pitFree(Sub(row,2),col) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.rs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit6 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Right_Square(row,col) ^ breeze(Sub(row,1),col) ^ OutOfBounds(Sub(row,2),col) ^ pitFree(Sub(row,1),Sub(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.rs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Right_Square(row,col) ^ breeze(Add(row,1),col) ^ InBounds(Add(row,2),col) ^ pitFree(Add(row,1),Sub(col,1)) ^ pitFree(Add(row,2),col) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.rs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit6 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Right_Square(row,col) ^ breeze(Add(row,1),col) ^ OutOfBounds(Add(row,2),col) ^ pitFree(Add(row,1),Sub(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.rs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Right_Square(row,col) ^ breeze(row,Sub(col,1)) ^ pitFree(Sub(row,1),Sub(col,1)) ^ pitFree(row,Sub(col,2)) ^ pitFree(Add(row,1),Sub(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.rs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false,new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		
		
		
		
		
		/*
		 * Top_Square(row,col) ^ breeze(Add(row,1),col) ^ pitFree(Add(row,1),Sub(col,1)) ^ pitFree(Add(row,2),col) ^ pitFree(Add(row,1),Add(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ts,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
	
		/*
		 * Top_Square(row,col) ^ breeze(row,Add(col,1)) ^ InBounds(Add(col,2)) ^ pitFree(Add(row,1),Add(col,1)) ^ pitFree(row,Add(col,2)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ts,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Top_Square(row,col) ^ breeze(row,Add(col,1)) ^ OutOfBounds(Add(col,2)) ^ pitFree(Add(row,1),Add(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ts,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit6);
	
		this.KB.addClause(rule);
		
		/*
		 * Top_Square(row,col) ^ breeze(row,Sub(col,1)) ^ InBounds(Sub(col,2)) ^ pitFree(row,Sub(col,2)) ^ pitFree(Add(row,1),Sub(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ts,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Top_Square(row,col) ^ breeze(row,Sub(col,1)) ^ OutOfBounds(Sub(col,2)) ^ pitFree(Add(row,1),Sub(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.ts,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		
		
		
		
		
		/*
		 * Bottom_Square(row,col) ^ breeze(Sub(row,1),col) ^ pitFree(Sub(row,1),Sub(col,1)) ^ pitFree(Sub(row,2),col) ^ pitFree(Sub(row,1),Add(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)) , new Variable("col"));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false , new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Square(row,col) ^ breeze(row,Add(col,1)) ^ InBounds(Add(col,2)) ^ pitFree(row,Add(col,2)) ^ pitFree(Sub(row,1),Add(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Square(row,col) ^ breeze(row,Add(col,1)) ^ OutOfBounds(Add(col,2)) ^ pitFree(Sub(row,1),Add(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false, new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Square(row,col) ^ breeze(row,Sub(col,1)) ^ InBounds(Sub(col,2)) ^ pitFree(row,Sub(col,2)) ^ pitFree(Sub(row,1),Sub(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.inbounds,false, new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Square(row,col) ^ breeze(row,Sub(col,1)) ^ OutOfBounds(Sub(col,2)) ^ pitFree(Sub(row,1),Sub(col,1)) => pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bs,false, new Variable("row") , new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false, new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.outbounds,false, new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit5 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)) , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit6 = new HornLiteral(LiteralSymbols.pit,true, new Variable("row") , new Variable("col"));
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit5);
		rule.addLiteral(lit6);
		
		this.KB.addClause(rule);	
	}
	
		
			
		
	//add the pit rules for the corner squares.
	private void addPitCornerRules()
	{
		HornClause rule; 
		HornLiteral lit1;
		HornLiteral lit2;
		HornLiteral lit3;
		HornLiteral lit4;
		HornLiteral lit5;
		
		/*
		 * Top_Left_Square(row,col) ^ Breeze(Add(row,1),col) ^ PitFree(Add(row,2),col) ^ PitFree(Add(row,1),Add(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.tls,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)),new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 * Top_Left_Square(row,col) ^ Breeze(row,Add(col,1)) ^ PitFree(row,Add(col,2)) ^ PitFree(Add(row,1),Add(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.tls,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 * Top_Right_Square(row,col) ^ Breeze(Add(row,1),col) ^ PitFree(Add(row,2),col) ^ PitFree(Add(row,1),Sub(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.trs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(2)),new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 *Top_Right_Square(row,col) ^ Breeze(row,Sub(col,1)) ^ PitFree(row,Sub(col,2)) ^ PitFree(Add(row,1),Sub(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.trs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false,new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.add,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 *Bottom_Left_Square(row,col) ^ Breeze(Sub(row,1),col) ^ PitFree(Sub(row,2),col) ^ PitFree(Sub(row,1),Add(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bls,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)),new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 *Bottom_Left_Square(row,col) ^ Breeze(row,Add(col,1)) ^ PitFree(row,Add(col,2)) ^ PitFree(Sub(row,1),Add(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.bls,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false,new Variable("row") , new Function(FunctionSymbols.add,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.add,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		
		/*
		 *Bottom_Right_Square(row,col) ^ Breeze(Sub(row,1),col) ^ PitFree(Sub(row,2),col) ^ PitFree(Sub(row,1),Sub(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.brs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)), new Variable("col"));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(2)),new Variable("col"));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
		
		/*
		 * Bottom_Right_Square(row,col) ^ Breeze(row,Sub(col,1)) ^ PitFree(row,Sub(col,2)) ^ PitFree(Sub(row,1),Sub(col,1)) => Pit(row,col)
		 */
		lit1 = new HornLiteral(LiteralSymbols.brs,false,new Variable("row"),new Variable("col"));
		lit2 = new HornLiteral(LiteralSymbols.breeze,false , new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit3 = new HornLiteral(LiteralSymbols.pitfree,false,new Variable("row") , new Function(FunctionSymbols.sub,new Variable("col"),new Constant(2)));
		lit4 = new HornLiteral(LiteralSymbols.pitfree,false,new Function(FunctionSymbols.sub,new Variable("row"),new Constant(1)),new Function(FunctionSymbols.sub,new Variable("col"),new Constant(1)));
		lit5 = new HornLiteral(LiteralSymbols.pit,true,new Variable("row"),new Variable("col"));
		
		
		rule = new HornClause();
		rule.addLiteral(lit1);
		rule.addLiteral(lit2);
		rule.addLiteral(lit3);
		rule.addLiteral(lit4);
		rule.addLiteral(lit5);
		
		this.KB.addClause(rule);
	}
	
	
	
	
	
	
	//------------------------------Killing wumpus methods------------------------------//
	
	//returns true if the agent can kill wumpus.
	private boolean CanKillWumpus()
	{
		return(this.hasArrow && this.wumpusAlive && KB.WumpusPosition()!=null);
	}
	
	
	
	
	//agent turns 90 degrees to the right.
	private void TurnRight()
	{
		int direction = this.current_position.getDirection();
		
		if(direction<4) direction++;
		else direction = 1;
		
		this.current_position.setDirection(direction);
	}
	
	
	
	
	//returns true if agent is looking towards wumpus , false otherwise.
	private boolean LookingTowardsWumpus()
	{
		int direction = this.current_position.getDirection();
		int row = this.current_position.getRow();
		int column = this.current_position.getColumn();
		
		if(direction == 1)
		{
			for(int i = row-1; i>=0; i--)
			{
				if(i == this.wumpusPosition.getRow()) return true;
			}
		}
		
		else if(direction == 2)
		{
			for(int i = column+1; i<this.gb.getSize(); i++)
			{
				if(i == this.wumpusPosition.getColumn()) return true;
			}
		}
		
		else if(direction == 3)
		{
			for(int i = row+1; i<this.gb.getSize(); i++)
			{
				if(i == this.wumpusPosition.getRow()) return true;
			}
		}
		
		else if(direction == 4)
		{
			for(int i = column-1; i>=0; i--)
			{
				if(i == this.wumpusPosition.getColumn()) return true;
			}
		}
		
		return false;
	}
	
	
	
	
	//agent fires his arrow...
	private void ShootArrow()
	{
		System.out.println("Agent fires his arrow...");
		
		this.hasArrow = false;
		
		int direction = this.current_position.getDirection();
		int row = this.current_position.getRow();
		int column = this.current_position.getColumn();
		
		if(direction == 1)
		{
			for(int i = row-1; i>=0; i--)
			{
				if(i == this.wumpusPosition.getRow()) 
				{
					this.wumpusAlive = false;
					this.KB.WumpusIsDead();
				}
			}
		}
		
		else if(direction == 2)
		{
			for(int i = column+1; i<this.gb.getSize(); i++)
			{
				if(i == this.wumpusPosition.getColumn()) 
				{
					this.wumpusAlive = false;
					this.KB.WumpusIsDead();
				}	
			}
		}
		
		else if(direction == 3)
		{
			for(int i = row+1; i<this.gb.getSize(); i++)
			{
				if(i == this.wumpusPosition.getRow())
				{
					this.wumpusAlive = false;
					this.KB.WumpusIsDead();
				}	
			}
		}
		
		else if(direction == 4)
		{
			for(int i = column-1; i>=0; i--)
			{
				if(i == this.wumpusPosition.getColumn())
				{
					this.wumpusAlive = false;
					this.KB.WumpusIsDead();
				}	
			}
		}
		
		
		if(this.wumpusAlive) System.out.println("Agent missed...!\n\n");
		
		else
		{
			System.out.println("Agent killed the Wumpus!!!\n\n");
			this.ModifyBoard();
		}
		
		this.UpdateWorld(this.current_position);
		this.printBoard(); 
	}
	
	
	
	
	//removes the Wumpus and stenches from the map.
	private void ModifyBoard()
	{
		this.gb.RemoveWumpus();
		
		int size = this.gb.getSize();
		
		for(int i=0; i<size; i++)
		{
			for(int j=0; j<size; j++)
			{
				if(world_explored[i][j].contains("W"))
				{
					
					if(!world_explored[i][j].contains("OK") && !world_explored[i][j].contains("P") && !world_explored[i][j].contains("B"))
					{
						world_explored[i][j] = world_explored[i][j].replace("W","OK");
					}
						
					else
					{
						if(world_explored[i][j].contains(",W"))       world_explored[i][j] = world_explored[i][j].replace(",W" , "");
						else if(world_explored[i][j].contains("W,")) world_explored[i][j] = world_explored[i][j].replace("W," , "");
					}
					
				}
				
				else if(world_explored[i][j].contains("S"))
				{
					if(!world_explored[i][j].contains("OK") && !world_explored[i][j].contains("P") && !world_explored[i][j].contains("B"))
					{
						world_explored[i][j] = world_explored[i][j].replace("S","OK");
					}
						
					else
					{
						if(world_explored[i][j].contains(",S"))       world_explored[i][j] = world_explored[i][j].replace(",S" , "");
						else if(world_explored[i][j].contains("S,")) world_explored[i][j] = world_explored[i][j].replace("S," , "");
					}
				}
			}
		}
	}
	
	
	
	
	//agent tries to kill the wumpus!
	private void HuntWumpus()
	{
		this.wumpusPosition = KB.WumpusPosition();
		
		//if the agent is on the same row with the wumpus.
		if(this.wumpusPosition.getRow() == this.current_position.getRow() || this.wumpusPosition.getColumn() == this.current_position.getColumn())
		{
			while(!this.LookingTowardsWumpus())
			{
				this.TurnRight();
			}
			
			this.ShootArrow();
		}
	}	
}//Class Agent end.