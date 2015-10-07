import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ngramAnalyzer {

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static void main(String[] args) throws IOException {
		//Making sure the input is correct. The first argument is n, the second is s
		//the third is the input, and the fourth is the output.
		Integer n = 0;
		Integer s = 0;
		if(args.length != 4){
			System.out.println("Incorrect number of arguments, please try again.");
			System.exit(0);
		}
		
		if(Integer.parseInt(args[0]) > 3 || Integer.parseInt(args[0]) < 1){
			System.out.println("Please input a valid number for n, where  1 <= n <= 3.");
			System.exit(0);
		}
		n = Integer.parseInt(args[0]);
		
		if(Integer.parseInt(args[1]) > n || Integer.parseInt(args[1]) < 1){
			System.out.println("Please input a valid number for s, where 1 <= s <= n.");
			System.exit(0);
		}
		s = Integer.parseInt(args[1]);
		
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(args[2]));
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[3]), "UTF-8"));
		
		
		//Preparing the hashmap, the temporary holder for ngrams, and the temporary holder for the window.
		//The window contains the new bytes that were not included in the previous ngram. Leftovers is the number
		//of bytes that were from the last ngram. 
		HashMap<ByteBuffer, Integer> map = new HashMap<ByteBuffer, Integer>();
		byte[] ngram = new byte[n];
		byte[] window = new byte[s];
		int leftovers = n - s;
		int fileLength = (int) new File(args[2]).length();
		
		//Places bytes into the hashmap for the first n bytes.
		int i = n;
		in.read(ngram);
		int count = 1;
		ByteBuffer buff = ByteBuffer.wrap(new byte[n]);
		buff.put(ngram);
		buff.flip();
		map.put(buff, count);
		
		//Continues for the rest of the file until the last ngram.
		//The value of the hashmap is the number of times the ngram has appeared.
		while(i <= fileLength-s){
			window = new byte[s];
			in.read(window);
			//Moves the still relevant bytes from the previous ngram
			for(int x = 0; x < n; x++){
				if(x - s >= 0){
					ngram[x-s] = ngram[x];
				}
			}
			//Completes the ngram with the new bytes
			int w = 0;
			for(int x = leftovers; x < n; x++){
				ngram[x] = window[w];
				w++;
			}
			buff = ByteBuffer.wrap(new byte[n]);
			buff.put(ngram);
			buff.flip();
			//Places or updates the ngram in the map.
			if(map.containsKey(buff) == true){
				count = (Integer) map.get(buff);
				count = count + 1;
				map.remove(buff);
				map.put(buff, count);
			}
			if(map.containsKey(ByteBuffer.wrap(ngram)) == false){
				count = 1;
				map.put(buff, count);
			}
			i = i + s;
		}
		
		in.close();
		//Preparing the output array
		hexCount temp = new hexCount("temp", 0);
		hexCount[] holder = new hexCount[20];
		for(int m = 0; m<20; m++){
			holder[m] = temp;
		}
		//Finds the output and sorts it.
		for(Map.Entry<ByteBuffer, Integer> entry : map.entrySet()){
			buff = entry.getKey();
			count = entry.getValue();
			if(count > 5){
				byte[] b = new byte[n];
				buff.get(b);
				//Byte array to hex characters.
				char[] hexChars = new char[b.length * 2];
				for(int j = 0; j < b.length; j++){
					int v = b[j] & 0xFF;
					hexChars[j*2] = hexArray[v >>>4];
					hexChars[j*2+1] = hexArray[v & 0x0F];
				}
				String hex = new String(hexChars);
				hexCount hexHolder = new hexCount(hex, count);
				//Finds the lowest value in the holder array and compares it to temp. If temp is higher, then it 
				//replaces that value. 
				int low = 0;
				temp = holder[0];
				for(int m = 1; m<20; m++){
					if(temp.getCount() > holder[m].getCount()){
						temp = holder[m];
						low = m;
					}
				}
				if(hexHolder.getCount() > holder[low].getCount()){
					holder[low] = hexHolder;
				}
				//String end = "Hex: " + hex + " " + "Count: " + Integer.toString(count) + "\n";
				//out.write(end);
			}
		}
		for(int m = 0; m<20; m++){
			String end = "Hex: " + holder[m].getHex() + " " + "Count: " + Integer.toString(holder[m].getCount()) + "\n";
			out.write(end);
		}
		
		out.close();
	}

}
