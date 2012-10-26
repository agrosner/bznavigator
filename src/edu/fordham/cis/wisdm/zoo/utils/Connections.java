package edu.fordham.cis.wisdm.zoo.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import edu.fordham.cis.wisdm.utils.SocketParser;
import edu.fordham.cis.wisdm.zoo.file.GPSWriter;

import android.app.Service;
import android.content.Context;
import android.util.Log;

/**
 * This class wraps around server class socket parser
 * @author agrosner
 *
 */
public class Connections {
	/**
	 * Log Tag
	 */
	private static String TAG = "Connections";
	
	/**
	 * Writes data to the server
	 */
	private static DataOutputStream mOutputStream = null;
	
	/**
	 * Reads data from the server
	 */
	private static DataInputStream mInputStream = null;
	
	/**
	 * port for connection
	 */
	private static int PORT = 2324;
	
	/**
	 * hostname
	 */
	private static String HOST = "netlab.cis.fordham.edu";
	
	/**
	 * The connection socket handle
	 */
	private static Socket mSocket = null;
	
	/**
	 * Timeout if it takes too long to connect to the server (30 seconds)
	 */
	private static int DATA_TIMEOUT = 30000;
	
	/**
	 * User authentication email
	 */
	private String mEmail = "none";
	
	/**
	 * User authentication password
	 */
	private String mPassword = "";
	
	/**
	 * the message string that stores what the server says in case of errors
	 */
	public static String mServerMessage = " ";

	/**
	 * The user's device ID
	 */
	private String mDevId = " ";
	
	/**
	 * Whether an active connection is established or not
	 */
	private static boolean isConnected = false;
	
	/**
	 * the unique ID of the current visit that will be pulled from the server initially
	 */
	private static int mVisitID = -1;
	
	private static boolean connect(){
		Log.v(TAG, "Connecting to server...");
		
		mServerMessage = " ";
		mSocket = new Socket();
		InetAddress addr;
		try {
			addr = InetAddress.getByName(HOST);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		SocketAddress sockaddr = new InetSocketAddress(addr, PORT);
		Log.v(TAG, "Initialized socket. Trying to connect...on" + HOST + ": " + PORT);
		
		try {
			mSocket.connect(sockaddr, DATA_TIMEOUT);
			mOutputStream = new DataOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));
			mInputStream = new DataInputStream(mSocket.getInputStream());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mServerMessage +="\n";
			mServerMessage+=e.getMessage();
			return false;
		}
		
