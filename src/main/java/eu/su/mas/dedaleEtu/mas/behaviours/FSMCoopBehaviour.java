package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.utils.HelpNeededForTreasure;
import jade.core.behaviours.FSMBehaviour;

public class FSMCoopBehaviour extends FSMBehaviour {
	
	private static final long serialVersionUID = -568863670879327961L;
	
	private boolean agentIsSilo; // If the agent is the silo
	
	private List<String> list_agentNames;
	private HashMap<String,MapRepresentation> allMaps;
	private String currentInterlocutor = null;
	
	private boolean pingSent = false;

	private List<String> currentPath = new ArrayList<String>(); // Current calculated path, to open node, destination of silo, or treasure location (soon)
	private Couple<Integer, String> siloDestinationClock = new Couple<>(0, ""); // Knowledge of the destination of the silo
	
	private boolean goingToSilo = false; // if the agent is in search of the silo
	private boolean learnedSiloPosition = false; // if the agent exchanged with the silo himself or an agent who knows a destination of the silo
	
	private HashMap<String, List<Couple<Observation, String>>> recordedTreasures = new HashMap<>(); // (NodeId, content of the treasure), information currently accumulated but not used
	
	private HelpNeededForTreasure helpNeeded = null;
	
	private Couple<String, Boolean> lastMoveSuccess = null;
	
	public FSMCoopBehaviour(AbstractDedaleAgent a, List<String> agentNames, boolean agentIsSilo) {
		super(a);
		this.list_agentNames = agentNames;
		this.setAgentIsSilo(agentIsSilo);
	}
	
	public boolean hasSentPing() {
		return pingSent;
	}

	public void setPingSent(boolean pingSent) {
		this.pingSent = pingSent;
	}

	public HashMap<String, MapRepresentation> getAllMaps() {
		return allMaps;
	}
	
	public MapRepresentation getMap(String name) {
		return this.allMaps.get(name);
	}
	
	public void initAllMaps() {
		this.allMaps = new HashMap<String, MapRepresentation>();
		for(String name : this.list_agentNames) {
			System.out.println(name);
			if (name.equals(this.myAgent.getLocalName())) {
				this.allMaps.put(name, new MapRepresentation(true));
			} else {
				this.allMaps.put(name, new MapRepresentation(false));
			}
		}
	}
	
	public void addNodeToMap(String posId, String name) {
		this.allMaps.get(name).addNode(posId, MapAttribute.closed);
	}
	
	public boolean addNewNodeToMap(String posId, String name) {
		return this.allMaps.get(name).addNewNode(posId);
	}
	
	public void addEdgeToMap(String pos1Id, String pos2Id, String name) {
		this.allMaps.get(name).addEdge(pos1Id, pos2Id);
	}

	public void setAllMaps(HashMap<String, MapRepresentation> allMaps) {
		for(String key : allMaps.keySet()) {
			this.allMaps.put(key, allMaps.get(key));
		}
	}
	
	public void resetMap(String name) {
		this.allMaps.put(name, new MapRepresentation(false));
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

	public boolean isAgentSilo() {
		return agentIsSilo;
	}

	public void setAgentIsSilo(boolean agentIsSilo) {
		this.agentIsSilo = agentIsSilo;
	}

	public boolean isGoingToSilo() {
		return goingToSilo;
	}

	public void setGoingToSilo(boolean goingToSilo) {
		this.goingToSilo = goingToSilo;
	}

	public boolean hasLearnedSiloPosition() {
		return learnedSiloPosition;
	}

	public void setLearnedSiloPosition(boolean learnedSiloPosition) {
		this.learnedSiloPosition = learnedSiloPosition;
	}

	public HashMap<String, List<Couple<Observation, String>>> getRecordedTreasures() {
		return recordedTreasures;
	}

	public void setRecordedTreasures(HashMap<String, List<Couple<Observation, String>>> recordedTreasures) {
		this.recordedTreasures = recordedTreasures;
	}

	public HelpNeededForTreasure getHelpNeeded() {
		return helpNeeded;
	}

	public void setHelpNeeded(HelpNeededForTreasure helpNeeded) {
		this.helpNeeded = helpNeeded;
	}

	public Couple<String, Boolean> getLastMoveSuccess() {
		return lastMoveSuccess;
	}

	public void setLastMoveSuccess(Couple<String, Boolean> lastMoveSuccess) {
		this.lastMoveSuccess = lastMoveSuccess;
	}
	
}
