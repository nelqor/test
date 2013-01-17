

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UnknownFormatConversionException;

import javax.persistence.EntityTransaction;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.openrdf.elmo.ElmoQuery;
import org.openrdf.elmo.annotations.rdf;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

public class UrlReaderElmo {
	public enum DIV {
		КОМПОНЕНТЫ
	}

	public enum UNIT {
		ЯЩИК("ящик", "ящика", "ящиков"), //
		ШТУКА("штука", "штуки", "штук", "шт"), //
		КАМЕНЬ("камень", "камня", "камней"), //
		КРИСТАЛЛ("кристалл", "кристалла", "кристаллов"), //
		ШКАТУЛКА("шкатулка", "шкатулки", "шкатулок"), //
		КОРОБОК("коробок", "коробка", "коробков"), //
		ЛАРЕЦ("ларец", "ларца", "ларцов"), //
		КУСОК("кусок", "куска", "кусков"), //
		ПРОБИРКА("пробирка", "пробирки", "пробирок"), //
		РЕТОРТА("реторта", "реторты", "реторт"), //
		КОЛБА("колба", "колбы", "колб"), //
		БАНКА("банка", "банки", "банок"), //
		КАТУШКА("катушка", "катушки", "катушек"), //
		ГРИБ("гриб", "гриба", "грибов"), //
		МИЛЛИЛИТР("мл"), //
		;

		private final String[] unitStr;

		private UNIT(final String... unitStr) {
			this.unitStr = unitStr;
		}

		public boolean equals(final String str) {
			for (final String unit : unitStr) {
				if (str.endsWith(unit)) {
					return true;
				}
			}
			return false;
		}
	}

	@rdf(":Component")
	public static interface Component {
		@rdf(":item")
		Item getItem();

		void setItem(Item item);

		@rdf(":count")
		double getCount();

		void setCount(double count);

		// private final UNIT unit;

	}

	@rdf(":Item")
	public static interface Item {
		@rdf("http://xmlns.com/foaf/0.1/name")
		String getName();

		void setName(String name);
	}

	@rdf(":Recipe")
	public static interface Recipe {
		@rdf(":image")
		String getImage();

		void setImage(String image);

		@rdf(":uri")
		String getUri();

		void setUri(String uri);

		@rdf("http://xmlns.com/foaf/0.1/name")
		String getName();

		void setName(String name);

		@rdf(":components")
		List<Component> getComponents();

		void setComponents(List<Component> components);

		@rdf(":time")
		int getTime();

		void setTime(int time);

		@rdf(":event")
		String getEvent();

