
public class RoutingTable {
	static int size = 10 + 1;
	int timeout = 0;
	int[] timeoutArray = new int[size];
	boolean full = false;
	DistanceVector[] table = new DistanceVector[size];
	DistanceVector[][] edge = new DistanceVector[size][size];
	DistanceVector[] pathList = new DistanceVector[size];
	int numTargets = 0;
	final static DistanceVector blank = new DistanceVector("blank", 1000);
	
	public RoutingTable(DistanceVector[] self, int time){
		for(int i = 1; i<size; i++){
			pathList[i] = new DistanceVector(blank.getDestination(), blank.getCost());
			for(int n = 0; n<size; n++){
				edge[i][n] = new DistanceVector(blank.getDestination(), blank.getCost());
			}
		}
		table = self;
		for(int i = 0; i<size; i++){
			edge[0][i] = new DistanceVector(table[i].getDestination(), table[i].getCost());
			edge[i][0] = new DistanceVector(table[i].getDestination(), 0);
			if(!table[i].getDestination().equals("blank")){
				pathList[i] = new DistanceVector(table[0].getDestination(), table[0].getCost());
			}
			timeoutArray[i] = 0;
		}
		timeout = time * 3;
		
	}
	
	//Linkup sees if the destination is already in the edge table. If it is, it
	//updates the cost, if not then it checks if there is space in the table,
	//and attempts to place the new distance vector in the table. 
	public void linkUp(String input){
		boolean newLink = false;
		boolean found = false;
		String[] splitter = input.split(" ");
		String destination = splitter[0];
		double cost = Double.parseDouble(splitter[1]);
		
		for(int i = 1; i<size; i++){
			if(destination.equals(edge[0][i].getDestination())){
				edge[0][i].setCost(cost);
				found = true;
			}
		}
		
		if(found == false){
			for(int i = 1; i<size; i++){
				if(edge[0][i].getDestination().equals("blank") && found == false){
					DistanceVector temp = new DistanceVector(destination, cost);
					edge[0][i] = temp;
					found = true;
					newLink = true;
				}
			}
		}
		
		if(newLink == true){
			boolean exists = false;
			found = false;
			//Finds if this is already in your routing table. 
			for(int i = 1; i<size; i++){
				if(table[i].getDestination().equals(destination) && exists == false){
					exists = true;
					found = true;
				}
			}
			if(exists == false){
				for(int i = 1; i<size; i++){
					if(edge[i][0].getDestination().equals("blank") && found == false){
						edge[i][0] = new DistanceVector(destination, 0);
						found = true;
					}
				}
			
				found = false;
				for(int i = 1; i<size; i++){
					if(table[i].getDestination().equals("blank") && found == false){
						table[i] = new DistanceVector(destination, cost);
						found = true;
					}
				}
			}
		}
		
		if(found == false){
			full = true;
			System.out.println("Routing Table full.");
		}
		else{
			System.out.println("Linkup successful.");
		}
	}
	
	//Checks if the destination is in the table, if it is then it sets the cost
	//to infinity, if not then it displays an error.
	public void linkDown(String destination){
		boolean found = false;
		
		for(int i = 1; i<size; i++){
			if(destination.equals(edge[0][i].getDestination())){
				found = true;
				edge[0][i].setCost(1000);
				System.out.println("Linkdown succesful on " + edge[0][i].getDestination());
			}
		}
		
		if(found == false){
			System.out.println("Destination not connected to this node.");
		}
	}
	
