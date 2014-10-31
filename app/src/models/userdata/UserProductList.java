package src.models.userdata;

import src.App;
import src.models.data.Product;
import src.models.user.User;
import src.models.util.BaseModel;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = UserProductList.TABLENAME)
public class UserProductList extends BaseModel {
	@Column(name = "_key", length = 255)
	private String key;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	private long product_id;

	//Getters

	public String getKey() {
		return key;
	}

	public User getUser() {
		return user;
	}

	public long getProduct_id() {
		return product_id;
	}

	//Setters

	public void setKey(String key) {
		this.key = key;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setProduct_id(long product_id) {
		this.product_id = product_id;
	}

	//Others

	public Product getProduct() {
		return App.cache().products.get(getProduct_id());
	}

	public void setProduct(Product product) {
		this.product_id = BaseModel.getIdIfExists(product);
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
