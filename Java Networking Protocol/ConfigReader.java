import java.io.*;
import java.net.InetAddress;

public class ConfigReader {
	private String ip = "";
	private static int size = 10+1;
	private BufferedReader in = null;
	private String[] clientList = new String[size];
	private DistanceVector[] vectorList = new DistanceVector[size];
	private int port = 0;
	private int timeout = 0;
	private String fileChunk = "";
	private int sequenceNum = 0;
	
	final static DistanceVector blank = new DistanceVector("blank", 1000);

	
	
	public ConfigReader(String config) throws IOException{
		//This reads the config file into an array.
		in = new BufferedReader(new FileReader(config));
		String temp = "";
		int i = 0;
		while(temp != null && i<size){
			temp = in.readLine();
			if(temp != null){
			clientList[i] = temp;
			i++;
			}
		}
		if(i == size){
			System.out.println("Routing Table full");
		}
		in.close();
		
		//This formats what was read into usable info.
		String[] self = clientList[0].split(" ");
		port = Integer.parseInt(self[0]);
		timeout = Integer.parseInt(self[1]);
		fileChunk = self[2];
		sequenceNum = Integer.parseInt(self[3]);
		ip = InetAddress.getLocalHost().getHostAddress();
		
		//Placing things into the distance vector (self)
		vectorList[0] = new DistanceVector(ip + ":" + port, 0);
		for(int n = 1; n<i; n++){
			String[] neighbor = clientList[n].split(" ");
			vectorList[n] = new DistanceVector(neighbor[0], Double.parseDouble(neighbor[1]));
		}
		if(i<size){
			for(int x = i; x<size; x++){
				vectorList[x] = blank;
			}
		}
	}
	
	public int getPort(){
		return port;
	}
	
	public int getTimeout(){
		return timeout;
	}
	
	public String getFile(){
		return fileChunk;
	}
	
	public int getSequence(){
		return sequenceNum;
	}
	
	public DistanceVector[] getList(){
		return vectorList;
	}
	
	public String getIP(){
		return ip;
	}
}
