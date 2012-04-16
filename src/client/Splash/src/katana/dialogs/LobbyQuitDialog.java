package katana.dialogs;

import katana.activities.KatanaActivity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.katana.splash.R;

public class LobbyQuitDialog extends Dialog {
	KatanaActivity katanaActivity;
	
	public LobbyQuitDialog(Context context, int theme) {
		super(context, theme);
		this.setContentView(R.layout.dialog_quit);
		katanaActivity = (KatanaActivity) context;
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
			katanaActivity.logout();
		} else {
			this.dismiss();
		}
	}
}