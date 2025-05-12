package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.utils.TreasureUtils;
import jade.core.behaviours.OneShotBehaviour;


public class ExplorationBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496788661L;
	
	private static int UNBLOCK_TO_EXPLO = 23;

	private boolean finished = false;
	private boolean treasureFound = false; // if a non-silo agent found a treasure in his location
	private boolean neighbor = false;

	private List<String> list_agentNames;

/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param agentNames name of the agents to share the map with
 */
	public ExplorationBehaviour(final AbstractDedaleAgent myagent, List<String> agentNames) {
		super(myagent);
		this.list_agentNames=agentNames;
		
		
	}

	@Override
	public void action() {
		
		if (finished) return;
		
		this.treasureFound = false;
		this.neighbor = false;
		System.out.println(this.myAgent.getLocalName()+" : ExplorationBehaviour");
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		if (fsm.getAllMaps() == null) {
			fsm.initAllMaps();
		}
		
		((AbstractDedaleAgent) this.myAgent).emptyMyBackPack("Tank");
		
		
		
		
		//0) Retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		System.out.println(this.myAgent.getLocalName()+" : I'm on position "+myPosition.getLocationId());

		if (myPosition!=null){
			Couple<String, Boolean> lastMove = fsm.getLastMoveSuccess();
			String nextNodeId=null;
			if (lastMove != null && !lastMove.getRight()) {
				nextNodeId = lastMove.getLeft();
				if (fsm.getBlocked() == false) {
					Debug.warning(this.myAgent.getLocalName()+" : Last move failed, doing it again - going to "+lastMove.getLeft()+" from "+myPosition.getLocationId());
					fsm.setBlocked(true);
				} else {
					//The two last moves failed. Try to communicate with neighbor.
					List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
					System.out.println(this.myAgent.getLocalName()+" : "+lobs);
					for(Couple<Location,List<Couple<Observation,String>>> loc : lobs) {
						if(loc.getLeft().getLocationId().equals(nextNodeId)) { //Checking at the destination coordinate
							for(Couple<Observation,String> obs : loc.getRight()) {
								switch (obs.getLeft()) {
									case AGENTNAME: //Checking for an agent neighbor
										neighbor = true;
										fsm.setBlockingNeighbor(obs.getRight());
										switch (obs.getRight()) {
											case "Wumpus":
												neighbor = false;
												List<String> openNodes = fsm.getMap(this.myAgent.getLocalName()).getOpenNodes();
												Random r= new Random();
												int newDestId=r.nextInt(openNodes.size()-1);
												List<String> path = fsm.getMap(this.myAgent.getLocalName()).getShortestPath(myPosition.getLocationId(), openNodes.get(newDestId));
												nextNodeId = path.remove(0);
												fsm.setCurrentPath(path);
												break;
											default: break;
										}
										break;
									default: break;
								}
							}
						}
					}
					if(neighbor) { //Initiate communication with neighbor
						fsm.setTransitionBackFromUnblock(UNBLOCK_TO_EXPLO);
						return;
					}
					else { //Blocked by the Wumpus
						Debug.warning(this.myAgent.getLocalName()+" : Unexpected blocking - going to "+lastMove.getLeft()+" from "+myPosition.getLocationId());
					}
				}
			} else {
				fsm.setBlocked(false);
				//List of observable from the agent's current position
				List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
	
				
	
				//1) remove the current node from openlist and add it to closedNodes.
				String currentNode = myPosition.getLocationId();
				for(String i : this.list_agentNames) {
					fsm.addNodeToMap(currentNode, i);
				}
				
				
	
				//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
				Iterator<Couple<Location, List<Couple<Observation, String>>>> iter=lobs.iterator();
				while(iter.hasNext()){
					Couple <Location, List<Couple<Observation, String>>> node = iter.next();
					Location accessibleNode=node.getLeft();
					for(String i : list_agentNames) {
						boolean isNewNode=fsm.addNewNodeToMap(accessibleNode.getLocationId(), i);
						//boolean isOpen=fsm.getMap(this.myAgent.getLocalName()).getOpenNodes().contains(accessibleNode.getLocationId());
						//the node may exist, but not necessarily the edge
						//System.out.println("Checking node: " + accessibleNode.getLocationId() 
		                   //+ " | isNewNode=" + isNewNode 
		                   //+ " | isOpen=" + isOpen);
	
						if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
							fsm.addEdgeToMap(myPosition.getLocationId(), accessibleNode.getLocationId(), i);
							if (i == this.myAgent.getLocalName() && nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
						} 
					}
				}
				
				if (!fsm.getMap(this.myAgent.getLocalName()).hasOpenNode()) {
					finished = true;
					fsm.setExploFinished(true);
				}
	
				// If the agent found a treasure on his location, he adds it to the list of located treasures
				// That list will be used by agent when they need to go to a specific treasure
				List<Couple<Observation, String>> tresor = TreasureUtils.getTreasureFromLocation(lobs, myPosition);
				//System.out.println(this.myAgent.getLocalName()+" : à ma position se trouve "+tresor);
				if (!tresor.isEmpty() && !TreasureUtils.treasureContainsNoRessources(tresor)) {
					// If the agent is not a silo, enable CollectBehaviour
					//System.out.println(this.myAgent.getLocalName()+" : trésor repéré dans explo "+tresor+ " à la position "+myPosition.getLocationId());
					fsm.putInRecordedTreasuresClock(myPosition.getLocationId(), tresor);
					this.treasureFound = true;
					return;
				}
				
	
				
				//System.out.println(this.myAgent.getLocalName()+" : "+fsm.getMap(this.myAgent.getLocalName()).getOpenNodes());
				if (finished){
					return;
				}
				
				
			}
			if (nextNodeId==null){
				// Calculation of path to shortest open node

				List<String> path = fsm.getCurrentPath();
				if (path.isEmpty()) {
					path = fsm.getMap(this.myAgent.getLocalName()).getShortestPathToClosestOpenNode(myPosition.getLocationId());
				}
				System.out.println(this.myAgent.getLocalName()+" : path restant - "+path);
				nextNodeId=path.remove(0);
				
				fsm.setCurrentPath(path);
			}
			
			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			fsm.setLastMoveSuccess(new Couple<>(nextNodeId, ((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId))));
		}
	}

	
	@Override
	public int onEnd() {
		if (finished) return 8; 			// MOVE_TO_TREASURE
		if (treasureFound) return 9;//9;	// COLLECT
		if (neighbor) return 22;			// UNBLOCK
		return 0; 							// MESSAGE
	}	
}
