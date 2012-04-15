package katana.objects;

import java.util.ArrayList;

import katana.adapters.RoomListAdapter;
import katana.constants.Opcode;
import android.widget.GridView;

import com.katana.splash.LobbyActivity;
import com.katana.splash.R;

public class Lobby extends ArrayList<Room> {
	private static final long serialVersionUID = 1L;
	
	private LobbyActivity context;
	private int selectedRoom;
	
	public Lobby(LobbyActivity a){
		context = a;
		selectedRoom = -1;
	}
	
	public String getSelectedName(){
		return this.get(selectedRoom).getName();
	}
	
	public String getRoomName(int position) {
		return this.get(position).getName();
	}
	
	public int getRoomDifficulty(int position) {
		return this.get(position).getDifficulty();
	}
	
	public int getRoomMaxPlyr(int position) {
		return this.get(position).getMaxPlyr();
	}
	
	public void setSelectedRoom(int i) {
		selectedRoom = i;
	}
	public int getSelectedRoom() {
		return selectedRoom;
	}
	
	public KatanaPacket getRefreshPacket(double lat, double lng){
		KatanaPacket packet = new KatanaPacket(Opcode.C_ROOM_LIST);
		packet.addData(Double.toString(lat));
		packet.addData(Double.toString(lng));
		return packet;
	}
	
	public void populate(ArrayList<String> al) {
		this.removeAll(this);
		for (int i = 0; i < al.size(); i++) {
			String[] lines = al.get(i).split(";");
			if(lines.length < 4)
				continue;
			this.add(new Room(Integer.parseInt(lines[0]),
							lines[1],Integer.parseInt(lines[2]),
							Integer.parseInt(lines[3]))
			);
		}
	}
	
	public void showRooms() {
		GridView gv = (GridView) context.findViewById(R.id.gv_roomslist);
		gv.setAdapter(new RoomListAdapter(context,this));
	}
	
	public KatanaPacket getJoinRequestPacket(int class_id) {
		KatanaPacket packet = new KatanaPacket(Opcode.C_ROOM_JOIN);
		packet.addData(Integer.toString(this.get(selectedRoom).getId()));
		packet.addData(Integer.toString(class_id));
		return packet;
	}
	
	public KatanaPacket getCreateRequestPacket(String name, int diff, int maxp, int class_id) {
		KatanaPacket packet = new KatanaPacket(Opcode.C_ROOM_CREATE);
		packet.addData(name);
		packet.addData(diff + "");
		packet.addData(maxp + "");
		packet.addData(class_id + "");
		return packet;
	}
}