package javabeans.alibaba;

import java.util.List;
import java.util.Set;

import org.openrdf.annotations.Iri;

@Iri(":Recipe")
public interface Recipe {
	@Iri(":image") 
	public String getImage();
	public void setImage(String image);

	@Iri(":uri") 
	public String getUri();
	public void setUri(String uri);

	@Iri(":name") 
	public String getName();
	public void setName(String name);

	@Iri(":components") 
	public Set<RecipeComponent> getComponents();
	public void setComponents(Set<RecipeComponent> components);

	@Iri(":time") 
	public int getTime();
	public void setTime(int time);

	@Iri(":event")
	public String getEvent();
	public void setEvent(String event);

}