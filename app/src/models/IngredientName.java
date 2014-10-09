package src.models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
public class IngredientName extends Model {
	@Id
	private long id;

	@Column(length = 1024, unique = true)
	private String name;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "ingredient_id", referencedColumnName = "id")
	private Ingredient ingredient;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Ingredient getIngredient() {
		return ingredient;
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
