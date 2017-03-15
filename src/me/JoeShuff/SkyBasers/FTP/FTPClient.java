/*
 * FTPConnector
 * 
 * This class is used to connect to and interact with an
 * FTP server. The methods allow you to download and upload
 * files to and from the server.
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.FTP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FTPClient {
	
	private String host = null;
	private String user = null;
	private String pass = null;
	
	/*
	 * This method sets the default  values for the server informations
	 * 
	 * @param host - The host IP or url as a String
	 * @param pass - The password for the FTP server
	 * @param username - The username for the FTP server
	 */
	public FTPClient(String host, String username, String pass)
	{
		this.host = host;
		this.user = username;
		this.pass = pass;
	}
	
	/*
	 * This method sets the default  values for the server informations
	 * 
	 * @param host - The host IP or url as a String
	 * @param pass - The password for the FTP server
	 * @param username - The username for the FTP server
	 */
	public void changeServerInfo(String host, String username, String pass)
	{
		this.host = host;
		this.user = username;
		this.pass = pass;
	}
	
	/*
	 * This method gets the URL connection to the specified file with default mode
	 * 
	 * @param filePath - The path of the file on the server to be found
	 */
	public URLConnection getConnection(String filePath)
	{
		return getConnection(filePath, "i");
	}
	
	/*
	 * This method gets the URL connection to the specified file with default mode
	 * 
	 * @param filePath - The path of the file on the server to be found
	 * @param mode - The mode of which the FTP request needs to be sent in.
	 */
	public URLConnection getConnection(String filePath, String mode)
	{
		try {
        	URL url = new URL("ftp://" + URLEncoder.encode(user, "UTF-8") + ":" + URLEncoder.encode(pass, "UTF-8") + "@" + host + "/" + URLEncoder.encode(filePath, "UTF-8") + ";type=" + mode);
            
            URLConnection conn = url.openConnection();
            
            return conn;
            
        } catch (IOException ex) {
            ex.printStackTrace();
            
            return null;
        }
	}
	
	/*
	 * This method checks the connection to the specified file.
	 * 
	 * It tests that it can both be read from but not written to, as
	 * it will create the file and there is no way to delete it.
	 * 
	 * @param filePath - The file that you want to check the connection to
	 * @returns boolean - true if it can connect to the file, false if not.
	 */
	public boolean testConnectionToFile(String filePath)
	{        
        try
        {  
            URLConnection conn = getConnection(filePath);
            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            inputStream.close();
            
            return true;
            
        } catch (FileNotFoundException e)
        {
        	System.out.println("Could not find file " + filePath + " on FTP server!");

        	return false;
        
        } catch (NullPointerException e)
        {
        	System.out.println("Unable to connect to the FTP server");
        	return false;
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
            
            return false;
        }
	}
	
	/*
	 * This method gets all the strings that are in a file and
	 * returns them in a list. For the developers manipulation.
	 * 
	 * @param filePath - The path to the file they wish to download
	 * 
	 * @returns List<String> - returns the list of all strings in the file.
	 * 						   can return null if the file doesn't exist, or
	 * 						   there is another error.
	 */
	public List<String> getFileStrings(String filePath)
	{
        List<String> results = new ArrayList<String>();
        
        try
        {  
            URLConnection conn = getConnection(filePath);
            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            String line = null;

            while ((line = reader.readLine()) != null) {
                results.add(line);
            }

            inputStream.close();
            
        } catch (FileNotFoundException e)
        {
        	System.out.println("Could not find file " + filePath + " on FTP server!");
        	
        	return null;
        }
        catch (NullPointerException e)
        {
        	System.out.println("Unable to connect to the FTP server");
        	
        	return null;
        	
        } catch (IOException ex) 
        {
            ex.printStackTrace();
            
            return null;
        }
        
        return results;
	}
	
	/*
	 * This method writes a list of strings to the specified file.
	 * It also CLEARS the existing file so make sure to first FETCH
	 * everything from the file if you wish to append it.
	 * 
	 * @param filePath - The file location where we are uploading to
	 * @param strings - The list of strings that are to be written to the file
	 * 					each string will be seperated by a new line.
	 */
	public void writeAllStrings(String filePath, List<String> strings)
	{
		try
		{
			URLConnection conn = getConnection(filePath);
			
			OutputStream outputStream = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
			
			for (String s : strings)
			{
				writer.write(s + "\n");
			}
			
			writer.close();
			outputStream.close();
			
		} catch (NullPointerException e)
        {
        	System.out.println("Unable to connect to the FTP server");
        } 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	 * This method provides the ability to append more strings to the end of a file
	 * 
	 * @param filePath - The path of the file on the FTP server to be appended
	 * @param strings - The list of strings that are to be appended on the file.
	 * 					Each string will be seperated by a new line.
	 * 
	 */
	public void appendToFile(String filePath, List<String> strings)
	{
		List<String> current = getFileStrings(filePath);
		
		if (current == null)
		{
			current = strings;
		}
		else
		{
			current.addAll(strings);
		}
		
		writeAllStrings(filePath, current);
	}
	
	/*
	 * This method downloads text from a file on the FTP server and saves it locally.
	 * 
	 * @param fromFile - The path of the file to be downloaded
	 * @param toFile - The path of the file to be outputted to
	 */
	public void downloadTextToLocalFile(String fromFile, String toFile)
	{
		List<String> fileData = getFileStrings(fromFile);
		
		try 
		{
			PrintWriter out = new PrintWriter(toFile);
			
			for (String line : fileData)
			{
				out.println(line);
			}
			
			out.close();
			
			System.out.println("Successfully downloaded file");
			
		} catch (NullPointerException e)
        {
        	System.out.println("Unable to connect to the FTP server");
        } 
		catch (FileNotFoundException e) 
		{
			System.out.println("Unable to find the file " + toFile);
		}
	}
	
	/*
	 * This method downloads the file in bytes from the FTP server.
	 * 
	 * @param fromFile - The path of the file to download on the FTP server
	 * @param toFile - The path of the file to store the file from the FTP
	 * 
	 */
	public void downloadBytesToLocalFile(String fromFile, String toFile)
	{
		try
		{
			URLConnection conn = getConnection(fromFile);
			
			InputStream inputSteam = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(inputSteam);
			
			OutputStream outputStream = new FileOutputStream(toFile);
			BufferedOutputStream bos = new BufferedOutputStream(outputStream);
			
			byte[] buffer = new byte[1024];
			
			int count;
			
			while ((count = bis.read(buffer)) > 0)
			{
				bos.write(buffer, 0, count);
			}
			
			bos.close();
			inputSteam.close();
			
			System.out.println("Successfully downloaded File");
			
			
		} catch (FileNotFoundException e)
		{
			System.out.println("Unable to find the file " + toFile);
		}
		catch (NullPointerException e)
        {
        	System.out.println("Unable to connect to the FTP server");
        } 
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
