package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.projectAgents.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.utils.TreasureUtils;
import jade.core.behaviours.OneShotBehaviour;

public class MoveToTreasureBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 5921098062301802226L;
	
	private static int UNBLOCK_TO_TREASURE = 26;

	private List<String> list_agentNames;
	private boolean attainedDestination = false;
	private boolean leader;
	private boolean neighbor = false;
	

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
		this.neighbor = false;
		System.out.println(this.myAgent.getLocalName()+" : MoveToTreasureBehaviour");
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		

		this.leader = fsm.isLeader();
		
		((AbstractDedaleAgent) this.myAgent).emptyMyBackPack("Tank");
		
		if (fsm.hasLearnedSiloPosition() && ((ExploreCoopAgent) this.myAgent).backpackIsNotEmpty()) {
			return;
		}
		
		//0) Retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

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
						fsm.setTransitionBackFromUnblock(UNBLOCK_TO_TREASURE);
						return;
					}
					else {
						Debug.warning(this.myAgent.getLocalName()+" : Unexpected blocking - going to "+lastMove.getLeft()+" from "+myPosition.getLocationId());
					}
				}
			} else {
			
				//List of observable from the agent's current position
				List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
				
				// If the agent found a treasure on his location, he adds it to the list of located treasures
				// That list will be used by agent when they need to go to a specific treasure
				List<Couple<Observation, String>> tresor = TreasureUtils.getTreasureFromLocation(lobs, myPosition);
				//System.out.println(this.myAgent.getLocalName()+" : à ma position se trouve "+tresor);
				if (!tresor.isEmpty() && !TreasureUtils.treasureContainsNoRessources(tresor)) {
					// If the agent is not a silo, enable CollectBehaviour
					//System.out.println(this.myAgent.getLocalName()+" : trésor repéré dans explo "+tresor+ " à la position "+myPosition.getLocationId());
					fsm.putInRecordedTreasuresClock(myPosition.getLocationId(), tresor);
				}
				
				if (fsm.isExploFinished() && fsm.recordedTreasuresClockIsEmpty() && !((ExploreCoopAgent) this.myAgent).backpackIsNotEmpty()) {
					fsm.setAllFinished(true);
					return;
				}
		
				
				// Calculate path to treasure destination
				List<String> path = fsm.getCurrentPath();
				if (path.isEmpty()) {
					if (fsm.isGoingToTreasure()) {
						this.attainedDestination = true;
						fsm.setGoingToTreasure(false);
						fsm.setLeader(false);
						
					}
					// Select the closest attainable treasure
					HashMap<String, List<Couple<Observation, String>>> filteredTreasures = 
						fsm.getRecordedTreasuresClock().getRight().entrySet().stream()
				        .filter(entry -> TreasureUtils.treasureType(entry.getValue()) == ((AbstractDedaleAgent) this.myAgent).getMyTreasureType())
				        .filter(entry -> !entry.getKey().equals(myPosition.getLocationId()))
				        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
				                                  (v1, v2) -> v1, HashMap::new));
					if (filteredTreasures.isEmpty()) {
						filteredTreasures = fsm.getRecordedTreasuresClock().getRight().entrySet().stream()
						        .filter(entry -> !entry.getKey().equals(myPosition.getLocationId()))
						        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
						                                  (v1, v2) -> v1, HashMap::new));
					}
					System.out.println(this.myAgent.getLocalName()+" : trésors filtrés : "+filteredTreasures);
					path = fsm.getMap(this.myAgent.getLocalName()).getShortestPathToClosestTreasure(myPosition.getLocationId(), filteredTreasures);
						
					fsm.setGoingToTreasure(true);
				}
				System.out.println(this.myAgent.getLocalName()+" : path restant - "+path);
				try {
					nextNodeId=path.remove(0);
				} catch (UnsupportedOperationException e) {
					Debug.warning(this.myAgent.getLocalName()+" : path vide, trésor repérés : "+fsm.getRecordedTreasuresClock().getRight());
					e.printStackTrace();
				} catch (IndexOutOfBoundsException e) {
					Debug.warning(this.myAgent.getLocalName()+" : path vide, trésor repérés : "+fsm.getRecordedTreasuresClock().getRight());
					e.printStackTrace();
				}
				
				
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
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		if (fsm.isAllFinished()) return 21; // END
		if (this.neighbor) return 25;		// UNBLOCK
		if (fsm.hasLearnedSiloPosition() && ((ExploreCoopAgent) this.myAgent).backpackIsNotEmpty())
			return 28;						// MOVE_TO_SILO
		if (this.attainedDestination) {
			if (this.leader) return 17;		// COLLECT
			return 18;						// EXPLO
		}
		return 19;							// MESS
	}

}
