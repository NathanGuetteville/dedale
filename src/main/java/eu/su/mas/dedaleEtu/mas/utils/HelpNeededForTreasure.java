package eu.su.mas.dedaleEtu.mas.utils;

import eu.su.mas.dedale.env.Observation;
import java.io.Serializable;

public class HelpNeededForTreasure implements Serializable {
	
	private static final long serialVersionUID = 241450461097585613L;
	
	private String locationId;
	private int lockpicking = 0;
	private int strength = 0;
	private Observation treasureType;
	private Observation personalTreasureType;
	private int myFreeSpace = 0;
	
	public HelpNeededForTreasure(String location, Observation type) {
		this.locationId = location;
		this.personalTreasureType = type;
	}
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	public int getLockpicking() {
		return lockpicking;
	}
	public void setLockpicking(int lockpicking) {
		this.lockpicking = lockpicking;
	}
	public int getStrength() {
		return strength;
	}
	public void setStrength(int strength) {
		this.strength = strength;
	}
	public Observation getTreasureType() {
		return treasureType;
	}
	public void setTreasureType(Observation treasureType) {
		this.treasureType = treasureType;
	}
	public Observation getPersonalTreasureType() {
		return personalTreasureType;
	}
	public void setPersonalTreasureType(Observation personalTreasureType) {
		this.personalTreasureType = personalTreasureType;
	}
	public int getMyFreeSpace() {
		return myFreeSpace;
	}
	public void setMyFreeSpace(int myFreeSpace) {
		this.myFreeSpace = myFreeSpace;
	}
	

}
