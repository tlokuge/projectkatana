package katana.dialogs;

import katana.shared.KatanaConstants;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.katana.splash.LobbyActivity;
import com.katana.splash.R;

public class SelectClassDialog extends Dialog {
	private ImageButton class1;
	private ImageButton class2;
	private ImageButton close;
	private TextView 	desc;
	private int 		selectedClass;
	LobbyActivity lobbyActivity;
	
	public SelectClassDialog(Context context, int theme) {
		super(context, theme);
		this.setContentView(R.layout.select_class);
		this.setTitle("Select Class");
		this.setCancelable(true);
		
		lobbyActivity = (LobbyActivity) context;
		
		class1 = (ImageButton) findViewById(R.id.b_class1);
		class2 = (ImageButton) findViewById(R.id.b_class2);
    	close =  (ImageButton) findViewById(R.id.b_close);
    	desc = 	 (TextView)	   findViewById(R.id.l_classdesc);
    	
    	class1.setImageResource(R.drawable.class1);
    	class2.setImageResource(R.drawable.class2);
    	close.setImageResource(R.drawable.close);
    	desc.setText(KatanaConstants.DEF_DESC);
    	selectedClass = 0;
    	
    	class1.setOnClickListener(onClickListener);
    	class2.setOnClickListener(onClickListener);
    	close.setOnClickListener(onClickListener);
	}
	
	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.b_class1: class1Pressed(); break;
				case R.id.b_class2: class2Pressed(); break;
				case R.id.b_close: 	closePressed(); break;
				default: break;
			}
		}	
	};
	
	private void class1Pressed() {
		if(selectedClass == 1) {
			if(lobbyActivity.isInRoom()) {
				lobbyActivity.waitingRoomRequestClassChange(1);
			} else {
				lobbyActivity.setPlayerClass(1);
				if (lobbyActivity.isRoomLeader())
					lobbyActivity.lobbySendCreateRequest();
				else
					lobbyActivity.lobbySendJoinRequest();
			}
			selectedClass = 0;
			this.dismiss();
		} else {
			selectedClass = 1;
			class1.setImageResource(R.drawable.class1_selected);
			class2.setImageResource(R.drawable.class2);
			desc.setText(KatanaConstants.DESC_CLASS1);
		}
	}
	
	private void class2Pressed() {
		if(selectedClass == 2) {
			if(lobbyActivity.isInRoom()) {
				lobbyActivity.waitingRoomRequestClassChange(2);
			} else {
				lobbyActivity.setPlayerClass(2);
				if (lobbyActivity.isRoomLeader())
					lobbyActivity.lobbySendCreateRequest();
				else
					lobbyActivity.lobbySendJoinRequest();
			}
			selectedClass = 0;
			this.dismiss();
		} else {
			selectedClass = 2;
			class1.setImageResource(R.drawable.class1);
			class2.setImageResource(R.drawable.class2_selected);
			desc.setText(KatanaConstants.DESC_CLASS2);
		}
	}
	
	private void closePressed() {
		selectedClass = 0;
		this.dismiss();
	}
}