import org.openrdf.elmo.annotations.rdf;


@rdf(":Item")
public interface Item {
	@rdf("http://xmlns.com/foaf/0.1/name")
	String getName();

	void setName(String name);
}