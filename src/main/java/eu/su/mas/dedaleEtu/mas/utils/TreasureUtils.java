package eu.su.mas.dedaleEtu.mas.utils;

import java.util.List;

import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.projectAgents.ExploreCoopAgent;

public class TreasureUtils {
	
	public static boolean isUnlocked(List<Couple<Observation, String>> tresor) {
		for (Couple<Observation, String> o:tresor) {
			switch (o.getLeft()) {
			case LOCKSTATUS: return Integer.parseInt(o.getRight()) == 1;
			default: break;
			}
		}
		return false;
	}
	
	public static boolean enoughSpace(List<Couple<Observation, String>> tresor, ExploreCoopAgent agent) {
		for (Couple<Observation, String> o:tresor) {
			switch (o.getLeft()) {
			case DIAMOND: return agent.getBackPackFreeSpaceFor(Observation.DIAMOND) >= Integer.parseInt(o.getRight());
			case GOLD: return agent.getBackPackFreeSpaceFor(Observation.GOLD) >= Integer.parseInt(o.getRight());
			default: break;
			}
		}
		return false;
	}
	
	public static Observation treasureType(List<Couple<Observation, String>> tresor) {
		for(Couple<Observation,String> o:tresor){
			switch (o.getLeft()) {
			case DIAMOND:case GOLD: return o.getLeft();
			default: break;
			}
		}
		Debug.warning("This treasure does not have a type ?");
		return null;
	}
	
	public static boolean treasureContainsNoRessources(List<Couple<Observation, String>> tresor) {
		for(Couple<Observation,String> o:tresor){
			switch (o.getLeft()) {
			case DIAMOND:case GOLD: return o.getRight().equals("0");
			default: break;
			}
		}
		Debug.warning("This treasure does not have a type ?");
		return true;
	}
	
	public static int getTreasureLockPicking(List<Couple<Observation, String>> tresor) {
		for(Couple<Observation,String> o:tresor){
			switch (o.getLeft()) {
			case LOCKPICKING: return Integer.parseInt(o.getRight());
			default: break;
			}
		}
		Debug.warning("This treasure does not have a lockpicking attribute ?");
		return -1;
	}
	
	public static int getTreasureStrength(List<Couple<Observation, String>> tresor) {
		for(Couple<Observation,String> o:tresor){
			switch (o.getLeft()) {
			case STRENGH: return Integer.parseInt(o.getRight());
			default: break;
			}
		}
		Debug.warning("This treasure does not have a lockpicking attribute ?");
		return -1;
	}
	
	public static List<Couple<Observation, String>> getTreasureFromLocation(List<Couple<Location,List<Couple<Observation,String>>>> lobs, Location loc) {
		for (Couple<Location, List<Couple<Observation, String>>> o : lobs) {
			if (loc.equals(o.getLeft())) {
				return o.getRight();
			}
		}
		return null;	
	}

}
