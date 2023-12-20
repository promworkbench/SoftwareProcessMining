package software.processmining.multiviewinteraction;

import software.processmining.componentbehaviordiscovery.HierarchicalPetriNet;

public class InterfaceID2HierarchicalPetriNet {
 private InterfaceID inter;
 private HierarchicalPetriNet hpn;
 
 public InterfaceID2HierarchicalPetriNet(InterfaceID inter, HierarchicalPetriNet hpn)
 {
	 this.inter=inter;
	 this.hpn = hpn;
 }

public InterfaceID getInter() {
	return inter;
}

public void setInter(InterfaceID inter) {
	this.inter = inter;
}

public HierarchicalPetriNet getHpn() {
	return hpn;
}

public void setHpn(HierarchicalPetriNet hpn) {
	this.hpn = hpn;
}
 
}
