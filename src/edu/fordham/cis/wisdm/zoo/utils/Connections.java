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
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import edu.fordham.cis.wisdm.utils.SocketParser;

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.util.Log;

/**
 * This class wraps around server class socket parser. 
 * Provides static connection handling and each object declared is used in connection methods
 * @author Andrew Grosner
 *
 */
public class Connections implements Serializable{
	/**
	 * For use in serializable interface
	 */
	private static final long serialVersionUID = -3412358925048736178L;

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
	private static int PORT = 2325;
	
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
	 * Whether authentication has passed or not
	 */
	private static boolean isAuthorized = false;
	
	/**
	 * Flag whether user already exists
	 */
	public boolean userExists = false;
	
	/**
	 * the unique ID of the current visit that will be pulled from the server initially
	 */
	private static int mVisitID = -1;
	
	
	private static boolean connect(){
		Log.v(TAG, "Connecting to server...");
		
		mServerMessage = " ";
		mSocket = new Socket();
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(HOST);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(addr!=null){
			SocketAddress sockaddr = new InetSocketAddress(addr, PORT);
			Log.v(TAG, "Initialized socket. Trying to connect...on" + HOST + ": " + PORT);
			
			try {
				mSocket.connect(sockaddr, DATA_TIMEOUT);
				mOutputStream = new DataOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));
				mInputStream = new DataInputStream(mSocket.getInputStream());
				isConnected = true;
			} catch (IOException e) {
				e.printStackTrace();
				mServerMessage +="\n";
				mServerMessage+=e.getMessage();
			}
		}
		
		if(isConnected)	Log.v(TAG, "Established Connection");
		
		return isConnected;
	}
	
	/**
	 * Will try to authorize a user with the server (given a connection is established)
	 * @param con
	 * @return
	 */
	private static boolean authorize(Connections con){
		if(!isConnected) return false;
		
		Log.v(TAG, "Authorizing user");
		boolean checkWrite = false;

		try {
			SocketParser.writeAuthReq(con.getmEmail().split("@")[0], con.getmPassword(), 
					con.getmEmail() , con.getmDevId(), mOutputStream);
			checkWrite = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mServerMessage+="\n";
			mServerMessage+=e.getMessage();
		} catch(NullPointerException n){
			mServerMessage+="\n";
			mServerMessage+=n.getMessage();
		}
		
		if(checkWrite){
			try {
				byte smsg = mInputStream.readByte();
				if(smsg == SocketParser.AUTH_CODE){
					Log.v(TAG, "User authorized");
					isAuthorized  = true;
				} else if(smsg == SocketParser.AUTH_DENY){
					Log.v(TAG, "User denied");
					mServerMessage+="\nUser denied";
				} else{
					Log.v(TAG, "Error?");
					mServerMessage+="\nUnknown error";
				}
			} catch (IOException e) {
			// 	TODO Auto-generated catch block
				e.printStackTrace();
				mServerMessage+="\n";
				mServerMessage+=e.getMessage();
			}
		}

		if(!isAuthorized){
			disconnect();
		}
		return isAuthorized;
	}
	
	/**
	 * Attempts to connect and authorize user with the server\
	 * If user is a guest, it attempts to create an account for them
	 * @param con
	 * @return
	 */
	public static synchronized boolean prepare(Connections con){
		if(con==null){
			return false;
		}
		if(con.getmEmail().equals("") && con.getmPassword().equals("")){
			//create temp password
			con.mEmail = con.mDevId + "@wisdmproject.com";
			con.mPassword = con.mDevId;
			
			if(Connections.createUser(con) || con.userExists)
				return connect() && authorize(con);
			else
				return false;
		}
		
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
			if(mInputStream.readByte()==SocketParser.VISIT_CODE)
				mVisitID = SocketParser.readVisitReq(mInputStream);
			else return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mVisitID!=0;
	}
	
	/**
	 * Authenticates a visit ID stored in this class
	 * @param con
	 * @return
	 */
	public static boolean visitAuth(Connections con){
		if(!isConnected) return false;
		
		boolean success = false;
		try {
			SocketParser.writeVisitReq(mOutputStream, mVisitID);
			byte signal = mInputStream.readByte();
			if(signal == SocketParser.AUTH_CODE){
				Log.d(TAG, "Visit authenticated");
				success = true;
			} else if(signal == SocketParser.AUTH_DENY){
				Log.e(TAG, "Visit denied!");
			} else{
				Log.e(TAG, "Visit not authenticated for some reason");
			}
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			Log.e(TAG, "Could not write a visit request: " + e2.getMessage());
		}
		if(!success) disconnect();
		return success;
	}
	
	/**
	 * Sends a search query to the server
	 * @param query
	 */
	public static boolean sendSearchQuery(Connections con, String query, Location loc){
		
		//ensure connection
		if(loc==null||!prepare(con) ||	!visitAuth(con))	return false;
		
		boolean success = false;
		try {
			SocketParser.writeSearchReq(mOutputStream, mVisitID, query, (float) loc.getLatitude(), (float) loc.getLongitude());
			byte signal = mInputStream.readByte();
			if(signal == SocketParser.AUTH_CODE){
				Log.d(TAG, "Search Query successfully sent");
				success = true;
				disconnect();
			} else if(signal == SocketParser.AUTH_DENY){
				Log.e(TAG, "Request denied!!");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			success = false;
		}
		if(!success)	disconnect();
		return success;
	}
	
	/**
	 * Writes a user request to the server if connection was established
	 * @param con
	 * @return
	 */
	public static boolean createUser(Connections con){
		if(connect() && !con.userExists){
			try {
				SocketParser.writeUsrReq(mOutputStream, con.mEmail.split("@")[0], con.mPassword, con.mEmail, con.mDevId);
				byte smsg = mInputStream.readByte();
				if(smsg == SocketParser.USR_TAKEN){
					mServerMessage+="\nUser taken";
					con.userExists = true;
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
		if(!prepare(con)){
			Log.v(TAG, "Sending failed");
			return;
		}
		
		if(!visitAuth(con)){
			Log.e(TAG, "Could not authenticate visit, stopping...");
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
				
				//GPSWriter.printDataLine(TAG, floats, Long.valueOf(values[1]), SocketParser.GPS_TUPLE_CODE);
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
	 * Sends survey data in one fell swoop
	 * @param conn
	 * @param responses
	 * @param qids
	 * @param types
	 * @throws IOException
	 */
	public static void sendSurvey(Connections conn, ArrayList<String> responses, ArrayList<Integer> qids, ArrayList<String> types) throws IOException{
		if(!prepare(conn) || !(visitAuth(conn))){
			return;
		}
		
		int index= 0;
		for(String type: types){
			String resp = responses.get(index);
			Log.v(TAG, resp);
			if(resp.length()>=1){
				if(type.length()>= 7 && type.equals("MultiStr")){
					String[] resps = resp.split(",");
					int id = qids.get(index);
					for(String respo: resps){
						SocketParser.writeSurvQ(mOutputStream, id, respo);
						mOutputStream.flush();
					}
				} else if(type.length() ==3 && type.equals("Str")){
					SocketParser.writeSurvQ(mOutputStream, qids.get(index), resp);
					mOutputStream.flush();
				} else if(type.length() == 3 && type.equals("Num")){
					float res = 0;
					try{
						res = Float.valueOf(resp);
					} catch(NumberFormatException n){//catch possible errors or ex: 5+ --> 5
						n.printStackTrace();
						int length = resp.length();
						resp = resp.substring(0, length-1);
						res = Float.valueOf(resp);
					} finally{
						SocketParser.writeSurvQ(mOutputStream, qids.get(index), res);
						mOutputStream.flush();
					}
				}
			}
			index++;
		}
		mOutputStream.flush();
		disconnect();
	}
	
	
	/**
	 * Attempts to disconnect from the server
	 * @return
	 */
	public static boolean disconnect(){
		isConnected = false;
		isAuthorized = false;
		Log.v(TAG, "Disconnecting...");
		try {
			SocketParser.disconnect(mOutputStream, mInputStream);
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
	
	/**
	 * Returns whether there is an active connection or not
	 * @return
	 */
	public static boolean isActive(){
		return isConnected&&isAuthorized;
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
	
	public String getmEmail() {
		if(mEmail.equals("none"))
			return mDevId + "@wisdmproject.com";
		return mEmail;
	}

	public String getmPassword() {
		return mPassword;
	}

	public String getmDevId() {
		return mDevId;
	}

}
