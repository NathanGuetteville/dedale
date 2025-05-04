package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;


import java.util.HashMap;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.projectAgents.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShareMapsBehaviour extends OneShotBehaviour{

	private String receiver = null;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public ShareMapsBehaviour(AbstractDedaleAgent a) {
		super(a);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	@Override
	public void action() {
		//System.out.println(this.myAgent.getLocalName()+" : ShareMapsBehaviour");
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
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
				if (!fsm.isAgentSilo() && ((ExploreCoopAgent) this.myAgent).backpackIsNotEmpty()) {
					fsm.setLearnedSiloPosition(true);
				}
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
					
		// Now, exchange maps
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
			
		SerializableSimpleGraph<String, MapAttribute> sg=fsm.getMap(receiver).getSerializableGraph();
		try {					
			msg.setContentObject(sg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		fsm.resetMap(receiver);
		
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
			fsm.getMap(this.myAgent.getLocalName()).mergeMap(sgreceived);
			fsm.setCurrentInterlocutor(null);
		}
		
	}
	
	@Override
	public int onEnd() {
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		if (fsm.hasLearnedSiloPosition()) return 14; 	//MOVE_TO_SILO
		return 7; 										// EXPLO
	}

}
