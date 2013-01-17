package main;

import infrastructure.ElmoInfrastructure;
import infrastructure.Finder;

import java.util.ArrayList;
import java.util.List;
import java.util.UnknownFormatConversionException;

import javabeans.RecipeComponent;
import javabeans.Item;
import javabeans.Recipe;
import javabeans.ItemUnit;

import org.htmlcleaner.TagNode;

public class RecipesParser {

	public enum DIV {
		КОМПОНЕНТЫ
	}
	static List<Recipe> parseRecipes(final TagNode rootNode,
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
			final List<RecipeComponent> components = new ArrayList<>();
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
											ItemUnit unit = null;
											for (final ItemUnit u : ItemUnit.values()) {
												if (u.equals(countStr)) {
													unit = u;
													break;
												}
											}
											if (unit != null) {
												final RecipeComponent component = repo
														.getElmoManager()
														.create(RecipeComponent.class);
												Item item = Finder.findItem(repo,
														Item.class,
														"name",
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

}
