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
public abstract class BaseBreakthroughPlayer extends GamePlayer {
	
	public static int N = BreakthroughState.N;
	public static int WINNING_SCORE = 100000;
	public static int PIECE_SCORE = 100;
	public static int BACK_ROW_SCORE = 20;
	
	protected static class BreakthroughPiece {

		public double score;
		public int row, col;
		public GameState.Who who;

		protected BreakthroughPiece(int row, int col, GameState.Who who) {
			this.row = row;
			this.col = col;
			this.who = who;
			this.score = evaluate();
		}

		private double evaluate() {
			return winner() + distanceFromWin() + inBackRow();
		}
		
		private int distanceFromWin() {
			if(who == GameState.Who.HOME)
				return N - row;
			else return row;
		}

		private int winner() {
			if(who == GameState.Who.HOME) {
				if(row == N-1)
					return WINNING_SCORE;
			} else {
				if(row == 0)
					return WINNING_SCORE;
			}
			return 0;
		}
		
		private int inBackRow() {
			if(who == GameState.Who.HOME) {
				if (row == 0)
					return BACK_ROW_SCORE;
			} else {
				if(row == N-1)
					return BACK_ROW_SCORE;
			}
			return 0;
		}
	};
	
	protected class ScoredBreakthroughMove extends BreakthroughMove {
		public double score;
		
		public ScoredBreakthroughMove(int r1, int c1, int r2, int c2, double s) {
			super(r1, c1, r2, c2);
			score = s;
		}

		public void set(int r1, int c1, int r2, int c2, double s) {
			startRow = r1;
			startCol = c1;
			endingRow = r2;
			endingCol = c2;
			score = s;
		}
		
		public void set(BreakthroughMove m, double s) {
			startRow = m.startRow;
			startCol = m.startCol;
			endingRow = m.endingRow;
			endingCol = m.endingCol;
			score = s;
		}
	};
	
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
						if(i == N - 1) score += WINNING_SCORE;
					}
					// If away team, add N-row value (lower rows are worth more since they start at top)
					else{
						score += (N - i);
						// If the state has an away piece in the first row, away wins. This massively favors away.
						if(i == 0) score += WINNING_SCORE;
					}
				}		
			}
		}

		return score;
	}
	
	/**
	 * Very simple eval function. Having more pieces is good. Thus, it will capture when possible.
	 * @param brd
	 * @param who
	 * @return
	 */
	private static int eval2(BreakthroughState brd, char who){
		int score = 0;
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				if(brd.board[i][j] == who) score++;
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
	
	/**
	 * Auxiliary evaluation function
	 * @param brd
	 * @return home eval - away eval based on number of remaining pieces
	 */
	protected static int evalBoard2(BreakthroughState brd){
		return eval2(brd, BreakthroughState.homeSym) - eval2(brd, BreakthroughState.awaySym);
	}
	
	protected static double evalBoard3(BreakthroughState brd) {
		double playerScore = 0;
		double opponentScore = 0;
		char player = brd.who == GameState.Who.AWAY ? BreakthroughState.awaySym : BreakthroughState.homeSym;
		for(int i = 0; i < N; i++) {
			for(int j = 0; j < N; j++) {
				if(brd.board[i][j] == player) {
					BreakthroughPiece piece = new BreakthroughPiece(i, j, brd.who);
					playerScore += piece.score;
				}
				else if(brd.board[i][j] != BreakthroughState.emptySym){
					BreakthroughPiece piece = new BreakthroughPiece(i, j, brd.who);
					opponentScore += piece.score;
				}
			}
		}
		return playerScore - opponentScore;
	}
}
