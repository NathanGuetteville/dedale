package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;
public class MoveToSiloBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496788661L;

	private List<String> list_agentNames;
	private boolean attainedDestination = false;
	private boolean neighbor = false;
	

/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public MoveToSiloBehaviour(final AbstractDedaleAgent myagent, List<String> agentNames) {
		super(myagent);
		this.list_agentNames=agentNames;
		
		
	}

	// Resembles ExplorationBehaviour, but focused on reaching silo destination
	@Override
	public void action() {
		System.out.println(this.myAgent.getLocalName()+" : MoveToSiloBehaviour");
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		this.neighbor = false;
		if (fsm.getAllMaps() == null) {
			fsm.initAllMaps();
		}
		
		
		
		//0) Retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		/**
		 * Just added here to let you see what the agent is doing, otherwise he will be too quick
		 */
		try {
			this.myAgent.doWait(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (myPosition!=null){
			Couple<String, Boolean> lastMove = fsm.getLastMoveSuccess();
			String nextNodeId=null;
			if (lastMove != null && !lastMove.getRight()) {
				Debug.warning(this.myAgent.getLocalName()+" : Last move failed, doing it again - going to "+lastMove.getLeft()+" from "+myPosition.getLocationId());
				nextNodeId = lastMove.getLeft();
			} 
			else if(lastMove != null && !lastMove.getRight()) {
				//The two last moves failed. Try to communicate with neighbor.
				List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
				for(Couple<Location,List<Couple<Observation,String>>> loc : lobs) {
					if(loc.getLeft().getLocationId().equals(nextNodeId)) { //Checking at the destination coordinate
						for(Couple<Observation,String> obs : loc.getRight()) {
							for(String name : this.list_agentNames) { //Checking for an agent neighbor
								if(obs.getLeft().getName().equals(name)) {
									neighbor = true;
									fsm.setBlockingNeighbor(obs.getLeft().getName());
								}
							}
						}
					}
				}
				if(neighbor) { //Initiate communication with neighbor
					fsm.setBlockedFromExplo(false);
					return;
				}
				else { //Blocked by the Wumpus
					Debug.warning(this.myAgent.getLocalName()+" : Last move failed because blocked by the Wumpus, doing it again - going to "+lastMove.getLeft()+" from "+myPosition.getLocationId());
					nextNodeId = lastMove.getLeft();
				}
			}
			else {
			
				//List of observable from the agent's current position
				List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		
				
				// Calculate path to silo destination
				List<String> path = fsm.getCurrentPath();
				if (path.isEmpty()) {
					if (!fsm.isGoingToSilo()) {
						String destination = fsm.getSiloDestinationClock().getRight();
						path = fsm.getMap(this.myAgent.getLocalName()).getShortestPath(myPosition.getLocationId(), destination);
						fsm.setGoingToSilo(true);
					} else {
						this.attainedDestination = true;
						fsm.setGoingToSilo(false);
						boolean emptied = false;
						for (String agent : this.list_agentNames) {
							emptied = emptied || ((AbstractDedaleAgent) this.myAgent).emptyMyBackPack(agent);
						}
						if (!emptied) {
							fsm.setLearnedSiloPosition(false);
							fsm.updateSiloDestinationClock(new Couple<>(0, ""));
						}
					}
				}
				System.out.println(this.myAgent.getLocalName()+" : path restant - "+path);
				nextNodeId=path.remove(0);
				fsm.setCurrentPath(path);
				
				//System.out.println("     - Destination : "+path.getLast());
				
				//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
			}
			
			//System.out.println("     - Next Node : "+nextNodeId);
			fsm.setLastMoveSuccess(new Couple<>(nextNodeId, ((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId))));

		}
	}
	
	@Override
	public int onEnd() {
		if (this.attainedDestination) return 11; 	// EXPLO
		return 12;									// MESS
	}	
}
