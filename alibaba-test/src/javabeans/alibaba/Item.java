package javabeans.alibaba;

import org.openrdf.annotations.Iri;

@Iri(":Item")
public interface Item {
	@Iri(":name")
	public String getName();

	public void setName(String value);
}