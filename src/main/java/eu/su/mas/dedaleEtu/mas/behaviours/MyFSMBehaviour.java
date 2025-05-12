package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.FSMBehaviour;

public class MyFSMBehaviour extends FSMBehaviour {

	private static final long serialVersionUID = -7400972632173420837L;
	
	protected List<String> list_agentNames;
	private String currentInterlocutor = null;
	
	private boolean pingSent = false;

	private List<String> currentPath = new ArrayList<String>(); // Current calculated path, to open node, destination of silo, or treasure location (soon)
	private Couple<Integer, String> siloDestinationClock = new Couple<>(0, ""); // Knowledge of the destination of the silo
	
	private Couple<Long, HashMap<String, List<Couple<Observation, String>>>> recordedTreasuresClock = new Couple<>(0L, new HashMap<>()); // (NodeId, content of the treasure), information currently accumulated but not used
	
	private Couple<String, Boolean> lastMoveSuccess = null;
	
	public MyFSMBehaviour(AbstractDedaleAgent a, List<String> agentNames) {
		super(a);
		this.list_agentNames = agentNames;
	}
	
	public boolean hasSentPing() {
		return pingSent;
	}

	public void setPingSent(boolean pingSent) {
		this.pingSent = pingSent;
	}

	public String getCurrentInterlocutor() {
		return currentInterlocutor;
	}

	public void setCurrentInterlocutor(String currentInterlocutor) {
		this.currentInterlocutor = currentInterlocutor;
	}
	
	public List<String> getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(List<String> currentPath) {
		this.currentPath = currentPath;
	}

	public Couple<Integer, String> getSiloDestinationClock() {
		return siloDestinationClock;
	}

	public boolean updateSiloDestinationClock(Couple<Integer, String> newSiloDestinationClock) {
		if (newSiloDestinationClock.getLeft() > this.siloDestinationClock.getLeft()) {
			this.siloDestinationClock = newSiloDestinationClock;
			return true;
		}
		return false;
	}

	public Couple<Long, HashMap<String, List<Couple<Observation, String>>>> getRecordedTreasuresClock() {
		return recordedTreasuresClock;
	}
	
	public boolean recordedTreasuresClockIsEmpty() {
		return this.recordedTreasuresClock.getRight().isEmpty();
	}
	
	public void putInRecordedTreasuresClock(String locId, List<Couple<Observation, String>> tresor) {
		this.recordedTreasuresClock.getRight().put(locId, tresor);
		this.recordedTreasuresClock = new Couple<>(System.nanoTime(), this.recordedTreasuresClock.getRight());
	}
	
	public void removeFromRecordedTreasuresClock(String locId) {
		this.recordedTreasuresClock.getRight().remove(locId);
		this.recordedTreasuresClock = new Couple<>(System.nanoTime(), this.recordedTreasuresClock.getRight());
	}

	public void mergeRecordedTreasuresClock(Couple<Long, HashMap<String, List<Couple<Observation, String>>>> incomingRecordedTreasures) {
		if (incomingRecordedTreasures.getLeft() > this.recordedTreasuresClock.getLeft()) {
			this.recordedTreasuresClock.getRight().putAll(incomingRecordedTreasures.getRight());
		} else {
			HashMap<String, List<Couple<Observation, String>>> newMap = new HashMap<>(incomingRecordedTreasures.getRight());
			newMap.putAll(this.recordedTreasuresClock.getRight());
			this.recordedTreasuresClock = new Couple<>(this.recordedTreasuresClock.getLeft(), newMap);
		}
	}

	public Couple<String, Boolean> getLastMoveSuccess() {
		return lastMoveSuccess;
	}

	public void setLastMoveSuccess(Couple<String, Boolean> lastMoveSuccess) {
		this.lastMoveSuccess = lastMoveSuccess;
	}
	
	public List<String> getList_agentNames() {
		return list_agentNames;
	}

	public void setList_agentNames(List<String> list_agentNames) {
		this.list_agentNames = list_agentNames;
	}
}