		Log.v(TAG, "Established Connection");
		isConnected = true;
		return true;
	}
	
	/**
	 * Will try to authorize a user with the server (given a connection is established)
	 * @param con
	 * @return
	 */
	private static boolean authorize(Connections con){
		Log.v(TAG, "Authorizing user");
		
		try {
			SocketParser.writeAuthReq(con.getmEmail().split("@")[0], con.getmPassword(), 
					con.getmEmail() , con.getmDevId(), mOutputStream, mInputStream);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mServerMessage+="\n";
			mServerMessage+=e.getMessage();
			return false;
		} catch(NullPointerException n){
			mServerMessage+="\n";
			mServerMessage+=n.getMessage();
			return false;
		}
		
		try {
			byte smsg = mInputStream.readByte();
			if(smsg == SocketParser.AUTH_CODE){
				Log.v(TAG, "User authorized");
				return true;
			} else if(smsg == SocketParser.AUTH_DENY){
				Log.v(TAG, "User denied");
				mServerMessage+="\nUser denied";
				return false;
			} else{
				Log.v(TAG, "Error?");
				mServerMessage+="\nUnknown error";
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mServerMessage+="\n";
			mServerMessage+=e.getMessage();
			return false;
		}
		
	}
	
	/**
	 * Stores user information
	 * @param email
	 * @param password
	 * @param devId
	 */
	public Connections(String email, String password, String devId){
		mEmail = email;
		mPassword = password;
		mDevId = devId;
	}
	
	/**
	 * Attempts to connect and authorize user with the server
	 * @param con
	 * @return
	 */
	public static boolean prepare(Connections con){
		return connect() && authorize(con);
	}
	
	/**
	 * Starts a new visit for the user
	 * @param con
	 * @return
	 */
	public static boolean visit(Connections con){
		if(!isConnected){
			Log.e(TAG, "User not connected to server!");
			return false;
		}
		
		try {
			SocketParser.writeNewVisitReq(mOutputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		try {
			mVisitID = SocketParser.readVisitReq(mInputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	/**
	 * Writes a user request to the server if connection was established
	 * @param con
	 * @return
	 */
	public static boolean createUser(Connections con){
		if(connect()){
			try {
				SocketParser.writeUsrReq(mOutputStream, con.mEmail.split("@")[0], con.mPassword, con.mEmail, con.mDevId);
				byte smsg = mInputStream.readByte();
				if(smsg == SocketParser.USR_TAKEN){
					mServerMessage+="\nUser taken";
					return false;
				} else if(smsg == SocketParser.AUTH_CODE){
					mServerMessage+="\nUser created!";
					return true;
				}
			} catch (IOException e) {
			// 	TODO Auto-generated catch block
				e.printStackTrace();
				mServerMessage+=e.getMessage();
			}
		}
		return false;
	}
	
	/**
	 * Sends a line to the server
	 * @param time
	 * @param values
	 * @param recordType
	 * @return
	 */
	private static boolean sendLine(long time, float[] values, byte recordType){
		if(!isConnected){
			Log.v(TAG, "Not connected!");
			return false;
		}
		
		try {
			SocketParser.writeNormalRecord(mOutputStream, values.length, values, time, recordType);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Sends all lines within a file to the server
	 * @param con
	 * @param fName
	 * @param mService
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	public static void sendData(Connections con, String fName, Service mService){
		if(!Connections.prepare(con)){
			Log.v(TAG, "Sending failed");
			return;
		}
		
		try {
			SocketParser.writeVisitReq(mOutputStream, mVisitID);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			Log.e(TAG, "Could not write a visit request: " + e2.getMessage());
			Connections.disconnect();
			return;
		}
		
		BufferedReader read = null;
		try {
			read = new BufferedReader(new InputStreamReader(mService.openFileInput(fName)));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Connections.disconnect();
			return;
		}
		
		Log.v(TAG, "Sending data..");
		boolean more = true;
		int lines = 0;
		while(more){
			String line = null;
			try {
				line = read.readLine();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				more = false;
			}
			if(line!=null){
				String[] values = line.split(",");
			
				float[] floats = new float[SocketParser.GPS_TUPLE_CODE_length];
				for(int i =2; i < values.length-2;i++){
					floats[i-2] = Float.valueOf(values[i]);
				}
				
				GPSWriter.printDataLine(TAG, floats, Long.valueOf(values[1]), SocketParser.GPS_TUPLE_CODE);
				if(!sendLine(Long.valueOf(values[1]), floats, SocketParser.GPS_TUPLE_CODE))
					more = false;
				lines++;
			} else{
				more = false;
				break;
			}
		}
		
		try {
			mOutputStream.flush();
			read.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(lines>0){
			try {
				mService.openFileOutput(fName, Context.MODE_PRIVATE);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.v(TAG, "Data sent succesfully with: " + lines + " lines");
		} else{
			Log.e(TAG, "Error in data: " + lines);
		}
		Connections.disconnect();
		
	}
	
	
	/**
	 * Attempts to disconnect from the server
	 * @return
	 */
	public static boolean disconnect(){
		isConnected = false;
		Log.v(TAG, "Disconnecting...");
		try {
			SocketParser.disconnect(mOutputStream, mInputStream);
			mOutputStream.flush();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch blockto
			e.printStackTrace();
			return false;
		} catch(RuntimeException e){
			e.printStackTrace();
			return false;
		}
	}
	
	public String getmEmail() {
		return mEmail;
	}

	public String getmPassword() {
		return mPassword;
	}

	public String getmDevId() {
		return mDevId;
	}

}
