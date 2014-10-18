package src.models.data;

import org.apache.commons.lang3.text.WordUtils;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = IngredientName.TABLENAME)
public class IngredientName extends NamedModel {

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinColumn(name = "ingredient_id", referencedColumnName = "id")
	private Ingredient ingredient;

	//Getters

	public String getDisplayName() {
		return WordUtils.capitalizeFully(getName());
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	//Setters

	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	//Static

	public static final String TABLENAME = "ingredient_name";

	public static Finder<Long, IngredientName> find = new Finder<>(Long.class, IngredientName.class);

	public static List<IngredientName> all() {
		return find.all();
	}

	public static IngredientName byId(long id) {
		return find.byId(id);
	}

	public static IngredientName byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}

	public static List<IngredientName> search(String query, int limit, int page) {
		return find.where()
				.like("name", query)
				.orderBy("name")
				.findPagingList(limit)
				.getPage(page)
				.getList();
	}
}
