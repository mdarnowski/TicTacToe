import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.Scanner;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;

public class Client {
    // Simple java swing implementation
    private final JButton playButton;
    private final JFrame frame = new JFrame("Tic Tac Toe");
    private final JLabel messageLabel = new JLabel("click play to find opponent");

    // Table holds current state of a game that client is playing
    private final Field[] board = new Field[9];
    private Field current;
    // Socket input stream
    private final Scanner in;
    // Socket output stream
    private final PrintWriter out;
    private char mark;
    private char opponentMark;
    private JPanel boardPanel;

    public Client(String serverAddress, int port) throws Exception {
        /*
        Buttons:
                "LOGOUT"
                    Client logs out.
                "LIST"
                    Client asks server for the list of players.
                    Then parses and shows the list from the received massage.
                "PLAY"
                    Client tells server that he is ready for a game.
                --Nine buttons representing fields that can be occupied during the game.
                    "MOVE 'place'"
                        Next move that needs to be verified by the server.
        */
        Socket socket = new Socket(serverAddress, port);
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);
        messageLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(messageLabel, BorderLayout.PAGE_START);
        doTitle();
        JPanel btnPanel = new JPanel();
        JButton exButton = new JButton("LOGOUT");
        exButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                out.println("LOGOUT");
                System.exit(0);
                //playButton.setEnabled(false);
            }
        });
        JButton listButton = new JButton("LIST");
        listButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                out.println("LIST");
            }
        });
        playButton = new JButton("PLAY");
        playButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                out.println("PLAY");
                playButton.setEnabled(false);
                setFields();
            }
        });

        btnPanel.add(exButton);
        btnPanel.add(listButton);
        btnPanel.add(playButton);

        frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
        frame.getContentPane().add(btnPanel, BorderLayout.SOUTH);
        frame.setTitle("Tic Tac Toe");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                out.println("LOGOUT");
                System.exit(0);

            }
        });

        // Adjusting frame settings
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public void play() {
        try {
            String response;
            while (in.hasNextLine()) {
                response = in.nextLine();
                if (response.startsWith("WELCOME")) {
                    mark = response.charAt(8);
                    opponentMark = mark == 'X' ? 'O' : 'X';
                    frame.setTitle("Player " + mark);
                }
                if (response.startsWith("LIST IS ")) {
                    // Client asks server for the list of players
                    // Then parses and shows the list from the received massage
                    JFrame f = new JFrame("list of players");
                    f.setSize(450, 200);
                    JScrollPane scrollPane = new JScrollPane();
                    JList<String> list = new JList<>(response.substring(8).split(","));
                    scrollPane.setViewportView(list);
                    f.setResizable(false);
                    f.getContentPane().add(scrollPane);
                    list.setSize(450, 200);
                    scrollPane.setSize(450, 200);
                    f.setVisible(true);
                }

                if (response.startsWith("VALID MOVE")) {
                    messageLabel.setText("Opponents turn");
                    current.setText(mark);
                    current.repaint();
                } else if (response.startsWith("OPPONENT MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setText(opponentMark);
                    board[loc].repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You won!!!");
                    continuePlaying();
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("you lost...");
                    continuePlaying();
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("No-win situation");
                    continuePlaying();
                } else if (response.startsWith("OTHER PLAYER LEFT")) {
                    messageLabel.setText("Opponent left");
                    continuePlaying();
                }
            }
        } catch (Exception e) {
            out.println("LOGOUT");
            System.exit(0);
        }
    }

    public void continuePlaying() {
        out.println("RESTART");
        frame.setTitle("Tic Tac Toe");
        playButton.setEnabled(true);
    }

    // Single Tic-tac-toe block
    static class Field extends JPanel {
        JLabel label = new JLabel();

        public Field() {
            setBackground(Color.white);
            setLayout(new GridBagLayout());
            label.setFont(new Font("Times New Roman", Font.BOLD, 100));
            add(label);
        }

        public Field(char text) {
            setBackground(Color.white);
            setLayout(new GridBagLayout());
            label.setFont(new Font("Times New Roman", Font.BOLD, 100));
            label.setText("" + text);
            add(label);
        }
        public void setText(char text) {
            label.setText(text + "");
        }
    }

    private void setFields() {
        // Setting interactive buttons
        if (boardPanel != null)
            frame.getContentPane().remove(boardPanel);
        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Field();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    current = board[j];
                    out.println("MOVE " + j);
                }
            });
            boardPanel.add(board[i]);
            boardPanel.repaint();
        }
        boardPanel.revalidate();
        boardPanel.validate();
        frame.getContentPane().add(boardPanel);
    }

    private void doTitle() {
        // Starting title "TICTACTOE"
        if (boardPanel != null)
            frame.getContentPane().remove(boardPanel);

        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));

        char[] tic = "TICTACTOE".toCharArray();
        for (int i = 0; i < tic.length; i++) {
            board[i] = new Field(tic[i]);

            boardPanel.add(board[i]);
            boardPanel.repaint();
        }
        boardPanel.revalidate();
        boardPanel.validate();
        frame.getContentPane().add(boardPanel);
    }


    public static void main(String[] args) throws Exception {
        System.out.println("welcome, client! \nset port value to proceed...");
        Scanner sc = new Scanner(System.in);
        String string;

        // Checks users input to create new Socket
        // (TCP port, server address)
        int port;
        String address;

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
        System.out.println("enter server address value to proceed...");

        while (true) {
            string = sc.nextLine();
            if (string.matches("^(?=.*[^.]$)((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.?){4}$")) {
                System.out.println("address will be set to: " + string);
                address = string;
                break;
            } else {
                System.out.println("unacceptable input, try again");
            }
        }
        sc.close();

        Client client = new Client(address, port);
        client.play();
    }


}