package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MessageBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = 1354754375697897L;

    private int transition = 0;
	
	
	public MessageBehaviour(AbstractDedaleAgent a) {
		super(a);
	}


	@Override
	public void action() {
		//System.out.println(this.myAgent.getLocalName()+" : MessageBehaviour");
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		
		if (fsm.hasSentPing()) {
			this.myAgent.doWait(500);
		}
		
		MessageTemplate pongTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("PONG"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage pongReceived=this.myAgent.receive(pongTemplate);
		if (pongReceived!=null) {
			String pongSender = pongReceived.getSender().getLocalName();
			if (!pongSender.equals(this.myAgent.getLocalName())) {
				this.transition = 4; // SHARE
				fsm.setPingSent(false);
				fsm.setCurrentInterlocutor(pongSender);
				return;
			}
			
		}

		MessageTemplate pingTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("PING"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage pingReceived=this.myAgent.receive(pingTemplate);
		if (pingReceived!=null) {
			String pingSender = pingReceived.getSender().getLocalName();
			if (!pingSender.equals(this.myAgent.getLocalName())) {
				this.transition = 3; // PONG
				fsm.setPingSent(false);
				fsm.setCurrentInterlocutor(pingReceived.getSender().getLocalName());
				return;
			}
		}
		
		if (fsm.hasSentPing()) {
			if (fsm.isGoingToTreasure()) {
				this.transition = 20;	// MOVE_TO_TREASURE
			} else if (fsm.isGoingToSilo()) {
				this.transition = 13;	// MOVE_TO_SILO
			} else this.transition = 1; // EXPLO
			
			System.out.println(this.myAgent.getLocalName()+" : je suis dans message, transition :"+this.transition);
			fsm.setPingSent(false);
			return;
		}
		
		this.transition = 2; // PING
	}
		
	@Override
	public int onEnd() {
		return this.transition;
	}

}
