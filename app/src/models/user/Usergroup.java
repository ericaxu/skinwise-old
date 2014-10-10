package src.models.user;

import src.models.Permissible;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.List;

@Entity
public class Usergroup extends Permissible {

	@Column(length = 512, unique = true)
	private String name;

	public Usergroup(String name) {
		this.name = name;
	}

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
