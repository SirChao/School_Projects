//Nicholas Chao nkc2116
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;
public class server {

	public static void main(String[] args) throws IOException{
		if(args.length != 3){
			System.err.println("Two port numbers and a mode required");
			System.exit(1);
		}
		if(!args[2].equals("u") && !args[2].equals("t")) {
			System.err.println("Please input a valid mode, u or t.");
			System.exit(1);
		}
		
		int portNumber1 = 0;
		String portString1 = args[0];
		if(portString1.contains(".")){
			System.err.println("Please enter a valid port for client 1");
			System.exit(1);
		}
		try{
			portNumber1 = Integer.parseInt(portString1);
			if(portNumber1 > 61000){
				System.err.println("Please enter a valid port for client 1");
				System.exit(1);
			}
		}
		catch(Exception e){
			System.err.println("Please enter a valid port for client 1");
			System.exit(1);
		}
		
		
		int portNumber2 = 0;
		String portString2 = args[1];
		if(portString2.contains(".")){
			System.err.println("Please enter a valid port for client 2");
			System.exit(1);
		}
		try{
			portNumber2 = Integer.parseInt(portString2);
			if(portNumber2 > 61000){
				System.err.println("Please enter a valid port for client 2");
				System.exit(1);
			}
		}
		catch(Exception e){
			System.err.println("Please enter a valid port for client 2");
			System.exit(1);
		}
		
		
		String mode = args[2];
		
		try{
			//Setting up the server so it connects to the clients on the port numbers
			DatagramSocket client1 = new DatagramSocket(portNumber1);
			DatagramSocket client2 = new DatagramSocket(portNumber2);
			
			//Gathering the information passed by client 1
			byte[] iv = new byte[16];
			byte[] salt = new byte[8];
			byte[] encryptPass = new byte[256];
			byte[] buf = new byte[256];
			byte[] byteLength = new byte[8];
			byte[] signLength = new byte[8];
			
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			client1.receive(packet);
			System.out.println("recieved1");
			InetAddress address = packet.getAddress();
			packet = new DatagramPacket(buf, buf.length, address, portNumber1);
			client1.send(packet);
			packet = new DatagramPacket(iv, iv.length);
			client1.receive(packet);
			packet = new DatagramPacket(salt, salt.length);
			client1.receive(packet);
			packet = new DatagramPacket(encryptPass, encryptPass.length);
			client1.receive(packet);
			packet = new DatagramPacket(byteLength, byteLength.length);
			client1.receive(packet);
			int textLength = ByteBuffer.wrap(byteLength).getInt();
			byte[] encryptedText = new byte[textLength];
			packet = new DatagramPacket(encryptedText, encryptedText.length);
			client1.receive(packet);
			packet = new DatagramPacket(signLength, signLength.length);
			client1.receive(packet);
			int sLength = ByteBuffer.wrap(signLength).getInt();
			byte[] signature = new byte[sLength];
			packet = new DatagramPacket(signature, signature.length);
			client1.receive(packet);
			//Telling client1 it's ok to close
			System.out.println("All data recieved");
			packet = new DatagramPacket(buf, buf.length, address, portNumber1);
			client1.send(packet);
			client1.close();
			
			packet = new DatagramPacket(buf, buf.length);
			client2.receive(packet);
			System.out.println("recieved1");
			address = packet.getAddress();
			
			//Untrusted mode actions
			if(mode.equals("u")){
				byte[] servData = null;
				try{
					File f = new File("serverdata");
					Scanner s = new Scanner(f);
					String serverData = s.useDelimiter("\\Z").next();
					servData = serverData.getBytes("UTF-8");
					s.close();
				} catch (FileNotFoundException e)
				{
					System.out.println("serverdata not found, please ensure that the file is in the same directory.");
					client2.close();
					System.exit(1);
				}
				packet = new DatagramPacket(iv, iv.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(salt, salt.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(encryptPass, encryptPass.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(byteLength, byteLength.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(servData, servData.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(signLength, signLength.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(signature, signature.length, address, portNumber2);
				client2.send(packet);
				System.out.println("All data sent");
			}
			
			//Trusted mode actions
			if(mode.equals("t")){
				packet = new DatagramPacket(iv, iv.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(salt, salt.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(encryptPass, encryptPass.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(byteLength, byteLength.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(encryptedText, encryptedText.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(signLength, signLength.length, address, portNumber2);
				client2.send(packet);
				packet = new DatagramPacket(signature, signature.length, address, portNumber2);
				client2.send(packet);
				System.out.println("All data sent");
			}
			packet = new DatagramPacket(buf, buf.length, address, portNumber1);
			client2.receive(packet);
			client2.close();
			System.out.println("All data recieved and sent, closing");
						
		}
		catch(IOException e){
			System.out.println(e.getMessage());
		} 
		
	}

}
