import infrastructure.ElmoInfrastructure;
import input.FileInput;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javabeans.RecipeComponent;
import javabeans.Item;
import javabeans.Recipe;

import javax.persistence.EntityTransaction;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

public class RecipesReader {

	private final static List<Class<?>> JAVA_BEANS = Arrays.<Class<?>> asList(
			Recipe.class, RecipeComponent.class, Item.class);

	public static void main(final String[] args) throws IOException,
			RepositoryException, RepositoryConfigException,
			NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {
		try (final ElmoInfrastructure repo = new ElmoInfrastructure(
				"target/repo", "test-db", JAVA_BEANS)) {
			System.out.println("----- initial ------");
			repo.print();
			final RepositoryConnection cxn = repo.getConnection();
			cxn.clear();
			cxn.commit();
			cxn.close();

			repo.beginTransaction();
			try {
				final String site = "https://www.magiclands.ru";
				final String url = site + "/library/recipes/jeweller/";
//				try (final InputStream is = HtmlInput.getUrlStream(url)) {
				final List<Recipe> recipes;
				try (final InputStream is = FileInput.getFileStream("jeweller.html")) {
					final HtmlCleaner c = new HtmlCleaner();
					final TagNode rootNode = c.clean(is);
					recipes = RecipesParser.parseRecipes(rootNode, site, url, repo);
					for (final Recipe recipe : recipes) {
						System.out.println(recipe);
					}
				}
			} finally {
				repo.commitTransaction();
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
			for (@SuppressWarnings("unused") final RecipeComponent component : repo.getElmoManager().findAll(
					RecipeComponent.class)) {
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
