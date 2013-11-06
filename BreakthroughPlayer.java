package BreakthroughPlayer;

import java.util.ArrayList;
import game.*;
import breakthrough.*;

public class BreakthroughPlayer extends BaseBreakthroughPlayer {
	
	public final int DEPTH_LIMIT = 7;
	public final int MAX_DEPTH = 50;
	public final double MAX_SCORE = Double.POSITIVE_INFINITY;
	public final double MIN_SCORE = Double.NEGATIVE_INFINITY;
	protected ScoredBreakthroughMove[] mvStack;
	
	public BreakthroughPlayer(String n) {
		super(n, false);
	}
	
	public void init() {
		mvStack = new ScoredBreakthroughMove[MAX_DEPTH];
		for(int i = 0; i < MAX_DEPTH; i++) {
			mvStack[i] = new ScoredBreakthroughMove(0,0,0,0,0);
		}
	}

	public ArrayList <BreakthroughMove> getMoves(BreakthroughState board, char who) {
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

	public BreakthroughState makeMove(BreakthroughState board, BreakthroughMove move) {
		BreakthroughState brd = board;
		brd.makeMove(move);
		return brd;
	}

	public boolean isTerminal(BreakthroughState board, ScoredBreakthroughMove move, int depth, int depthLimit) {
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

	public void alphabeta(BreakthroughState board, int depth, int depthLimit, double alpha, double beta) {
		boolean toMaximize = (board.getWho() == GameState.Who.HOME);	
		boolean toMinimize = !toMaximize;
		boolean isTerminal = isTerminal(board, mvStack[depth], depth, depthLimit);
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
					if (bestMove.score <= alpha || bestMove.score == MIN_SCORE) {
						return;
					}
				} else {
					alpha = Math.max(bestMove.score, alpha);
					if (bestMove.score >= beta || bestMove.score == MAX_SCORE) {
						return;
					}
				}
				mvStack[depth] = bestMove;
			}
		}
	}

	public GameMove getMove(GameState state, String lastMove) {
		alphabeta((BreakthroughState) state, 0, DEPTH_LIMIT, MIN_SCORE, MAX_SCORE);
		return mvStack[0];
	}

	public static void main(String [] args) {
		GamePlayer p = new BreakthroughPlayer("Test Breakthrough");
		p.compete(args);
	}
}