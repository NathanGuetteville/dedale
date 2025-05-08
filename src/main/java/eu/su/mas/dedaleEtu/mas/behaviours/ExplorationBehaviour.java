package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

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

	private boolean finished = false;
	private boolean treasureFound = false; // if a non-silo agent found a treasure in his location

	private List<String> list_agentNames;

/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public ExplorationBehaviour(final AbstractDedaleAgent myagent, List<String> agentNames) {
		super(myagent);
		this.list_agentNames=agentNames;
		
		
	}

	@Override
	public void action() {
		this.treasureFound = false;
		System.out.println(this.myAgent.getLocalName()+" : ExplorationBehaviour");
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		if (fsm.getAllMaps() == null) {
			fsm.initAllMaps();
		}
		
		
		
		//0) Retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		System.out.println(this.myAgent.getLocalName()+" : I'm on position "+myPosition.getLocationId());

		if (myPosition!=null){
			Couple<String, Boolean> lastMove = fsm.getLastMoveSuccess();
			String nextNodeId=null;
			if (lastMove != null && !lastMove.getRight()) {
				Debug.warning(this.myAgent.getLocalName()+" : Last move failed, doing it again - going to "+lastMove.getLeft()+" from "+myPosition.getLocationId());
				nextNodeId = lastMove.getLeft();
			} else {
				//List of observable from the agent's current position
				List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
	
				/**
				 * Just added here to let you see what the agent is doing, otherwise he will be too quick
				 */
				try {
					this.myAgent.doWait(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
	
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
	
				// If the agent found a treasure on his location, he adds it to the list of located treasures
				// That list will be used by agent when they need to go to a specific treasure
				List<Couple<Observation, String>> tresor = TreasureUtils.getTreasureFromLocation(lobs, myPosition);
				//System.out.println(this.myAgent.getLocalName()+" : à ma position se trouve "+tresor);
				if (!tresor.isEmpty() && !TreasureUtils.treasureContainsNoRessources(tresor)) {
					// If the agent is not a silo, enable CollectBehaviour
					System.out.println(this.myAgent.getLocalName()+" : trésor repéré dans explo "+tresor+ " à la position "+myPosition.getLocationId());
					fsm.getRecordedTreasures().put(myPosition.getLocationId(), tresor);
					this.treasureFound = true;
					return;
				}
				
	
				//3) while openNodes is not empty, continues.
				//System.out.println(this.myAgent.getLocalName()+" : "+fsm.getMap(this.myAgent.getLocalName()).getOpenNodes());
				if (!fsm.getMap(this.myAgent.getLocalName()).hasOpenNode()){
					//Explo finished
					finished=true;
					fsm.setExploFinished(true);
					//System.out.println(this.myAgent.getLocalName()+" - Exploration successfully done, behaviour removed.");
				}else{
					//4) select next move.
					//4.1 If there exist one open node directly reachable, go for it,
					//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
					if (nextNodeId==null){
						// Calculation of path to shortest open node

						List<String> path = fsm.getCurrentPath();
						if (path.isEmpty()) {
							path = fsm.getMap(this.myAgent.getLocalName()).getShortestPathToClosestOpenNode(myPosition.getLocationId());
						}
						System.out.println(this.myAgent.getLocalName()+" : path restant - "+path);
						nextNodeId=path.remove(0);
						fsm.setCurrentPath(path);
						
						//System.out.println("     - Destination : "+path.getLast());
						
						//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
					}else {
						//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
					}
				}
				//System.out.println("     - Next Node : "+nextNodeId);
				fsm.setLastMoveSuccess(new Couple<>(nextNodeId, ((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId))));
			}
		}
	}

	
	@Override
	public int onEnd() {
		if (finished) return 8; 			// MOVE_TO_TREASURE
		if (treasureFound) return 9;//9;	// COLLECT
		return 0; 							// MESSAGE
	}	
}
