

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.inferencer.fc.config.ForwardChainingRDFSInferencerConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

class SesameInfrastructure implements Closeable {
	private final LocalRepositoryManager manager;
	private final Repository repository;
	final RepositoryConnection connection;
	private final ValueFactory vf;

	public SesameInfrastructure(final String baseDirName,
			final String repositoryId) throws RepositoryException,
			RepositoryConfigException {
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

		connection = repository.getConnection();
		connection.setAutoCommit(false);
		vf = connection.getValueFactory();
	}

	public void print() throws RepositoryException {
		final RepositoryResult<Statement> stmts = connection.getStatements(
				null, null, null, true);
		try {
			while (stmts.hasNext()) {
				final Statement stmt = stmts.next();
				System.out.println(String.format("%s %s %s", stmt.getSubject()
						.stringValue(), stmt.getPredicate().stringValue(), stmt
						.getObject().stringValue()));
			}
		} finally {
			stmts.close();
		}
	}

	@Override
	public void close() throws IOException {

		try {
			connection.close();
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

	public URI add(final Object obj) throws NoSuchFieldException,
			SecurityException, IllegalArgumentException,
			IllegalAccessException, RepositoryException {
		Object subject;
		try {
			final Field uriField = obj.getClass().getDeclaredField("uri");
			uriField.setAccessible(true);
			subject = uriField.get(obj);
		} catch (final NoSuchFieldException e) {
			subject = vf.createBNode();// ":" + UUID.randomUUID();
		}
		final URI subjectUri = new URIImpl(subject.toString());

		for (final Field field : obj.getClass().getDeclaredFields()) {
			final String predicate = ":" + field.getName();
			field.setAccessible(true);
			final Object object = field.get(obj);

			if (object != null) {
				if (object instanceof List) {
					final List<?> list = (List<?>) object;
					final List<URI> uris = new ArrayList<URI>();
					for (final Object item : list) {
						uris.add(add(item));
					}
					connection.add(new URIImpl(subject.toString()),
							new URIImpl(predicate),
							vf.createLiteral(uris.toString()));
				}
				if (object instanceof Collection) {
				} else {
					connection.add(subjectUri, new URIImpl(predicate),
							vf.createLiteral(object.toString()));
				}
			}
		}
		return subjectUri;
	}

	public void add(final Collection<?> collection)
			throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException,
			RepositoryException {
		for (final Object obj : collection) {
			add(obj);
		}
	}

	public void commit() throws RepositoryException {
		if (!connection.isAutoCommit()) {
			connection.commit();
		}
	}
}