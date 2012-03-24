package com.katana.splash;

public class Room {
	private int id;
	private String name;
	private String difficulty;
	private int maxPlayer;
	
	public Room(int i, String n, String d, int p){
		id = i;
		name = n;
		difficulty = d;
		maxPlayer = p;
	}
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDifficulty(){
		return difficulty;
	}
	
	public int getMaxPlyr(){
		return maxPlayer;
	}
}