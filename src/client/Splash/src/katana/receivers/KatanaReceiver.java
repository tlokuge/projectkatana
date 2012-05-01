package katana.receivers;

import java.util.ArrayList;

import katana.constants.KatanaConstants;
import katana.constants.Opcode;
import katana.services.KatanaService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.katana.splash.GameActivity;
import com.katana.splash.LobbyActivity;
import com.katana.splash.LoginActivity;

public class KatanaReceiver extends BroadcastReceiver {
	public static final int MODE_SPLASH = 0;
	public static final int MODE_LOGIN = 1;
	public static final int MODE_LOBBY = 2;
	public static final int MODE_GAME = 3;
	
	private int mode;
	
	public KatanaReceiver(int m){
		mode = m;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(mode == 0) {
			// SplashActivity
    		if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_AUTH_OK.name())){
    			startMyActivity(context, LobbyActivity.class, null);
    			context.getSharedPreferences(KatanaConstants.PREFS_LOGIN, Context.MODE_PRIVATE).edit().putInt(
    					KatanaConstants.PREFS_LOGIN_PLAYERID, 
    					Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID))
    					);
    			
    			KatanaService.player_id = Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID));
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_AUTH_NO.name())){
    			startMyActivity(context, LoginActivity.class, null);
    		} 
		} else if(mode == 1) {
			// LoginActivity
			LoginActivity loginActivity = (LoginActivity) context;
			if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_REG_OK.name())){
    			// Registered new user on server    			
    			loginActivity.setUserLoginPrefs(true);
    			context.getSharedPreferences(KatanaConstants.PREFS_LOGIN, Context.MODE_PRIVATE).edit().putInt(
    					KatanaConstants.PREFS_LOGIN_PLAYERID, 
    					Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID))
    					);
    			
    			KatanaService.player_id = Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID));
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_AUTH_OK.name())){
    			loginActivity.setUserLoginPrefs(false);
    			context.getSharedPreferences(KatanaConstants.PREFS_LOGIN, Context.MODE_PRIVATE).edit().putInt(
    					KatanaConstants.PREFS_LOGIN_PLAYERID, 
    					Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID))
    					);
    			KatanaService.player_id = Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID));
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_REG_NO.name())){
    			Toast.makeText(loginActivity, "Username already exists or wrong password!", Toast.LENGTH_SHORT).show();
    		}
		} else if(mode == 2) {
			// LobbyActivity
			LobbyActivity activity = (LobbyActivity) context;
			if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_LIST.name())){
				
				activity.setLayoutTheme(intent.getIntExtra(KatanaService.EXTRAS_LOCID, 0));
				activity.setInValidLocation(true);
    			String locName = intent.getStringExtra(KatanaService.EXTRAS_LOCNAME);
    			ArrayList<String> al = intent.getStringArrayListExtra(KatanaService.EXTRAS_ROOMSLIST);
    			activity.lobbyShowRooms(locName, al);
    		} else if (intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_JOIN_OK.name())) {
    			ArrayList<String> al = intent.getStringArrayListExtra(KatanaService.EXTRAS_PLAYERLIST);
    			activity.waitingRoomShowPlayers(al);
    			activity.lobbyTransitionToWaitingRoom();
    		} else if (intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_JOIN_NO.name())) {
    			Toast.makeText(activity, "You cant join this room.", Toast.LENGTH_SHORT).show();
    		} else if (intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_PLAYER_JOIN.name())) {
    			String s = intent.getStringExtra(KatanaService.EXTRAS_PLAYERNAME);
    	    	String lines[] = s.split(";");
    			activity.waitingRoomAddPlayer(lines);
    		} else if (intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_PLAYER_LEAVE.name())) {
    			activity.waitingRoomRemovePlayer(Integer.parseInt((intent.getStringExtra(KatanaService.EXTRAS_PLAYERNAME))));
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_CREATE_OK.name()))	{
    			activity.setRoomLeader(true);
    			activity.lobbyTransitionToWaitingRoom();
    			activity.lobbyRefresh(null);
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_CREATE_NO.name()))	{
    			Toast.makeText(activity, "Room create failed!", Toast.LENGTH_SHORT).show();
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_PLAYER_UPDATE_CLASS.name())) {
    			if(activity.isInRoom()){
    				int playerid = intent.getIntExtra(KatanaService.EXTRAS_PLAYERID, -1);
    				int playerclass = intent.getIntExtra(KatanaService.EXTRAS_PLAYERCLASS, -1);
    				activity.waitingRoomClassUpdate(playerid, playerclass);
    			}
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_DESTROY.name())) {
    			if(activity.isInRoom()){
    				activity.lobbyRefresh(null);
    				activity.setInRoom(false);
    				activity.setRoomLeader(false);
    				activity.transitionToLobby();
    			}
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_LEADERBOARD.name())) {
    			String[] pl_lines = intent.getStringExtra(KatanaService.EXTRAS_SCORES).split(KatanaConstants.PACKET_DATA_SEPERATOR);
    			String score = "";
    			String names = "";
    			
    			for(int i = 0; i < pl_lines.length; i++) {
    				String[] lines = pl_lines[i].split(";");
    				if(lines.length < 2)
    					continue;
    				score = score + lines[1] + "\n";
    				names = names + lines[0] + "\n";
    			}
    			
    			activity.setLeaderboardNames(names);
    			activity.setLeaderboardScores(score);
    			activity.showLeaderboardDialog();
    		}
    		else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_GAME_START.name())) {
    			Bundle gBundle = new Bundle();
    			gBundle.putString(KatanaService.EXTRAS_GAMEBG, intent.getStringExtra(KatanaService.EXTRAS_GAMEBG));
    			gBundle.putStringArrayList(KatanaService.EXTRAS_GAMESTART, intent.getStringArrayListExtra(KatanaService.EXTRAS_GAMESTART));
    			System.out.println("gBundle: " + gBundle);
    			activity.setInValidLocation(false);
    			startMyActivity(activity, GameActivity.class, gBundle);
    			activity.finish();
    		}
		} else if(mode == 3) {
			// GameActivity
			GameActivity game = (GameActivity) context;
			if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_GAME_UPDATE_MOVE.name())) {
				int id = intent.getIntExtra(KatanaService.EXTRAS_UNITMOVE, 0);
				float x = intent.getFloatExtra(KatanaService.EXTRAS_UNITMOVE_X, 0.0f);
				float y = intent.getFloatExtra(KatanaService.EXTRAS_UNITMOVE_Y, 0.0f);
				
				game.moveUnit(id, x, y);
			}else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_GAME_UPDATE_SYNC.name())) {
				game.updateUnits(intent.getStringArrayListExtra(KatanaService.EXTRAS_GAMESYNC));
			}
			else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_GAME_DESPAWN_UNIT.name())) {
				game.despawnUnit(intent.getIntExtra(KatanaService.EXTRAS_DESPAWN, -1));
			}
			else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_GAME_END.name())) {
				ArrayList<String> al = intent.getStringArrayListExtra(KatanaService.EXTRAS_GAMESCORES);
				game.showScoresDialog(al);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void startMyActivity(Context context, Class myClass, Bundle myBundle) {
		Intent intent = new Intent();
		intent.setClass(context, myClass);
		if(myBundle != null)
			intent.putExtras(myBundle);
		context.startActivity(intent);
	}
}