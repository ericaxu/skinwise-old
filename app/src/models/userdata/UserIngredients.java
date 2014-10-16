package src.models.userdata;

import src.models.BaseModel;
import src.models.data.Ingredient;
import src.models.user.User;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = UserIngredients.TABLENAME)
public class UserIngredients extends BaseModel {
	@Column(length = 256)
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

	public static final String TABLENAME = "user_ingredients";

	public static Finder<Long, UserIngredients> find = new Finder<>(Long.class, UserIngredients.class);

	public static UserIngredients byId(long id) {
		return find.byId(id);
	}

	public static List<UserIngredients> byUser(User user) {
		return find.where()
				.eq("user", user)
				.findList();

	}

}
