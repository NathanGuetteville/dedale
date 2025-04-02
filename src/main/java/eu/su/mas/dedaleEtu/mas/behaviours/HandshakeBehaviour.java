package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

public class HandshakeBehaviour extends OneShotBehaviour{
	
	private static final long serialVersionUID = 8567689731466788661L;
	
	private HashMap<String, MapRepresentation> allMaps;
	private String receiver;
	
	public HandshakeBehaviour(AbstractDedaleAgent a, HashMap<String, MapRepresentation> allMaps, String receiver, List<String> agentNames) {
		super(a);
		this.allMaps = allMaps;
		this.receiver = receiver;
	}
	
	@Override
	public void action() {
		if (((ParallelComm) getParent()).onEnd()==1) return;
		// Handshake
		// Send handshake
		
		ACLMessage handshakeSend = new ACLMessage(ACLMessage.INFORM);
		handshakeSend.setProtocol("HANDSHAKE");
		handshakeSend.setSender(this.myAgent.getAID());
		handshakeSend.addReceiver(new AID(receiver, AID.ISLOCALNAME));
		((AbstractDedaleAgent) this.myAgent).sendMessage(handshakeSend);
		
		// Wait for feedback
		
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("HANDSHAKE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		ACLMessage msgReceived=null;
		msgReceived = this.myAgent.receive(msgTemplate);
		
		if (msgReceived != null) {
			AID idSender = msgReceived.getSender();
			String senderName = idSender.getLocalName();
			
			ShareMapsBehaviour smb = new ShareMapsBehaviour((AbstractDedaleAgent) this.myAgent, allMaps, senderName);
			this.myAgent.addBehaviour(smb);
		}
		
	
	}
}
