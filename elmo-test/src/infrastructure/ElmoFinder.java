package infrastructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javabeans.elmo.Item;
import javabeans.elmo.Recipe;

import org.openrdf.elmo.ElmoQuery;

public class ElmoFinder {

	@SuppressWarnings("unchecked")
	public static Item findItem(final ElmoInfrastructure repo,
			Class<?> cl, final String property, final String value) {
		final String queryStr = "SELECT ?item { ?item <:"+property+"> ?value . ?item a ?type . }";
		System.out.println(queryStr);
		final ElmoQuery query = repo.getElmoManager().createQuery(queryStr);
		query.setParameter("value", value);
		query.setType("type", cl);
		final Iterator<?> queryResult = query.evaluate();
		while (queryResult.hasNext()) {
			final Object item = queryResult.next();
//			if (cl.isInstance(item))
				return ((Item) item);
		}
		return null;
	}

	public static List<Recipe> findRecipesByComponent(//
			ElmoInfrastructure repo, Item item) {
		final String queryStr = "SELECT DISTINCT ?recipe {"+//
			"?recipe <:components> ?components . "+//
			"?components <http://www.w3.org/2000/01/rdf-schema#member> ?component . " +//
			"?component <:item> ?item . "+//
			"}";
		final ElmoQuery query = repo.getElmoManager().createQuery(queryStr);
		query.setParameter("item", item);
		final Iterator<?> queryResult = query.evaluate();
		ArrayList<Recipe> result = new ArrayList<Recipe>();
		while (queryResult.hasNext()) {
			final Object recipe = queryResult.next();
			result.add((Recipe) recipe);
		}
		return result;
	}

}
