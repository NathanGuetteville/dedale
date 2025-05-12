package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.projectAgents.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.utils.TreasureUtils;
import jade.core.behaviours.OneShotBehaviour;
public class MoveToSiloBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496788661L;

	private static int UNBLOCK_TO_SILO = 24;
	
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
		this.neighbor = false;
		System.out.println(this.myAgent.getLocalName()+" : MoveToSiloBehaviour");
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		this.neighbor = false;
		if (fsm.getAllMaps() == null) {
			fsm.initAllMaps();
		}
		
		System.out.println(this.myAgent.getLocalName()+" : J'ai tendance à "+(fsm.getBlocked()? " " : "pas ")+ "bloquer");
		
		boolean luckilyEmptied = ((AbstractDedaleAgent) this.myAgent).emptyMyBackPack("Tank");
		if (luckilyEmptied) {
			this.attainedDestination = true;
			fsm.getCurrentPath().clear();
			fsm.setGoingToSilo(false);
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
													int newDestId=r.nextInt(lobs.size());
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
						fsm.setTransitionBackFromUnblock(UNBLOCK_TO_SILO);
						return;
					}
					else {
						Debug.warning(this.myAgent.getLocalName()+" : Unexpected blocking - going to "+lastMove.getLeft()+" from "+myPosition.getLocationId());
					}
				}
			}
			else {
			
				//List of observable from the agent's current position
				List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
				
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
		
				if (fsm.hasLearnedSiloPosition()) {
					// Calculate path to silo destination
					List<String> path = fsm.getCurrentPath();
					if (path.isEmpty()) {
						if (fsm.isGoingToSilo()) {
							System.out.println(this.myAgent.getLocalName()+" : arrrived at supposed tank position, trying to drop my ressources");
							this.attainedDestination = true;
							fsm.setGoingToSilo(false);
							boolean emptied = false;
							for(Couple<Location,List<Couple<Observation,String>>> loc : lobs) {
								if(loc.getLeft().getLocationId().equals(nextNodeId)) { //Checking at the destination coordinate
									for(Couple<Observation,String> obs : loc.getRight()) {
										switch (obs.getLeft()) {
											case AGENTNAME:
												emptied = emptied || ((AbstractDedaleAgent) this.myAgent).emptyMyBackPack(obs.getRight());
												break;
											default: break;
										}
									}
								}
							}
							if (!emptied) {
								fsm.setLearnedSiloPosition(false);
								fsm.updateSiloDestinationClock(new Couple<>(0, ""));
								return;
								
							}
						} else {
							String destination = fsm.getSiloDestinationClock().getRight();
							path = fsm.getMap(this.myAgent.getLocalName()).getShortestPath(myPosition.getLocationId(), destination);
							path.remove(path.size()-1);
							fsm.setGoingToSilo(true);
						}
					}
					System.out.println(this.myAgent.getLocalName()+" : path restant - "+path);
					nextNodeId=path.remove(0);
					fsm.setCurrentPath(path);
				} else {
					// Random Move, hoping to find information about the Silo
					Random r= new Random();
					int moveId=r.nextInt(lobs.size()-1);
					nextNodeId = lobs.get(moveId).getLeft().getLocationId();
				}
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
		if (this.neighbor) return 27;				// UNBLOCK
		if (this.attainedDestination) return 11; 	// EXPLO
		return 12;									// MESS
	}	
}
