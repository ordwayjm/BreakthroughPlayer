package BreakthroughPlayer;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import game.*;
import breakthrough.*;

public class ThreadedBreakthroughPlayer extends BaseBreakthroughPlayer implements Runnable{
	
	
	public final int DEPTH_LIMIT = 7;
	public final int MAX_DEPTH = 50;
	public static final int NUM_THREADS = 4;
	public final double MAX_SCORE = Double.POSITIVE_INFINITY;
	public final double MIN_SCORE = Double.NEGATIVE_INFINITY;
	
	protected ScoredBreakthroughMove[] mvStack;
	protected int threadID, startIndex, endIndex;
	protected BreakthroughState board;
	
	static CountDownLatch threadLatch = new CountDownLatch(NUM_THREADS);
	
	static PrintWriter writer;
	
	/**
	 * 
	 * @param n
	 */
	public ThreadedBreakthroughPlayer(String n) {
		super(n, false);
		startIndex = 0;
		endIndex = 0;
		threadID = 0;
	}
	
	public ThreadedBreakthroughPlayer(String n, BreakthroughState brd, int id, int moveCount){
		super(n, false);
		
		threadID = id;
		// Start at ID times width of one thread's portion of possible moves
		startIndex = id*(moveCount/NUM_THREADS);
		// End at the next start index or at the number of moves. Exclusive either way.
		endIndex = Math.min((moveCount), startIndex+(moveCount/NUM_THREADS));
		board = (BreakthroughState) brd.clone();
		
		mvStack = new ScoredBreakthroughMove[MAX_DEPTH];
		for(int i = 0; i < MAX_DEPTH; i++) {
			mvStack[i] = new ScoredBreakthroughMove(0,0,0,0,0);
		}
	}
	
	// TODO: Implement the run() method to run alphabetaThreaded on appropriate indices and get best move into mvstack[0]
	public void run(){
		alphabetaThreaded(board, 0, DEPTH_LIMIT, MIN_SCORE, MAX_SCORE, startIndex, endIndex);
		writer.println("Thread number " + threadID + " found move:");
		writer.println(board.toString());
		writer.println(mvStack[0].toString());
		
		threadLatch.countDown();
	}
	
	/**
	 * 
	 */
	public void init() {
		mvStack = new ScoredBreakthroughMove[MAX_DEPTH];
		for(int i = 0; i < MAX_DEPTH; i++) {
			mvStack[i] = new ScoredBreakthroughMove(0,0,0,0,0);
		}
	}
	
	/**
	 * 
	 * @param board
	 * @param who
	 * @return
	 */
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
	
	/**
	 * 
	 * @param board
	 * @param who
	 * @param r1
	 * @param c1
	 * @param r2
	 * @param c2
	 * @return
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
	 * 
	 * @param board
	 * @param move
	 * @return
	 */
	public BreakthroughState makeMove(BreakthroughState board, BreakthroughMove move) {
		BreakthroughState brd = board;
		brd.makeMove(move);
		return brd;
	}
	
	/**
	 * 
	 * @param board
	 * @param move
	 * @param depth
	 * @param depthLimit
	 * @return
	 */
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
	
	/**
	 * 
	 * @param board
	 * @param depth
	 * @param depthLimit
	 * @param alpha
	 * @param beta
	 * @param startIndex
	 * @param endIndex
	 */
	public void alphabetaThreaded(BreakthroughState board, int depth, int depthLimit, double alpha, double beta, int startIndex, int endIndex) {
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
			for(int i = startIndex; i < endIndex; i++) {
				alphabeta(makeMove((BreakthroughState)board.clone(), moves.get(i)), depth + 1, depthLimit, alpha, beta);
				if(toMaximize && nextMove.score > bestMove.score) {
					bestMove.set(moves.get(i), nextMove.score);
				}else if(!toMaximize && nextMove.score < bestMove.score) {
					bestMove.set(moves.get(i), nextMove.score);
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
	 * 
	 * @param board
	 * @param depth
	 * @param depthLimit
	 * @param alpha
	 * @param beta
	 */
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
	 * TODO: Implement for multithreaded getMove
	 */
	public GameMove getMove(GameState state, String lastMove) {
		// Set threadLatch to wait for NUM_THREADS threads to complete
		threadLatch = new CountDownLatch(NUM_THREADS);
		//alphabeta((BreakthroughState) state, 0, DEPTH_LIMIT, MIN_SCORE, MAX_SCORE);
		int numMoves = getMoves((BreakthroughState) state, (state.getWho() 
				== GameState.Who.HOME ? BreakthroughState.homeSym : BreakthroughState.awaySym)).size();
		// Create threads, set each to run alphabetaMulti on a portion of the top-level moves
		ThreadedBreakthroughPlayer[] threads = new ThreadedBreakthroughPlayer[NUM_THREADS];
		for(int i = 0; i < NUM_THREADS; i++){
			threads[i] = new ThreadedBreakthroughPlayer("Thread", (BreakthroughState) state, i, numMoves);
			threads[i].run();
		}
		// Join the threads, compare to find best score, copy it to this one's movestack
		try{
			threadLatch.await();
		}
		catch(InterruptedException e){
			writer.println("Exception waiting on threads to find moves");
		}
		
		double bestScore = threads[0].mvStack[0].score;
		int bestIndex = 0;
		for(int i = 0; i < NUM_THREADS; i++){
			if(state.getWho() == GameState.Who.HOME && threads[i].mvStack[0].score > bestScore) bestIndex = i;
			else if(state.getWho() == GameState.Who.AWAY && threads[i].mvStack[0].score < bestScore) bestIndex = i;
		}
		
		mvStack[0] = threads[bestIndex].mvStack[0];
		
		writer.println(state.toString());
		writer.println(mvStack[0].toString());
		return mvStack[0];
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			writer = new PrintWriter("openingBook.txt", "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		GamePlayer p = new ThreadedBreakthroughPlayer("Stonewall Jackson");
		p.compete(args);
		writer.close();
	}
}