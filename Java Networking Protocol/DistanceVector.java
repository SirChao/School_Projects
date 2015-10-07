import java.net.InetAddress;
import java.net.UnknownHostException;


public class DistanceVector {
	private String destination;
	private double cost;
	
	public DistanceVector(String dest, double c){
		this.destination = dest;
		this.cost = c;
	}
	
	public String converter(){
		return (destination + " " + cost + "\n");
	}
	
	public String getDestination(){
		return this.destination;
	}
	
	public double getCost(){
		return this.cost;
	}
	
	public void setCost(double num){
		this.cost = num;
	}
	
	public void setDestination(String s){
		this.destination = s;
	}
	
	public InetAddress getIP() throws UnknownHostException{
		String[] temp = destination.split(":");
		return InetAddress.getByName(temp[0]);
	}
	
	public int getPort(){
		String[] temp = destination.split(":");
		return Integer.parseInt(temp[1]);
	}

}
