/**
 * 
 */
package BreakthroughPlayer;

import breakthrough.*;
import game.*;

/**
 * @author Ben
 *
 */
public abstract class BaseBreakthroughPlayer extends GamePlayer{
	public static int N = BreakthroughState.N;
	public static int ARBITRARILY_HIGH_NUMBER = 100000;
	
	public BaseBreakthroughPlayer(String name, boolean deterministic){
		super(name, new BreakthroughState(), deterministic);
	}
	
	/**
	 * Counts how many rows forward from the player's back row
	 * all their pieces are. Their backmost row counts as 1 forward
	 * to incentivise preserving pieces. The foremost row counts for an
	 * arbitrarily high number, as it is a victory condition.
	 * @param brd board to be evaluated
	 * @param who BreakthroughState.homeSym or .awaySym
	 * @return the score. Higher than 2(N^2) indicates victory.
	 */
	private static int eval(BreakthroughState brd, char who){
		int score = 0;
		// Validate the player character
		if(who != BreakthroughState.homeSym && who != BreakthroughState.awaySym){
			System.out.println("Error: " + who + " is not the symbol of a valid Breakthrough player. Use either " + 
					BreakthroughState.homeSym + " or " + BreakthroughState.awaySym + ".");
			System.exit(0);
		}
		// Compute the score
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				if(brd.board[i][j] == who){
					// If home team, add row value+1 (to give some value to row 0)
					if(who == BreakthroughState.homeSym){
						score += i + 1;
						// If the state has a home piece in the last row, home wins. This massively favors home. 
						if(i == N - 1) score += ARBITRARILY_HIGH_NUMBER;
					}
					// If away team, add N-row value (lower rows are worth more since they start at top)
					else{
						score += (N - i);
						// If the state has an away piece in the first row, away wins. This massively favors away.
						if(i == 0) score += ARBITRARILY_HIGH_NUMBER;
					}
				}		
			}
		}

		return score;
	}
	
	/**
	 * The evaluation function
	 * @param brd board to be evaluated
	 * @return home evaluation - away evaluation
	 */
	protected static int evalBoard(BreakthroughState brd){
		return eval(brd, BreakthroughState.homeSym) - eval(brd,BreakthroughState.awaySym);
	}
}
