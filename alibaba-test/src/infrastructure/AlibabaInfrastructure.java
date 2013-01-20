package infrastructure;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryConfig;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.inferencer.fc.config.ForwardChainingRDFSInferencerConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

public class AlibabaInfrastructure implements Closeable {
	private final LocalRepositoryManager manager;
	private final Repository repository;
	private ObjectRepository alibabaRepository;

	public AlibabaInfrastructure(final String baseDirName,
			final String repositoryId, final List<Class<?>> concepts)
			throws RepositoryException, RepositoryConfigException {

		final File baseDir = new File(baseDirName);

		manager = new LocalRepositoryManager(baseDir);
		manager.initialize();

		// create a configuration for the SAIL stack
		final boolean persist = true;
		SailImplConfig backendConfig = new MemoryStoreConfig(persist);

		// stack an inferencer config on top of our backend-config
		backendConfig = new ForwardChainingRDFSInferencerConfig(backendConfig);

		// create a configuration for the repository implementation
		final SailRepositoryConfig repositoryTypeSpec = new SailRepositoryConfig(
				backendConfig);

		final RepositoryConfig repConfig = new RepositoryConfig(repositoryId,
				repositoryTypeSpec);
		manager.addRepositoryConfig(repConfig);
		repository = manager.getRepository(repositoryId);
		repository.initialize();

		ObjectRepositoryFactory factory = new ObjectRepositoryFactory();
		ObjectRepositoryConfig config = new ObjectRepositoryConfig();
		
//		module = new ElmoModule();
		for (final Class<?> concept : concepts) {
			config.addConcept(concept);
		}
		
//		elmoFactory = new SesameManagerFactory(module, repository);
//		elmoFactory.setQueryLanguage(QueryLanguage.SPARQL);
//		elmoManager = elmoFactory.createElmoManager();
//		elmoManager.setFlushMode(FlushModeType.COMMIT);
		alibabaRepository = factory.createRepository(config, repository);
	}

	public void print() throws RepositoryException {
		final RepositoryConnection cxn = repository.getConnection();
		final RepositoryResult<Statement> stmts = cxn.getStatements(null, null,
				null, true);
		long count = 0;
		try {
			while (stmts.hasNext()) {
				final Statement stmt = stmts.next();
				System.out.println(stmt);
				count++;
			}
		} finally {
			stmts.close();
			cxn.close();
		}
		System.out.println("Statements count: " + count);
	}
	
	public RepositoryConnection getConnection() throws RepositoryException {
		return repository.getConnection();
	}

	@Override
	public void close() throws IOException {

		try {
			alibabaRepository.shutDown();
		} catch (final RepositoryException e) {
			throw new IOException(e);
		}
		try {
			repository.shutDown();
		} catch (final RepositoryException e) {
			throw new IOException(e);
		}
		manager.shutDown();
	}

	public ObjectConnection getObjectConnection() throws RepositoryException {
		return alibabaRepository.getConnection();
	}

	public <T> T createObject(ObjectConnection objectConnection, Class<T> cl) throws RepositoryException {
		return objectConnection.addDesignation(objectConnection.getObjectFactory().createObject(), cl);
	}

}