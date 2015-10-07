//Nicholas Chao nkc2116
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.*;
public class rsaGenerator {

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, FileNotFoundException, UnsupportedEncodingException {
		//Generate private and public keys for client 1
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		KeyPair pair = keyGen.genKeyPair();
		byte[] publicKey = pair.getPublic().getEncoded();
		StringBuffer publicString = new StringBuffer();
		for (int i = 0; i<publicKey.length; ++i){
			publicString.append(Integer.toHexString(0x0100 + (publicKey[i] & 0x00FF)).substring(1));
		}
		byte[] privateKey = pair.getPrivate().getEncoded();
		StringBuffer privateString = new StringBuffer();
		for (int i = 0; i<privateKey.length; ++i){
			privateString.append(Integer.toHexString(0x0100 + (privateKey[i] & 0x00FF)).substring(1));
		}
		
		//0 is client 1's public key, 1 is client 1's private key, 2 is client 2's public key
		//and 3 is client 2's private key
		String[] keys = new String[4];
		keys[0] = publicString.toString();
		keys[1] = privateString.toString();
		
		//Creating the keys for client 2
		pair = keyGen.genKeyPair();
		publicKey = pair.getPublic().getEncoded();
		publicString = new StringBuffer();
		for (int i = 0; i<publicKey.length; ++i){
			publicString.append(Integer.toHexString(0x0100 + (publicKey[i] & 0x00FF)).substring(1));
		}
		privateKey = pair.getPrivate().getEncoded();
		privateString = new StringBuffer();
		for (int i = 0; i<privateKey.length; ++i){
			privateString.append(Integer.toHexString(0x0100 + (privateKey[i] & 0x00FF)).substring(1));
		}
		
		//0 is client 1's public key, 1 is client 1's private key, 2 is client 2's public key
		//and 3 is client 2's private key
		keys[2] = publicString.toString();
		keys[3] = privateString.toString();
		
		PrintWriter keys1 = new PrintWriter("RSA_Keys1.txt", "UTF-8");
		keys1.println(keys[0]);
		keys1.println(keys[1]);
		keys1.println(keys[2]);
		keys1.close();
		
		PrintWriter keys2 = new PrintWriter("RSA_Keys2.txt", "UTF-8");
		keys2.println(keys[2]);
		keys2.println(keys[3]);
		keys2.println(keys[0]);
		keys2.close();
		
	}

}
