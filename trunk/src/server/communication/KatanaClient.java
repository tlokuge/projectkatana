package server.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import server.game.Player;
import server.handlers.PacketHandler;
import server.shared.Constants;
import server.shared.KatanaPacket;
import server.shared.Opcode;

/**
 * This class is a worker class for all client connections, running its own thread, sending and receiving packets to/from that specific client.
 */
public class KatanaClient implements Runnable
{
    private int id;
    private int pings_sent;
    private Socket client;
    private Player player;
    
    Thread thread;
    
    private static int client_count = 0;
    
    /**
     * Constructs a new KatanaClient containing the client socket, which runs in its own thread and services all client requests and responses (packets)
     * @param client The socket of the client
     */
    public KatanaClient(Socket client)
    {
        try
        {
            client.setSoLinger(true,0);
        }
        catch(SocketException ex)
        {
            ex.printStackTrace();
        }
        id = -1;
        pings_sent = 0;
        this.client = client;
        player = null;
        
        // Since we are not authenticated at the start, we are added into a list reserved for unauthenticated clients
        KatanaServer.instance().addWaitingClient(this);
        
        thread = new Thread(this, "Client " + client_count++ + ": " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
        thread.start();
    }
    
    // These methods simply handle ping-pongs between the client and the server.
    public void sentPing()    { pings_sent++; }
    public void pongReceived(){ pings_sent = 0; }
    public int getPingsSent() { return pings_sent; }
    
    // Simple player (and player ID) methods for convenience
    public void setPlayer(Player pl) 
    {
        this.player = pl;
        this.id     = pl.getId();
    }
    public Player getPlayer() { return player; }
    public int getId()        { return id; }
    
    /**
     * Logs the client out and removes the player from the server. Also interrupts this thread.
     * @param logout If this flag is set, also sends a logout packet to the client for confirmation.
     */
    public void remove(boolean logout)
    {
        System.out.println("Removing client " + id);
        try
        {
            if(player != null)
                player.logout();
            
            // Unless some sort of connection problem occurs, this is how all clients should be logged out.
            if(logout)
                sendPacket(new KatanaPacket(Opcode.S_LOGOUT));
            player = null;
            
            client.close();
            thread.interrupt();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        
    }
    
    /**
     * Sends a packet to the client
     * @param packet The packet to be sent
     */
    public synchronized void sendPacket(KatanaPacket packet)
    {
        try
        {
            // First make sure that everything is in order
            if(client.isClosed() || client.isOutputShutdown() || thread.isInterrupted())
                return;
            
            // Then write the packet to the socket
            System.out.println("[" + packet.getPacketId() + " - " + packet.getOpcode().name() + "] ==> [CLIENT " + id + "]");
            OutputStream out = client.getOutputStream();
            out.write(packet.convertToBytes());
            out.flush();
            
        } // Simple error handling
        catch(SocketException ex)
        {
            System.err.println("KatanaClient: Received SocketException - " + ex.getLocalizedMessage() + " - Removing client: " + id);
            remove(false);
        }
        catch(IOException ex)
        {
            System.err.println("KatanaClient: Received IOException - " + ex.getLocalizedMessage() + "!");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * Listens for incoming packets from the client and then passes it to the PacketHandler for parsing
     */
    public void listen()
    {
        try
        {
            if(client.isClosed() || client.isInputShutdown())
                return;
            
            byte[] buffer = new byte[Constants.MAX_PACKET_BUF];
            InputStream in = client.getInputStream();
            in.read(buffer); // Reads in the packet
            KatanaPacket packet = KatanaPacket.createPacketFromBuffer(new String(buffer));
            if(packet != null) // Makes sure the packet was properly parsed (and is valid)
            {
                System.out.println("[" + packet.getPacketId() + " - " + packet.getOpcode().name() + "] <== [CLIENT " + id + "]");
                PacketHandler.handlePacket(this, packet);
            }
        }
        catch(IOException ex)
        {
            System.err.println("KatanaClient " + id + " - received IOEXception " + ex.getLocalizedMessage() + "!");
            remove(false);
        }
        catch(NullPointerException ex) 
        {
            ex.printStackTrace();
        }
        catch(Exception ex)
        {
            System.err.println("KatanaClient " + id + " - received " + ex.getLocalizedMessage() + ".");
            ex.printStackTrace();
        }
    }
    
    /**
     * Runs this client thread until interrupted
     */
    @Override
    public void run()
    {
        while(!thread.isInterrupted())
            listen();
    }
    
    /**
     * Just some simple garbage collection
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable
    {
        try
        {
            super.finalize();
            System.out.println("KatanaClient: Closing socket: " + client);
            remove(true);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}