		void setEvent(String event);

	}

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
//				try (final InputStream is = getUrlStream(url)) {
				final List<Recipe> recipes;
				try (final InputStream is = getFileStream("jeweller.html")) {
					final HtmlCleaner c = new HtmlCleaner();
					final TagNode rootNode = c.clean(is);
					recipes = parseRecipes(rootNode, site, url, repo);
					for (final Recipe recipe : recipes) {
						System.out.println(recipe);
					}
				}
			} finally {
				transaction.commit();
			}

			// repo.add(recipes);
			// repo.commit();
			// final URI uri1 = new URIImpl("uri:uri1");
			// repo.connection.add(uri1, uri1, uri1);
			System.out.println("----- updated ------");
			repo.print();
			int recipesCount = 0;
			for (final Recipe recipe : repo.getElmoManager().findAll(
					Recipe.class)) {
				recipesCount++;
			}
			System.out.println("Recipes: " + recipesCount);
			int componentsCount = 0;
			for (final Component component : repo.getElmoManager().findAll(
					Component.class)) {
				componentsCount++;
			}
			System.out.println("Components: " + componentsCount);
			int itemsCount = 0;
			for (final Item item : repo.getElmoManager().findAll(Item.class)) {
				itemsCount++;
			}
			System.out.println("Items: " + itemsCount);
		}
	}

	private static InputStream getFileStream(final String fileName)
			throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(fileName));
	}

	private static List<Recipe> parseRecipes(final TagNode rootNode,
			final String siteUrl, final String pageUrl,
			final ElmoInfrastructure repo) {
		final List<Recipe> result = new ArrayList<Recipe>();
		for (final TagNode recipeNode : rootNode.getElementsByAttValue("class",
				"entry", true, false)) {
			String image = null;
			String uri = null;
			String name = null;
			String event = null;
			Integer time = null;
			DIV div = null;
			final List<Component> components = new ArrayList<>();
			for (final TagNode td : recipeNode.getAllElements(true)) {
				if ("td".equals(td.getName())) {
					final String tdClass = td.getAttributeByName("class");
					if ("image".equals(tdClass)) {
						for (final TagNode node : td.getAllElements(true)) {
							if ("img".equals(node.getName())) {
								final String _image = node
										.getAttributeByName("src");
								if (image == null) {
									image = siteUrl + _image;
								}
							}
						}
					} else if ("info".equals(tdClass)) {
						for (final TagNode node : td.getAllElements(true)) {
							final String nodeName = node.getName();
							if ("a".equals(nodeName)) {
								final String href = node
										.getAttributeByName("name");
								if (href != null) {
									uri = pageUrl + "#" + href;
								}
							} else if ("div".equals(nodeName)) {
								final String nodeClass = node
										.getAttributeByName("class");
								final String text = node.getText().toString();
								if ("name".equals(nodeClass)) {
									name = text;
								} else if ("header".equals(nodeClass)) {
									if ("Компоненты".equals(text)) {
										div = DIV.КОМПОНЕНТЫ;
									} else {
										div = null;
									}
								} else if (nodeClass == null) {
									if (DIV.КОМПОНЕНТЫ.equals(div)) {
										if (text.startsWith("Время: ")) {
											final String timeStr = text
													.substring(6).trim();
											if (timeStr.endsWith(" сек")) {
												time = Integer.valueOf(timeStr
														.substring(0, timeStr
																.length() - 4));
											} else {
												throw new UnknownFormatConversionException(
														"Unknown time type: "
																+ timeStr);
											}
										} else if (text
												.startsWith("Глобальное событие: ")) {
											final String _event = text
													.substring(20).trim();
											if ((event == null)
													&& !_event.isEmpty()) {
												event = _event;
											}
										} else {
											final String componentName = text
													.substring(0,
															text.indexOf(':'))
													.trim();
											final String countStr = text
													.substring(
															componentName
																	.length() + 2)
													.trim();
											UNIT unit = null;
											for (final UNIT u : UNIT.values()) {
												if (u.equals(countStr)) {
													unit = u;
													break;
												}
											}
											if (unit != null) {
												final Component component = repo
														.getElmoManager()
														.create(Component.class);
												Item item = findItem(repo,
														componentName);
												if (item == null) {
													item = repo
															.getElmoManager()
															.create(Item.class);
													item.setName(componentName);
												}
												component.setItem(item);
												component
														.setCount(Double
																.valueOf(countStr
																		.substring(
																				0,
																				countStr.lastIndexOf(' '))));
												components.add(component);
											} else {
												throw new UnknownFormatConversionException(
														"Unknown count type: "
																+ text);
											}
										}
									}
								}
							}
						}
					}

				}
			}
			final Recipe recipe = repo.getElmoManager().create(Recipe.class);
			recipe.setUri(uri);
			recipe.setName(name);
			recipe.setImage(image);
			recipe.setComponents(components);
			recipe.setTime(time);
			recipe.setEvent(event);
			result.add(recipe);
		}
		return result;
	}

	private static Item findItem(final ElmoInfrastructure repo,
			final String name) {
		final String queryStr = "SELECT item FROM {item} <http://xmlns.com/foaf/0.1/name> {name}, {item} a {type}";
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

	@SuppressWarnings("unused")
	private static InputStream getUrlStream(final String fileURL)
			throws IOException {
		final HttpUriRequest get = new HttpGet(fileURL);
		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpParams params = client.getParams();
		params.setParameter(
				org.apache.http.params.CoreConnectionPNames.SO_TIMEOUT, 2000);
		client.setParams(params);
		HttpResponse response;
		try {
			response = client.execute(get);
		} catch (final ConnectException e) {
			// Add some context to the exception and rethrow
			throw new IOException("ConnectionException trying to GET "
					+ fileURL, e);
		}

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new FileNotFoundException("Server returned "
					+ response.getStatusLine());
		}

		// Get the input stream
		final BufferedInputStream bis = new BufferedInputStream(response
				.getEntity().getContent());

		// Read the file and stream it out
		return bis;
	}

}
