/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.MySQL;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import me.JoeShuff.SkyBasers.SkyBasers;

import org.bukkit.configuration.file.FileConfiguration;

public class MySQL
{
  private static Connection connection;
  private static Statement s;
  
  private static SkyBasers plugin;
  
  private static FileConfiguration sqlData;
  
  public MySQL(String ip, String name, String user, String pass,SkyBasers plugin)
  {
	  
	  sqlData = plugin.getMySQLData();
	  
	  this.plugin = plugin;
	  
    try
    {
    	String DB_NAME = "jdbc:mysql://" + ip + ":3306/" + name + "?autoReconnect=true&interactive_timeout=" + Integer.MAX_VALUE;
        Class.forName("com.mysql.jdbc.Driver");
        System.out.println("Skybasers connecting!");
        System.out.println("Connecting to database");
        connection = DriverManager.getConnection(DB_NAME, user, pass);
        System.out.println("Connected to database");
        System.out.println("Skybasers connected!");
        s = connection.createStatement();
        System.out.println("Created a statement");
        DatabaseMetaData dm = connection.getMetaData();
        System.out.println("Attempting to find table SkyBasers");
        ResultSet tables = dm.getTables(null, null, "SkyBasers", null);
      if (!tables.next()) {
    	  s.executeUpdate("CREATE TABLE `SkyBasers` (`UUID` char(40),`NSoloWins` int(20),`NSoloKills` int(20),`NTeamWins` int(20),`NTeamKills` int(20),`JSoloWins` int(20),`JSoloKills` int(20),`JTeamWins` int(20),`JTeamKills` int(20), `Deaths` int(20), `Kit` char(20), `Kits` char(100), `Block` char(20));");
      }
      ResultSet tables2 = dm.getTables(null, null, "Justice Coins", null);
      if (!tables2.next())
      {
    	  System.err.println("CANNOT FIND Justice Coins TABLE");
      }
      else
      {
    	  System.out.println("Found Justice Coins Table!");
      }
      ResultSet tables3 = dm.getTables(null, null, "SBRequests", null);
      if (!tables3.next())
      {
    	  s.executeUpdate("CREATE TABLE `SBRequests` (`mode` char(40), `name` char(40), `value` char(50), `amount` int(10), `forceUp` char(50), `type` char(50), `cancel` char(10));");
      }
      
    }
    catch (Exception e)
    {
      System.out.println("Couldn't connect to database {" + e.getMessage() + "}");
    }
  }
  
  public static void openConnection()
  {
    String ip = sqlData.getString("ip");
    String name = sqlData.getString("name");
    String user = sqlData.getString("username");
    String pass = sqlData.getString("password");
    new MySQL(ip, name, user, pass,plugin);
  }
  
  public static Connection getConnection()
  {
    return connection;
  }
}

