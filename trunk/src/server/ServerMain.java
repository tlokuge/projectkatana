package server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import shared.Constants;
import shared.KatanaPacket;
import shared.KatanaSocket;
import shared.Opcode;

public class ServerMain
{
    public static void setupServer()
    {
        Config.loadConfig();
        SQLHandler.initSingleton(
                Config.getConfig(Constants.CONFIG_SQL_HOSTNAME), 
                Config.getConfig(Constants.CONFIG_SQL_DATABASE),
                Config.getConfig(Constants.CONFIG_SQL_USERNAME),
                Config.getConfig(Constants.CONFIG_SQL_PASSWORD));
        
        KatanaServer katana = new KatanaServer();
        PingServer ping = new PingServer(
                Integer.parseInt(Config.getConfig(Constants.CONFIG_PING_INTERVAL)));
    }
    

    // Remove this method, just for testing
    public static void testStuff()
    {
        try
        {
            KatanaPacket packet = new KatanaPacket(0, Opcode.C_REGISTER);
            packet.addData("katana"); // username
            packet.addData("password"); // password
            packet.addData("1"); // location
            
            KatanaSocket.sendPacket(InetAddress.getLocalHost().getHostAddress(), Constants.SERVER_LISTEN_PORT, packet);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {
        System.out.println("  _  __      _______       _   _        ");
        System.out.println(" | |/ /   /\\|__   __|/\\   | \\ | |   /\\    ");
        System.out.println(" | ' /   /  \\  | |  /  \\  |  \\| |  /  \\   ");
        System.out.println(" |  <   / /\\ \\ | | / /\\ \\ | . ` | / /\\ \\  ");
        System.out.println(" | . \\ / ____ \\| |/ ____ \\| |\\  |/ ____ \\ ");
        System.out.println(" |_|\\_|_/    \\_\\_/_/    \\_\\_| \\_/_/    \\_\\");
        
        setupServer();
        
        testStuff();
        
    }
}