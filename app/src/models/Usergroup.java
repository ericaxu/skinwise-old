package src.models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Usergroup extends Model {
	@Id
	private long id;

	@Column(length = 512, unique = true)
	private String name;

	public Usergroup(String name) {
		this.name = name;
	}

	@ManyToMany(fetch = FetchType.EAGER)
	private Set<Permission> permissions = new HashSet<>();

	public void addPermission(String permission) {
		permissions.add(Permission.get(permission));
	}

	public void removePermission(String permission) {
		permissions.remove(Permission.get(permission));
	}

	public boolean hasPermission(String permission) {
		return permissions.contains(Permission.get(permission));
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
