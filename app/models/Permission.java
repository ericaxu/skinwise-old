package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Permission extends Model {
	@Id
	@Column(length = 512)
	private String name;

	public Permission(String name) {
		this.name = name;
	}

	//Static

	public static final String ADMIN_ALL = "ADMIN.ALL";
	private static Finder<String, Permission> find = new Finder<>(String.class, Permission.class);

	public static Permission get(String name) {
		Permission permission = find.byId(name);

		if (permission == null) {
			try {
				new Permission(name).save();
			}
			catch (Exception ignored) {
				//Tried to save permission twice?
			}
			permission = find.byId(name);
		}

		return permission;
	}
}
