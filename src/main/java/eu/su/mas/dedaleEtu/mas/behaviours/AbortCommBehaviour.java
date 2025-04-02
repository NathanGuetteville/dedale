package eu.su.mas.dedaleEtu.mas.behaviours;
import jade.core.behaviours.WakerBehaviour;
import jade.core.Agent;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import java.util.HashMap;
import java.util.List;

public class AbortCommBehaviour extends WakerBehaviour {

	private HashMap<String, MapRepresentation> allMaps;
	private List<String> agent_names;
	
	public AbortCommBehaviour(final AbstractDedaleAgent a, long ms, List<String> agent_names,
								HashMap<String, MapRepresentation> allMaps) {
		super(a, ms);
		this.agent_names = agent_names;
		this.allMaps = allMaps;
	}
	
	public void onWake() {
		System.out.println(this.myAgent.getLocalName()+" : Finie la récré !");
		this.myAgent.addBehaviour(new ExplorationBehaviour((AbstractDedaleAgent)this.myAgent, this.allMaps, agent_names, true));
		((ParallelComm) getParent()).setStop();
	}	
}
