package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class PongBehaviour extends OneShotBehaviour{
	
	private static final long serialVersionUID = -568863346649327961L;
	
	private String receiver = null;
	

	
	public PongBehaviour(AbstractDedaleAgent a) {
		super(a);
	}
	
	
	@Override
	public void action() {
		System.out.println(this.myAgent.getLocalName()+" : PongBehaviour");
		receiver = ((MyFSMBehaviour) getParent()).getCurrentInterlocutor();
		if (receiver == null || receiver.equals(this.myAgent.getLocalName())) return;
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("PONG");
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
	}
	
	@Override
	public int onEnd() {
		return 6;
	}
}
