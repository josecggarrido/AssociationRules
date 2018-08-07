
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;

public class AssociationRule {
	
	private Set<Individual> subjects;
	private Pair pa;
	private Pair consequent;
	private Set<OntClass> ontclass;
	public Set<OntClass> getOntclass() {
		return ontclass;
	}



	public void setOntclass(Set<OntClass> ontclass) {
		this.ontclass = ontclass;
	}

	private double sup;
	private double confidence;
	private double lift;




	public AssociationRule(Set<Individual> subjects, Pair pa, Pair consequent) {
		this.subjects=subjects;
		this.pa=pa;
		this.consequent=consequent;
		
	
		
		List<OntClass>cl=new ArrayList<OntClass>();
		for(Individual ind:subjects) {
			OntClass caux=ind.getOntClass();
			if(!cl.contains(caux)) {
				cl.add(caux);
			}
		}
		this.ontclass=new HashSet<OntClass>();
		this.ontclass.addAll(cl);
	}
	
	

	public Set<Individual> getSubjects() {
		return subjects;
	}


	public void setSubjects(Set<Individual> subjects) {
		this.subjects = subjects;
	}


	public Pair getPa() {
		return pa;
	}


	public void setPa(Pair pa) {
		this.pa = pa;
	}


	public Pair getConsequent() {
		return consequent;
	}


	public void setConsequent(Pair consequent) {
		this.consequent = consequent;
	}

	public double getSup() {
		return sup;
	}


	public void setSup(double sup) {
		this.sup = sup;
	}
	
	public double getConfidence() {
		return confidence;
	}


	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public double getLift() {
		return lift;
	}


	public void setLift(double lift) {
		this.lift = lift;
	}

	public String toString() {
		 DecimalFormat df = new DecimalFormat("#0.00");
		Set<String> individuals=subjects.stream().map(n -> n.getLocalName()).collect(Collectors.toSet());
		String ontclasses="";
		int i=0;
		for(OntClass c:this.ontclass) {
			if(i>0) {
				ontclasses+=" U ";
			}
			ontclasses+=c.getLocalName();
			i++;
		}
		return individuals+"     "+"{"+ontclasses+"}"+": ("+pa+") => ("+consequent+")     sup:"+df.format(sup)+"      con:"+df.format(confidence)+"       lift:"+df.format(lift)+"\n";
	}
	
}
