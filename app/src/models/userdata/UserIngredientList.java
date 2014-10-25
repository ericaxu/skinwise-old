package src.models.userdata;

import src.models.util.BaseModel;
import src.models.data.Ingredient;
import src.models.user.User;

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

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "ingredient_id", referencedColumnName = "id")
	private Ingredient ingredient;

	//Getters

	public String getKey() {
		return key;
	}

	public User getUser() {
		return user;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	//Setters

	public void setKey(String key) {
		this.key = key;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
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
