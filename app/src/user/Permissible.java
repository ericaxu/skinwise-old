package src.user;

public interface Permissible {
	public void addPermission(String permission);

	public void removePermission(String permission);

	public boolean hasPermission(String permission);
}
