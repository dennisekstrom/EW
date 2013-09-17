package trading.util;

/**
 * A struct for FXCM trading user
 * 
 * @author Tobias W
 * 
 */

public class User {

	private String id = "";
	private String password = "";
	private String connection = "";
	private String url = "";

	public User(String id, String pass, String url, String con) {
		this.id = id;
		this.password = pass;
		this.url = url;
		this.connection = con;
	}

	public String getId() {
		return id;
	}

	public String getConnection() {
		return connection;
	}

	public void setConnection(String connection) {
		this.connection = connection;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPassword() {
		return password;
	}

}
