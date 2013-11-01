package BreakthroughPlayer;

import connect4.Connect4Move;
import game.*;
import breakthrough.*;

public class BreakthroughPlayer extends GamePlayer {
	
	protected ScoredBreakthroughMove[] mvStack;
	
	protected class ScoredBreakthroughMove extends BreakthroughMove {
		public ScoredBreakthroughMove(int r1, int c1, int r2, int c2, double s)
		{
			super(r1, c1, r2, c2);
			score = s;
		}
		
		public void set(int r1, int c1, int r2, int c2, double s)
		{
			startRow = r1;
			startCol = c1;
			endingRow = r2;
			endingCol = c2;
			score = s;
		}
		public double score;
	}
	
	public BreakthroughPlayer(String n) {
		super(n, new BreakthroughState(), false);
	}
	
	public BreakthroughMove[] getMoves(BreakthroughState board) {
		return mvStack;
	}
	
	public BreakthroughState makeMove(BreakthroughState board, BreakthroughMove move) {
		board.makeMove(move);
		return board;
	}
	
	public boolean isTerminal(BreakthroughState board, ScoredBreakthroughMove move, int depth, int depthLimit) {
		GameState.Status status = board.getStatus();
        boolean isTerminal = false;
        if(status == GameState.Status.HOME_WIN) {
        	move.set(0,0,0,0, Double.POSITIVE_INFINITY);
        	isTerminal = true;
        }else if(status == GameState.Status.AWAY_WIN) {
        	move.set(0,0,0,0, Double.NEGATIVE_INFINITY);
        	isTerminal = true;
        }else if(depth < depthLimit) {
        	move.set(0,0,0,0,0);
        	isTerminal = true;
        }
        return isTerminal;
	}
	
	public void minimax(BreakthroughState board, int depth, int depthLimit) {
		boolean toMaximize = (board.getWho() == GameState.Who.HOME);		
		boolean isTerminal = isTerminal(board, mvStack[depth], depth, depthLimit);
		double bestValue;
		if(depth == 0 || isTerminal) {
			
		}
		if(toMaximize) {
			bestValue = Double.NEGATIVE_INFINITY;
			for(BreakthroughMove mv : getMoves(board)) {
				BreakthroughState temp = makeMove(board, mv);
				minimax(temp, depth - 1, depthLimit);
			}
		} else {
			bestValue = Double.POSITIVE_INFINITY;
			for(BreakthroughMove mv : getMoves(board)) {
				BreakthroughState temp = makeMove(board, mv);
				minimax(temp, depth - 1, depthLimit);
			}
		}
	}
	
	public GameMove getMove(GameState state, String lastMove) {
		minimax((BreakthroughState) state, 0, 100);
		return mvStack[0];
	}
	
	public static void main(String [] args) {
		GamePlayer p = new RandomBreakthroughPlayer("Test Breakthrough");
		p.compete(args);
	}
}