package main;

import infrastructure.AlibabaFinder;
import infrastructure.AlibabaInfrastructure;
import input.FileInput;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javabeans.alibaba.Item;
import javabeans.alibaba.Recipe;
import javabeans.alibaba.RecipeComponent;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.result.Result;

public class AlibabaRecipesReader {

	private final static List<Class<?>> JAVA_BEANS = Arrays.<Class<?>> asList(
			Recipe.class, RecipeComponent.class, Item.class);

	public static void main(final String[] args) throws IOException,
			RepositoryException, RepositoryConfigException,
			NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, QueryEvaluationException {
		long startNano = System.nanoTime();
		try (final AlibabaInfrastructure repo = new AlibabaInfrastructure(
				"target/repo", "test-db", JAVA_BEANS)) {
			// System.out.println("----- initial ------");
			// repo.print();
			final RepositoryConnection cxn = repo.getConnection();
			cxn.clear();
			cxn.commit();
			cxn.close();
			ObjectConnection objectConnection = repo.getObjectConnection();
			objectConnection.setAutoCommit(false);
			// repo.getElmoManager().clear();
			// repo.beginTransaction();
			try {
				try {
					final String site = "https://www.magiclands.ru";
					String url = site + "/library/recipes/jeweller/";
					// try (final InputStream is = HtmlInput.getUrlStream(url))
					// {
					try (final InputStream is = FileInput
							.getFileStream("jeweller.html")) {
						final HtmlCleaner c = new HtmlCleaner();
						final TagNode rootNode = c.clean(is);
						for (final Recipe recipe : AlibabaRecipesParser
								.parseRecipes(rootNode, site, url,
										objectConnection)) {
							//System.out.println(recipe);
						}
					}
					url = site + "/library/recipes/artisan/";
					// try (final InputStream is = HtmlInput.getUrlStream(url))
					// {
					try (final InputStream is = FileInput
							.getFileStream("artisan.html")) {
						final HtmlCleaner c = new HtmlCleaner();
						final TagNode rootNode = c.clean(is);
						for (final Recipe recipe : AlibabaRecipesParser
								.parseRecipes(rootNode, site, url,
										objectConnection)) {
							// System.out.println(recipe);
						}
					}
					url = site + "/library/recipes/alchemy/";
					// try (final InputStream is = HtmlInput.getUrlStream(url))
					// {
					try (final InputStream is = FileInput
							.getFileStream("alchemy.html")) {
						final HtmlCleaner c = new HtmlCleaner();
						final TagNode rootNode = c.clean(is);
						for (final Recipe recipe : AlibabaRecipesParser
								.parseRecipes(rootNode, site, url,
										objectConnection)) {
							// System.out.println(recipe);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					// repo.commitTransaction();
					objectConnection.commit();
					// objectConnection.close();
				}

				// System.out.println("----- updated ------");
				//repo.print();
				int recipesCount = 0;
				Result<Recipe> objects = objectConnection
						.getObjects(Recipe.class);
				try {
					while (objects.hasNext()) {
						Recipe recipe = objects.next();
						recipesCount++;
//						System.out.println(recipe.getName() + "(" + recipe
//								+ ")");
//						for (RecipeComponent component : recipe.getComponents())
//							System.out.println("\t"
//									+ component.getItem().getName() + "("
//									+ component + ")");
					}
				} finally {
					objects.close();
				}
				System.out.println("Recipes: " + recipesCount);

				int componentsCount = 0;
				Result<RecipeComponent> components = objectConnection
						.getObjects(RecipeComponent.class);
				try {
					while (components.hasNext()) {
						RecipeComponent item = components.next();
						// System.out.println(item.getItem()+"("+item+")");
						componentsCount++;
					}
				} finally {
					components.close();
				}
				System.out.println("Components: " + componentsCount);
				int itemsCount = 0;
				Result<Item> items = objectConnection.getObjects(Item.class);
				try {
					while (items.hasNext()) {
						Item item = items.next();
						itemsCount++;
//						System.out.println(item.getName() + "(" + item + ")");
//						for (Recipe recipe : AlibabaFinder
//								.findRecipesByComponent(item, objectConnection))
//							System.out.println("\t" + recipe.getName() + "("
//									+ recipe + ")");
					}
				} finally {
					items.close();
				}
				System.out.println("Items: " + itemsCount);
			} finally {
				objectConnection.close();
			}
		}
		System.out.println("Time: "
				+ ((1d * System.nanoTime() - startNano) / 1000000000));
	}

}
