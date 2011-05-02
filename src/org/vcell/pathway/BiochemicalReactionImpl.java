package org.vcell.pathway;

import java.util.ArrayList;

import org.vcell.pathway.persistence.BiopaxProxy.RdfObjectProxy;

public class BiochemicalReactionImpl extends ConversionImpl implements BiochemicalReaction {

	private ArrayList<DeltaG> deltaG = new ArrayList<DeltaG>();
	private ArrayList<Double> deltaH = new ArrayList<Double>();
	private ArrayList<Double> deltaS = new ArrayList<Double>();
	private ArrayList<String> ECNumber = new ArrayList<String>();
	private ArrayList<KPrime> kEQ = new ArrayList<KPrime>();

	public ArrayList<DeltaG> getDeltaG() {
		return deltaG;
	}
	public ArrayList<Double> getDeltaH() {
		return deltaH;
	}
	public ArrayList<Double> getDeltaS() {
		return deltaS;
	}
	public ArrayList<String> getECNumber() {
		return ECNumber;
	}
	public ArrayList<KPrime> getkEQ() {
		return kEQ;
	}
	public void setDeltaG(ArrayList<DeltaG> deltaG) {
		this.deltaG = deltaG;
	}
	public void setDeltaH(ArrayList<Double> deltaH) {
		this.deltaH = deltaH;
	}
	public void setDeltaS(ArrayList<Double> deltaS) {
		this.deltaS = deltaS;
	}
	public void setECNumber(ArrayList<String> eCNumber) {
		ECNumber = eCNumber;
	}
	public void setkEQ(ArrayList<KPrime> kEQ) {
		this.kEQ = kEQ;
	}
	
	@Override
	public void replace(RdfObjectProxy objectProxy, BioPaxObject concreteObject){
		super.replace(objectProxy, concreteObject);
		
		for (int i=0; i<deltaG.size(); i++) {
			DeltaG thing = deltaG.get(i);
			if(thing == objectProxy) {
				deltaG.set(i, (DeltaG)concreteObject);
			}
		}
		for (int i=0; i<kEQ.size(); i++) {
			KPrime thing = kEQ.get(i);
			if(thing == objectProxy) {
				kEQ.set(i, (KPrime)concreteObject);
			}
		}
	}
	
	public void showChildren(StringBuffer sb, int level){
		super.showChildren(sb,level);
		printObjects(sb,"deltaG",deltaG,level);
		printDoubles(sb,"deltaH",deltaH,level);
		printDoubles(sb,"deltaS",deltaS,level);
		printStrings(sb,"ECNumber",ECNumber,level);
		printObjects(sb,"kEQ",kEQ,level);
	}

	public String getTypeLabel(){
		return "Biochemical Reaction";
	}

}
