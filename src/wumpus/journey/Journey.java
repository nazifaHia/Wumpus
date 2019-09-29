package wumpus.journey;

import java.util.Random;

public class Journey 
{
	public static void main(String[] args)
	{
		final String[] proper_noun = {"world0.xml", "world1.xml", "world2.xml", "world3.xml","world4.xml","world5.xml"};
		Random random = new Random();
		int index = random.nextInt(proper_noun.length);
		
		System.out.println("You Selected World number "+(index+1));
		Agent agent=new Agent("res/"+proper_noun[index]);
		agent.FindGold();
	}
}