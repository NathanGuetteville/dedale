package eu.su.mas.dedaleEtu.mas.agents.projectAgents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.StartMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.EndFSMBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExplorationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.FSMCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MessageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PingBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PongBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapsBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class SiloAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	private HashMap<String,MapRepresentation> myMaps;
	
	private static final String EXPLO = "Explo";
	private static final String MESS = "Mess";
	private static final String PING = "Ping";
	private static final String PONG = "Pong";
	private static final String SHARE = "Share";
	private static final String END = "End";
	

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
		
		FSMCoopBehaviour fsm = new FSMCoopBehaviour(this, list_agentNames, true);
		//FSM States
		fsm.registerFirstState(new ExplorationBehaviour(this, list_agentNames), EXPLO);
		fsm.registerState(new MessageBehaviour(this), MESS);
		fsm.registerState(new PingBehaviour(this, list_agentNames), PING);
		fsm.registerState(new PongBehaviour(this), PONG);
		fsm.registerState(new ShareMapsBehaviour(this), SHARE);
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
