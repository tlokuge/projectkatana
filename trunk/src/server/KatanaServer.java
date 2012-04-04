package server;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import shared.Constants;

public class KatanaServer implements Runnable
{
    private ServerSocket listener;
    private int port;
    private Thread thread;
    
    private ArrayList<KatanaClient> waitingClients;
    private HashMap<Integer, Player> players;
    private HashMap<Integer, Lobby> lobbies;
    
    private static KatanaServer instance;
    
    private KatanaServer(int port)
    {
        listener  = null;
        this.port = port;
        
        waitingClients = new ArrayList<KatanaClient>();
        players        = new HashMap<Integer, Player>();
        lobbies        = new HashMap<Integer, Lobby>();
        
        thread = new Thread(this, "KatanaServer-Thread");
        thread.start();
    }
    
    public void loadCache()    { SQLCache.createCache(); }
    
    public void run() 
    { 
        System.out.println("Starting Thread: " + thread.getName());
        
        listenLoop();
    }
    
    protected void finalize()
    {
        try
        {
            if(listener != null)
                listener.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("DON'T CLONE SINGLETONS D<");
    }
    
    public static void initSingleton(int port)
    {
        if(instance == null)
            instance = new KatanaServer(port);
    }
    
    public synchronized static KatanaServer instance()
    {
        if(instance == null)
        {
            System.err.println("ERROR: LOST KATANASERVER SINGLETON!");
            System.exit(Constants.ERR_SERVER_SINGLETON_LOST);
        }
        
        return instance;
    }
    
    public void addWaitingClient(KatanaClient client)       { waitingClients.add(client); }
    public void removeWaitingClient(KatanaClient client)    { waitingClients.remove(client); }
    public ArrayList<KatanaClient> getWaitingClients() { return waitingClients; }
    
    public void addPlayer(int id, Player player){ players.put(id, player); }
    public Player getPlayer(int id)             { return players.get(id); }
    public void removePlayer(int id)            { players.remove(id); }
    public HashMap<Integer, Player> getPlayers(){ return players; }
    
    public void addLobby(int id, Lobby lobby)   { lobbies.put(id, lobby); }
    public Lobby getLobby(int id)               { return lobbies.get(id); }
    public HashMap<Integer, Lobby> getLobbies() { return lobbies; }
    public Set<Integer> getLocationIDs()        { return lobbies.keySet(); }
    
    public void listenLoop()
    {
        try
        {
            listener = new ServerSocket(port);
            while(true)
                new KatanaClient(listener.accept());
            
            // Garbage collection thread needed? Probably not
        }
        catch(Exception ex)
        {
            System.err.println("ERR: Unable to open listen port");
            ex.printStackTrace();
            System.exit(Constants.ERR_SERVER_LISTEN_FAIL);
        }
    }
    
    public int getPort() { return port; }
}