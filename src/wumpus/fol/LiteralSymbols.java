package wumpus.fol;


public class LiteralSymbols 
{
	//Wumpus(x,y).
	public static String wumpus = "Wumpus";
	
	//Stench(x,y).
	public static String stench = "Stench";
	
	//Pit(x,y).
	public static String pit = "Pit";
	
	//Breeze(x,y).
	public static String breeze = "Breeze";
	
	//OK(x,y).
	public static String ok = "OK";
	
	//WumpusFree(x,y) = not Wumpus(x,y).
	public static String wumpusfree = "WumpusFree";
		
	//PitFree(x,y) = not Pit(x,y).
	public static String pitfree = "PitFree";
	
	//Center_Square(x,y).
	public static String cs = "Center_Square";
	
	//Left_Square(x,y).
	public static String ls = "Left_Square";
	
	//Right_Square(x,y).
	public static String rs = "Right_Square";
	
	//Top_Square(x,y).
	public static String ts = "Top_Square";
	
	//Bottom_Square(x,y).
	public static String bs = "Bottom_Square";
	
	//Top_Left_Square(x,y).
	public static String tls = "Top_Left_Square";
	
	//Top_Right_Square(x,y).
	public static String trs = "Top_Right_Square";
	
	//Bottom_Left_Square(x,y).
	public static String bls = "Bottom_Left_Square";
	
	//Bottom_Right_Square.
	public static String brs = "Bottom_Right_Square";
	
	//InBounds(x) :  0<=x<map size. 
	public static String inbounds = "InBounds";
	
	//OutOfBounds(x) :  x>map size or x<0. 
	public static String outbounds = "OutOfBounds";
	
}