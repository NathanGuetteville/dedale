package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.projectAgents.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShareSiloBehaviour extends OneShotBehaviour{

	private String receiver = null;

	public ShareSiloBehaviour(AbstractDedaleAgent a) {
		super(a);
	}

	private static final long serialVersionUID = -568863390879327961L;

	@Override
	public void action() {
		System.out.println(this.myAgent.getLocalName()+" : ShareMapsBehaviour");
		FSMSiloBehaviour fsm = ((FSMSiloBehaviour) getParent());
		receiver = fsm.getCurrentInterlocutor();
		if (receiver == null) return;
		

		//System.out.println(this.myAgent.getLocalName()+" vs "+receiver);
		
		this.myAgent.doWait(100);

		// Before exchanging maps, try exchange known silo position
		ACLMessage pos_msg = new ACLMessage(ACLMessage.INFORM);
		pos_msg.setProtocol("SHARE-POS");
		pos_msg.setSender(this.myAgent.getAID());
		pos_msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
		
		Couple<Integer, String> pair = fsm.getSiloDestinationClock();
		try {
			pos_msg.setContentObject(pair);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		MessageTemplate pos_template =MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-POS"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage posReceived = this.myAgent.receive(pos_template);
		
		if (posReceived != null) {
			try {
				fsm.updateSiloDestinationClock((Couple<Integer, String> ) posReceived.getContentObject());

			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
					
		// Now, exchange maps
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
			
		SerializableSimpleGraph<String, MapAttribute> sg=fsm.getMap().getSerializableGraph();
		try {					
			msg.setContentObject(sg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		if (msgReceived!=null) {
			//System.out.println(this.myAgent.getLocalName()+" : Map Received");
			SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
			try {
				sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fsm.getMap().mergeMap(sgreceived);
			fsm.setCurrentInterlocutor(null);
			
			String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();
			String destination = fsm.getMap().getNodeWithMaxDegree();
			if (!destination.equals(myPosition)) {
				fsm.setStayingPut(false);
				fsm.setCurrentPath(fsm.getMap().getShortestPath(myPosition, destination));
				Couple<Integer, String> currentDest = fsm.getSiloDestinationClock();
				fsm.updateSiloDestinationClock(new Couple<>(currentDest.getLeft()+1, destination));

			}
		}
		
	}
	
	@Override
	public int onEnd() {
		FSMSiloBehaviour fsm = ((FSMSiloBehaviour) getParent());
		if (fsm.isStayingPut()) return 9; 	// MOVE_SILO
		return 7; 							// MESS
	}

}
