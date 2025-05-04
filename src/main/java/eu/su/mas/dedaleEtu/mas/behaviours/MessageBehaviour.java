package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MessageBehaviour extends OneShotBehaviour {
	
	private int transition = 0;
	
	public MessageBehaviour(AbstractDedaleAgent a) {
		super(a);
	}

	@Override
	public void action() {
		//System.out.println(this.myAgent.getLocalName()+" : MessageBehaviour");
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		if (fsm.hasSentPing()) {
			this.myAgent.doWait(100);
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
			this.transition = fsm.isGoingToSilo() ? 13 : 1; // MOVE_TO_SILO or EXPLO
			fsm.setPingSent(false);
			return;
		}
		
		this.transition = 2; // PING
		return;
		
		
	}
		
	@Override
	public int onEnd() {
		return this.transition;
	}

}
