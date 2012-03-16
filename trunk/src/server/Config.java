package server;

import java.io.File;

import java.io.FileReader;
import java.util.Scanner;
import shared.Constants;

public abstract class Config
{
    public static String CONFIG_HOSTNAME = "";
    public static String CONFIG_DATABASE = "";
    public static String CONFIG_USERNAME = "";
    public static String CONFIG_PASSWORD = "";
    
    public static String CONFIG_FILE = Constants.SERVER_CONFIG_FILE;
    
    public static void loadConfig()
    {
        try
        {
            File config = new File(CONFIG_FILE);
            Scanner scan = new Scanner(new FileReader(config));
        }
        catch(Exception ex)
        {
            System.err.println("Config: Error loading '" + CONFIG_FILE + "'");
            ex.printStackTrace();
            System.exit(Constants.ERR_CONFIG_LOAD_FAIL);
        }
    }
}