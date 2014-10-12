package src.models.data;

import src.models.BaseModel;

import javax.persistence.*;
import java.util.List;

@Entity
public class IngredientName extends BaseModel {

	@Column(length = 1024)
	private String name;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinColumn(name = "ingredient_id", referencedColumnName = "id")
	private Ingredient ingredient;

	//Getters

	public String getName() {
		return name;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	//Setters

	public void setName(String name) {
		this.name = name;
	}

	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	//Static

	public static Finder<Long, IngredientName> find = new Finder<>(Long.class, IngredientName.class);

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
