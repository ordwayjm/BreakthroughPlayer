package breakthrough;
import game.*;
import java.util.*;

public class BreakthroughPlayer extends GamePlayer {
	public BreakthroughPlayer(String n) 
	{
		super(n, new BreakthroughState(), false);
	}
	public GameMove getMove(GameState state, String lastMove)
	{
		
	}
	public static void main(String [] args)
	{
		GamePlayer p = new BreakthroughPlayer("Random BT+");
		p.compete(args);
	}
}
