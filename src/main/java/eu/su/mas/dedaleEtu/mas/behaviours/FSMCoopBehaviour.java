package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.utils.HelpNeededForTreasure;

public class FSMCoopBehaviour extends MyFSMBehaviour {
	
	private static final long serialVersionUID = -568863670879327961L;
	
	private HashMap<String,MapRepresentation> allMaps;
	
	private boolean goingToSilo = false; // if the agent is in search of the silo
	private boolean learnedSiloPosition = false; // if the agent exchanged with the silo himself or an agent who knows a destination of the silo
	private boolean blocked = false; // if the agent is blocked
	private int transitionBackFromUnblock = 23; // 23, 24, or 26
	
	private String blockingNeighbor = null;
	
	private boolean goingToTreasure = false;
	private boolean exploFinished = false;
	private boolean allFinished = false;
		
	private HelpNeededForTreasure helpNeeded = null;
	private boolean leader = false;
	
	private final int priority;
	
	public FSMCoopBehaviour(AbstractDedaleAgent a, List<String> agentNames, int priority) {
		super(a, agentNames);
		this.priority = priority;
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

	public boolean isGoingToTreasure() {
		return goingToTreasure;
	}

	public void setGoingToTreasure(boolean goingToTreasure) {
		this.goingToTreasure = goingToTreasure;
	}

	public boolean isLeader() {
		return leader;
	}

	public void setLeader(boolean leader) {
		this.leader = leader;
	}

	public boolean isExploFinished() {
		return exploFinished;
	}

	public void setExploFinished(boolean exploFinished) {
		this.exploFinished = exploFinished;
	}

	public boolean isAllFinished() {
		return allFinished;
	}

	public void setAllFinished(boolean allFinished) {
		this.allFinished = allFinished;
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
	
	public int getPriority() {
		return this.priority;
	}

	public int getTransitionBackFromUnblock() {
		return transitionBackFromUnblock;
	}

	public void setTransitionBackFromUnblock(int transitionBackFromUnblock) {
		this.transitionBackFromUnblock = transitionBackFromUnblock;
	}
	
}
