package student_player.mytools;

import hus.HusBoardState;
import hus.HusMove;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;

public class MyTools {

  public static HusMove getBestMove(final HusBoardState board_state)
  {
    final int turn = board_state.getTurnPlayer();

// fixed move if move first
    if (board_state.getTurnNumber() == 0 && turn == 0) {
      return new HusMove(23,0);
    }

    // keep track of the move to return. Use final array to be able to keep it updated in inner classes
    final HusMove[] best_move = new HusMove[1];
    best_move[0] = Greedy.getGreedyMove(board_state);

    Callable<Object> to_run = new Callable<Object>()
    {
      @Override
      public Object call() throws Exception
      {
        AlphaBeta.alphaPrune(board_state, turn, Integer.MIN_VALUE, Integer.MAX_VALUE, AlphaBeta.TOTAL_STEPS, best_move);
        return 1;
      }
    };

    final ExecutorService thread = Executors.newSingleThreadExecutor();

    try
    {
      final Future<Object> f = thread.submit(to_run);

      f.get(1800, TimeUnit.MILLISECONDS);
    }
    catch (final TimeoutException e)
    {
      return best_move[0];
    }
    catch (final Exception e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      thread.shutdown();
    }
    return best_move[0];
  }
}

  /******************
  An algorithm using alpha-beta pruning.
  *******************/
class AlphaBeta{
  public static final int TOTAL_STEPS = 7;

  private static int getAlphaScore(HusBoardState board_state, int turn) {

    int seeds = 0;
    int moves = 0;
    int attacks = 0;
    int op_moves = 0;

    int[][] pits = board_state.getPits();

    int[] my_pits = pits[turn];
    int[] op_pits = pits[(turn+1)%2];


    for (int i=0; i<32; i++) {
      seeds += my_pits[i];
      if (my_pits[i] > 1) {
        moves++;
        if ((i+my_pits[i])%32 >= 16 && my_pits[i+my_pits[i]]>0 && op_pits[47-(i+my_pits[i])] > 0) {
          attacks += (op_pits[47-(i+my_pits[i])]+op_pits[31-(i+my_pits[i])]);
        }
      }
      if (op_pits[i] > 1) {
        op_moves++;
      }
    }
    if (turn == 0){
      return (seeds-48)*4 + moves*3 + attacks/2 - op_moves*3;
    }
    return (seeds-48)*3 + moves*3+ attacks - op_moves*4;
  }

  public static int alphaPrune(HusBoardState board_state, int turn, int alpha, int beta, int steps_left, HusMove[] best_move) {
    if ((steps_left == 0) || (board_state.gameOver() == true)) {
      return getAlphaScore(board_state, turn);
    }
    int best_score = Integer.MIN_VALUE;
    ArrayList<HusMove> possible_moves = board_state.getLegalMoves();
    Collections.shuffle(possible_moves);
    for (HusMove m : possible_moves) {
      HusBoardState cloned_board_state = (HusBoardState) board_state.clone();
      cloned_board_state.move(m);
      int score = betaPrune(cloned_board_state, turn, alpha, beta, steps_left - 1, best_move);
      if (score > best_score) {
        best_score = score;
        if (steps_left == TOTAL_STEPS)  {
          best_move[0] = m;
        }
      }
      if (best_score > alpha){
        alpha = best_score;
      }
      if (beta <= alpha) break;
    }
    return best_score;
  }

  public static int betaPrune(HusBoardState board_state, int turn, int alpha, int beta, int steps_left, HusMove[] best_move) {
    if ((steps_left == 0) || (board_state.gameOver() == true)) {
      return getAlphaScore(board_state, turn);
    }
    int best_score = Integer.MAX_VALUE;
    ArrayList<HusMove> possible_moves = board_state.getLegalMoves();
    Collections.shuffle(possible_moves);
    for (HusMove m : possible_moves) {
      HusBoardState cloned_board_state = (HusBoardState) board_state.clone();
      cloned_board_state.move(m);
      int score = alphaPrune(cloned_board_state, turn, alpha, beta, steps_left - 1, best_move);
      if (score < best_score){
        best_score = score;
      }
      if (best_score < beta){
        beta = best_score;
      }
      if (beta <= alpha) break;
    }
    return best_score;
  }
}
/******************
A Greedy algorithm to return a backup move in case full algorithm times out.
*******************/
class Greedy{
  public static HusMove getGreedyMove(HusBoardState board_state){
    // Get the contents of the pits so we can use it to make decisions.
    int[][] pits = board_state.getPits();
    // Use ``player_id`` and ``opponent_id`` to get my pits and opponent pits.
    int[] my_pits = pits[board_state.getTurnPlayer()];
    int prev_score = 0;
    // Get the legal moves for the current board state.
    ArrayList<HusMove> possible_moves = board_state.getLegalMoves();
    HusMove best_move = possible_moves.get(0);
    HusBoardState cloned_board_state = (HusBoardState) board_state.clone();
    int current_score = 0;
    int max_score = 0;
    Collections.shuffle(possible_moves);
    for (HusMove m: possible_moves){
      cloned_board_state = (HusBoardState) board_state.clone();
      cloned_board_state.move(m);
      current_score = getGreedyScore(cloned_board_state);
      if (current_score >= max_score){
        max_score = current_score;
        best_move = m;
      }
    }
    return best_move;

  }

  private static int getGreedyScore(HusBoardState board_state){
    int seeds = 0;
    int moves = 0;
    int attacks = 0;
    int score = 0;
    int best_score = Integer.MAX_VALUE;
    int[][] pits = board_state.getPits();
    int[] my_pits = pits[0];
    int[] op_pits = pits[0];
    ArrayList<HusMove> possible_moves = board_state.getLegalMoves();
    HusBoardState cloned_board_state = (HusBoardState) board_state.clone();
    for (HusMove m: possible_moves){
      cloned_board_state = (HusBoardState) board_state.clone();
      cloned_board_state.move(m);
      pits = cloned_board_state.getPits();
      my_pits = pits[cloned_board_state.getTurnPlayer()];
      op_pits = pits[(cloned_board_state.getTurnPlayer()+1)%2];
      score = 0;
      for (int i=0; i<32; i++) {
        seeds += my_pits[i];
        if (my_pits[i] > 1) {
          moves++;
          if ((i+my_pits[i])%32 >= 16 && my_pits[i+my_pits[i]]>0 && op_pits[47-(i+my_pits[i])] > 0){
            attacks += (op_pits[47-(i+my_pits[i])]+op_pits[31-(i+my_pits[i])]);
          }
        }
        score = seeds + moves + attacks/2;
        if (score<best_score){
          best_score = score;
        }
      }
    }
    return best_score;
  }
}
