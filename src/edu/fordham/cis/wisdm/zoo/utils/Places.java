package edu.fordham.cis.wisdm.zoo.utils;

/**
 * The enumerator for display of the splash screen activity. 
 * @author agrosner
 *
 */
public enum Places {
	LIST,
	MAP,
	FIND,
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
		else if(this == FIND)		return 1;
		else if(this == NEWS)		return 2;
		else if(this == SHOPS) 		return 3;
		else if(this == SPECIAL)	return 4;
		else if(this == FOOD)		return 5;
		else if(this == EXHIBITS)	return 6;
		else if(this == RESTROOMS)	return 7;
		else if(this == EXITS) 		return 8;
		else if(this == PARKING) 	return 9;
		else if(this == ADMIN) 		return 10;
		else						return -1;
	}
	
	/**
	 * turns a position into a Places value
	 * @param position
	 * @return Places value
	 */
	public static Places toPlace(int position){
			 if(position == 0) return MAP;
		else if(position == 1) return FIND;
		else if(position == 2) return NEWS;
		else if(position == 3) return SHOPS;
		else if(position == 4) return SPECIAL;
		else if(position == 5) return FOOD;
		else if(position == 6) return EXHIBITS;
		else if(position == 7) return RESTROOMS;
		else if(position == 8) return EXITS;
		else if(position == 9) return PARKING;
		else if(position == 10) return ADMIN;
		else 				   return LIST;
	}
}
