package eu.su.mas.dedaleEtu.mas.behaviours;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.NoSuchElementException;
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
		
		Observation myTreasureType = ((AbstractDedaleAgent) this.myAgent).getMyTreasureType();
		
		HelpNeededForTreasure hnft = new HelpNeededForTreasure(lobs.get(0).getLeft().getLocationId(), myTreasureType);
		
		Observation treasureType = TreasureUtils.treasureType(tresor);
		System.out.println(this.myAgent.getLocalName()+" : I try to collect "+tresor+" at position "+myPosition.getLocationId());
		
		boolean sameTypes = treasureType.equals(myTreasureType);
		if (sameTypes) {
			hnft.setMyFreeSpace(((ExploreCoopAgent) this.myAgent).getBackPackFreeSpaceFor(treasureType));
		}
		
		// Search for agent with the adequate type
		hnft.setTreasureType(treasureType);
		
		
		boolean openLockSuccess = ((AbstractDedaleAgent) this.myAgent).openLock(myTreasureType);
		if (!openLockSuccess) {
			// Search for more lockpicking power
			int lockpickingAsked = TreasureUtils.getTreasureLockPicking(tresor);
			int myLockpicking = ((ExploreCoopAgent )this.myAgent).getExpertiseIn(Observation.LOCKPICKING);
			hnft.setLockpicking(lockpickingAsked - myLockpicking);
			

			int strengthAsked = TreasureUtils.getTreasureStrength(tresor);
			int myStrength = ((ExploreCoopAgent) this.myAgent).getExpertiseIn(Observation.STRENGH);
			hnft.setStrength(strengthAsked-myStrength);
			
		}
		
		int amount = ((AbstractDedaleAgent) this.myAgent).pick();
		if (amount == 0) { // The agent couldn't pick up the treasure
			fsm.setHelpNeeded(hnft);
		} else { // The agent did pick up some of the treasure
			List<Couple<Observation,String>> tresor_post = ((AbstractDedaleAgent)this.myAgent).observe().get(0).getRight();
			if (TreasureUtils.treasureContainsNoRessources(tresor_post)) {
				fsm.removeFromRecordedTreasuresClock(myPosition.getLocationId());
			} else {
				fsm.putInRecordedTreasuresClock(myPosition.getLocationId(), tresor);
			}
		}
		
		String nextNodeId = null;
		List<String> path = fsm.getCurrentPath();
		System.out.println(this.myAgent.getLocalName()+" : I have "+amount+" and I know where the silo is ("+fsm.hasLearnedSiloPosition()+")");
		if (amount > 0 && fsm.hasLearnedSiloPosition()) {
			String destination = fsm.getSiloDestinationClock().getRight();
			path = fsm.getMap(this.myAgent.getLocalName()).getShortestPath(myPosition.getLocationId(), destination);
			fsm.setGoingToSilo(true);
			System.out.println(this.myAgent.getLocalName()+" : calculated path to silo position/destination");
			
		} else if (!fsm.isExploFinished()){
			if (path.isEmpty()) {
				try {
					path = fsm.getMap(this.myAgent.getLocalName()).getShortestPathToClosestOpenNode(myPosition.getLocationId());
				} catch (NoSuchElementException e) {
					e.printStackTrace();
					Debug.warning("openNodes : "+fsm.getMap(this.myAgent.getLocalName()).getOpenNodes());
				}

				System.out.println(this.myAgent.getLocalName()+" : calculated path to closest open node");
			}
		}
		
		
		System.out.println(this.myAgent.getLocalName()+" : path restant - "+path);
		
		if (!path.isEmpty()) {
			nextNodeId = path.remove(0);
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
	
	@Override
	public int onEnd() {
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		if (fsm.getHelpNeeded()!=null || !fsm.hasLearnedSiloPosition()) return 15;		// EXPLO
		return 10; 					// MESS then MOVE_TO_SILO
	}	

}
