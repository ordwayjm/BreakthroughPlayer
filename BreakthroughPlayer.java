package BreakthroughPlayer;

import game.*;
import breakthrough.*;

public class BreakthroughPlayer extends GamePlayer {
	
	protected BreakthroughMove[] mvStack;
	
	public BreakthroughPlayer(String n) {
		super(n, new BreakthroughState(), false);
	}
	
	public boolean isTerminal(BreakthroughState board) {
		return false;
	}
	
	public BreakthroughMove[] getMoves(BreakthroughState board) {
		return mvStack;
	}
	
	public BreakthroughState makeMove(BreakthroughState board, BreakthroughMove move) {
		return board;
	}
	
	public BreakthroughMove minimax(BreakthroughState board, int depth, boolean maximizingPlayer, int depthLimit) {
		double bestValue;
		if(depth == 0 || isTerminal(board)) {
			
		}
		if(maximizingPlayer) {
			bestValue = Double.NEGATIVE_INFINITY;
			for(BreakthroughMove mv : getMoves(board)) {
				BreakthroughState temp = makeMove(board, mv);
				minimax(temp, depth - 1, false, depthLimit);
			}
		}
		else {
			bestValue = Double.POSITIVE_INFINITY;
			for(BreakthroughMove mv : getMoves(board)) {
				BreakthroughState temp = makeMove(board, mv);
				minimax(temp, depth - 1, true, depthLimit);
			}
		}
		return null;
	}
	
	public GameMove getMove(GameState state, String lastMove) {
		return minimax((BreakthroughState) state, 0, true, 100);
	}
	
	public static void main(String [] args) {
		GamePlayer p = new RandomBreakthroughPlayer("Test Breakthrough");
		p.compete(args);
	}
}