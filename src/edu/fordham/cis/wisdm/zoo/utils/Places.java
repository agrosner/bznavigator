package edu.fordham.cis.wisdm.zoo.utils;

/**
 * The enumerator for display of the splash screen activity. 
 * @author agrosner
 *
 */
public enum Places {
	LIST,
	MAP,
	NEWS,
	SHOPS,
	SPECIAL,
	FOOD,
	EXHIBITS,
	RESTROOMS,
	EXITS,
	PARKING,
	ADMIN;
	
	/**
	 * Returns an int value of the corresponding place position
	 * @return int value
	 */
	public int toInt(){
			 if(this == MAP) 		return 0;
		else if(this == NEWS)		return 1;
		else if(this == SHOPS) 		return 2;
		else if(this == SPECIAL)	return 3;
		else if(this == FOOD)		return 4;
		else if(this == EXHIBITS)	return 5;
		else if(this == RESTROOMS)	return 6;
		else if(this == EXITS) 		return 7;
		else if(this == PARKING) 	return 8;
		else if(this == ADMIN) 		return 9;
		else						return -1;
	}
	
	/**
	 * turns a position into a Places value
	 * @param position
	 * @return Places value
	 */
	public static Places toPlace(int position){
			 if(position == 0) return MAP;
		else if(position == 1) return NEWS;
		else if(position == 2) return SHOPS;
		else if(position == 3) return SPECIAL;
		else if(position == 4) return FOOD;
		else if(position == 5) return EXHIBITS;
		else if(position == 6) return RESTROOMS;
		else if(position == 7) return EXITS;
		else if(position == 8) return PARKING;
		else if(position == 9) return ADMIN;
		else 				   return LIST;
	}
}
