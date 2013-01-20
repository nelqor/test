package main;

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
		try (final AlibabaInfrastructure repo = new AlibabaInfrastructure(
				"target/repo", "test-db", JAVA_BEANS)) {
//			System.out.println("----- initial ------");
//			repo.print();
			final RepositoryConnection cxn = repo.getConnection();
			cxn.clear();
			cxn.commit();
			cxn.close();
//			repo.getElmoManager().clear();
//			repo.beginTransaction();
			try {
				final String site = "https://www.magiclands.ru";
				String url = site + "/library/recipes/jeweller/";
//				try (final InputStream is = HtmlInput.getUrlStream(url)) {
				try (final InputStream is = FileInput.getFileStream("jeweller.html")) {
					final HtmlCleaner c = new HtmlCleaner();
					final TagNode rootNode = c.clean(is);
					for (final Recipe recipe : AlibabaRecipesParser.parseRecipes(rootNode, site, url, repo)) {
						System.out.println(recipe);
					}
				}
//				url = site + "/library/recipes/artisan/";
////				try (final InputStream is = HtmlInput.getUrlStream(url)) {
//				try (final InputStream is = FileInput.getFileStream("artisan.html")) {
//					final HtmlCleaner c = new HtmlCleaner();
//					final TagNode rootNode = c.clean(is);
//					for (final Recipe recipe : AlibabaRecipesParser.parseRecipes(rootNode, site, url, repo)) {
////						System.out.println(recipe);
//					}
//				}
//				url = site + "/library/recipes/alchemy/";
////				try (final InputStream is = HtmlInput.getUrlStream(url)) {
//				try (final InputStream is = FileInput.getFileStream("alchemy.html")) {
//					final HtmlCleaner c = new HtmlCleaner();
//					final TagNode rootNode = c.clean(is);
//					for (final Recipe recipe : AlibabaRecipesParser.parseRecipes(rootNode, site, url, repo)) {
////						System.out.println(recipe);
//					}
//				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
//				repo.commitTransaction();
			}
			
//			System.out.println("----- updated ------");
			repo.print();
			ObjectConnection objectConnection = repo.getObjectConnection();
			try {
			int recipesCount = 0;
			Result<Recipe> objects = objectConnection.getObjects(
					Recipe.class);
			try {
			while(objects.hasNext()) {
				Recipe item = objects.next();
				System.out.println(item.getName()+"("+item+")");
				recipesCount++;
			} } finally {objects.close();}
			System.out.println("Recipes: " + recipesCount);
//			int componentsCount = 0;
//			for (@SuppressWarnings("unused") final RecipeComponent component : repo.getElmoManager().findAll(
//					RecipeComponent.class)) {
//				componentsCount++;
//			}
//			System.out.println("Components: " + componentsCount);
//			int itemsCount = 0;
//			for (@SuppressWarnings("unused") final Item item : repo.getElmoManager().findAll(Item.class)) {
//				itemsCount++;
//				System.out.println(item.getName()+"("+item+")");
//				for(Recipe recipe:Finder.findRecipesByComponent(repo, item))
//					System.out.println("\t"+recipe.getName()+"("+recipe+")");
//					
//			}
//			System.out.println("Items: " + itemsCount);
			} finally {
				objectConnection.close();
			}
		}
	}

}
