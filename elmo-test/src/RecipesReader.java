import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityTransaction;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

public class RecipesReader {

	public static void main(final String[] args) throws IOException,
			RepositoryException, RepositoryConfigException,
			NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {
		try (final ElmoInfrastructure repo = new ElmoInfrastructure(
				"target/repo", "test-db", Arrays.<Class<?>> asList(
						Recipe.class, Component.class, Item.class))) {
			System.out.println("----- initial ------");
			repo.print();
			final RepositoryConnection cxn = repo.getConnection();
			cxn.clear();
			cxn.commit();
			cxn.close();

			final EntityTransaction transaction = repo.getElmoManager()
					.getTransaction();
			transaction.begin();
			try {
				final String site = "https://www.magiclands.ru";
				final String url = site + "/library/recipes/jeweller/";
//				try (final InputStream is = HtmlInput.getUrlStream(url)) {
				final List<Recipe> recipes;
				try (final InputStream is = FileInput.getFileStream("jeweller.html")) {
					final HtmlCleaner c = new HtmlCleaner();
					final TagNode rootNode = c.clean(is);
					recipes = Parser.parseRecipes(rootNode, site, url, repo);
					for (final Recipe recipe : recipes) {
						System.out.println(recipe);
					}
				}
			} finally {
				transaction.commit();
			}

			System.out.println("----- updated ------");
			repo.print();
			int recipesCount = 0;
			for (@SuppressWarnings("unused") final Recipe recipe : repo.getElmoManager().findAll(
					Recipe.class)) {
				recipesCount++;
			}
			System.out.println("Recipes: " + recipesCount);
			int componentsCount = 0;
			for (@SuppressWarnings("unused") final Component component : repo.getElmoManager().findAll(
					Component.class)) {
				componentsCount++;
			}
			System.out.println("Components: " + componentsCount);
			int itemsCount = 0;
			for (@SuppressWarnings("unused") final Item item : repo.getElmoManager().findAll(Item.class)) {
				itemsCount++;
			}
			System.out.println("Items: " + itemsCount);
		}
	}

}
