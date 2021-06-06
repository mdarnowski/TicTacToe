import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

class TicTacToe {
    // Holding track of players squares occupation
    private final Player[] board = new Player[9];
    // Current player ('0') - the one that starts the battle.
    Player currentPlayer;

    TicTacToe(Player p1, Player p2) {
        // assigns players a random 'mark' ('X' or 'O')
        Random r = new Random();
        if(r.nextBoolean()) {
            p1.setMark( 'O');
            p2.setMark( 'X');
            currentPlayer = p1;
        }else {
            p2.setMark( 'O');
            p1.setMark( 'X');
            currentPlayer = p2;
        }
        p1.myGame = this;
        p2.myGame = this;
        p1.opponent = p2;
        p2.opponent = p1;
        p1.callGame();
        p2.callGame();
    }

    public boolean hasWinner() {
        // Determines if TicTacToe game has a result
        return (board[0] != null && board[0] == board[1] && board[0] == board[2])
                || (board[3] != null && board[3] == board[4] && board[3] == board[5])
                || (board[6] != null && board[6] == board[7] && board[6] == board[8])
                || (board[0] != null && board[0] == board[3] && board[0] == board[6])
                || (board[1] != null && board[1] == board[4] && board[1] == board[7])
                || (board[2] != null && board[2] == board[5] && board[2] == board[8])
                || (board[0] != null && board[0] == board[4] && board[0] == board[8])
                || (board[2] != null && board[2] == board[4] && board[2] == board[6]
        );
    }

    public boolean isTie() {
        // Determines if TicTacToe game is a tie
        return Arrays.stream(board).allMatch(Objects::nonNull);
    }

    public synchronized void move(int location, Player player) {
        // Checks if move is valid
        if (player != currentPlayer) {
            throw new IllegalStateException("Not your turn");
        } else if (player.opponent == null) {
            throw new IllegalStateException("You don't have an opponent yet");
        } else if (board[location] != null) {
            throw new IllegalStateException("Cell already occupied");
        }
        board[location] = currentPlayer;
        currentPlayer = currentPlayer.opponent;
    }
}