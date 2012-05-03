====================================
=== Project Katana Client README ===
====================================

Android client is located in: svn/trunk/src/client/Splash/
Android activities are located in: Splash/src/com/katana/splash/
Supporting java packages located in: Splash/src/katana/

=== Android Activities ===
SplashActivity.java
	- Always the first activity to be loaded by android OS when the user opens 
	the application
	- The activity checks if the user has saved login preferences and tries to 
	log in to the server with the saved credentials,
	- If no login preferences are found, or if the login fails, LoginActivity.java 
	is loaded.
	- If the login is successful, the activity starts LobbyActivity.java

LoginActivity.java
	- Loaded if it is the first time the user has ever opened the application,
	or the authentication with saved credentials fails.
	- User enters either an existing or new username and password to be sent 
	to the server. 
	- If the username exists already, the server checks if the 
	entered password is correct. If it is correct, the activity saves the 
	credentials and starts LobbyActivity.java, else the user needs to select 
	a new username.
	- If the username does not exist, the server registers the new user.
	
LobbyActivity.java
	- This activity allows a user to create a game room on the server, or join
	an existing game room created by another user. There are two main parts to 
	this functionality. A ViewFlipper is used to separate the room list (where the 
	user selects a room to join or create), and waiting room (where the users wait
	for the game to start).
	- When a user creates a room or joins a room, the ViewFlipper swaps to the 
	waiting room view, and when they leave it swaps back to the room list view.
	- Based on the user's location, the available games that the user can select to
	join will be different. (Available rooms to the users are localized to the area
	around them).
	- Once the game starts, GameActivity.java is started.

GameActivity.java
	- This activity uses an open source game engine called AndEngine. We used the
	engine to render the sprites, backgrounds, and deal with user interaction with
	the game. The actual game mechanics are handled by the server.
	
=== Client-Server Communication ===
Client-Server communication is handled by setting up a service that listens for 
incoming server messages. The service then communicates with the application by
sending broadcast intents with the information the client needs to perform the 
functions requested. 

Each android activity is bound to the service so it is able to send messages to 
the server via the service. To recieve broadcasts from the service, each android 
activity sets up a receiver that has specific instructions for each function that 
is possible for itself.