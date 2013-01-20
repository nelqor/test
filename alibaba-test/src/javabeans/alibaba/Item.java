package javabeans.alibaba;

import org.openrdf.annotations.Iri;

@Iri(Item.NS+"Item")
public interface Item {
	public static final String NS = "http://example.com/rdf/2013/rp#";

	@Iri(Item.NS+"name")
	public String getName();

	public void setName(String value);
}