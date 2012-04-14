package katana.objects;

public class Room {
	private int id;
	private String name;
	private int difficulty;
	private int maxPlayer;
	private boolean selected;
	
	public Room(int i, String n, int d, int p){
		id = i;
		name = n;
		difficulty = d;
		maxPlayer = p;
		selected = false;
	}
	
	public void setId(int i) { this.id = i; }
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public int getDifficulty(){
		return difficulty;
	}
	
	public int getMaxPlyr(){
		return maxPlayer;
	}
	
	public void setSelected(boolean b){
		selected = b;
	}
	
	public boolean isSelected(){
		return selected;
	}
}