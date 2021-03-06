package katana.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;

import katana.constants.KatanaConstants;
import katana.constants.Opcode;
import katana.objects.KatanaPacket;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class KatanaService extends Service {
	public static final String BROADCAST_ACTION = "com.katana.splash.server";
	public static final String BROADCAST_LOCATION = "com.katana.splash.loc";
	public static final String OPCODE = "opCode";
	public static final String EXTRAS_LOCNAME = "locName";
	public static final String EXTRAS_ROOMSLIST = "roomList";
	public static final String EXTRAS_PLAYERLIST = "playerList";
	public static final String EXTRAS_PLAYERCLASS = "playerClass";
	public static final String EXTRAS_PLAYERNAME = "playerName";
	public static final String EXTRAS_PLAYERID = "playerId";
	public static final String EXTRAS_ROOMID = "roomId";
	public static final String EXTRAS_SCORES = "scores";
	public static final String EXTRAS_GAMEBG = "gameBackground";
	public static final String EXTRAS_GAMESTART = "gameStart";
	public static final String EXTRAS_UNITMOVE = "unitMove";
	public static final String EXTRAS_UNITMOVE_X = "unitMove_x";
	public static final String EXTRAS_UNITMOVE_Y = "unitMove_y";
	public static final String EXTRAS_GAMESYNC = "gameSync";
	public static final String EXTRAS_DESPAWN = "despawn";
	public static final String EXTRAS_GAMESCORES = "gameScore";
	
	
	public static int player_id = 0;
	
	private final IBinder mBinder = new KatanaSBinder();
	
	private Socket socket;
	private KatanaSocketListener socketListener;
	
	public class KatanaSBinder extends Binder {
		public KatanaService getService() {
			return KatanaService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
				KatanaConstants.GPS_MIN_REFRESHTIME, 
				KatanaConstants.GPS_MIN_REFRESHDIST, 
				locList);
		
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
				KatanaConstants.GPS_MIN_REFRESHTIME, 
				KatanaConstants.GPS_MIN_REFRESHDIST, 
				locList);
		
		try {
			socket = new Socket(KatanaConstants.SERVER_IP, KatanaConstants.SERVER_PORT);
			socketListener = new KatanaSocketListener(socket);
			socket.setSoLinger(true, 0);
		} catch(Exception ex) {
			socket = null;
			socketListener = null;
			ex.printStackTrace();
			// Print error, quit?
		}
		return START_NOT_STICKY;
	}
	
	/* methods for clients */
	public void sendPacket(KatanaPacket packet) {
		if(socket == null) {
			System.err.println("Socket is null!");
			return;
		}
		try	{
	    	System.out.println("Sending packet: " + packet.getOpcode().name());
	    	System.out.println("** Packet Content ** ");
			System.out.println(packet);
			System.out.println("** End Packet **");
			
			OutputStream out = socket.getOutputStream();
			out.write(packet.convertToBytes());
			out.flush();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	class KatanaSocketListener implements Runnable 	{
		private Socket listener;
		private Thread thread;
		private boolean bInterrupt;

		public KatanaSocketListener(Socket socket) {
			listener = socket;
			bInterrupt = false;
			thread = new Thread(this, "SocketListener");
			thread.start();
		}

		public void listen() {
			if (listener == null || listener.isClosed() || bInterrupt)
				return;
			
			try {
				byte[] buffer = new byte[KatanaConstants.MAX_PACKET_BUF];
				InputStream in = listener.getInputStream();
				in.read(buffer);
				KatanaPacket packet = KatanaPacket.createPacketFromBuffer(new String(buffer));
				
				if(packet == null)
					return;
				parsePacket(packet);
			} catch (ConnectException ex) {
				System.err.println("KatanaSocketListener: Received ConnectException: "
								+ ex.getLocalizedMessage());
				closeListener();
			} catch (SocketException ex) {
				System.err.println("KatanaSocketListener: Received SocketException: "
								+ ex.getLocalizedMessage());
				closeListener();
			} catch(ClosedByInterruptException ex) {
				System.err.println("CBI: " + ex.getLocalizedMessage());
				closeListener();
				ex.printStackTrace();
			} catch (Exception ex) {
				System.err.println("Exception: " + ex.getLocalizedMessage());
				closeListener();
				ex.printStackTrace();
			}
		}

		public void closeListener() {
			System.out.println("CloseListener");
			try {
				System.out.println("Removing listener thread");
				listener.close();
				thread.interrupt();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void run() {
			while (!thread.isInterrupted() || bInterrupt)
				listen();
		}

		@Override
		public void finalize() throws Throwable {
			System.out.println("finalize");
			super.finalize();
			closeListener();
		}
	}
	
	private void parsePacket(KatanaPacket packet) {
    	Intent intent = new Intent(BROADCAST_ACTION);
    	intent.putExtra(OPCODE, packet.getOpcode().name());
    	System.out.println("Received packet: " + packet.getOpcode().name());
    	System.out.println("** Packet Content ** ");
		System.out.println(packet);
		System.out.println("** End Packet **");
    	
    	switch(packet.getOpcode()) {
    		case S_LOGOUT: 		handleLogout(); break;
    		case S_PING: 		handlePing(); break;
    		case S_REG_NO: 
    		case S_REG_OK:
    		case S_AUTH_NO: 
    		case S_AUTH_OK: 	handleRegisterLoginReply(packet, intent); break;
    		case S_LEADERBOARD: handleLeaderboardReply(packet, intent); break;
    		case S_ROOM_LIST: 	handleRoomListReply(packet, intent); break;
    		case S_ROOM_JOIN_OK: 	handleRoomJoinReply(packet, intent); break;
    		case S_ROOM_CREATE_OK: 	handleRoomCreateOkReply(packet, intent); break;
    		case S_ROOM_PLAYER_JOIN: 
    		case S_ROOM_PLAYER_LEAVE: 	handleRoomPlayerEvents(packet, intent); break;
    		case S_PLAYER_UPDATE_CLASS: handleClassChange(packet, intent); break;
    		case S_ROOM_DESTROY:
    		case S_ROOM_JOIN_NO:
    		case S_ROOM_CREATE_NO: break;
    		case S_GAME_START: 		 handleGamePopulate(packet, intent); break;
    		case S_GAME_POPULATE: 	 handleGamePopulate(packet, intent); break;
    		case S_GAME_UPDATE_MOVE: handleGameUpdateMove(packet, intent); break;
    		case S_GAME_UPDATE_SYNC: handleGameUpdateSync(packet, intent); break;
    		case S_GAME_DESPAWN_UNIT:handleGameDespawnUnit(packet, intent); break;
    		case S_GAME_END: 		 handleGameEnd(packet, intent); break;
    		default: break;
    	}
    	sendBroadcast(intent);
    }
	
	private void handleLogout() {
		socketListener.closeListener();
		locManager.removeUpdates(locList);
		locManager = null;
		this.stopSelf();
	}
	
	private void handlePing() {
		sendPacket(new KatanaPacket(Opcode.C_PONG)); 
	}
	
	private void handleRegisterLoginReply(KatanaPacket packet, Intent intent){
		String[] lines = packet.getData().split(KatanaConstants.PACKET_DATA_SEPERATOR);
		
		intent.putExtra(EXTRAS_PLAYERID, lines[0]);
	}

	private void handleLeaderboardReply(KatanaPacket packet, Intent intent) {
		intent.putExtra(EXTRAS_SCORES, packet.getData());
	}
	
	private void handleRoomListReply(KatanaPacket packet, Intent intent){
		ArrayList<String> roomsList = new ArrayList<String>();
		String[] lines = packet.getData().split(KatanaConstants.PACKET_DATA_SEPERATOR);
		String locName = lines[0];
		for(int i = 1; i < lines.length; i++) {
			roomsList.add(lines[i]);
		}
		
		intent.putExtra(EXTRAS_LOCNAME , locName);
		intent.putStringArrayListExtra(EXTRAS_ROOMSLIST, roomsList);
	}

	private void handleRoomJoinReply(KatanaPacket packet, Intent intent){
		ArrayList<String> playerList = new ArrayList<String>();
		String[] lines = packet.getData().split(KatanaConstants.PACKET_DATA_SEPERATOR);
		for(int i = 0; i < lines.length; i++) {
			playerList.add(lines[i]);
		}
		
		intent.putStringArrayListExtra(EXTRAS_PLAYERLIST, playerList);
	}

	private void handleRoomCreateOkReply(KatanaPacket packet, Intent intent) {
		intent.putExtra(EXTRAS_ROOMID, packet.getData().split(KatanaConstants.PACKET_DATA_SEPERATOR)[0]);
	}

	private void handleRoomPlayerEvents(KatanaPacket packet, Intent intent) {
		String[] lines = packet.getData().split(KatanaConstants.PACKET_DATA_SEPERATOR);
		
		intent.putExtra(EXTRAS_PLAYERNAME, lines[0]);
	}
	
	private void handleClassChange(KatanaPacket packet, Intent intent) {
		String[] lines = packet.getData().split(KatanaConstants.PACKET_DATA_SEPERATOR);
		
		intent.putExtra(EXTRAS_PLAYERID, Integer.parseInt(lines[0]));
		intent.putExtra(EXTRAS_PLAYERCLASS, Integer.parseInt(lines[1]));
	}
	
	private void handleGamePopulate(KatanaPacket packet, Intent intent) {
		String[] lines = packet.getData().split(KatanaConstants.PACKET_DATA_SEPERATOR);
		ArrayList<String> unitList = new ArrayList<String>();
		for(int i = 1; i < lines.length; ++i)
			unitList.add(lines[i]);
		
		intent.putExtra(EXTRAS_GAMEBG, lines[0]);
		intent.putStringArrayListExtra(EXTRAS_GAMESTART, unitList);
	}
	
	private void handleGameUpdateMove(KatanaPacket packet, Intent intent)
	{
		String[] lines = packet.getData().split(KatanaConstants.PACKET_DATA_SEPERATOR);
		
		intent.putExtra(EXTRAS_UNITMOVE, Integer.parseInt(lines[0]));
		intent.putExtra(EXTRAS_UNITMOVE_X, Float.parseFloat(lines[1]));
		intent.putExtra(EXTRAS_UNITMOVE_Y, Float.parseFloat(lines[2]));
	}
	private void handleGameUpdateSync(KatanaPacket packet, Intent intent)
	{
		String[] lines = packet.getData().split(KatanaConstants.PACKET_DATA_SEPERATOR);
		
		ArrayList<String> unitList = new ArrayList<String>();
		for(String s : lines)
			unitList.add(s);
		
		intent.putStringArrayListExtra(EXTRAS_GAMESYNC, unitList);
	}
	
	private void handleGameDespawnUnit(KatanaPacket packet, Intent intent)
	{
		intent.putExtra(EXTRAS_DESPAWN, Integer.parseInt(packet.getData().split(KatanaConstants.PACKET_DATA_SEPERATOR)[0]));
	}

	private void handleGameEnd(KatanaPacket packet, Intent intent)
	{
		String[] lines = packet.getData().split(KatanaConstants.PACKET_DATA_SEPERATOR);
		ArrayList<String> scoreList = new ArrayList<String>();
		for(int i = 0; i < lines.length; i++)
			scoreList.add(lines[i]);
		
		intent.putStringArrayListExtra(EXTRAS_GAMESCORES, scoreList);
	}
	
	/** For Location Manager */
	private LocationManager locManager;
	private double latitude;
	private double longitude;
	
	
	public static final String EXTRAS_LATITUDE = "lat";
	public static final String EXTRAS_LONGITUDE = "lng";
	
	private LocationListener locList = new LocationListener(){
		@Override
		public void onLocationChanged(Location location) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			broadcastLocation(latitude, longitude);
		}

		@Override
		public void onProviderDisabled(String arg0) {
		}

		@Override
		public void onProviderEnabled(String arg0) {
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

		}
	};
	
	private void broadcastLocation(double lat, double lng) {
		Intent intent = new Intent(BROADCAST_LOCATION);
		intent.putExtra(EXTRAS_LATITUDE, latitude);
		intent.putExtra(EXTRAS_LONGITUDE, longitude);
		sendBroadcast(intent);
	}
	
	@SuppressWarnings("static-access")
	public Location getLastKnownLocation() {
		Location lastKnownLocation = locManager.getLastKnownLocation(locManager.GPS_PROVIDER);
		if(lastKnownLocation == null)
			lastKnownLocation = locManager.getLastKnownLocation(locManager.NETWORK_PROVIDER);
		return lastKnownLocation;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public double getLongitude(){
		return longitude;
	}
}