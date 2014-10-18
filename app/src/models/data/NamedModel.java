package src.models.data;

import src.models.BaseModel;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class NamedModel extends BaseModel {
	@Column(length = 1024, nullable = false)
	private String name;

	@Column(length = 8192)
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
