package katana.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.katana.splash.GameActivity;
import com.katana.splash.LobbyActivity;
import com.katana.splash.R;

public class ScoresDialog extends Dialog {
	LobbyActivity lobbyActivity;
	SharedPreferences gamePrefs;
	ImageButton close;
	TextView scores;
	
	public ScoresDialog(final Context context, int theme) {
		super(context, theme);
		this.setContentView(R.layout.dialog_scores);
		this.setTitle("Leaderboards");
		this.setCancelable(true);
		final GameActivity game = (GameActivity) context;
		this.setOnCancelListener( new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				game.finish();
				game.katanaReceiver.startMyActivity(context, LobbyActivity.class, null);
			}
		});	

		close = (ImageButton) findViewById(R.id.b_close);
		close.setImageResource(R.drawable.ic_button_close);
		close.setOnClickListener(onClickListener);
		
		scores = (TextView) findViewById(R.id.l_scores);
		scores.setText(lobbyActivity.getLeaderboardScores());
	}

	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
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
}