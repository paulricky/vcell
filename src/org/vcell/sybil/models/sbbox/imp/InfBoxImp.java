package org.vcell.sybil.models.sbbox.imp;

/*   DataTray  --- by Oliver Ruebenacker, UCHC --- June 2008 to December 2009
 *   Organizes the RDF data and structures to edit it
 */

import org.vcell.sybil.models.sbbox.InferenceBox;
import org.vcell.sybil.rdf.reason.SYBREAMFactory;
import org.vcell.sybil.rdf.schemas.SBPAX;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;

public class InfBoxImp implements InferenceBox {
	
	protected Model data;
	protected Model schema;
	protected Model infered;
	protected Model rdf;
	
	public InfBoxImp(Model coreNew, Model schemaNew) {
		schema = schemaNew;
		data = coreNew;
		infered = ModelFactory.createDefaultModel();
		infered.add(schema);
		infered.add(data);
		rdf = ModelFactory.createUnion(data, infered);
	}
	
	public void performOWLMicroReasoning() {
		InfModel infModelNew = ModelFactory.createInfModel(ReasonerRegistry.getOWLMicroReasoner(), 
				schema, data);
		infered.removeAll();
		infered.add(infModelNew);
		rdf = ModelFactory.createUnion(data, infered);
	}
	
	public void performSYBREAMReasoning() {
		InfModel infModelNew = ModelFactory.createInfModel(SYBREAMFactory.defaultSYBREAM(), 
				schema, data);
		infered.removeAll();
		infered.add(infModelNew);
		rdf = ModelFactory.createUnion(data, infered);
	}
	
	public Model sbpax() { return SBPAX.schema; }
	public Model schema() { return schema; }
	public Model data() { return data; }
	public Model getRdf() { return rdf; }
}