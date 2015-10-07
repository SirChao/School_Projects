import java.io.Serializable;

public class File_Chunk implements Serializable {

	private static final long serialVersionUID = 1L;
	private int sequence;
	private long fileSize;
	private byte[] fileData;
	private String destination;
	private String path = "";
	
	public File_Chunk(String s, int i, byte[] data){
		this.destination = s;
		this.sequence = i;
		this.fileData = data;
		fileSize = fileData.length;
	}
	
	public int getSequence(){
		return this.sequence;
	}
	
	public long getFileSize(){
		return this.fileSize;
	}

	
	public String getDestination(){
		return this.destination;
	}
	
	public byte[] getFileData(){
		return this.fileData;
	}
	
	public String getPath(){
		return path;
	}
	public void addPath(String s){
		if(path.equals("")){
			path = path + s;
		}
		else{
		path = path + ", " + s;
		}
	}
	
}
