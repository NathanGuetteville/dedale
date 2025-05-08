package eu.su.mas.dedaleEtu.mas.behaviours;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.projectAgents.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.utils.HelpNeededForTreasure;
import eu.su.mas.dedaleEtu.mas.utils.TreasureUtils;
import jade.core.behaviours.OneShotBehaviour;

public class CollectBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = 8567689796788661L;
	
	public CollectBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}
	
	

	public void action() {
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		System.out.println(this.myAgent.getLocalName()+" : collect");
		

		Location myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition(); // Is my position always the first element of observe() ?
		List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
		List<Couple<Observation,String>> tresor= TreasureUtils.getTreasureFromLocation(lobs, myPosition);
		
		
		HelpNeededForTreasure hnft = new HelpNeededForTreasure(lobs.get(0).getLeft().getLocationId());
		
		Observation treasureType = TreasureUtils.treasureType(tresor);
		System.out.println(this.myAgent.getLocalName()+" : I try to collect "+tresor+" at position "+myPosition.getLocationId());
		Observation myTreasureType = ((AbstractDedaleAgent) this.myAgent).getMyTreasureType();
		
		boolean sameTypes = treasureType.equals(myTreasureType);
		if (!sameTypes) {
			// Search for agent with the adequate type
			//hnft.activate();
			hnft.setTreasureType(treasureType);
		}
		
		boolean openLockSuccess = ((AbstractDedaleAgent) this.myAgent).openLock(myTreasureType);
		if (!openLockSuccess) {
			// Search for more lockpicking power
			//hnft.activate();
			hnft.setLockpicking(TreasureUtils.getTreasureLockPicking(tresor));
		}
		
		int amount = ((AbstractDedaleAgent) this.myAgent).pick();
		if (amount == 0) { // The agent couldn't pick up the treasure
			if (sameTypes && openLockSuccess) {
				// Not enough Strength, look for more
				//hnft.activate();
				hnft.setStrength(TreasureUtils.getTreasureStrength(tresor));
			}
			fsm.setHelpNeeded(hnft);
		} else { // The agent did pick up some of the treasure
			List<Couple<Observation,String>> tresor_post = ((AbstractDedaleAgent)this.myAgent).observe().get(0).getRight();
			if (TreasureUtils.treasureContainsNoRessources(tresor_post)) { 	// He took everything
				fsm.getRecordedTreasures().remove(myPosition.getLocationId());
			} else { 														// He took part of it
				fsm.getRecordedTreasures().put(myPosition.getLocationId(), tresor_post);
			}
		}
		
		String nextNodeId = null;
		List<String> path = fsm.getCurrentPath();
		if (path.isEmpty()) {
			if (amount == 0 || !fsm.hasLearnedSiloPosition()) {
				path = fsm.getMap(this.myAgent.getLocalName()).getShortestPathToClosestOpenNode(myPosition.getLocationId());
				System.out.println(this.myAgent.getLocalName()+" : calculated path to closest open node");
			} else {
				String destination = fsm.getSiloDestinationClock().getRight();
				path = fsm.getMap(this.myAgent.getLocalName()).getShortestPath(myPosition.getLocationId(), destination);
				fsm.setGoingToSilo(true);
				System.out.println(this.myAgent.getLocalName()+" : calculated path to silo position/destination");
			}
		}
		System.out.println(this.myAgent.getLocalName()+" : path restant - "+path);
		nextNodeId=path.remove(0);
		fsm.setCurrentPath(path);
		
		fsm.setLastMoveSuccess(new Couple<>(nextNodeId, ((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId))));
		
	}
	
	@Override
	public int onEnd() {
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		if (fsm.getHelpNeeded()!=null || !fsm.hasLearnedSiloPosition()) return 15;		// EXPLO
		System.out.println(this.myAgent.getLocalName()+" : I collected something, let's go to the silo");
		return 10; 					// MESS then MOVE_TO_SILO
	}	

}
