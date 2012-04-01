package katana.adapters;

import java.util.ArrayList;

import com.katana.splash.R;

import katana.objects.Room;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RoomListAdapter extends BaseAdapter {
	private final Activity activity;
	private final ArrayList<Room> rooms;

	public RoomListAdapter(Activity activity, ArrayList<Room> objects) {
		super();
		this.activity = activity;
		this.rooms = objects;
	}

	@Override
	public int getCount() {
		return rooms.size();
	}

	@Override
	public Room getItem(int position) {
		return rooms.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View gridView = convertView;
		RoomView roomView = null;
		
		if(gridView == null) {
			// Get a new instance of the grid layout view
			LayoutInflater inflater = activity.getLayoutInflater();
			gridView = inflater.inflate(R.layout.lobby_item,null);
			
			// Hold the view objects in an object so they don't need to be re-fetched
			roomView = new RoomView();
			roomView.name = (TextView) gridView.findViewById(R.id.name);
			//roomView.diff = (TextView) gridView.findViewById(R.id.diff);
			//roomView.maxp = (TextView) gridView.findViewById(R.id.maxp);
			roomView.icon = (ImageView) gridView.findViewById(R.id.icon);
			
			gridView.setTag(roomView);
		} else {
			roomView = (RoomView) gridView.getTag();
		}
		
		// Transfer the stock data from the data object to the view objects
		Room currentRoom = (Room) rooms.get(position);
		roomView.name.setText(currentRoom.getName());
		//roomView.diff.setText(currentRoom.getDifficulty());
		//roomView.maxp.setText(Integer.toString(currentRoom.getMaxPlyr()));
		roomView.icon.setImageResource(R.drawable.qr);
		
		return gridView;
	}
	
	protected static class RoomView {
		protected TextView name;
		//protected TextView diff;
		//protected TextView maxp;
		protected ImageView icon;
	}
}