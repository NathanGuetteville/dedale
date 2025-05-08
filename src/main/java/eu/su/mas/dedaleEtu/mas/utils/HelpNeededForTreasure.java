package eu.su.mas.dedaleEtu.mas.utils;

import eu.su.mas.dedale.env.Observation;
import java.io.Serializable;

public class HelpNeededForTreasure implements Serializable {
	private String locationId;
	private int lockpicking;
	private int strength;
	private Observation treasureType;
	
	public HelpNeededForTreasure(String location) {
		this.locationId = location;
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
	

}
