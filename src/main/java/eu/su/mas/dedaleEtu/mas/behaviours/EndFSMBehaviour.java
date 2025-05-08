package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.behaviours.OneShotBehaviour;

public class EndFSMBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = -5673491450879327961L;
	
	@Override
	public void action() {
		System.out.println(this.myAgent.getLocalName()+" - Exploration successfully done, end FSM.");
		this.myAgent.doDelete();
	}

}
