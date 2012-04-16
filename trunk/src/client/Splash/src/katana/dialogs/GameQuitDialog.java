package katana.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.katana.splash.GameActivity;
import com.katana.splash.R;

public class GameQuitDialog extends Dialog {
	GameActivity gameActivity;
	
	public GameQuitDialog(Context context, int theme) {
		super(context, theme);
		this.setContentView(R.layout.dialog_quit);
		gameActivity = (GameActivity) context;
		Button yes = (Button) findViewById(R.id.dialog_quit_yes);
		Button no = (Button) findViewById(R.id.dialog_quit_no);
		
		yes.setOnClickListener(onClickListener);
		no.setOnClickListener(onClickListener);
	}

	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			boolean quit = false;
			switch (v.getId()) {
				case R.id.dialog_quit_no:	quit = false; break;
				case R.id.dialog_quit_yes:	quit = true; break;
				default: break;
			}
			buttonPressed(quit);
		}
	};
	
	private void buttonPressed(boolean b) {
		if(b){
			// Quit game
			this.dismiss();
			gameActivity.logout();
		} else {
			this.dismiss();
		}
	}
}