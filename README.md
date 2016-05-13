# Project-424
## Launching the server

The server waits for two clients to connect. Closing the GUI window will not terminate the server; the
server exits once the game is finished. If the -k flag was passed, then a new server starts up and waits for
connections as soon as the previous one exits. Log files for each game are automatically written to the logs
subdirectory. The log file for a game contains a list of all moves, names of the two players that participated,
and other parameters. The server also maintains a file, outcomes.txt, which stores a summary of all game
results. At present this consists of the integer game sequence number, the name of each player, the color
and name of the winning player, the number of moves, and the name of the log file.

    java -cp bin boardgame.Server [-p port] [-ng] [-q] [-t n] [-ft n] [-k]

## Launching the client

If using the GUI, one can launch clients (which will run on the same machine as the server) from the Launch menu.
This starts a regular client running in a background thread, which plays using the selected player class. In
order to play a game of Hus using the GUI by clicking on pits to select moves, choose Launch human
player from the Launch menu.

Clients can also be launched from the command line. From the root directory of the project package,
run the command:
    java -cp bin boardgame.Client [playerClass [serverName [serverPort]]]

##Reference
GUI, game rules, and a random player was provided by Joelle Pineau for the course: COMP 424 at McGill University. 
