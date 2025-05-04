package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.Agent;
import eu.su.mas.dedaleEtu.mas.agents.projectAgents.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javafx.util.Pair;

public class MoveToSiloBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496788661L;

	private List<String> list_agentNames;
	private boolean attainedDestination = false;
	

/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public MoveToSiloBehaviour(final AbstractDedaleAgent myagent, List<String> agentNames) {
		super(myagent);
		this.list_agentNames=agentNames;
		
		
	}

	// Resembles ExplorationBehaviour, but focused on reaching silo destination
	@Override
	public void action() {
		//System.out.println(this.myAgent.getLocalName()+" : ExplorationBehaviour");
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		if (fsm.getAllMaps() == null) {
			fsm.initAllMaps();
		}
		
		
		
		//0) Retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			String currentNode = myPosition.getLocationId();
			for(String i : this.list_agentNames) {
				fsm.addNodeToMap(currentNode, i);
			}
			
			

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNodeId=null;
			
			Iterator<Couple<Location, List<Couple<Observation, String>>>> iter=lobs.iterator();
			List<String> nodesWithTreasures = new ArrayList<String>();
			while(iter.hasNext()){
				Couple <Location, List<Couple<Observation, String>>> voisin = iter.next();
				Location accessibleNode=voisin.getLeft();
				for(String i : list_agentNames) {
					boolean isNewNode=fsm.addNewNodeToMap(accessibleNode.getLocationId(), i);
					//boolean isOpen=fsm.getMap(this.myAgent.getLocalName()).getOpenNodes().contains(accessibleNode.getLocationId());
					//the node may exist, but not necessarily the edge
					//System.out.println("Checking node: " + accessibleNode.getLocationId() 
	                   //+ " | isNewNode=" + isNewNode 
	                   //+ " | isOpen=" + isOpen);

					if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
						fsm.addEdgeToMap(myPosition.getLocationId(), accessibleNode.getLocationId(), i);
					}
				}
				List<Couple<Observation, String>> tresors = voisin.getRight();
				if (!tresors.isEmpty()) {
					//System.out.println("Trésors du voisin : "+tresors);
					nodesWithTreasures.add(accessibleNode.getLocationId());
				}
				
			}
			/*if (!nodesWithTreasures.isEmpty()) {
				nextNodeId = nodesWithTreasures.getFirst();
				this.treasureNearby = true;
			}*/
			
			// Calculate path to silo destination
			List<String> path = fsm.getCurrentPath();
			if (path.isEmpty()) {
				if (!fsm.isGoingToSilo()) {
					String destination = fsm.getSiloDestinationClock().getRight();
					path = fsm.getMap(this.myAgent.getLocalName()).getShortestPath(myPosition.getLocationId(), destination);
					fsm.setGoingToSilo(true);
				} else {
					this.attainedDestination = true;
					fsm.setGoingToSilo(false);
				}
			}
			System.out.println(this.myAgent.getLocalName()+" : path restant - "+path);
			nextNodeId=path.remove(0);
			fsm.setCurrentPath(path);
			
			//System.out.println("     - Destination : "+path.getLast());
			
			//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
			
			//System.out.println("     - Next Node : "+nextNodeId);
			((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId));

		}
	}
	
	@Override
	public int onEnd() {
		if (this.attainedDestination) return 11;
		return 12;
	}	
}
