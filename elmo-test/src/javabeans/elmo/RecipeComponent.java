package javabeans.elmo;
import org.openrdf.elmo.annotations.rdf;

@rdf(":RecipeComponent")
public interface RecipeComponent {
	@rdf(":item")
	Item getItem();

	void setItem(Item item);

	@rdf(":count")
	double getCount();

	void setCount(double count);

	@rdf(":unit")
	ItemUnit getUnit();
	void setUnit(ItemUnit unit);

}