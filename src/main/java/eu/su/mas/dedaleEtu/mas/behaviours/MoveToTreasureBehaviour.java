package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;

public class MoveToTreasureBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 5921098062301802226L;

	private List<String> list_agentNames;
	private boolean attainedDestination = false;
	private boolean leader;
	

/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public MoveToTreasureBehaviour(final AbstractDedaleAgent myagent, List<String> agentNames) {
		super(myagent);
		this.list_agentNames=agentNames;
		
		
	}

	// Resembles ExplorationBehaviour, but focused on reaching treasure destination
	@Override
	public void action() {
		System.out.println(this.myAgent.getLocalName()+" : MoveToSiloBehaviour");
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		

		this.leader = fsm.isLeader();
		
		
		
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
			} else {
			
				//List of observable from the agent's current position
				List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		
				
				// Calculate path to silo destination
				List<String> path = fsm.getCurrentPath();
				if (path.isEmpty()) {
					if (!fsm.isGoingToTreasure()) {
						// Select the closest attainable treasure
						path = fsm.getMap(this.myAgent.getLocalName()).getShortestPathToClosestTreasure(myPosition.getLocationId(), fsm.getRecordedTreasures());
						fsm.setGoingToTreasure(true);
					} else {
						this.attainedDestination = true;
						fsm.setGoingToTreasure(false);
						fsm.setLeader(false);
						
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
		if (this.attainedDestination) {
			if (this.leader) return 17;		// COLLECT
			return 18;						// EXPLO
		}
		return 19;							// MESS
	}

}
