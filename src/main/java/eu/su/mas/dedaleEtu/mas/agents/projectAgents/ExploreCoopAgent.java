package eu.su.mas.dedaleEtu.mas.agents.projectAgents;

import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import debug.Debug;

import java.util.HashMap;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;
import eu.su.mas.dedaleEtu.mas.behaviours.CollectBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.EndFSMBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExplorationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.FSMCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MessageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MoveToSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.UnblockBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PingBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PongBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapsBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;
import javafx.application.Platform;

/**
 * <pre>
 * ExploreCoop agent. 
 * Basic example of how to "collaboratively" explore the map
 *  - It explore the map using a DFS algorithm and blindly tries to share the topology with the agents within reach.
 *  - The shortestPath computation is not optimized
 *  - Agents do not coordinate themselves on the node(s) to visit, thus progressively creating a single file. It's bad.
 *  - The agent sends all its map, periodically, forever. Its bad x3.
 *  - You should give him the list of agents'name to send its map to in parameter when creating the agent.
 *   Object [] entityParameters={"Name1","Name2};
 *   ag=createNewDedaleAgent(c, agentName, ExploreCoopAgent.class.getName(), entityParameters);
 *  
 * It stops when all nodes have been visited.
 * 
 * 
 *  </pre>
 *  
 * @author hc
 *
 */


public class ExploreCoopAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	private HashMap<String,MapRepresentation> myMaps;
	
	private static final String EXPLO = "Explo";
	private static final String MESS = "Mess";
	private static final String PING = "Ping";
	private static final String PONG = "Pong";
	private static final String SHARE = "Share";
	private static final String END = "End";
	private static final String COLLECT = "Collect";
	private static final String MOVE_TO_SILO = "Move to Silo";
	private static final String UNBLOCK = "Unblock";
	
	//private final int priority = this.getBackPackFreeSpaceFor(Observation.ANY_TREASURE);
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		
		//get the parameters added to the agent at creation (if any)
		final Object[] args = getArguments();
		
		List<String> list_agentNames=new ArrayList<String>();
		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				//this.myMaps.put((String)args[i], new MapRepresentation());
				i++;
			}
			list_agentNames.add(this.getLocalName());
		}
		
		

		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Moving Agent
		 * 
		 ************************************************/
		
		System.out.println("initialisation des agents : "+list_agentNames);
		int priority = this.getBackPackFreeSpaceFor(Observation.ANY_TREASURE);
		FSMCoopBehaviour fsm = new FSMCoopBehaviour(this, list_agentNames, priority);
		//FSM States
		fsm.registerFirstState(new ExplorationBehaviour(this, list_agentNames), EXPLO);
		fsm.registerState(new MessageBehaviour(this), MESS);
		fsm.registerState(new PingBehaviour(this, list_agentNames), PING);
		fsm.registerState(new PongBehaviour(this), PONG);
		fsm.registerState(new ShareMapsBehaviour(this), SHARE);
		fsm.registerState(new CollectBehaviour(this), COLLECT);
		fsm.registerState(new MoveToSiloBehaviour(this, list_agentNames), MOVE_TO_SILO);
		fsm.registerState(new UnblockBehaviour(this), UNBLOCK);
		fsm.registerLastState(new EndFSMBehaviour(), END);
		
		//FSM Transitions
		fsm.registerTransition(EXPLO, MESS, 0);
		fsm.registerTransition(MESS, EXPLO, 1);
		fsm.registerTransition(MESS, PING, 2);
		fsm.registerTransition(MESS, PONG, 3);
		fsm.registerTransition(MESS, SHARE, 4);
		fsm.registerTransition(PING, MESS, 5);
		fsm.registerTransition(PONG, SHARE, 6);
		fsm.registerTransition(SHARE, EXPLO, 7);
		fsm.registerTransition(EXPLO, END, 8);
		fsm.registerTransition(EXPLO, COLLECT, 9);
		fsm.registerTransition(COLLECT, MESS, 10);
		fsm.registerTransition(MOVE_TO_SILO, EXPLO, 11);
		fsm.registerTransition(MOVE_TO_SILO, MESS, 12);
		fsm.registerTransition(MESS, MOVE_TO_SILO, 13);
		fsm.registerTransition(SHARE, MOVE_TO_SILO, 14);
		fsm.registerTransition(COLLECT, EXPLO, 15);
		fsm.registerTransition(EXPLO, UNBLOCK, 16);
		fsm.registerTransition(UNBLOCK, EXPLO, 17);
		fsm.registerTransition(UNBLOCK, MOVE_TO_SILO, 18);

		
		lb.add(fsm);

		
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new StartMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	public void setAllMaps(HashMap<String, MapRepresentation> allMaps) {
		this.myMaps = allMaps;
	}
	
	public boolean backpackIsNotEmpty() {
		for (Couple<Observation, Integer> ressource : this.getBackPackFreeSpace()) {
			if (ressource.getRight() > 0) return true;
		}
		return false;
	}
	
	public int getExpertiseIn(Observation ability) {
		for (Couple<Observation, Integer> pair : this.getMyExpertise()) {
			if (pair.getLeft().equals(ability)) {
				return pair.getRight();
			}
		}
		Debug.warning(this.getClass().getName()+ "- This type of Observation does not correspond to an ability : "+ability);
		return 0;
	}
	
	public int getBackPackFreeSpaceFor(Observation type) {
		for (Couple<Observation, Integer> pair : this.getBackPackFreeSpace()) {
			if (pair.getLeft().equals(type)) {
				return pair.getRight();
			}
		}
		Debug.warning(this.getClass().getName()+ "- This type of Observation does not correspond to a type of ressources: "+type);
		return 0;
	}
	
	
	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		super.takeDown();
	}

	protected void beforeMove(){
		super.beforeMove();
		//System.out.println("I migrate");
	}

	protected void afterMove(){
		super.afterMove();
		//System.out.println("I migrated");
	}

}
