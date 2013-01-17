import org.openrdf.elmo.annotations.rdf;

@rdf(":Component")
public interface Component {
	@rdf(":item")
	Item getItem();

	void setItem(Item item);

	@rdf(":count")
	double getCount();

	void setCount(double count);

	// private final UNIT unit;

}