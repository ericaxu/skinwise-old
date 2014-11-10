package src.models.util;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ManyToManyModel extends BaseModel {
	//This temporarily fixes a bug in Ebean 4.1.8
	//Refer to https://groups.google.com/forum/#!topic/ebean/lRLT9cTqSLQ
	public boolean manytomany;
	public abstract long getLeftId();
	public abstract long getRightId();
}