	//costs are calculated when you receive a new distance vector.(?)
	public void receive(byte[] updateBytes){
		String update = new String(updateBytes, 0, updateBytes.length);
		String[] split1 = update.split("U:U");
		update = split1[1];
		boolean found = false;
		DistanceVector[] temp = new DistanceVector[size];
		String[] temp2 = update.split("\n");
		for(int i = 0; i<size; i++){
			String[] temp3 = temp2[i].split(" ");
			temp[i] = new DistanceVector(temp3[0], Double.parseDouble(temp3[1]));
		}
		
		//find the home address of the distance vector array that was just received.
		for(int i = 0; i<size; i++){
			if(edge[i][0].getDestination().equals(temp[0].getDestination())){
				found = true;
				for(int n = 0; n<size; n++){
					edge[i][n] = temp[n];
				}
				//Adds whatever new nodes the node has 
				for(int n = 0; n<size; n++){
					boolean found2 = false;
					boolean found3 = false;
					for(int w = 0; w<size; w++){
						if(edge[i][n].getDestination().equals(table[w].getDestination())){
							found2 = true;
						}
					}
					if(found2 == false){
						for(int w = 1; w<size; w++){
							if(table[w].getDestination().equals("blank") && found2 == false){
								for(int x = 0; x<size; x++){
									if(edge[i][0].getDestination().equals(table[x].getDestination())){
										table[w] = new DistanceVector(edge[i][n].getDestination(), table[x].getCost() + edge[i][n].getCost());
										found2 = true;
									}
								}
							}
						}
						for(int w = 1; w<size; w++){
							if(edge[w][0].getDestination().equals("blank") && found3 == false){
								edge[w][0] = new DistanceVector(edge[i][n].getDestination(), 0);
								found3 = true;
							}
						}
					}
					if(found2 == false){
						full = true;
					}
				}
				//end
			}
		}
		//If the home address isn't there, attempt to find a space to place it.
		if(found == false && full == false){
			for(int i = 0; i<size; i++){
				if(edge[i][0].getDestination().equals("blank") && found == false){
					for(int n = 0; n<size; n++){
						edge[i][n] = temp[n];
					}
					//begin
					for(int n = 0; n<size; n++){
						boolean found2 = false;
						boolean found3 = false;
						for(int w = 0; w<size; w++){
							if(edge[i][n].getDestination().equals(table[w].getDestination())){
								found2 = true;
							}
						}
						if(found2 == false){
							for(int w = 1; w<size; w++){
								if(table[w].getDestination().equals("blank") && found2 == false){
									for(int x = 0; x<size; x++){
										if(edge[i][0].getDestination().equals(table[x].getDestination())){
											table[w] = new DistanceVector(edge[i][n].getDestination(), table[x].getCost() + edge[i][n].getCost());
											found2 = true;
										}
									}
								}
							}
							for(int w = 1; w<size; w++){
								if(edge[w][0].getDestination().equals("blank") && found3 == false){
									edge[w][0] = new DistanceVector(edge[i][n].getDestination(), 0);
									found3 = true;
								}
							}
						}
						if(found2 == false){
							full = true;
						}
					}
					//end
					found = true;
				}				
			}
		}
		
		for(int i = 0; i<size; i++){
			if(edge[0][i].getDestination().equals(temp[0].getDestination())){
				timeoutArray[i] = 0;
			}
		}
		
		loop:for(int i = 0; i<size; i++){
			if(table[i].getDestination().equals("blank")){
				full = false;
				break loop;
			}
			full = true;
		}
		
		if(found == false || full == true){
			System.out.println("Routing table full, cannot add new entry");
		}
		
				
	}
	
	public void calculatePath(){
		for(int i=1; i<size; i++){
			if(table[i].getCost() != 1000){
			table[i].setCost(1000);
			}
		}
		for(int b = 0; b < size; b++){
		//Relaxing the edges
			for(int i = 0; i<size; i++){
				//Finds the current node
				if(table[i].getCost() < 1000){
					int n = 0;
					//Finds the location of the node in the edge table
					for(int x = 0; x<size; x++){
						if(table[i].getDestination().equals(edge[x][0].getDestination())){
							n = x;
						}
					}
					for(int w = 0; w<size; w++){
						//Finds all edges of the current node
						if(edge[n][w].getCost() < 1000){
							int temp = 0;
							//Finds the destination of the current edge in the table
							for(int a = 0; a<size; a++){
								if(table[a].getDestination().equals(edge[n][w].getDestination())){
									temp = a;
								}
							}
							//Compares the cost in the table to destination to calculated cost of 
							//node + edge
							if(table[temp].getCost() > table[i].getCost() + edge[n][w].getCost()){
								table[temp].setCost(table[i].getCost() + edge[n][w].getCost());
								pathList[temp] = new DistanceVector(table[i].getDestination(), table[i].getCost());
							}
						}
					}
				}
			}
		}
	}
	
