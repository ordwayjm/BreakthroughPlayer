package BreakthroughPlayer;

import java.util.ArrayList;

import game.*;
import breakthrough.*;

public class BreakthroughPlayer extends BaseBreakthroughPlayer {
	
	public final int DEPTH_LIMIT = 10;
	protected ScoredBreakthroughMove[] mvStack = new ScoredBreakthroughMove[DEPTH_LIMIT + 1];
	
	public BreakthroughPlayer(String n) {
		super(n, false);
		for(int i = 0; i < mvStack.length; i++) {
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
		board.makeMove(move);
		return board;
	}

	public boolean isTerminal(BreakthroughState board, ScoredBreakthroughMove move) {
		GameState.Status status = board.getStatus();
		boolean isTerminal = true;
		if(status == GameState.Status.HOME_WIN) {
			move.set(0,0,0,0, Double.POSITIVE_INFINITY);
		} else if(status == GameState.Status.AWAY_WIN) {
			move.set(0,0,0,0, Double.NEGATIVE_INFINITY);
		}
		else isTerminal = false;
		return isTerminal;
	}

	public void minimax(BreakthroughState board, int depth, int depthLimit) {
		boolean toMaximize = (board.getWho() == GameState.Who.HOME);		
		boolean isTerminal = isTerminal(board, mvStack[depth]);
		ScoredBreakthroughMove bestMove = mvStack[depth];
		if(isTerminal) {
			;
		} else if(depth == depthLimit) {
			 mvStack[depth].set(0,0,0,0, evalBoard2(board));
		}
		else if(toMaximize) {
			ArrayList<BreakthroughMove> moves = getMoves(board, BreakthroughState.homeSym);
			//System.out.println(depth + " - " + getMoves(board, BreakthroughState.homeSym).size());
			bestMove.set(moves.get(moves.size()-1), Double.NEGATIVE_INFINITY);
			for(BreakthroughMove mv : moves) {
				BreakthroughState temp = makeMove(board, mv);
				minimax(temp, depth + 1, depthLimit);
				if(mvStack[depth+1].score > bestMove.score) {
					bestMove.set(mv.startRow, mv.startCol, mv.endingRow, mv.endingCol, mvStack[depth+1].score);
				}
			}
		} else {
			ArrayList<BreakthroughMove> moves = getMoves(board, BreakthroughState.awaySym);
			bestMove.set(moves.get(moves.size()-1), Double.POSITIVE_INFINITY);
			for(BreakthroughMove mv : moves) {
				BreakthroughState temp = makeMove(board, mv);
				minimax(temp, depth + 1, depthLimit);
				if(mvStack[depth+1].score < bestMove.score) {
					bestMove.set(mv.startRow, mv.startCol, mv.endingRow, mv.endingCol, mvStack[depth+1].score);
				}
			}
		}
		mvStack[depth] = bestMove;
	}

	public GameMove getMove(GameState state, String lastMove) {
		minimax((BreakthroughState) state, 0, DEPTH_LIMIT);
		return mvStack[0];
	}

	public static void main(String [] args) {
		GamePlayer p = new BreakthroughPlayer("Test Breakthrough");
		p.compete(args);
	}
}