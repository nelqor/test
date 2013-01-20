package javabeans.elmo;
import org.openrdf.elmo.annotations.rdf;


@rdf(":Item")
public interface Item {
	@rdf(":name")
	String getName();

	void setName(String name);
}