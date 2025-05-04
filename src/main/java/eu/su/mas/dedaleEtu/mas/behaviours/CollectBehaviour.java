package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.projectAgents.ExploreCoopAgent;
import jade.core.behaviours.OneShotBehaviour;

public class CollectBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = 8567689796788661L;
	
	public CollectBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}
	
	private static boolean isUnlocked(List<Couple<Observation, String>> tresor) {
		for (Couple<Observation, String> o:tresor) {
			switch (o.getLeft()) {
			case LOCKSTATUS: return Integer.parseInt(o.getRight()) == 1;
			default: break;
			}
		}
		return false;
	}
	
	private static boolean enoughSpace(List<Couple<Observation, String>> tresor, ExploreCoopAgent agent) {
		for (Couple<Observation, String> o:tresor) {
			switch (o.getLeft()) {
			case DIAMOND: return agent.getBackPackFreeSpaceFor(Observation.DIAMOND) >= Integer.parseInt(o.getRight());
			case GOLD: return agent.getBackPackFreeSpaceFor(Observation.GOLD) >= Integer.parseInt(o.getRight());
			default: break;
			}
		}
		return false;
	}

	public void action() {
		// WIP
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		System.out.println(this.myAgent.getLocalName()+" : collect");
		
		List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
		List<Couple<Observation,String>> lObservations= lobs.get(0).getRight();
		
		/*if (isUnlocked(lObservations)) {
			if ((AbstractDedaleAgent) this.myAgent).openLock(null))
			if (enoughSpace(lObservations, (ExploreCoopAgent) this.myAgent)) {
				
			}
		}*/
		
		boolean b = false;
		for(Couple<Observation,String> o:lObservations){
			switch (o.getLeft()) {
			case DIAMOND:case GOLD:
				
				System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
				System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
				System.out.println(this.myAgent.getLocalName()+" - I try to open the safe: "+((AbstractDedaleAgent) this.myAgent).openLock(Observation.GOLD));
				System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
				int amount = ((AbstractDedaleAgent) this.myAgent).pick();
				System.out.println(this.myAgent.getLocalName()+" - The agent grabbed :"+amount);
				System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
				b=amount>0;
				break;
			default:
				break;
			}
		}
		
		//If the agent picked (part of) the treasure
		if (b){
			List<Couple<Location,List<Couple<Observation,String>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			System.out.println("State of the observations after picking "+lobs2);
			fsm.getRecordedTreasures().put(lobs2.get(0).getLeft().getLocationId(), lobs2.get(0).getRight());
		} else {
			//Random move from the current position
			Random r= new Random();
			int moveId=1+r.nextInt(lobs.size()-1);//removing the current position from the list of target to accelerate the tests, but not necessary as to stay is an action	
		}
	}
	
	@Override
	public int onEnd() {
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		if (fsm.hasLearnedSiloPosition()) return 10; 	// MOVE_TO_SILO
		return 15;										// EXPLO
	}	

}
