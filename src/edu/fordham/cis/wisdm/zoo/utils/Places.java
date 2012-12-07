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
	AMENITIES,
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
		else if(this == AMENITIES) 	return 7;
		else if(this == ADMIN) 		return 8;
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
		else if(position == 7) return AMENITIES;
		else if(position == 8) return ADMIN;
		else 				   return LIST;
	}
	
	/**
	 * Whether this is considered a place fragment type
	 * @return
	 */
	public boolean isPlaceFragment(){
		return !(this == MAP || this == LIST || this == NEWS);
	}
	
	/**
	 * Whether an int position is a place fragment
	 * @param position
	 * @return
	 */
	public static boolean isIntPlaceFragment(int position){
		return toPlace(position).isPlaceFragment();
	}
}
