import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Player implements Runnable {
    private final Socket socket;
    private PrintWriter output;
    private boolean willingly;
    private char mark;
    private final int id;

    Player opponent;
    TicTacToe myGame;
	// Players with a game 
    ArrayList<Player> players;
	// Players without a game
    ArrayList<Player> activePlayers;

    public Player(Socket socket, ArrayList<Player> players, ArrayList<Player> activePlayers, int id) {
        this.socket = socket;
        this.willingly = false;
        this.players = players;
        this.activePlayers = activePlayers;
        players.add(this);
        this.id = id;
    }

    public void setMark(char mark) {
        this.mark = mark;
    }

    public void callGame() {
        // Starting, setting marks
        output.println("WELCOME " + mark);
        output.println("MESSAGE 'O' starts the game");
    }

    public void findGame() {
        for (Player p : players) {
            if (p.willingly && p != this) {
                new TicTacToe(p, this);
                System.out.println("new game started!!!");
                if (players.remove(p))
                    activePlayers.add(p);
                if (players.remove(this))
                    activePlayers.add(this);
                return;
            }
        }
        output.println("MESSAGE Waiting for opponent...");
    }

    @Override
    public String toString() {
        return " ID: " + id + " port: " + socket.getPort() + " local port: " +
                socket.getLocalPort() + " host address: " + socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            // Reacting to the commands from the client
            Scanner input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);

            while (input.hasNextLine()) {
                String command = input.nextLine();

                if (command.startsWith("PLAY") && !willingly) {
                    willingly = true;
                    findGame();
                }

                if (command.startsWith("LIST")) {
                    // Server forms a list of logged in players
                    // packs information from this list to a message
                    ArrayList<Player> toPrint = new ArrayList<>(activePlayers);
                    toPrint.addAll(players);
                    StringBuilder mess = new StringBuilder("LIST IS ");

                    for (Player p : toPrint) {
                        if (mess.length() != 8)
                            mess.append(",");
                        mess.append(p.toString());
                    }
                    output.println(mess);
                }

                if (command.startsWith("RESTART")) {
                    // Player is removed from the list of players willing to play
                    // His game is terminated
                    willingly = false;
                    myGame = null;
                    if (activePlayers.remove(this))
                        players.add(this);
                }

                if (command.startsWith("QUIT") || command.startsWith("LOGOUT")) {
                    // Closing
                    players.remove(this);
                    activePlayers.remove(this);
                    if (myGame != null)
                        if (myGame.currentPlayer == this)
                            myGame.currentPlayer = null;
                        else
                            myGame.currentPlayer.opponent = null;
                    try {
                        socket.close();
                    } catch (IOException ignored) { }

                    return;
                }

                if (myGame != null) {
                    try {
                        if (command.startsWith("MOVE")) {
                            int location = Integer.parseInt(command.substring(5));
                            // Server checks if move is valid
                            // Sends the response to the client and to this opponent
                            myGame.move(location, this);
                            output.println("VALID MOVE");
                            opponent.output.println("OPPONENT MOVED " + location);
                            if (myGame.hasWinner()) {
                                output.println("VICTORY");
                                opponent.output.println("DEFEAT");
                            } else if (myGame.isTie()) {
                                output.println("TIE");
                                opponent.output.println("TIE");
                            }
                        }
                    } catch (IllegalStateException e) {
                        output.println("MESSAGE " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Opponent logouts
            // Server informs client that the game is invalid
            if (opponent != null && opponent.output != null) {
                opponent.output.println("OTHER PLAYER LEFT");
                willingly = false;
                myGame = null;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("welcome to the server! \nset port value to proceed...");

        Scanner sc = new Scanner(System.in);
        String string;

        // TCP port
        int port;

        // Program checks users input to create new ServerSocket
        while (true) {
            string = sc.nextLine();
            if (string.matches(
                    "^([1-9]|[1-5]?[0-9]{2,4}|6[1-4][0-9]{3}|65[1-4][0-9]{2}|655[1-2][0-9]|6553[1-5])$")) {
                System.out.println("port will be set to: " + string);
                port = Integer.parseInt(string);
                break;
            } else {
                System.out.println("unacceptable input, try again");
            }
        }
        sc.close();

        // Players with a game
        ArrayList<Player> activePlayers = new ArrayList<>();
        // Players without a game
        ArrayList<Player> players = new ArrayList<>();

        // Prints info
        try {
            System.out.println("Your current IP address : " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("Your current Hostname : " + InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try (ServerSocket listener = new ServerSocket(port)) {
            System.out.println("Waiting for players...");
            ExecutorService pool = Executors.newFixedThreadPool(400);
            int id = 0;
            // Creates new Player
            // Adds him to the list of players without a game

            while (true) {
                Player p = new Player(listener.accept(), players, activePlayers, id);
                pool.execute(p);
                id++;
            }
        }
    }
}