
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class Main {

	private static OntModel model;
	private static String uri;

	public Main(String fichero) {
		try {
			cargarOnt(fichero);
			uri = model.getNsPrefixURI("");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void cargarOnt(String fichero) throws MalformedURLException {
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		File file = new File(fichero);
		if (file.exists()) {
			if (fichero.substring(fichero.length() - 2, fichero.length()).equals("nt")) {
				System.out.println("Formato leido: nt");
				System.out.println("-------");
				model.read(new File(fichero).toURL().toString(), "N-TRIPLES");
			} else if (fichero.substring(fichero.length() - 3, fichero.length()).equals("ttl")) {
				System.out.println("Formato leido: ttl");
				System.out.println("-------");
				model.read(new File(fichero).toURL().toString(), "TURTLE");
			} else if (fichero.substring(fichero.length() - 3, fichero.length()).equals("rdf")
					|| fichero.substring(fichero.length() - 3, fichero.length()).equals("owl")) {
				System.out.println("Formato leido: RDF");
				System.out.println("-------");
				model.read(new File(fichero).toURL().toString(), "RDF/XML");
			} else {
				System.out.println("Extensi�n leida :" + fichero.substring(fichero.length() - 3, fichero.length()));
				System.out.println("Formato incorrecto o extensi�n del fichero incorrecta");
			}
		} else {
			System.out.println("No se ha encontrado el fichero");
		}
	}


	public List<SemanticItem> semItGen() {	
		List<SemanticItem>siList=new ArrayList<SemanticItem>();
		
		boolean flag=false;
		for(Iterator i=model.listIndividuals();i.hasNext();) {
			Individual rdf= (Individual) i.next();
			for(ExtendedIterator<ObjectProperty> j=model.listObjectProperties();j.hasNext();) {
				ObjectProperty s =  j.next();
				Statement indprop=rdf.getProperty(s); 
				if(indprop !=null) {
					Pair pa = new Pair(indprop.getPredicate().getLocalName(), indprop.getObject().asNode().getLocalName());
					for (SemanticItem sij : siList) {
						if (sij.getPair().getX().equals(pa.getX()) && sij.getPair().getY().equals(pa.getY())) {
							sij.addX(rdf);
							flag = true;
						}
					}
					if (flag == false) {
						SemanticItem si = new SemanticItem();
						si.addX(rdf);
						si.setPair(new Pair(indprop.getPredicate().getLocalName(), indprop.getObject().asNode().getLocalName()));
						siList.add(si);
					}
					flag = false;
				}
			}
		}
		
		return siList;
	}

	public static Map<String, Set<SemanticItem>> totalCommonBehaviorSetGen(List<SemanticItem> sil, double SimTh) {
		Map<String, Set<SemanticItem>> tcbs = new HashMap<String, Set<SemanticItem>>();
		boolean flag = false;
		int i = 0;
		for (SemanticItem si : sil) {
			for (String key : tcbs.keySet()) {
				Set<Individual> setA = new HashSet<Individual>();
				Set<Individual> setB = new HashSet<Individual>();
				fillSet(setA, tcbs.get(key));
				setA.retainAll(si.getX());
				fillSet(setB, tcbs.get(key));

				setB.addAll(si.getX());
				setB.retainAll(setA);
				float sd = setB.size();
				if (sd != 0) {
					sd = 1 / sd;
				}
				if (sd >= SimTh) {
					flag = true;
					tcbs.get(key).add(si);
				}

			}

			if (flag == false) {
				i++;
				Set<SemanticItem> sis = new HashSet<SemanticItem>();
				sis.add(si);
				tcbs.put("cbs" + i, sis);
			}
			flag = false;
		}
		return tcbs;
	}

	public static List<AssociationRule> getAssociationRules(Map<String, Set<SemanticItem>> tcbs, int option) {
		List<AssociationRule> arl = new ArrayList<AssociationRule>();
		for (String key : tcbs.keySet()) {
			Set<Individual> subjects = new HashSet<Individual>();
			fillSet(subjects, tcbs.get(key));

			for (SemanticItem si : tcbs.get(key)) {
				for (SemanticItem si2 : tcbs.get(key)) {
					if (!si.equals(si2)) {
						Pair pa = si.getPair();
						Pair consequent = si2.getPair();
						AssociationRule ar = new AssociationRule(subjects, pa, consequent);
						if(option==1) {
							calculateSup(ar);
							calculateConfidence(ar);
							calculateLift(ar);
						}else if(option==2) {
							calculateSup2(ar);
							calculateConfidence2(ar);
							calculateLift2(ar);
						}
						
						arl.add(ar);
					}
				}
			}

		}
		return arl;
	}

	
	
	//segun la clase mas alta
	public static void calculateSup(AssociationRule ar) {
		int num = 0;
		int den = 0;
			
		OntClass higher=calculateHigher(ar);
		for (Iterator<Individual> it = model.listIndividuals(); it.hasNext();) {
			Individual ind = it.next();
			if(ind.getOntClass().equals(higher) || isSuperClass(ind.getOntClass(),higher)) {
				for (Individual subj : ar.getSubjects()) {				
					if (ind.equals(subj)) {
						num++;
					}
					
				}
			}
			if(ind.getOntClass().equals(ar.getSubjects().iterator().next().getOntClass())) {
				den++;
			}
		}
		
		ar.setSup((double)num/den);
		
	}
	
	
	//segun la rama
	public static void calculateSup2(AssociationRule ar) {
		int num = 0;
		int den = 0;
		List<OntClass>commonClasses=new ArrayList<OntClass>();
		commonClasses.addAll(getCommonClasses(ar.getSubjects()));
		for (Iterator<Individual> it = model.listIndividuals(); it.hasNext();) {
			Individual ind = it.next();
			if(commonClasses.contains(ind.getOntClass())) {
				den++;
				for (Individual subj : ar.getSubjects()) {				
					if (ind.equals(subj)) {
						num++;
					}
					
				}
			}
			
		
		}
		ar.setSup((double)num/den);
		
	}
	

	


	public static void calculateConfidence(AssociationRule ar) {
		int num = 0;
		int den = 0;
		OntClass higher=calculateHigher(ar);
		for(Individual ind:ar.getSubjects()) {
			if(ind.getOntClass().equals(higher) || isSuperClass(ind.getOntClass(),higher)) {
				for(StmtIterator it2=ind.listProperties();it2.hasNext();) {
					Statement prop=it2.next();
					if(prop.getObject().asResource().getLocalName().equals(ar.getPa().getY()) && prop.getPredicate().getLocalName().equals(ar.getPa().getX())) {
						den++;
					}
					if(prop.getObject().asResource().getLocalName().equals(ar.getConsequent().getY()) && prop.getPredicate().getLocalName().equals(ar.getConsequent().getX()) ) {
						for(StmtIterator it3=ind.listProperties();it3.hasNext();) {
							Statement prop2=it3.next();
							if((prop2.getObject().asResource().getLocalName().equals(ar.getPa().getY()) && prop2.getPredicate().getLocalName().equals(ar.getPa().getX()))) {
								num++;
							}
						}
						
					}	
				}
			}
		}
		ar.setConfidence((double)num/den);	
		
	}
	
	public static void calculateConfidence2(AssociationRule ar) {
		int num = 0;
		int den = 0;
		List<OntClass>commonClasses=new ArrayList<OntClass>();
		commonClasses.addAll(getCommonClasses(ar.getSubjects()));
		
		for(Individual ind:ar.getSubjects()) {
		
				for(StmtIterator it2=ind.listProperties();it2.hasNext();) {
					Statement prop=it2.next();
					if(prop.getObject().asResource().getLocalName().equals(ar.getPa().getY()) && prop.getPredicate().getLocalName().equals(ar.getPa().getX()) && commonClasses.contains(ind.getOntClass())) {
						den++;
					}
					if(prop.getObject().asResource().getLocalName().equals(ar.getConsequent().getY()) && prop.getPredicate().getLocalName().equals(ar.getConsequent().getX()) && commonClasses.contains(ind.getOntClass()) ) {
						for(StmtIterator it3=ind.listProperties();it3.hasNext();) {
							Statement prop2=it3.next();
							if((prop2.getObject().asResource().getLocalName().equals(ar.getPa().getY()) && prop2.getPredicate().getLocalName().equals(ar.getPa().getX()))) {
								num++;
							}
						}
						
					}	
				}
			
		}
		ar.setConfidence((double)num/den);	
		
	}
	

	public static void calculateLift(AssociationRule ar) {
		int num = 0;
		int den1 = 0;
		int den2 =0;
		OntClass higher=calculateHigher(ar);
		for(Individual ind:ar.getSubjects()) {
			if(ind.getOntClass().equals(higher) || isSuperClass(ind.getOntClass(),higher)) {
				for(StmtIterator it2=ind.listProperties();it2.hasNext();) {
					Statement prop=it2.next();
					if(prop.getObject().asResource().getLocalName().equals(ar.getPa().getY()) && prop.getPredicate().getLocalName().equals(ar.getPa().getX())) {
						den1++;
					}
					if(prop.getObject().asResource().getLocalName().equals(ar.getConsequent().getY()) && prop.getPredicate().getLocalName().equals(ar.getConsequent().getX())) {
						den2++;
					}
					if(prop.getObject().asResource().getLocalName().equals(ar.getConsequent().getY()) && prop.getPredicate().getLocalName().equals(ar.getConsequent().getX()) ) {
						for(StmtIterator it3=ind.listProperties();it3.hasNext();) {
							Statement prop2=it3.next();
							if((prop2.getObject().asResource().getLocalName().equals(ar.getPa().getY()) && prop2.getPredicate().getLocalName().equals(ar.getPa().getX()))) {
								num++;
							}
						}
						
					}
				}
			}
		}
		ar.setLift((double)num/(den1*den2));
	}
	
	public static void calculateLift2(AssociationRule ar) {
		int num = 0;
		int den1 = 0;
		int den2 =0;
		List<OntClass>commonClasses=new ArrayList<OntClass>();
		commonClasses.addAll(getCommonClasses(ar.getSubjects()));
		
		for(Individual ind:ar.getSubjects()) {
			
			for(StmtIterator it2=ind.listProperties();it2.hasNext();) {
				Statement prop=it2.next();
				if(prop.getObject().asResource().getLocalName().equals(ar.getPa().getY()) && prop.getPredicate().getLocalName().equals(ar.getPa().getX()) && commonClasses.contains(ind.getOntClass())) {
					den1++;
				}
				if(prop.getObject().asResource().getLocalName().equals(ar.getConsequent().getY()) && prop.getPredicate().getLocalName().equals(ar.getConsequent().getX())&& commonClasses.contains(ind.getOntClass())) {
					den2++;
				}
				if(prop.getObject().asResource().getLocalName().equals(ar.getConsequent().getY()) && prop.getPredicate().getLocalName().equals(ar.getConsequent().getX()) && commonClasses.contains(ind.getOntClass())) {
					for(StmtIterator it3=ind.listProperties();it3.hasNext();) {
						Statement prop2=it3.next();
						if((prop2.getObject().asResource().getLocalName().equals(ar.getPa().getY()) && prop2.getPredicate().getLocalName().equals(ar.getPa().getX())) && commonClasses.contains(ind.getOntClass())) {
							num++;
						}
					}
						
				}			
			}
		}
		ar.setLift((double)num/(den1*den2));
	}
	
	
	private static OntClass calculateHigher(AssociationRule ar) {
		
		OntClass higher=null;
		List<OntClass>cl=new ArrayList<OntClass>();
		List<Individual> individuals=new ArrayList<Individual>();
		individuals.addAll(ar.getSubjects());
		for(Individual ind:ar.getSubjects()) {
			OntClass caux=ind.getOntClass();
			cl.add(caux);
		}
		higher=findHigherOntClass(cl);
		
		return higher;
	}

	
	private static OntClass findHigherOntClass(List<OntClass> cl) {
		OntClass c=cl.get(0);
		OntClass higher=c;		
		for(int i=1;i<cl.size();i++) {
			if(isSuperClass(c,cl.get(i))) {
				c=cl.get(i);
				higher=c;
			}
		}
		return higher;
		
	}

	/**
	 * if c2 is superclass of c1
	 * @param c1
	 * @param c2
	 * @return
	 */
	private static boolean isSuperClass(OntClass c1, OntClass c2) {
		boolean res=false;
		while(c1.hasSuperClass()) {
			c1=c1.getSuperClass();
			if(c1.equals(c2)) {
				res=true;
				return res;
			}
		}
		return res;
	}

	public static List<OntClass> getClassesBranch(Individual ind){
		List<OntClass> branch=new ArrayList<OntClass>();
		OntClass c=ind.getOntClass();
		while(c.hasSuperClass()) {
			branch.add(c);
			c=c.getSuperClass();
		}
		return branch;		
	}
	
	private static List<OntClass> getCommonClasses(Set<Individual> subjects) {
		List<OntClass>commonClasses=new ArrayList<OntClass>();
		/*List<Individual>individuals=new ArrayList<Individual>();
		individuals.addAll(subjects);
		
		Map<Individual,List<OntClass>> indAndClass=new HashMap<Individual,List<OntClass>>();
		for(Individual ind:subjects) {
			indAndClass.put(ind, getClassesBranch(ind));
			
		}*/
		
		List<IndividualWithClass>listIndCla=new ArrayList<IndividualWithClass>();
		for(Individual ind:subjects) {
			listIndCla.add(new IndividualWithClass(ind,getClassesBranch(ind)));
		}
		boolean added=false;
		for(int i=0;i<listIndCla.size();i++) {
			for(int j=i+1;j<listIndCla.size();j++) {
				for(OntClass c:listIndCla.get(i).getC()) {
					if(c.equals(listIndCla.get(j).getC().get(0))) {
						if(!commonClasses.contains(c)) {
							commonClasses.add(c);
						}						
						added=true;
						break;
					}
				}
				if(!added) {
					if(!commonClasses.contains(listIndCla.get(i).getC().get(0))) {
						commonClasses.add(listIndCla.get(i).getC().get(0));			
					}
					if(!commonClasses.contains(listIndCla.get(j).getC().get(0))) {
						commonClasses.add(listIndCla.get(j).getC().get(0));
					}
				}
			}
			
		}
		
		
		
		
		
		
		
		
		/*Iterator it=indAndClass.keySet().iterator();
		Individual first=(Individual) it.next();
		boolean added=false;
		while(it.hasNext()){
			for(OntClass c: indAndClass.get(first)) {
				Individual next=(Individual) it.next();
				if(c.equals(indAndClass.get(next).get(0))){
					commonClasses.add(c);
					added=true;
				}
			}
		}
		*/
		
		
		
		
		/*
		List<List<OntClass>>allClasses=new ArrayList<List<OntClass>>();
		for(int i=0;i<individuals.size();i++) {
			allClasses.add(getClassesBranch(individuals.get(i)));
		}
		boolean added=false;
		List<OntClass> claux=allClasses.get(0);
		for(int i=1;i<allClasses.size()-1;i++) {
			for(OntClass c:claux) {
				if(c.equals(allClasses.get(i).get(0))) {
					commonClasses.add(c);
					added=true;
				}
			}
			if(!added) {
				commonClasses.add(allClasses.get(i).get(0));
			}
			added=false;
			
		}
		*/
		
		return commonClasses;
	}
	
	
	private static void fillSet(Set<Individual> set, Set<SemanticItem> values) {
		for (SemanticItem siAux : values) {
			set.addAll(siAux.getX());
		}
	}

        public  List<AssociationRule> getResults(String url, int option){
           // Main ra = new Main("D:\\Users\\Jose Carlos Garcia Garrido\\Desktop\\TFG\\ontologiaExample.owl");
           Main ra = new Main(url);
          List<SemanticItem> sil = new ArrayList<SemanticItem>();
		for (SemanticItem si : ra.semItGen()) {
			if (si.getX().size() > 1) {
				sil.add(si);
			}

		}
            Map<String, Set<SemanticItem>> tcbs = totalCommonBehaviorSetGen(sil, 0.5);
            List<AssociationRule> arl = getAssociationRules(tcbs,option);
            return arl;
        }
        
	public static void main(String[] args) {

		// SimTh % de entidades que tiene cada cbs del conjunto de comportamientos
		// sup division de clase que tiene un par con el numero de clases que hay (6
		// personas interseccion 2 con instrument guitar / 6 personas)=0.33
		
		// confidence porcentaje de entidades de que si cumple un par cumpla el otro

		Main ra = new Main(
				"D:\\Users\\Jose Carlos Garcia Garrido\\Desktop\\TFG\\ontologiaExample.owl");

		List<SemanticItem> sil = new ArrayList<SemanticItem>();
		for (SemanticItem si : ra.semItGen()) {
			if (si.getX().size() > 1) {
				sil.add(si);
			}

		}
		System.out.println("\nSemantic Items\n");
		System.out.println(sil);
		System.out.println("------------------");
		System.out.println("\nTotal Common Behaviour\n");
		System.out.println(totalCommonBehaviorSetGen(sil, 0.5));
		System.out.println("------------------");
		Map<String, Set<SemanticItem>> tcbs = totalCommonBehaviorSetGen(sil, 0.5);
		List<AssociationRule> arl = getAssociationRules(tcbs,2);
		System.out.println("\nAssociation Rules\n");
		System.out.println(arl);
		
		/*String filtro="";
		System.out.println("Introduce si quieres filtrar por clase: ");
		Scanner sc=new Scanner(System.in);
		filtro=sc.nextLine();
		for(AssociationRule a:arl) {
			for(OntClass c:a.getOntclass()) {
				if(c.getLocalName().equals(filtro)) {
					System.out.println(a);
				}
			}
		}*/
		/*
		double filtro2;
		System.out.println("Introduce si quieres filtrar por mayor sup: ");
		Scanner sc2=new Scanner(System.in);
		filtro2=sc2.nextDouble();
		for(AssociationRule a:arl) {
			if(a.getSup()>filtro2) {
				System.out.println(a);
			}
			
		}
                */
		
	}

}
