package katana.objects;

public class Player {
	private int player_id;
	private String username;
	private int class_id;
	
	public Player(int i, String u, int c){
		player_id = i;
		username = u;
		class_id = c;
	}
	
	public int getPlayerId(){
		return player_id;
	}
	
	public String getUsername(){
		return username;
	}
	
	public int getClassId(){
		return class_id;
	}
}