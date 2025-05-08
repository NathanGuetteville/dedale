package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MessageSiloBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = 1345754375697897L;
	
    
    private int transition = 0;
	
	
	public MessageSiloBehaviour(AbstractDedaleAgent a) {
		super(a);
	}

	@Override
	public void action() {
		System.out.println(this.myAgent.getLocalName()+" : MessageBehaviour");
		FSMSiloBehaviour fsm = ((FSMSiloBehaviour) getParent());
		
		if (fsm.getMap() == null) {
			fsm.setMap(new MapRepresentation(true));
			Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			if (myPosition!=null){
				List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
				//1) remove the current node from openlist and add it to closedNodes.
				String currentNode = myPosition.getLocationId();
				fsm.addNodeToMap(currentNode);
				
				//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
				Iterator<Couple<Location, List<Couple<Observation, String>>>> iter=lobs.iterator();
				while(iter.hasNext()){
					Couple <Location, List<Couple<Observation, String>>> node = iter.next();
					Location accessibleNode=node.getLeft();
						fsm.addNewNodeToMap(accessibleNode.getLocationId());
	
						if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
							fsm.addEdgeToMap(myPosition.getLocationId(), accessibleNode.getLocationId());
						} 
				}
			}
		}
		
		if (fsm.hasSentPing()) {
			this.myAgent.doWait(500);
		}
		
		System.out.println(this.myAgent.getLocalName()+" : trying to receive pong");
		MessageTemplate pongTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("PONG"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage pongReceived=this.myAgent.receive(pongTemplate);
		if (pongReceived!=null) {
			System.out.println(this.myAgent.getLocalName()+" : pong successfully received");
			String pongSender = pongReceived.getSender().getLocalName();
			if (!pongSender.equals(this.myAgent.getLocalName())) {
				this.transition = 4; // SHARE
				fsm.setPingSent(false);
				fsm.setCurrentInterlocutor(pongSender);
				return;
			}
			
		}

		System.out.println(this.myAgent.getLocalName()+" : trying to receive ping");
		MessageTemplate pingTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("PING"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage pingReceived=this.myAgent.receive(pingTemplate);
		if (pingReceived!=null) {
			System.out.println(this.myAgent.getLocalName()+" : ping successfully received");
			String pingSender = pingReceived.getSender().getLocalName();
			if (!pingSender.equals(this.myAgent.getLocalName())) {
				this.transition = 3; // PONG
				fsm.setPingSent(false);
				fsm.setCurrentInterlocutor(pingReceived.getSender().getLocalName());
				return;
			}
		}
		
		if (fsm.hasSentPing()) {
			this.transition = fsm.isStayingPut()? 10 : 1; // MESS or MOVE_SILO
			fsm.setPingSent(false);
			return;
		}
		
		this.transition = 2; // PING
		
		this.myAgent.doWait(1000);
	}
		
	@Override
	public int onEnd() {
		return this.transition;
	}

}
