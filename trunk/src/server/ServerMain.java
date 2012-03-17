package server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    
    public static void testStuff() throws UnknownHostException, InterruptedException, SQLException
    {
        // Remove this method, just for testing
        SQLHandler.instance().execute("SELECT * FROM `users`");
        KatanaSocket.sendPacket(InetAddress.getLocalHost().getHostAddress(), Constants.SERVER_LISTEN_PORT, new KatanaPacket(0, Opcode.C_LOGIN));
        KatanaSocket.sendPacket(InetAddress.getLocalHost().getHostAddress(), Constants.SERVER_LISTEN_PORT, new KatanaPacket(0, Opcode.C_LOGOUT));
        
        KatanaSocket.sendPacket(InetAddress.getLocalHost().getHostAddress(), Constants.SERVER_LISTEN_PORT, new KatanaPacket(1, Opcode.S_AUTH_NO));
        KatanaSocket.sendPacket(InetAddress.getLocalHost().getHostAddress(), Constants.SERVER_LISTEN_PORT, new KatanaPacket(1, Opcode.S_AUTH_OK));
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
        
    }
}