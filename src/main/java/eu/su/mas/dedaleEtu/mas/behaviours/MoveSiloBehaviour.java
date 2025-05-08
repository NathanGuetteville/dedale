package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;

public class MoveSiloBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = -7287384974581665282L;

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
			} else {
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
		return 0;
	}

}
