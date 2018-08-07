
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hp.hpl.jena.ontology.Individual;

public class SemanticItem  {

	private Set<Individual> x;
	private Pair pa;

	public SemanticItem() {
		this.x = new HashSet<Individual>();
		this.pa = new Pair();
	}

	public Set<Individual> getX() {
		return x;
	}

	public void setX(Set<Individual> x) {
		this.x = x;
	}

	public Pair getPair() {
		return pa;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pa == null) ? 0 : pa.hashCode());
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SemanticItem other = (SemanticItem) obj;
		if (pa == null) {
			if (other.pa != null)
				return false;
		} else if (!pa.equals(other.pa))
			return false;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		return true;
	}

	public void setPair(Pair pa) {
		this.pa = pa;
	}

	public void addX(Individual x) {
		this.x.add(x);
	}

	public String toString() {
		return  x.stream().map(n -> n.getLocalName()).collect(Collectors.toSet()) + "(" + pa +")\n";
	}

	 
	
}
