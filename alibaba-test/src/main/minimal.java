package main;

import org.openrdf.annotations.Iri;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.advisers.FieldBehaviour;
import org.openrdf.repository.object.config.ObjectRepositoryConfig;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class minimal {

	@Iri(Document.NS + "Document")
	public interface Document {
	  public static final String NS = "http://example.com/rdf/2012/gs#";

	  @Iri(NS + "title") 
	  public String getTitle();
	  @Iri(NS + "title") 
	  public void setTitle(String title);
	}
	
	public static void main (String[] args) throws RepositoryException, QueryEvaluationException, RepositoryConfigException{
		SailRepository repository = new SailRepository(new MemoryStore());
		repository.initialize();
		ObjectRepositoryFactory objectRepositoryFactory = new ObjectRepositoryFactory();
		
		ObjectRepositoryConfig config=new ObjectRepositoryConfig();
		config.addConcept(Document.class);
//		config.addBehaviour(FieldBehaviour.class);
		ObjectRepository objectRepository = objectRepositoryFactory.createRepository(config, repository);
		
		// add a Document to the repository
		ObjectConnection con = objectRepository.getConnection();
//		ValueFactory vf = con.getValueFactory();
//		URI id = vf.createURI("http://example.com/data/2012/getting-started");

		// create a Document
		Document doc = con.addDesignation(con.getObjectFactory().createObject(),Document.class);
		doc.setTitle("Getting Started 3");
		Value id = con.addObject(doc);

		// retrieve a Document by id
//		doc = (Document) con.getObject(id);

		doc = (Document) con.getObject(id);
		System.out.println(doc.getTitle());
		
		// remove a Document from the repository
//		doc = con.getObject(Document.class, id);
		doc.setTitle(null);
		con.removeDesignation(doc, Document.class);
	}
}
