package eu.su.mas.dedaleEtu.mas.agents.projectAgents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.StartMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.EndFSMBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExplorationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.FSMCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.FSMSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.UnblockSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MessageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MessageSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MoveSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PingBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PongBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapsBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class SiloAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	private HashMap<String,MapRepresentation> myMaps;
	
	private static final String MOVE = "Move Silo";
	private static final String MESS = "Mess Silo";
	private static final String PING = "Ping";
	private static final String PONG = "Pong";
	private static final String SHARE = "Share Silo";
	private static final String UNBLOCK = "Unblock";
	
	private final int priorite = -1;
	

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
		
		FSMSiloBehaviour fsm = new FSMSiloBehaviour(this, list_agentNames, this.priorite);
		//FSM States
		fsm.registerFirstState(new MessageSiloBehaviour(this), MESS);
		fsm.registerState(new MoveSiloBehaviour(this, list_agentNames), MOVE);
		fsm.registerState(new PingBehaviour(this, list_agentNames), PING);
		fsm.registerState(new PongBehaviour(this), PONG);
		fsm.registerState(new ShareSiloBehaviour(this), SHARE);
		fsm.registerState(new UnblockSiloBehaviour(this), UNBLOCK);
		
		//FSM Transitions
		fsm.registerTransition(MOVE, MESS, 0);
		fsm.registerTransition(MESS, MOVE, 1);
		fsm.registerTransition(MESS, PING, 2);
		fsm.registerTransition(MESS, PONG, 3);
		fsm.registerTransition(MESS, SHARE, 4);
		fsm.registerTransition(PING, MESS, 5);
		fsm.registerTransition(PONG, SHARE, 6);
		fsm.registerTransition(SHARE, MESS, 7);
		fsm.registerTransition(SHARE, MOVE, 9);
		fsm.registerTransition(MESS, MESS, 10);
		fsm.registerTransition(MOVE, UNBLOCK, 11);
		fsm.registerTransition(UNBLOCK, MOVE, 12);
		fsm.registerTransition(MESS, UNBLOCK, 13);
		
		
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
	
	public int getPriorite() {
		return this.priorite;
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
