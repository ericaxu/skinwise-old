package src.models.userdata;

import src.App;
import src.models.data.Ingredient;
import src.models.user.User;
import src.models.util.BaseModel;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = UserIngredientList.TABLENAME)
public class UserIngredientList extends BaseModel {
	@Column(name = "_key", length = 255)
	private String key;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	private long ingredient_id;

	//Getters

	public String getKey() {
		return key;
	}

	public User getUser() {
		return user;
	}

	public long getIngredient_id() {
		return ingredient_id;
	}
	//Setters

	public void setKey(String key) {
		this.key = key;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setIngredient_id(long ingredient_id) {
		this.ingredient_id = ingredient_id;
	}

	//Others

	public Ingredient getIngredient() {
		return App.cache().ingredients.get(getIngredient_id());
	}

	public void setIngredient(Ingredient ingredient) {
		this.ingredient_id = BaseModel.getIdIfExists(ingredient);
	}

	//Static

	public static final String TABLENAME = "user_ingredient_list";

	public static Finder<Long, UserIngredientList> find = new Finder<>(Long.class, UserIngredientList.class);

	public static UserIngredientList byId(long id) {
		return find.byId(id);
	}

	public static List<UserIngredientList> byUser(User user) {
		return find.where()
				.eq("user", user)
				.findList();
	}
}
