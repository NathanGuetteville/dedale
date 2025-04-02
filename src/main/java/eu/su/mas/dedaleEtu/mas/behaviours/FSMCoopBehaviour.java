package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.FSMBehaviour;

public class FSMCoopBehaviour extends FSMBehaviour {
	
	private static final long serialVersionUID = -568863670879327961L;
	
	private List<String> list_agentNames;
	private HashMap<String,MapRepresentation> allMaps;
	private String currentInterlocutor = null;
	private boolean pingSent = false;
	
	public FSMCoopBehaviour(AbstractDedaleAgent a, List<String> agentNames) {
		super(a);
		this.list_agentNames = agentNames;
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
	
}