	public void showRT(){
		for(int i = 1; i<size; i++){
			/*if(!table[i].getDestination().equals("blank") && table[i].getCost() != 1000){
				System.out.println("Destination = " + table[i].getDestination() + ", Cost = " + table[i].getCost());
			}*/
			if(!table[i].getDestination().equals("blank") && table[i].getCost() != 1000){
				int pathFinder = i;
				loop:while(!pathList[pathFinder].getDestination().equals(table[0].getDestination())){
					for(int x = 0; x<size; x++){
						if(pathList[pathFinder].getDestination().equals(table[x].getDestination())){
							pathFinder = x;
							if(pathList[pathFinder].getDestination().equals(table[0].getDestination())){
								break loop;
							}
						}
					}
				}
				System.out.println("Destination = " + table[i].getDestination() + ", Cost = " + table[i].getCost() + ", Link = " + table[pathFinder].getDestination() );
			}
		}	
	}
	
	public String nextStep(String input){
		String step = "";
		for(int i = 0; i<size; i++){
			if(table[i].getDestination().equals(input)){
				int pathFinder = i;
				loop:while(!pathList[pathFinder].getDestination().equals(table[0].getDestination())){
					for(int x = 0; x<size; x++){
						if(pathList[pathFinder].getDestination().equals(table[x].getDestination())){
							pathFinder = x;
							if(pathList[pathFinder].getDestination().equals(table[9].getDestination())){
								break loop;
							}
						}
					}
				}
				step = table[pathFinder].getDestination();
				
			}
		}
		return step;
	}
	
	//Returns an updated list after it changes. 
	public byte[] update(){
		String temp = "Update U:U";
		for(int i = 0; i<size; i++){
			temp = temp + edge[0][i].converter();
		}
		byte[] buf = new byte[1024];
		buf = temp.getBytes();
		return buf;
	}
	
	//Takes in the string from the update, checks the updated table against what it has for
	//that node. If they're similar it doesn't need to update, otherwise it should update
	// True is does not need to update, false is opposite.
	public boolean updateCheck(byte[] updateBytes){
		String update = new String(updateBytes, 0, updateBytes.length);
		String[] split1 = update.split("U:U");
		update = split1[1];
		boolean found = true;
		DistanceVector[] temp = new DistanceVector[size];
		String[] temp2 = update.split("\n");
		for(int i = 0; i<size; i++){
			String[] temp3 = temp2[i].split(" ");
			temp[i] = new DistanceVector(temp3[0], Double.parseDouble(temp3[1]));
		}
		//find the home address of the distance vector array that was just received.
		for(int i = 1; i<size; i++){
			if(edge[i][0].getDestination().equals(temp[0].getDestination())){
				for(int n = 1; n<size; n++){
					if(!edge[i][n].getDestination().equals(temp[n]) || edge[i][n].getCost() != temp[n].getCost()){
						found = false;
					}
				}
			}
		}
		return found;		
	}
	
	public String[] targetList(){
		numTargets = 0;
		String[] targets = new String[size];
		int n = 0;
		for(int i = 1; i<size; i++){
			if(table[i].getCost() != 1000){
				targets[n] = table[i].getDestination();
				n++;
			}
		}
		numTargets = n;
		return targets;
	}
	
	public int getNumTargets(){
		int temp = 0;
		temp = temp + numTargets;
		return temp;
	}
	
	public void showEdge(String input){
		boolean found = false;
		for(int w = 0; w<size; w++){
			if(edge[w][0].getDestination().equals(input)){
				found = true;
				for(int i = 1; i<size; i++){
					if(!edge[w][i].getDestination().equals("blank") && edge[w][i].getCost() != 1000){
						System.out.println("Destination = " + edge[w][i].getDestination() + ", Cost = " + edge[w][i].getCost());
					}
				}	
			}
		}
		if(found == false){
			System.out.println("node does not exist in edge table");
		}
	}
	
	public void impendingTimer(){
		for(int i = 1; i<size; i++){
			if(edge[0][i].getCost() != 1000 ){
			timeoutArray[i] = timeoutArray[i] + 1;
			if(timeoutArray[i] > timeout){
				this.linkDown(edge[0][i].getDestination());
				this.calculatePath();
			}
			}
		}
	}
	
	public void showPath(){
		for(int i = 0; i<size; i++){
			/*if(table[i].getDestination().equals(input)){
				System.out.println(pathList[i].getDestination());
			}*/
			System.out.println(pathList[i].getDestination());
		}
	}

	public String destinationCheck(){
		return table[0].getDestination();
	}
}
