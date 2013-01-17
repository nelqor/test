import java.util.List;

import org.openrdf.elmo.annotations.rdf;


@rdf(":Recipe")
public interface Recipe {
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