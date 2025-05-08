package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class PingBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = -568933390879327961L;

	private List<String> agentNames;

	
	public PingBehaviour(AbstractDedaleAgent a, List<String> agentNames) {
		super(a);
		this.agentNames = agentNames;
	}
	
	
	@Override
	public void action() {
		System.out.println(this.myAgent.getLocalName()+" : PingBehaviour");
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("PING");
		msg.setSender(this.myAgent.getAID());
		for (String receiver : this.agentNames) {
			if (!receiver.equals(this.myAgent.getLocalName()))
				msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
		}
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		((FSMCoopBehaviour) getParent()).setPingSent(true);
	}
	
	@Override
	public int onEnd() {
		return 5;
	}

}
