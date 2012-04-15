package katana.dialogs;

import katana.constants.KatanaConstants;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
		this.setContentView(R.layout.dialog_selectclass);
		this.setTitle("Select Class");
		this.setCancelable(true);
		this.setOnCancelListener( new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				lobbyActivity.setRoomLeader(false);
				arg0.dismiss();
			}
		});
		lobbyActivity = (LobbyActivity) context;
		
		class1 = (ImageButton) findViewById(R.id.b_class1);
		class2 = (ImageButton) findViewById(R.id.b_class2);
    	close =  (ImageButton) findViewById(R.id.b_close);
    	desc = 	 (TextView)	   findViewById(R.id.l_classdesc);
    	
    	class1.setImageResource(R.drawable.ic_class1_a);
    	class2.setImageResource(R.drawable.ic_class2_a);
    	close.setImageResource(R.drawable.ic_button_close);
    	desc.setText(KatanaConstants.CLASS_DESC_DEFAULT);
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
			class1.setImageResource(R.drawable.ic_class1_b);
			class2.setImageResource(R.drawable.ic_class2_a);
			desc.setText(KatanaConstants.CLASS_DESC_1);
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
			class1.setImageResource(R.drawable.ic_class1_a);
			class2.setImageResource(R.drawable.ic_class2_b);
			desc.setText(KatanaConstants.CLASS_DESC_2);
		}
	}
	
	private void closePressed() {
		selectedClass = 0;
		this.dismiss();
	}
}