package katana.adapters;

import java.util.ArrayList;

import katana.objects.Player;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.katana.splash.R;

public class PlayerListAdapter extends BaseAdapter {
	private final Activity activity;
	private final ArrayList<Player> players;

	public PlayerListAdapter(Activity activity, ArrayList<Player> objects) {
		super();
		this.activity = activity;
		this.players = objects;
	}

	@Override
	public int getCount() {
		return players.size();
	}

	@Override
	public Player getItem(int position) {
		return players.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View gridView = convertView;
		PlayerView playerView = null;
		
		if(gridView == null) {
			// Get a new instance of the grid layout view
			LayoutInflater inflater = activity.getLayoutInflater();
			gridView = inflater.inflate(R.layout.gvitem_wroom, null);
			
			// Hold the view objects in an object so they don't need to be re-fetched
			playerView = new PlayerView();
			playerView.username = (TextView) gridView.findViewById(R.id.name); 
			playerView.icon = (ImageView) gridView.findViewById(R.id.icon);
			
			gridView.setTag(playerView);
		} else {
			playerView = (PlayerView) gridView.getTag();
		}
		
		// Transfer the stock data from the data object to the view objects
		Player currentPlayer = (Player) players.get(position);
		playerView.username.setText(currentPlayer.getUsername());
		switch(currentPlayer.getClassId()) {
			case 1: playerView.icon.setImageResource(R.drawable.ic_class1_a); break;
			case 2: playerView.icon.setImageResource(R.drawable.ic_class2_a); break;
			default: break;
		}
		return gridView;
	}
	
	protected static class PlayerView {
		protected TextView username;
		protected ImageView icon;
	}
}