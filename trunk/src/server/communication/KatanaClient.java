package server.communication;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import server.game.Player;
import server.handlers.GameHandler;
import server.handlers.PacketHandler;
import server.limbo.Lobby;
import server.shared.Constants;
import server.shared.KatanaPacket;
import server.shared.Opcode;


public class KatanaClient implements Runnable
{
    private int id;
    private int pings_sent;
    private Socket client;
    private Player player;
    
    Thread thread;
    
    private static int client_count = 0;
    
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
        
        KatanaServer.instance().addWaitingClient(this);
        
        thread = new Thread(this, "Client " + client_count++ + ": " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
        thread.start();
    }
    
    public void sentPing()    { pings_sent++; }
    public void pongReceived(){ pings_sent = 0; }
    public int getPingsSent() { return pings_sent; }
    
    public void setPlayer(Player pl) 
    {
        this.player = pl;
        this.id     = pl.getId();
    }
    public Player getPlayer() { return player; }
    public int getId()        { return id; }
    
    public void remove(boolean logout)
    {
        System.out.println("Removing client " + id);
        try
        {
            if(player != null)
                player.logout();
            
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
    
    public void sendPacket(KatanaPacket packet)
    {
        try
        {
            if(client.isClosed() || client.isOutputShutdown() || thread.isInterrupted())
                return;
            
            System.out.println("[" + packet.getPacketId() + " - " + packet.getOpcode().name() + "] ==> [CLIENT " + id + "]");
            OutputStream out = client.getOutputStream();
            out.write(packet.convertToBytes());
            out.flush();
            
        }
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
            //remove(false);
        }
    }
    
    public void listen()
    {
        try
        {
            if(client.isClosed() || client.isInputShutdown())
                return;
            
            byte[] buffer = new byte[Constants.MAX_PACKET_BUF];
            InputStream in = client.getInputStream();
            in.read(buffer);
            KatanaPacket packet = KatanaPacket.createPacketFromBuffer(new String(buffer));
            if(packet != null)
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
        catch(NullPointerException ex) {}
        catch(Exception ex)
        {
            //remove(true);
            System.err.println("KatanaClient " + id + " - received " + ex.getLocalizedMessage() + ".");
            ex.printStackTrace();
        }
    }
    
    @Override
    public void run()
    {
        while(!thread.isInterrupted())
            listen();
    }
    
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