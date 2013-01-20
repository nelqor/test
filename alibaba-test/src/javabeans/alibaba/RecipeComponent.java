package javabeans.alibaba;

import org.openrdf.annotations.Iri;

@Iri(":RecipeComponent")
public interface RecipeComponent {
	@Iri(":item")
	public Item getItem();
	public void setItem(Item item);

	@Iri(":count")
	public double getCount();
	public void setCount(double count);

	@Iri(":unit")
	public ItemUnit getUnit();
	public void setUnit(ItemUnit unit);

}