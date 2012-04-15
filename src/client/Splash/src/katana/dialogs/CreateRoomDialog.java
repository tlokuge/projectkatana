package katana.dialogs;

import katana.constants.KatanaConstants;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.katana.splash.LobbyActivity;
import com.katana.splash.R;

public class CreateRoomDialog extends Dialog {
	LobbyActivity lobbyActivity;
	SharedPreferences gamePrefs;
	ImageButton close;
	private EditText name;
	private RadioGroup diff;
	private RadioGroup maxp;
	Button confirm;

	public CreateRoomDialog(Context context, int theme) {
		super(context, theme);
		this.setContentView(R.layout.dialog_createroom);
		this.setTitle("Create Room");
		this.setCancelable(true);
		this.setOnCancelListener( new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				lobbyActivity.setRoomLeader(false);
				arg0.dismiss();
			}
		});
		
		lobbyActivity = (LobbyActivity) context;
		gamePrefs = lobbyActivity.getGamePrefs();

		close = (ImageButton) findViewById(R.id.b_close);
		name = (EditText) findViewById(R.id.l_roomname);
		diff = (RadioGroup) findViewById(R.id.rg_roomdiff);
		maxp = (RadioGroup) findViewById(R.id.rg_roommaxplr);
		confirm = (Button) findViewById(R.id.b_makeroom);

		String gp_name = gamePrefs.getString(
				KatanaConstants.PREFS_GAME_NAME,
				KatanaConstants.ROOM_NAME_DEFAULT);
		int gp_diff = gamePrefs.getInt(
				KatanaConstants.PREFS_GAME_DIFF,
				KatanaConstants.ROOM_DIFF_DEFAULT);
		int gp_maxp = gamePrefs.getInt(
				KatanaConstants.PREFS_GAME_MAXP,
				KatanaConstants.ROOM_MAXP_DEFAULT);

		switch (gp_diff) {
			case 1: diff.check(R.id.diff1); break;
			case 2: diff.check(R.id.diff2); break;
			case 3: diff.check(R.id.diff3); break;
			default: diff.check(R.id.diff2); break;
				
		}

		switch (gp_maxp) {
			case 1: maxp.check(R.id.plyr1);	break;
			case 2: maxp.check(R.id.plyr2);	break;
			case 3: maxp.check(R.id.plyr3);	break;
			case 4: 
			default: maxp.check(R.id.plyr4);	break;
		}

		name.setText(gp_name);

		close.setOnClickListener(onClickListener);
		close.setImageResource(R.drawable.ic_button_close);
		confirm.setOnClickListener(onClickListener);
	}

	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.b_makeroom:
				makeRoomPressed();
				break;
			case R.id.b_close:
				closePressed();
				break;
			default:
				break;
			}
		}
	};

	private void closePressed() {
		this.cancel();
	}

	private void makeRoomPressed() {
		if (name.length() < KatanaConstants.LOBBY_MIN_ROOMNAME) {
			// Room name is left blank
			gamePrefs.edit().putString(
					KatanaConstants.PREFS_GAME_NAME,
					KatanaConstants.ROOM_NAME_DEFAULT
					).commit();
		} else {
			gamePrefs.edit().putString(
					KatanaConstants.PREFS_GAME_NAME,
					name.getText().toString()
					).commit();
		}

		// Set game difficulty preferences
		switch (diff.getCheckedRadioButtonId()) {
		case R.id.diff1:
			gamePrefs.edit().putInt(KatanaConstants.PREFS_GAME_DIFF, 1).commit();
			break;
		case R.id.diff2:
			gamePrefs.edit().putInt(KatanaConstants.PREFS_GAME_DIFF, 2).commit();
			break;
		case R.id.diff3:
			gamePrefs.edit().putInt(KatanaConstants.PREFS_GAME_DIFF, 3).commit();
			break;
		default:
			gamePrefs.edit().putInt(KatanaConstants.PREFS_GAME_DIFF, 2).commit();
			break;
		}

		// Set game max player preferences
		switch (maxp.getCheckedRadioButtonId()) {
		case R.id.plyr1:
			gamePrefs.edit().putInt(KatanaConstants.PREFS_GAME_MAXP, 1).commit();
			break;
		case R.id.plyr2:
			gamePrefs.edit().putInt(KatanaConstants.PREFS_GAME_MAXP, 2).commit();
			break;
		case R.id.plyr3:
			gamePrefs.edit().putInt(KatanaConstants.PREFS_GAME_MAXP, 3).commit();
			break;
		case R.id.plyr4:
		default:
			gamePrefs.edit().putInt(KatanaConstants.PREFS_GAME_MAXP, 4).commit();
			break;
		}

		this.dismiss();
		lobbyActivity.setGamePrefs(gamePrefs);
		lobbyActivity.showSelectClassDialog();
	}
}