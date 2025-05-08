package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;


import java.util.HashMap;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.projectAgents.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.utils.HelpNeededForTreasure;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShareMapsBehaviour extends OneShotBehaviour{

	private String receiver = null;

	public ShareMapsBehaviour(AbstractDedaleAgent a) {
		super(a);
	}

	private static final long serialVersionUID = -568863390879327961L;

	@Override
	public void action() {
		System.out.println(this.myAgent.getLocalName()+" : ShareBehaviour");
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
				if (((ExploreCoopAgent) this.myAgent).backpackIsNotEmpty()) {
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
		if (fsm.hasLearnedSiloPosition()) {
			return;
		}
		
		this.myAgent.doWait(200);
		
		
		HelpNeededForTreasure helpNeededByCorrespondant = null;
		MessageTemplate helpMsgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("NEED-HELP"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage helpMsgReceived=this.myAgent.receive(helpMsgTemplate);
		if (helpMsgReceived!=null) {
			try {
				helpNeededByCorrespondant = (HelpNeededForTreasure) helpMsgReceived.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			if (helpNeededByCorrespondant!=null) {
				
				ACLMessage helpAnswer = new ACLMessage(ACLMessage.INFORM);
				helpAnswer.setProtocol("HELP-ANS");
				helpAnswer.setSender(this.myAgent.getAID());
				helpAnswer.addReceiver(new AID(receiver, AID.ISLOCALNAME));
				
				Observation myTreasureType = ((ExploreCoopAgent) this.myAgent).getMyTreasureType();
				Observation hisTreasureType = helpNeededByCorrespondant.getPersonalTreasureType();
				boolean iAmTheRightType = myTreasureType.equals(helpNeededByCorrespondant.getTreasureType());
				boolean heIsTheRightType = hisTreasureType.equals(helpNeededByCorrespondant.getTreasureType());
				
				if (iAmTheRightType || heIsTheRightType) {
				    int lockpickingRequired = helpNeededByCorrespondant.getLockpicking();
				    int strengthRequired = helpNeededByCorrespondant.getStrength();

				    int myLockpicking = ((ExploreCoopAgent) this.myAgent).getExpertiseIn(Observation.LOCKPICKING);
				    int myStrength = ((ExploreCoopAgent) this.myAgent).getExpertiseIn(Observation.STRENGH);

				    boolean lockpickingOK = (lockpickingRequired == 0) || (myLockpicking >= lockpickingRequired);
				    boolean strengthOK = (strengthRequired == 0) || (myStrength >= strengthRequired);

				    if (lockpickingOK && strengthOK) {
				        // Join your comrade
				        fsm.setGoingToTreasure(true);
				        List<String> path = fsm.getMap(this.myAgent.getLocalName())
				                                .getShortestPath(
				                                    ((AbstractDedaleAgent)this.myAgent).getCurrentPosition().getLocationId(),
				                                    helpNeededByCorrespondant.getLocationId()
				                                );

				        boolean iHaveMoreSpace = ((ExploreCoopAgent) this.myAgent).getBackPackFreeSpaceFor(myTreasureType)
				                                 > helpNeededByCorrespondant.getMyFreeSpace();

				        if (iAmTheRightType && (!heIsTheRightType || iHaveMoreSpace)) {
				            // Prendre le lead
				        	helpAnswer.setContent("lead");
							fsm.setLeader(true);
				        } else {
				        	path.remove(path.size()-1);
				        	helpAnswer.setContent("yes");
				        }

				        fsm.setCurrentPath(path);
				    } else {
				    	helpAnswer.setContent("no");
				    }
				}

				
				((AbstractDedaleAgent) this.myAgent).sendMessage(helpAnswer);
			}
			
			if (fsm.getHelpNeeded() != null) {
				ACLMessage helpSend = new ACLMessage(ACLMessage.INFORM);
				helpSend.setProtocol("NEED-HELP");
				helpSend.setSender(this.myAgent.getAID());
				helpSend.addReceiver(new AID(receiver, AID.ISLOCALNAME));
				try {
					helpSend.setContentObject(fsm.getHelpNeeded());
				} catch (IOException e) {
					e.printStackTrace();
				}
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				this.myAgent.doWait(200);
				
				MessageTemplate helpAnsTemplate=MessageTemplate.and(
						MessageTemplate.MatchProtocol("HELP-ANS"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				ACLMessage helpAnsReceived=this.myAgent.receive(helpAnsTemplate);
				if (helpAnsReceived!=null) {
					String answer = helpAnsReceived.getContent();
					switch (answer) {
					case "oui": 
						fsm.setGoingToTreasure(true);
						List<String> path = fsm.getMap(this.myAgent.getLocalName())
                                .getShortestPath(
                                    ((AbstractDedaleAgent)this.myAgent).getCurrentPosition().getLocationId(),
                                    fsm.getHelpNeeded().getLocationId()
                                );
						fsm.setLeader(true);
						break;
					case "lead": 
						List<String> path_lead = fsm.getMap(this.myAgent.getLocalName())
                        .getShortestPath(
                            ((AbstractDedaleAgent)this.myAgent).getCurrentPosition().getLocationId(),
                            fsm.getHelpNeeded().getLocationId());
						path_lead.remove(path_lead.size()-1);
						break;
					case "non":
					default: break;
					}
				}
				
			}
		}
			

		
	}
	
	@Override
	public int onEnd() {
		FSMCoopBehaviour fsm = ((FSMCoopBehaviour) getParent());
		if (fsm.hasLearnedSiloPosition()) return 14; 	// MOVE_TO_SILO
		if (fsm.isGoingToTreasure()) return 16;			// MOVE_TO_TREASURE
		return 7; 										// EXPLO
	}

}
