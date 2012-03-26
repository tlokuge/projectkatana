package server;

import java.net.ServerSocket;
import java.util.HashMap;
import shared.Constants;

public class KatanaServer implements Runnable
{
    private ServerSocket listener;
    private int port;
    private Thread thread;
    private HashMap<Integer, KatanaClient> clients;
    
    private static KatanaServer instance;
    
    private KatanaServer(int port)
    {
        listener = null;
        this.port = port;
        clients = new HashMap<Integer, KatanaClient>();
        thread = new Thread(this, "KatanaServer-Thread");
        thread.start();
    }
    
    
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
    
    public static KatanaServer instance()
    {
        if(instance == null)
        {
            System.err.println("ERROR: LOST KATANASERVER SINGLETON!");
            System.exit(Constants.ERR_SERVER_SINGLETON_LOST);
        }
        
        return instance;
    }
    
    public void addClient(int id, KatanaClient client)
    {
        clients.put(id, client);
    }
    
    public void removeClient(int id)
    {
        clients.remove(id);
    }
    
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
    public HashMap<Integer, KatanaClient> getClients() { return clients; }
}