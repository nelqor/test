package javabeans.elmo;
import java.util.List;
import java.util.Set;

import org.openrdf.elmo.annotations.rdf;


@rdf(":Recipe")
public interface Recipe {
	@rdf(":image")
	String getImage();

	void setImage(String image);

	@rdf(":uri")
	String getUri();

	void setUri(String uri);

	@rdf(":name")
	String getName();

	void setName(String name);

	@rdf(":components")
	Set<RecipeComponent> getComponents();

	void setComponents(Set<RecipeComponent> components);

	@rdf(":time")
	int getTime();

	void setTime(int time);

	@rdf(":event")
	String getEvent();

	void setEvent(String event);

}