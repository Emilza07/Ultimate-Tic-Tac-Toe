package com.emil_z.model;

import android.graphics.Point;
import java.util.ArrayList;
import java.util.List;

public class CPU {
    private static final int MAX_DEPTH = 5;

    /**
     * Finds the best move for the CPU using the minimax algorithm
     * @param outerBoard The current board state
     * @param player The CPU player (X or O)
     * @return The best move as a BoardLocation object
     */
    public static BoardLocation findBestMove(OuterBoard outerBoard, char player) {
        // Keep track of the best move and its score
        BoardLocation bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        // Get all possible moves
        List<BoardLocation> availableMoves = getPossibleMoves(outerBoard);

        // Evaluate each move using minimax
        for (BoardLocation move : availableMoves) {
            // Make the move
            OuterBoard boardCopy = deepCopyOuterBoard(outerBoard);
            // Set current player to ensure correct player makes the move
            boardCopy.makeMove(move);

            // Calculate score for this move using minimax
            int score = minimax(boardCopy, MAX_DEPTH, false, player,
                               Integer.MIN_VALUE, Integer.MAX_VALUE);

            // Update best move if this move has a better score
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * The minimax algorithm with alpha-beta pruning
     * @param outerBoard The current board state
     * @param depth Current depth in the game tree
     * @param isMaximizing Whether it's the maximizing player's turn
     * @param cpuPlayer The CPU player symbol (X or O)
     * @param alpha Alpha value for pruning
     * @param beta Beta value for pruning
     * @return The best score for the current board state
     */
    private static int minimax(OuterBoard outerBoard, int depth, boolean isMaximizing,
                       char cpuPlayer, int alpha, int beta) {
        char opponent = (cpuPlayer == 'X') ? 'O' : 'X';

        // Terminal conditions: reached max depth or game over
        if (depth == 0 || outerBoard.isGameOver()) {
            return evaluateBoard(outerBoard, cpuPlayer);
        }

        List<BoardLocation> availableMoves = getPossibleMoves(outerBoard);

        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (BoardLocation move : availableMoves) {
                OuterBoard boardCopy = deepCopyOuterBoard(outerBoard);
                boardCopy.makeMove(move);

                int score = minimax(boardCopy, depth - 1, false, cpuPlayer, alpha, beta);
                maxScore = Math.max(maxScore, score);

                // Alpha-beta pruning
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (BoardLocation move : availableMoves) {
                OuterBoard boardCopy = deepCopyOuterBoard(outerBoard);
                boardCopy.makeMove(move);

                int score = minimax(boardCopy, depth - 1, true, cpuPlayer, alpha, beta);
                minScore = Math.min(minScore, score);

                // Alpha-beta pruning
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    break;
                }
            }
            return minScore;
        }
    }

    /**
     * Creates a deep copy of the OuterBoard
     * @param original The original board to copy
     * @return A deep copy of the board
     */
    private static OuterBoard deepCopyOuterBoard(OuterBoard original) {
        // Implement a deep copy of the OuterBoard
        // This is a placeholder - you'll need to create a proper deep copy method
        // either here or in the OuterBoard class
        OuterBoard copy = new OuterBoard(original);
        // You need to implement the actual copying of the board state
        return copy;
    }

    /**
     * Evaluates the board state and returns a score
     * @param outerBoard The current board state
     * @param player The CPU player
     * @return A score representing how favorable the board is for the CPU
     */
    private static int evaluateBoard(OuterBoard outerBoard, char player) {
        int score = 0;

        // Check each small board (subgame)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                InnerBoard innerBoard = outerBoard.getBoard(new Point(i, j));
                char winner = innerBoard.getWinner();

                if (winner == player) {
                    score += 10;
                } else if (winner != '\0' && winner != '-') {
                    score -= 10;
                }
            }
        }

        // Check overall game winner
        char gameWinner = outerBoard.getWinner();
        if (gameWinner == player) {
            score += 1000;
        } else if (gameWinner != '\0' && gameWinner != '-') {
            score -= 1000;
        }

        return score;
    }

    /**
     * Gets all possible moves for the current board state
     * @param outerBoard The current board state
     * @return A list of all possible moves
     */
    private static List<BoardLocation> getPossibleMoves(OuterBoard outerBoard) {
        List<BoardLocation> moves = new ArrayList<>();

        // Check all possible positions
        for (int outerRow = 0; outerRow < 3; outerRow++) {
            for (int outerCol = 0; outerCol < 3; outerCol++) {
                Point outer = new Point(outerRow, outerCol);

                for (int innerRow = 0; innerRow < 3; innerRow++) {
                    for (int innerCol = 0; innerCol < 3; innerCol++) {
                        Point inner = new Point(innerRow, innerCol);
                        BoardLocation location = new BoardLocation(outer, inner);

                        // Check if the move is legal according to the rules
                        if (outerBoard.isLegal(location)) {
                            moves.add(location);
                        }
                    }
                }
            }
        }

        return moves;
    }
}