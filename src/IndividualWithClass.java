
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;

public class IndividualWithClass {

	private Individual ind;
	private List<OntClass> c;
	
	public IndividualWithClass(Individual ind, List<OntClass> c) {
		this.ind=ind;
		this.c=c;
	}
	
	
	public Individual getInd() {
		return ind;
	}
	public void setInd(Individual ind) {
		this.ind = ind;
	}
	public List<OntClass> getC() {
		return c;
	}
	public void setC(List<OntClass> c) {
		this.c = c;
	}
	
	
	
	
}
