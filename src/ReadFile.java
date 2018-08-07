
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author JoséCarlosGarcíaGarr
 */
public class ReadFile {
    
    
    private OntModel model;
    
    public OntModel getOntModel(){
        return model;
    } 
   
    public ReadFile(String fichero) {
        try {
            cargarOnt(fichero);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ReadFile.class.getName()).log(Level.SEVERE, null, ex);
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
}
