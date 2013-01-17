import infrastructure.ElmoInfrastructure;

import java.util.Iterator;

import javabeans.Item;

import org.openrdf.elmo.ElmoQuery;


public class Finder {

	static Item findItem(final ElmoInfrastructure repo,
			final String name) {
		final String queryStr = "SELECT ?item { ?item <:name> ?name . ?item a ?type . }";
		final ElmoQuery query = repo.getElmoManager().createQuery(queryStr);
		query.setParameter("name", name);
		query.setType("type", Item.class);
		final Iterator<?> result = query.evaluate();
		while (result.hasNext()) {
			final Object item = result.next();
			return ((Item) item);
		}
		return null;
	}

}
