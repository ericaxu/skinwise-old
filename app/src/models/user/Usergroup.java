package src.models.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.List;

@Entity
public class Usergroup extends Permissible {

	@Column(length = 255, unique = true)
	private String name;

	//Getters

	public String getName() {
		return name;
	}

	//Setters

	public void setName(String name) {
		this.name = name;
	}

	//Others

	@Override
	protected Permissible getParent() {
		return null;
	}

	//Static

	private static Finder<Long, Usergroup> find = new Finder<>(Long.class, Usergroup.class);

	public static List<Usergroup> all() {
		return find.all();
	}

	public static Usergroup byId(long id) {
		return find.byId(id);
	}

	public static Usergroup byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}
}
