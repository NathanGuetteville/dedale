package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.projectAgents.ExploreCoopAgent;
import jade.core.AID;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class UnblockBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = 1354754475697867L;
	
	private int transition;
	
	public UnblockBehaviour(AbstractDedaleAgent a) {
		super(a);
	}


	@Override
	public void action() {
		System.out.println(this.myAgent.getLocalName()+" : UnblockBehaviour");
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		String receiver = fsm.getBlockingNeighbor();
		if(receiver == null) {
			this.transition = fsm.getTransitionBackFromUnblock();
			return;
		}
		
		//Sending priority and position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		int priority = fsm.getPriority();
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("UNBLOCK");
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
		Couple<Integer, Location> pair = new Couple<Integer, Location>(priority, myPosition);
		try {
			msg.setContentObject(pair);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
		this.myAgent.doWait(300);
		
		MessageTemplate blk_template =MessageTemplate.and(
				MessageTemplate.MatchProtocol("UNBLOCK"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage blkReceived = this.myAgent.receive(blk_template);
		
		if (blkReceived != null) {
			try {
				List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
				if (lobs.size() < 3) priority += 500; // If the agent is completely stuck, he gain priority for this specific interaction
				
				Couple<Integer, Location> content = (Couple<Integer,Location>)blkReceived.getContentObject();
				if(priority > content.getLeft()) {
					this.transition = fsm.getTransitionBackFromUnblock();
					return;
				}

				Random r= new Random();
				int moveId=1+r.nextInt(lobs.size()-1);
				while(lobs.get(moveId).getLeft() == content.getRight()) {
					moveId=1+r.nextInt(lobs.size()-1);
				}
				this.transition = fsm.getTransitionBackFromUnblock();
				fsm.getCurrentPath().clear();;
				System.out.println(this.myAgent.getLocalName()+" : path restant (débloquage, normalement vide) : "+fsm.getCurrentPath());
				
				
				/**
				 * Just added here to let you see what the agent is doing, otherwise he will be too quick
				 */
				try {
					this.myAgent.doWait(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				String nextNodeId = lobs.get(moveId).getLeft().getLocationId();
				fsm.setLastMoveSuccess(new Couple<>(nextNodeId, ((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId))));
				return;
				
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
		this.transition = fsm.getTransitionBackFromUnblock();
				
	}
		
	@Override
	public int onEnd() {
		return this.transition;
	}

}
