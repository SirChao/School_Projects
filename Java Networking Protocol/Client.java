import java.io.*;
import java.util.*;
import java.net.*;


public class Client {

	public static void main(String[] args) throws IOException {
		Timer timer = new Timer();
		Timer timeoutTimer = new Timer();
		Timer updateTimer = new Timer();
		Scanner scanner = new Scanner(System.in);
		final File_Chunk[] product = new File_Chunk[4];
		
		//Reading the config file and placing all of the lines into an array.
		ConfigReader reader = null;
		try{
		reader = new ConfigReader(args[0]);
		}
		catch(FileNotFoundException e){
			System.out.println("Filename not valid");
			System.exit(0);
		}
		final RoutingTable RT = new RoutingTable(reader.getList(), reader.getTimeout());
		byte[] buf = new byte[1024*1000*50];
		final DatagramPacket packet = new DatagramPacket(buf, buf.length);
	
		int port = reader.getPort();
		int timeout = reader.getTimeout();
		
		//Creates the read only port
		final DatagramSocket read = new DatagramSocket(port);
		final DatagramSocket write = new DatagramSocket();
		read.setSoTimeout(10);
		System.out.println("Please input command.");
		String command = "";
		
		//The timer for ROUTE UPDATE
		timer.scheduleAtFixedRate(new TimerTask(){
			public void run(){
				try{
					read.receive(packet);
					if(packet != null){
						String update = new String(packet.getData(), 0, packet.getData().length);
						if(update.startsWith("Update")){
							if(!RT.updateCheck(packet.getData())){
								RT.receive(packet.getData());
								RT.calculatePath();
							}
						}
						else{
							byte[] incFile = packet.getData();
							ByteArrayInputStream in = new ByteArrayInputStream(incFile);
							ObjectInputStream is = new ObjectInputStream(in);
							File_Chunk fileChunk = (File_Chunk) is.readObject();
							if(fileChunk.getDestination().equals(RT.destinationCheck())){
								System.out.println("Chunk recieved! Path:");
								System.out.println(fileChunk.getPath());
								product[fileChunk.getSequence()-1] = fileChunk;
								if(product[0] != null && product[1] != null){
									byte[] seq1 = product[0].getFileData();
									byte[] seq2 = product[1].getFileData();
									System.out.println("Both chunks recieved, creating file.");
									FileOutputStream fileOutputStream = null;
									try{
										fileOutputStream = new FileOutputStream("Output");
										fileOutputStream.write(seq1);
										fileOutputStream.write(seq2);
										fileOutputStream.flush();
										fileOutputStream.close();
										System.out.println("File created");
									}
									catch(Exception e){
										e.printStackTrace();
									}
								}
							}
							else{
								fileChunk.addPath(RT.destinationCheck());
								ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
								ObjectOutputStream os = new ObjectOutputStream(outputStream);
								os.writeObject(fileChunk);
								byte[] sentData = outputStream.toByteArray();
								String temp = RT.nextStep(fileChunk.getDestination());
								String stepIP = temp.split(":")[0];
								String stepPort = temp.split(":")[1];
								DatagramPacket sentPacket = new DatagramPacket(sentData, sentData.length, InetAddress.getByName(stepIP), Integer.parseInt(stepPort));
								write.send(sentPacket);
								System.out.println("bouncing");
							}
						}
						
					}
					}
					catch(SocketTimeoutException e){
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}, 0, 500);
		
		timeoutTimer.scheduleAtFixedRate(new TimerTask(){
			public void run(){
				RT.impendingTimer();
			}
		}, 1000, 1000);
		
		updateTimer.scheduleAtFixedRate(new TimerTask()
		{
			public void run(){
				String[] sendList = RT.targetList();
				byte[] send = RT.update();
				for(int i = 0; i<RT.getNumTargets(); i++){
					String temp[] = sendList[i].split(":");
					DatagramPacket sent = null;
					try {
						sent = new DatagramPacket(send, send.length, InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
					} catch (NumberFormatException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						write.send(sent);
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}
			}
		}, 0, timeout*1000);
		
		//Does stuff while the sockets are still open
		while(!command.equals("CLOSE")){
			try{
			command = scanner.nextLine();
			if(command.startsWith("LINKUP ")){
				command = command.replace("LINKUP ", "");
				try{
				String[] ipTest = command.split(":");
				@SuppressWarnings("unused")
				InetAddress tester = InetAddress.getByName(ipTest[0]);
				RT.linkUp(command);
				RT.calculatePath();
				String[] sendList = RT.targetList();
				byte[] send = RT.update();
				for(int i = 0; i<RT.getNumTargets(); i++){
					String temp[] = sendList[i].split(":");
					DatagramPacket sent = new DatagramPacket(send, send.length, InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
					write.send(sent);
				}
				}
				catch(UnknownHostException e){
					System.out.println("invalid IP address");
				}
			}
			else if(command.startsWith("LINKDOWN ")){
				command = command.replace("LINKDOWN ", "");
				command = command.replace(" ", ":");
				RT.linkDown(command);
				RT.calculatePath();
				String[] sendList = RT.targetList();
				byte[] send = RT.update();
				for(int i = 0; i<RT.getNumTargets(); i++){
					String temp[] = sendList[i].split(":");
					DatagramPacket sent = new DatagramPacket(send, send.length, InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
					write.send(sent);
				}
			}
			else if(command.equals("SHOWRT")){
				RT.showRT();
			}
			else if(command.equals("CLOSE")){
				System.out.println("Socket closed.");
			}

			else if(command.startsWith("Edge")){
				String[] temp = command.split(" ");
				RT.showEdge(temp[1]);
			}
			else if(command.startsWith("Path")){
				//String[] temp = command.split(" ");
				RT.showPath();
			}
			else if (command.startsWith("TRANSFER")){
				try{
				command = command.replace("TRANSFER ", "");
				command = command.replace(" ", ":");
				try{
					File sender = new File(reader.getFile());
					String temp = RT.nextStep(command);
					String stepIP = temp.split(":")[0];
					String stepPort = temp.split(":")[1];
					DataInputStream diStream = new DataInputStream(new FileInputStream(sender));
					long length = (int) sender.length();
					int sequence = reader.getSequence();
					byte[] senderBytes = new byte[(int) length];
					int read1 = 0;
					int numread = 0;
					while(read1<senderBytes.length && (numread = diStream.read(senderBytes, read1, senderBytes.length - read1)) >= 0);{
						read1 = read1+numread;
					}
					File_Chunk chunkSend = new File_Chunk(command, sequence, senderBytes);
					chunkSend.addPath(RT.destinationCheck());
					diStream.close();
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(chunkSend);
					byte[] sentData = outputStream.toByteArray();
					DatagramPacket sendPacket = new DatagramPacket(sentData, sentData.length, InetAddress.getByName(stepIP), Integer.parseInt(stepPort) );
					write.send(sendPacket);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
			}
			else{
				System.out.println("Invalid command");
			}
			}
			catch(ArrayIndexOutOfBoundsException e){
				System.out.println("invalid arugment, retry your command with a valid argument");
			}
			
			
		}
		write.close();
		read.close();
		scanner.close();
		timer.cancel();
		timeoutTimer.cancel();
		updateTimer.cancel();
		
	}
	
	

}
