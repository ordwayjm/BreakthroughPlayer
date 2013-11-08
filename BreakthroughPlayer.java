package BreakthroughPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import game.*;
import breakthrough.*;

public class BreakthroughPlayer extends BaseBreakthroughPlayer {
	
	public final int DEPTH_LIMIT = 7;
	public final int MAX_DEPTH = 50;
	public final double MAX_SCORE = Double.POSITIVE_INFINITY;
	public final double MIN_SCORE = Double.NEGATIVE_INFINITY;
	protected ScoredBreakthroughMove[] mvStack;
	
	static PrintWriter writer;
	
	File file = new File("openingBook10.txt");
	int numMove;
	
	/**
	 * Constructor for BreakthroughPlayer. Takes a String name and is passed to the BaseBreakthroughPlayer.
	 * @param n name of BreakthroughPlayer
	 */
	public BreakthroughPlayer(String n) {
		super(n, false);
	}
	
	/**
	 * Initializes mvStack to empty ScoredBreakthroughMoves
	 */
	public void init() {
		mvStack = new ScoredBreakthroughMove[MAX_DEPTH];
		for(int i = 0; i < MAX_DEPTH; i++) {
			mvStack[i] = new ScoredBreakthroughMove(0,0,0,0,0);
		}
	}
	
	/**
	 * Gets a list of moves that are possible with the current board
	 * @param board current board
	 * @param who the current player who's turn it is
	 * @return an ArrayList of possible BreakthroughMoves
	 */
	public ArrayList<BreakthroughMove> getMoves(BreakthroughState board, char who) {
		ArrayList<BreakthroughMove> moves = new ArrayList<BreakthroughMove>();
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				// Home team moves from lower rows to higher rows
				if(who == BreakthroughState.homeSym){
					if(possibleMove(board, who, i, j, i+1, j-1)) moves.add(new BreakthroughMove(i,j,i+1,j-1));
					if(possibleMove(board, who, i, j, i+1, j)) moves.add(new BreakthroughMove(i,j,i+1,j));
					if(possibleMove(board, who, i, j, i+1, j+1)) moves.add(new BreakthroughMove(i,j, i+1, j+1));
				}
				// Away team moves from higher rows to lower rows
				else{
					if(possibleMove(board, who, i, j, i-1, j-1)) moves.add(new BreakthroughMove(i, j, i-1, j-1));
					if(possibleMove(board, who, i, j, i-1, j)) moves.add(new BreakthroughMove(i, j, i-1, j));
					if(possibleMove(board, who, i, j, i-1, j+1)) moves.add(new BreakthroughMove(i, j, i-1, j+1));	
				}
			}
		}
		return moves;
	}
	
	/**
	 * Checks to see if a BreakthroughMove is possible to make on the current board
	 * @param board the current board
	 * @param who the current player who's turn it is
	 * @param r1 start row of the BreakthroughMove
	 * @param c1 start col of the BreakthroughMove
	 * @param r2 end row of the BreakthroughMove
	 * @param c2 end col of the BreakthroughMove
	 * @return true if the move is possible, false otherwise
	 */
	public boolean possibleMove(BreakthroughState board, char who, int r1, int c1, int r2, int c2){
		// Not possible if any index is off the board
		if(r1 < 0 || c1 < 0 || r2 < 0 || c2 < 0 || r1 >= N || 
				c1 >= N || r2 >= N || c2 >= N) return false;
		// No move can change row or column by more than 1
		else if(Math.abs(r1-r2) > 1 || Math.abs(c1 - c2) > 1) return false;
		// Not possible if the start position doesn't have a piece of the moving player's
		else if(board.board[r1][c1] != who) return false;
		// Not possible if the end position does have a piece of the moving player's (can't self-capture)
		else if(board.board[r2][c2] == who) return false;
		// Non-diagonal move can only be to an empty space
		else if(c1 == c2 && board.board[r2][c2] != BreakthroughState.emptySym) return false;
		else return true;
	}
	
	/**
	 * Makes a BreakthroughMove on the current board, returns a copy of the board
	 * @param board the current Breakthrough board
	 * @param move the BreakthroughMove to be made
	 * @return the BreakthroughBoard after the move has been applied
	 */
	public BreakthroughState makeMove(BreakthroughState board, BreakthroughMove move) {
		BreakthroughState brd = board;
		brd.makeMove(move);
		return brd;
	}
	
	/**
	 * Determines if the current BreakthroughBoard is the end of the game
	 * @param board the current BreakthroughBoard
	 * @param move current move being made
	 * @return true if the board is terminal, false otherwise
	 */
	public boolean isTerminal(BreakthroughState board, ScoredBreakthroughMove move) {
		GameState.Status status = board.getStatus();
		boolean isTerminal = true;
		if(status == GameState.Status.HOME_WIN) {
			move.set(0,0,0,0, MAX_SCORE);
		}else if(status == GameState.Status.AWAY_WIN) {
			move.set(0,0,0,0, MIN_SCORE);
		}
		else isTerminal = false;
		return isTerminal;
	}
	
	/**
	 * Gets a starting move from the opening book file if there is one available
	 * @param board the current BreakthroughBoard
	 * @return true if a move was found, false otherwise
	 */
	public boolean getOpeningBook(BreakthroughState board) {
		Scanner in = null;
		String state = "";
		String move = "";
		try {
			in = new Scanner(file);
		} catch (FileNotFoundException e) {
			return false;
		}
		while(in.hasNextLine()) {
			state = "";
			move = "";
			for(int i = 0; i < N + 1; i++) {
				state += in.nextLine();
				if(i < N)
					state += "\n";
			}
			move = in.nextLine();
			if(state.equals(board.toString())) {
				System.out.println("MATCH");
				mvStack[0].set(Integer.parseInt(move.substring(0,1)), Integer.parseInt(move.substring(2,3)), 
									Integer.parseInt(move.substring(4,5)), Integer.parseInt(move.substring(6,7)), 0);
				in.close();
				return true;
			}
		}
		in.close();
		return false;
	}
	
	/**
	 * AlphaBeta search to determine the best possible move to make with the current BreakthroughState
	 * @param board the current BreakthroughState
	 * @param depth the current depth of the search
	 * @param depthLimit the deepest the search can go to
	 * @param alpha best min value seen
	 * @param beta best max value seen
	 */
	public void alphabeta(BreakthroughState board, int depth, int depthLimit, double alpha, double beta) {
		boolean toMaximize = (board.getWho() == GameState.Who.HOME);	
		boolean toMinimize = !toMaximize;
		boolean isTerminal = isTerminal(board, mvStack[depth]);
		if(isTerminal) {
			;
		} else if(depth == depthLimit) {
			 mvStack[depth].set(0,0,0,0, evalBoard2(board));
		} else {
			double bestScore = (board.getWho() == GameState.Who.HOME ? MIN_SCORE: MAX_SCORE);
			char who = (board.getWho() == GameState.Who.HOME ? BreakthroughState.homeSym : BreakthroughState.awaySym);
			ArrayList<BreakthroughMove> moves = getMoves(board, who);
			ScoredBreakthroughMove bestMove = mvStack[depth];
			ScoredBreakthroughMove nextMove = mvStack[depth+1];
			bestMove.set(moves.get(0), bestScore);
			for(BreakthroughMove mv : moves) {
				alphabeta(makeMove((BreakthroughState)board.clone(), mv), depth + 1, depthLimit, alpha, beta);
				if(toMaximize && nextMove.score > bestMove.score) {
					bestMove.set(mv, nextMove.score);
				}else if(!toMaximize && nextMove.score < bestMove.score) {
					bestMove.set(mv, nextMove.score);
				}
				if(toMinimize) {
					beta = Math.min(bestMove.score, beta);
					if(bestMove.score <= alpha || bestMove.score == MIN_SCORE) {
						return;
					}
				}else {
					alpha = Math.max(bestMove.score, alpha);
					if(bestMove.score >= beta || bestMove.score == MAX_SCORE) {
						return;
					}
				}
				mvStack[depth] = bestMove;
			}
		}
	}
	
	/**
	 * Sends the current move to be played to the tournament
	 */
	public GameMove getMove(GameState state, String lastMove) {
		/*
		if(numMove < 8) {
			writer.println(state.toString());
			writer.println(mvStack[0].toString());
		} */
		if(numMove < 5) {
			System.out.println("Checking Move Book...");
			if(!getOpeningBook((BreakthroughState) state)) {
				System.out.println("...doing AlphaBeta instead.");
				alphabeta((BreakthroughState) state, 0, DEPTH_LIMIT, MIN_SCORE, MAX_SCORE);
			}
		}
		else alphabeta((BreakthroughState) state, 0, DEPTH_LIMIT, MIN_SCORE, MAX_SCORE);
		numMove++;
		return mvStack[0];
	}
	
	/**
	 * Main method. Called by the tournament to start the BreakthroughPlayer
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		try {
			writer = new PrintWriter("openingBook6.txt", "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} */
		GamePlayer p = new BreakthroughPlayer("Stonewall Jackson");
		p.compete(args);
		//writer.close();
	}
}