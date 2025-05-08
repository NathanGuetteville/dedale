package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class FSMSiloBehaviour extends MyFSMBehaviour{

	private static final long serialVersionUID = -925723552514016863L;
	
	private MapRepresentation map;
	private boolean stayingPut = true;
		
	public FSMSiloBehaviour(AbstractDedaleAgent a, List<String> agentNames) {
		super(a, agentNames);
	}
	
	public MapRepresentation getMap() {
		return map;
	}

	public void setMap(MapRepresentation map) {
		this.map = map;
	}

	public List<String> getList_agentNames() {
		return list_agentNames;
	}

	public void setList_agentNames(List<String> list_agentNames) {
		this.list_agentNames = list_agentNames;
	}
	
	public boolean isStayingPut() {
		return stayingPut;
	}

	public void setStayingPut(boolean stayingPut) {
		this.stayingPut = stayingPut;
	}

	public void addNodeToMap(String nodeId) {
		this.map.addNode(nodeId, MapAttribute.closed);
	}
	
	public boolean addNewNodeToMap(String nodeId) {
		return this.map.addNewNode(nodeId);
	}
	public void addEdgeToMap(String pos1Id, String pos2Id) {
		this.map.addEdge(pos1Id, pos2Id);
	}

}
