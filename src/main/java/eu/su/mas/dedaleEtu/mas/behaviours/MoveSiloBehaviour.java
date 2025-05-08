package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;

public class MoveSiloBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = -7287384974581665282L;
	
	private boolean neighbor = false;
	private List<String> list_agentNames;
	
	public MoveSiloBehaviour(final AbstractDedaleAgent myagent, List<String> agentNames) {
		super(myagent);
		this.list_agentNames=agentNames;
		
		
	}

	@Override
	public void action() {
		FSMSiloBehaviour fsm = ((FSMSiloBehaviour) getParent());
		List<String> path = fsm.getCurrentPath();
		if (path == null || path.isEmpty()) {
			fsm.setStayingPut(true);
			return;
		}
		
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		if (myPosition!=null) {
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
					return;
				}
				else { //Blocked by the Wumpus
					Debug.warning(this.myAgent.getLocalName()+" : Last move failed because blocked by the Wumpus, doing it again - going to "+lastMove.getLeft()+" from "+myPosition.getLocationId());
					nextNodeId = lastMove.getLeft();
				}
			}
			else {
				System.out.println(this.myAgent.getLocalName()+" : path restant - "+path);
				nextNodeId=path.remove(0);
				fsm.setCurrentPath(path);
				
				// If the agent is the silo, update his clock with his destination
				
				Couple<Integer, String> currentDest = fsm.getSiloDestinationClock();
				String destination = path.isEmpty()? nextNodeId : path.getLast();
				fsm.updateSiloDestinationClock(new Couple<>(currentDest.getLeft()+1, destination));
				
			}
			fsm.setLastMoveSuccess(new Couple<>(nextNodeId, ((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId))));
		}
	}
	
	@Override
	public int onEnd() {
		if(neighbor) return 11;
		return 0;
	}

}
