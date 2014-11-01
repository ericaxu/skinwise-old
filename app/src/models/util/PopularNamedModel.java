package src.models.util;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class PopularNamedModel extends NamedModel {
	private long popularity;

	//Getters

	public long getPopularity() {
		return popularity;
	}

	//Setters

	public void setPopularity(long popularity) {
		this.popularity = popularity;
	}

	//Others

	public void incrementPopularity(long increment) {
		synchronized (this) {
			if (this.popularity >= 0) {
				this.setPopularity(this.popularity + increment);
				this.save();
			}
		}
	}
}
