SERVER

    -->Player
    
            Program checks users input to create a new ServerSocket
            with the integer taken as a parameter that represents TCP port.
            ServerSocket will listen for new clients over the network.
            If connection is established, program creates a new Player
            and adds him to the ArrayList of 'players' (list of players without a game).
            
            --reacting to the commands from the client

                "PLAY"
                    Player ads himself to the que of players wanting to play
                    and checks if there is another player willing to play with him.
                    If there are no opponents available, player sends to the client:
                    "MESSAGE Waiting for opponent...".
                    Two willing to compete players form a game ('TicTacToe').

                "LIST"
                    Server forms a list of logged in players(players with game and players without a game)
                    and packs information from this list to a String message.
                    Sends list with prefix "LIST IS".

                 "RESTART"
                    Player is removed from the list of players willing to play. His game is terminated.

                 "LOGOUT"
                    Client logs out. Socket closes.

                 "MOVE" (tic-tac-toe playing)
                    Client wants to move. Server checks if move is valid
                    and sends the response to the client and to this opponent;


            --sending to the client

                 "VICTORY" AND "DEFEAT" (tic-tac-toe playing)
                     If game has a winner. Server informs player and his opponent about the results;
                 "TIE"
                      Game is inconclusive. Server informs clients that there is no winner.
                 "OTHER PLAYER LEFT" (tic-tac-toe playing)
                      Opponent logouts. Server informs client that the game is invalid.


    -->TicTacToe
           Constructor takes the players and assigns to them a random 'mark' ('X' or 'O').
           Current player ('0') - the one that starts the battle.
           If currentPlayer moves, 'currentPlayer' pointer takes the value of currentPlayer opponent.

           TicTacToe holds track of players squares occupation and determines the winner.

           --used by Player class
               hasWinner()
                    Determines if TicTacToe game has a result.
               isTie()
                    Determines if TicTacToe game is a tie.
               move()
                    Checks if move is valid;

CLIENT

    ->>Client
    
        Program checks users input to create a new Socket
        with the integer and String taken as a parameter(TCP port, server address).
        Socket connects to server.
        Simple java swing implementation. Jtable holds current state of a game that client is playing.

        --client output
            --Buttons
                "LOGOUT"
                    Client logs out.
                "LIST"
                    Client asks server for the list of players. Then parses and shows the list from the received massage.
                "PLAY"
                    Client tells server that he is ready for a game.
                --Nine buttons representing fields that can be occupied during the game.
                    "MOVE 'place'"
                        Next move that needs to be verified by the server.

            "RESTART"
                Client is ready to ask for another game.


        --commands from server
            "VALID MOVE"
                Move received by the server. Opponents turn.
            ENDING THE GAME COMMANDS
                "VICTORY", "DEFEAT","TIE", "OTHER PLAYER LEFT" (--> TicTacToe)
                    After receiving one of this commands, client sends message "RESTART" to the server.


HOW TO USE

    * compile and run Player
    * set port, copy server ip
    * compile and run Client
    * set port, set server ip
