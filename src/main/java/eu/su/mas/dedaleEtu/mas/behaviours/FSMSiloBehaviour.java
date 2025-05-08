package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class FSMSiloBehaviour extends MyFSMBehaviour{

	private static final long serialVersionUID = -925723552514016863L;
	
	private MapRepresentation map;
	private boolean stayingPut = true;
	private boolean blocked = false; // if the agent is blocked
	private String blockingNeighbor = null;
	
	private final int priority;
		
	public FSMSiloBehaviour(AbstractDedaleAgent a, List<String> agentNames, int priority) {
		super(a, agentNames);
		this.priority = priority;
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
	public int getPriority() {
		return this.priority;
	}
	public boolean getBlocked() {
		return this.blocked;
	}
	
	public void setBlocked(boolean b) {
		this.blocked = b;
	}
	
	public String getBlockingNeighbor() {
		return this.blockingNeighbor;
	}
	
	public void setBlockingNeighbor(String blockingNeighbor) {
		this.blockingNeighbor = blockingNeighbor;
	}

}
