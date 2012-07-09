package edu.fordham.cis.wisdm.zoo.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.fordham.cis.wisdm.zoo.main.LocationUpdateService;

import android.location.Location;

import android.util.Log;

/**
 * GPS file object that adds more functionality to BufferedWriter
 * @author Andrew Grosner
 * @version 1.0
 */
public class GFile extends BufferedWriter{

	//the file name
	private String fName;
	
	//the file object
	private File file;
	
	//the root where file is stored
	private static File root =null;
	
	//the values of the GPS reading
	private String[] values = new String[8];
	
	/**
	 * Constructs new instance of GFile with specified name and file root
	 * @param name
	 * @param fRoot
	 * @throws IOException 
	 */
	public GFile(String name, File root) throws IOException{
		super(new FileWriter(new File(root, name)));
		file = new File(root, name);
		this.root = root;
		setfName(name);
		for(int i =0; i < values.length; i++){
			values[i] = "";
		}
		
	}
	
	/**
	 * Stores location in object
	 * @param l
	 */
	public void storeLocation(Location l){
		values[0] = l.getTime() +"";
		values[1] = l.getLatitude()+"";
		values[2] = l.getLongitude()+"";
		values[3] = l.getAccuracy()+"";
		values[4] = l.getSpeed()+"";
		values[5] = l.getAccuracy()+"";
		values[6] = l.getBearing()+"";
		values[7] = l.getProvider()+"";
		if(checkForNull()){
			Log.e(LocationUpdateService.TAG, "Null value!!!");
		}
	}
	
	private boolean checkForNull(){
		for(int i =0; i < values.length; i++){
			if (values[i].equals(null)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Writes a location reading to file
	 * @param timestamp
	 * @param l
	 */
	public void writeLocation(Location l){
		storeLocation(l);
		writeLocation();
	}
	
	/**
	 * Writes the stored values to file
	 */
	public void writeLocation() throws NullPointerException{
		String line = "";
		for(int i =0; i < values.length-1; i++){
			line+=values[i]+",";
		}
		line+=values[values.length-1]+"\n";
		try {
			write(line);
			flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes file and keeps closed
	 */
	public void delete(){
		file.delete();
	}
	

	public String getfName() {
		return fName;
	}

	public void setfName(String fName) {
		this.fName = fName;
	}

	public File getRoot() {
		return root;
	}

	public void setRoot(File root) {
		this.root = root;
	}

}
