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
	
	private HashMap<String, List<Couple<Observation, String>>> recordedTreasures = new HashMap<>(); // (NodeId, content of the treasure), information currently accumulated but not used
	
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

	public void updateSiloDestinationClock(Couple<Integer, String> newSiloDestinationClock) {
		if (newSiloDestinationClock.getLeft() > this.siloDestinationClock.getLeft())
			this.siloDestinationClock = newSiloDestinationClock;
	}

	public HashMap<String, List<Couple<Observation, String>>> getRecordedTreasures() {
		return recordedTreasures;
	}

	public void setRecordedTreasures(HashMap<String, List<Couple<Observation, String>>> recordedTreasures) {
		this.recordedTreasures = recordedTreasures;
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
