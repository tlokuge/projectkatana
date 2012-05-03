==== SERVER ====
Before starting the server up, make sure the SQL databases have been created and set up. The table structure and some sample data have already been provided (see db\create_tables.sql and db\populate_tables.sql files). In the working directory of the server (the folder which the server will be run from), make sure that the Server.conf file has been properly filled out. Once this is done, start the server up and it will cache certain data from the SQL tables (this can be seen in the output console window). If no errors were encountered, the server should now be listening for connections.

The KatanaServer class handles all incoming connections, using a ServerSocket listening on a port specified by the Server.conf file. As soon as a connection is made, a new KatanaClient object is made which will service that client connection for it's duration. 

The KatanaClient class runs each instance of itself in a new thread. It contains the client socket (which was passed in by KatanaServer previously) and transmits and receives data packets from that socket. Once a packet has been received (given that it is a valid KatanaPacket), the packet will be parsed and serviced within the PacketHandler.

The PacketHandler is an abtract class that contain static functions designed for specific packets. Every packet has an opcode, which serves as an identifier for what type of packet it is, with each type being serviced differently. A quick list of packet opcodes and what the packet contains follows below. Note that all opcodes originating from the client starts with C_<opcode name> whereas all opcodes from the server are S_<opcode name>

C_REGISTER: Contains the username and password of the user, which will then be registered in the database and can be used for authentication from then on. If the registration is successful, the user is authenticated and a S_REG_OK packet is sent to the client, if not a S_REG_NO is sent. If the username already exists on the server, and the password is correct, this packet acts as a C_LOGIN packet (below) and authenticates the user. If the password is wrong, a S_AUTH_NO is sent instead.
C_LOGIN: Similar to C_REGISTER, contains the username and password of the user. If they are valid, the user will be authenticated via S_AUTH_OK. Otherwise, S_AUTH_NO is sent.
C_LOGOUT: Logs the client out from the server, which also sends a S_LOGOUT to the client, which signals the client to close the connection.
C_ROOM_LIST:  Contains the latitude and longitude of the client's current location. If a valid location exists for those coordinates, an S_ROOM_LIST packet is sent (see below), and the user is added to the lobby of that location. If the coordinates sent do not have a valid location, a S_BAD_LOCATION is sent.
C_LEADERBOARD: Requests the leaderboard of that specific lobby. The server responds with S_LEADERBOARD.
C_ROOM_CREATE: Contains the room name, game difficulty, and max players for the room to be created. The server then creates that specified room in that location, and adds the user to it (as it's leader). This room will now be visible on the lobby to all players.
C_ROOM_DESTROY: If the player is in a room, and is the leader of that room, all players will be booted to the lobby and the room destroyed.
C_ROOM_JOIN: Contains the ID of a room in the lobby that the player wishes to join as well as the ID of the class that the player has chosen. If the room exists and is not full, the player is added to it and has the icon of the class that she chose. All other players in that room will be sent a S_ROOM_PLAYER_JOIN.
C_ROOM_LEAVE: The server removes the player from the room that she is in. If the player is the leader of that room, the room is also destroyed similar to C_ROOM_DESTROY. If the room is not destroyed, all other players in that room is sent a S_ROOM_PLAYER_LEAVE.
C_CLASS_CHANGE: Contains the new class ID that the player wants to change to, can only be sent from within a room, allows the player to change her class. All other players will be sent a S_PLAYER_UPDATE_CLASS.
C_GAME_START: Can only be sent from within a room by the room leader, signalling the server to start the game. All players are sent a S_GAME_START.
C_GAME_READY: Sent once the game has started, signalling the server that the player is ready to play the game (everything within the game engine has been loaded).
C_MOVE: Contains the x,y coordinates of the location on the map that the player is moving to. All other players in the map are sent a S_GAME_UPDATE_MOVE.
C_MOVE_COMPLETE: Signals the server that the player has reached the destination she sent in the previous C_MOVE. 
C_SPELL: Currently unused, was supposed to be used when the player casts a spell via a swipe, but our game was changed to make spells unnecessary.

S_REG_OK: Contains the player ID on the server of the player, signalling the client that the registration was successful and that the client is now authenticated on the server. When the player is authenticated, they are added to a hash map, with the key being the player ID and the value being the player object.
S_REG_NO: Informs the client that the registration was not successful, and the client is still unauthenticated.
S_AUTH_OK: Similar to S_REG_OK, contains the player ID on the server of the player, telling the client that she is now authenticated on the server.
S_AUTH_NO: Similar to S_REG_NO, informs the client that the authentication failed.
S_LOGOUT: Informs the client that the connection is closing and logs them off.
S_BAD_LOCATION: Sent after receiving a C_ROOM_LIST, if the sent coordinates do not match any location on the database.
S_ROOM_LIST: Sent after receiving a C_ROOM_LIST, if the sent coordinates have a valid location on the database. Contains the location ID, the location name, and all existing rooms in that location's lobby.
S_LEADERBOARD: Sent after receiving a C_LEADERBOARD, containing the names and scores of all players for that location.
S_ROOM_CREATE_OK: Sent after receiving a C_ROOM_CREATE, if the room was successfully created on the server.
S_ROOM_CREATE_NO: Sent after receiving a C_ROOM_CREATE, if the room could not be created for some reason.
S_ROOM_DESTROY: Sent after receiving a C_ROOM_DESTROY from the room leader, informing all players in the room that their room is being destroyed, removing them.
S_ROOM_PLAYER_JOIN: Sent after receiving a C_ROOM_JOIN from a player to all other players in that room, informing them of the new player in their room. Contains the player ID, name and class ID of the joining player.
S_ROOM_JOIN_OK: Sent after receiving a C_ROOM_JOIN, informing the player that she joined the room successfully.
S_ROOM_JOIN_NO: Sent after receiving a C_ROOM_JOIN, informing the player that she could not join the room.
S_ROOM_PLAYER_LEAVE: Sent after receiving a C_ROOM_LEAVE, informing all other players in that room that a player has left their room. Contains the player ID of the leaving player.
S_PLAYER_UPDATE_CLASS: Sent after receiving a C_CLASS_CHANGE, informing players in the room that a player has changed her class. Contains the player ID of the player changing her class, as well as the class ID she is changing to.
S_GAME_START: Sent after receiving a C_GAME_START, informing all players in that room that the game is ready to be started. Contains the background image name for the map as well as stats and display info for each unit (players and creatures) in the game.
S_GAME_POPULATE: Sent after receiving a C_GAME_READY, contains the same info as S_GAME_START. Was initially used to finish setting up the game engine, but is now deprecated (albeit still being sent). The client ignores this packet.
S_GAME_UPDATE_SYNC: Sent on a set interval (every 2.5 seconds) to all players in the map, contains stat and display data for every unit on the map. This is used to keep all clients properly synced.
S_GAME_UPDATE_MOVE: Sent when a C_MOVE is received, informing all other players that a certain player is moving to a new location in the map. Contains the player ID of the moving player, and the x-y coordinates of the new location in the map.
S_GAME_DESPAWN_UNIT: Sent when a unit on the map is being despawned, contains the ID of the despawning unit.
S_GAME_SPELL_CAST: Currently unused, was supposed to be used to inform all other players when a player casts a spell.
S_GAME_END: Signals the end of the game, contains the scores of all players in the map for that specific game.

THE GAME:
The game consists of a Map, and Units. A Unit is an abstract parent class, being extended into a Player and Creature class. Players are the actual connected clients, which Creatures are non player characters. While the player is controlled by a physical person via her own phone (sending the server packets), the Creatures have been given an AI, controlling their actions. The AI is updated every 100 ms (by default, can be changed in the Server.conf file), allowing it to perform various actions on timers. 

The way this works is that there is a seperate thread called the UpdateThread running in the background. Every 100 ms (by default), it calls an update function on every map. Each map contains a list of all players within it, as well as map of all Creatures within it. The map contains the timer for the game, and sends the S_GAME_END packet once the timer runs out. It also is responsible for sending the update sync packet every 2.5 seconds. During each update, the map also calls update for every creature in the map. This in turn cascades down, and within each creature calls the CreatureAI's update function. Each CreatureAI contains simple functions allowing it a variety of actions (movement, spawning new creatures, etc).

Timers work by simply being an integer containing the length of the timer (in ms). Every time update is called, the time since it was last called (diff) is subtracted from each timer integer. Once this timer integer drops below diff, the timer has run out and the event is run.