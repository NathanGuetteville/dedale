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

public class FSMCoopBehaviour extends MyFSMBehaviour {
	
	private static final long serialVersionUID = -568863670879327961L;
	
	private HashMap<String,MapRepresentation> allMaps;
	
	private boolean goingToSilo = false; // if the agent is in search of the silo
	private boolean learnedSiloPosition = false; // if the agent exchanged with the silo himself or an agent who knows a destination of the silo
		
	private HelpNeededForTreasure helpNeeded = null;
	
	public FSMCoopBehaviour(AbstractDedaleAgent a, List<String> agentNames) {
		super(a, agentNames);
	}

	public HashMap<String, MapRepresentation> getAllMaps() {
		return allMaps;
	}
	
	public MapRepresentation getMap(String name) {
		return this.allMaps.get(name);
	}
	
	public void initAllMaps() {
		this.allMaps = new HashMap<String, MapRepresentation>();
		for(String name : super.list_agentNames) {
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

	public HelpNeededForTreasure getHelpNeeded() {
		return helpNeeded;
	}

	public void setHelpNeeded(HelpNeededForTreasure helpNeeded) {
		this.helpNeeded = helpNeeded;
	}
	
}
