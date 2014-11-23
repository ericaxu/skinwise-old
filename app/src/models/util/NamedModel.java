package src.models.util;

import com.avaje.ebean.annotation.Index;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class NamedModel extends BaseModel {
	@Index
	@Column(length = 767, nullable = false)
	private String name;

	@Column(length = 8191)
	private String description;

	//Getters

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	//Setters

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	//Others

	public boolean hasDescription() {
		return !description.isEmpty();
	}
}
