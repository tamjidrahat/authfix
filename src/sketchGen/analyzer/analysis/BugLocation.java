package sketchGen.analyzer.analysis;

public class BugLocation {
	private String filePath;
	private String method;
	private int location;
	private int id;

	public BugLocation(String filepath, String method, int loc, int id) {
		filePath = filepath;
		this.method = method;
		location = loc;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

}
