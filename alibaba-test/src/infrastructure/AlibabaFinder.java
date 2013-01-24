package infrastructure;

import java.util.ArrayList;
import java.util.List;

import javabeans.alibaba.Item;
import javabeans.alibaba.Recipe;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectQuery;
import org.openrdf.result.Result;

public class AlibabaFinder {

	public static Item findItem(final ObjectConnection objectConnection,
			Class<?> cl, final String property, final String value) {
		final String queryStr = "SELECT ?item { ?item <:"+property+"> ?value . ?item a ?type . }";
//		System.out.println(queryStr);
			try {
				final ObjectQuery query = objectConnection.prepareObjectQuery(queryStr);
				query.setObject("value", value);
				query.setType("type", cl);
				Result<Item> queryResult = query.evaluate(Item.class);
				try { 
				if (queryResult.hasNext())
					return queryResult.singleResult();
				} finally {
					queryResult.close();
				}
		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e){
			e.printStackTrace();
		}
		return null;
	}

	public static List<Recipe> findRecipesByComponent(//
			Item item, ObjectConnection objectConnection) {
		final String queryStr = "SELECT DISTINCT ?recipe {"+//
			"?recipe <:components> ?components . "+//
			"?components <http://www.w3.org/2000/01/rdf-schema#member> ?component . " +//
			"?component <:item> ?item . "+//
			"}";
		ArrayList<Recipe> result = new ArrayList<Recipe>();
		try {
				ObjectQuery query = objectConnection.prepareObjectQuery(queryStr);
				query.setObject("item", item);
				final Result<Recipe> queryResult = query.evaluate(Recipe.class);
				try {
					while (queryResult.hasNext()) {
						final Object recipe = queryResult.next();
						result.add((Recipe) recipe);
					}
				} finally {
					queryResult.close();
				}
		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			
		}
		return result;
	}

}
