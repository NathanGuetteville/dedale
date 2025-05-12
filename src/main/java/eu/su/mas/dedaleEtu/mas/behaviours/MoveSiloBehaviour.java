package eu.su.mas.dedaleEtu.mas.behaviours;

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
		this.neighbor = false;
		System.out.println(this.myAgent.getLocalName()+" : MoveSiloBehaviour");
		FSMSiloBehaviour fsm = ((FSMSiloBehaviour) getParent());
		List<String> path = fsm.getCurrentPath();
		if (path == null || path.isEmpty()) {
			fsm.setStayingPut(true);
			return;
		}
		
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
		
		if (myPosition!=null) {
			Couple<String, Boolean> lastMove = fsm.getLastMoveSuccess();
			String nextNodeId=null;
			if (lastMove != null && !lastMove.getRight()) {
				nextNodeId = lastMove.getLeft();
				if (fsm.getBlocked() == false) {
					Debug.warning(this.myAgent.getLocalName()+" : Last move failed, doing it again - going to "+lastMove.getLeft()+" from "+myPosition.getLocationId());
					fsm.setBlocked(true);
				} else {
					//The two last moves failed. Try to communicate with neighbor.
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
											Random r= new Random();
											nextNodeId = fsm.getLastMoveSuccess().getLeft();
											while (nextNodeId.equals(fsm.getLastMoveSuccess().getLeft())) {
												int newDestId=r.nextInt(lobs.size()-1);
												nextNodeId = lobs.get(newDestId).getLeft().getLocationId();
											}
											
											fsm.getCurrentPath().clear();
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
						return;
					}
					else { //Blocked by the Wumpus
						Debug.warning(this.myAgent.getLocalName()+" : Unexpected blocking - going to "+lastMove.getLeft()+" from "+myPosition.getLocationId());
					}
				}
			}
			else {
				System.out.println(this.myAgent.getLocalName()+" : path restant - "+path);
				nextNodeId=path.remove(0);
				fsm.setCurrentPath(path);
				
				// If the agent found a treasure on his location, he adds it to the list of located treasures
				// That list will be used by agent when they need to go to a specific treasure
				List<Couple<Observation, String>> tresor = TreasureUtils.getTreasureFromLocation(lobs, myPosition);
				//System.out.println(this.myAgent.getLocalName()+" : à ma position se trouve "+tresor);
				if (!tresor.isEmpty() && !TreasureUtils.treasureContainsNoRessources(tresor)) {
					// If the agent is not a silo, enable CollectBehaviour
					//System.out.println(this.myAgent.getLocalName()+" : trésor repéré dans explo "+tresor+ " à la position "+myPosition.getLocationId());
					fsm.putInRecordedTreasuresClock(myPosition.getLocationId(), tresor);
				}
				
				// The agent is the silo, update his clock with his destination
				
				Couple<Integer, String> currentDest = fsm.getSiloDestinationClock();
				String destination = path.isEmpty()? nextNodeId : path.getLast();
				fsm.updateSiloDestinationClock(new Couple<>(currentDest.getLeft()+1, destination));
				
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
		if(neighbor) return 11;
		return 0;
	}

}
