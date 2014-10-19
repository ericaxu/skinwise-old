package src.models.userdata;

import src.models.BaseModel;
import src.models.data.Product;
import src.models.user.User;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = UserProductList.TABLENAME)
public class UserProductList extends BaseModel {
	@Column(name = "_key", length = 256)
	private String key;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	private Product product;

	//Getters

	public String getKey() {
		return key;
	}

	public User getUser() {
		return user;
	}

	public Product getProduct() {
		return product;
	}
	//Setters

	public void setKey(String key) {
		this.key = key;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
	//Static

	public static final String TABLENAME = "user_product_list";

	public static Finder<Long, UserProductList> find = new Finder<>(Long.class, UserProductList.class);

	public static UserProductList byId(long id) {
		return find.byId(id);
	}

	public static List<UserProductList> byUser(User user) {
		return find.where()
				.eq("user", user)
				.findList();
	}
}
