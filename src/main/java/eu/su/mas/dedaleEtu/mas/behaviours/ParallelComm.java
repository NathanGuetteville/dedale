package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.ParallelBehaviour;

public class ParallelComm extends ParallelBehaviour {
	
	private boolean stop;
	
	public ParallelComm(final AbstractDedaleAgent a) {
		super(a, ParallelBehaviour.WHEN_ALL);
	}
	
	public void setStop() {
		stop = true;
	}
	
	@Override
	public int onEnd() {
		return stop ? 1 : 0;
	}

}
