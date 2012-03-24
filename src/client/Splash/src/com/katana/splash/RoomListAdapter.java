package com.katana.splash;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RoomListAdapter extends ArrayAdapter<Room> {
    private final Activity activity;
    private final ArrayList<Room> rooms;

    public RoomListAdapter(Activity activity, ArrayList<Room> objects) {
        super(activity, R.layout.room_list_item, objects);
        this.activity = activity;
        this.rooms = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        RoomView roomView = null;

        if(rowView == null)
        {
            // Get a new instance of the row layout view
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.room_list_item, null);

            // Hold the view objects in an object,
            // so they don't need to be re-fetched
            roomView = new RoomView();
            roomView.name = (TextView) rowView.findViewById(R.id.name);
            roomView.diff = (TextView) rowView.findViewById(R.id.diff);
            roomView.maxp = (TextView) rowView.findViewById(R.id.maxp);

            // Cache the view objects in the tag,
            // so they can be re-accessed later
            rowView.setTag(roomView);
        } else {
            roomView = (RoomView) rowView.getTag();
        }

        // Transfer the stock data from the data object
        // to the view objects
        Room currentRoom = (Room) rooms.get(position);
        roomView.name.setText(currentRoom.getName());
        roomView.diff.setText(currentRoom.getDifficulty());
        roomView.maxp.setText(Integer.toString(currentRoom.getMaxPlyr()));

        return rowView;
    }

    protected static class RoomView {
        protected TextView name;
        protected TextView diff;
        protected TextView maxp;
    }
}