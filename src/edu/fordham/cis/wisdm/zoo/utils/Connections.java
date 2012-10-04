package edu.fordham.cis.wisdm.zoo.utils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import edu.fordham.cis.wisdm.utils.SocketParser;

import android.util.Log;

/**
 * This class wraps around server class socket parser
 * @author agrosner
 *
 */
public class Connections {
	private static String TAG = "Connections";
	
	private static DataOutputStream mOutputStream = null;
	
	private static DataInputStream mInputStream = null;
	
	private static int PORT = 2324;
	
	private static String HOST = "netlab.cis.fordham.edu";
	
	private static Socket mSocket = null;
	
	private static int DATA_TIMEOUT = 30000;
	
	private String mEmail = "none";
	
	private String mPassword = "";
	
	public static String mServerMessage = " ";

	private String mDevId = " ";
	
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
		
		return true;
	}
	
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
		}
		
		try {
			byte smsg = mInputStream.readByte();
			if(smsg == SocketParser.AUTH_CODE){
				Log.v(TAG, "User authorized");
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
		
		return true;
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
				mServerMessage+="\n";
				mServerMessage+=e.getMessage();
			}
		}
		return false;
	}
	
	public static boolean disconnect(){
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
