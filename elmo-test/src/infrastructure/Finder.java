package infrastructure;

import java.util.Iterator;
import org.openrdf.elmo.ElmoQuery;


public class Finder {

	@SuppressWarnings("unchecked")
	public static <T>T findItem(final ElmoInfrastructure repo,
			Class<T> cl, final String property, final String value) {
		final String queryStr = "SELECT ?item { ?item <:"+property+"> ?value . ?item a ?type . }";
		final ElmoQuery query = repo.getElmoManager().createQuery(queryStr);
		query.setParameter("value", value);
		query.setType("type", cl);
		final Iterator<?> result = query.evaluate();
		while (result.hasNext()) {
			final Object item = result.next();
			if (cl.isInstance(item))
				return ((T) item);
		}
		return null;
	}

}
