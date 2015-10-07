//Nicholas Chao nkc2116
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.net.*;
import java.nio.ByteBuffer;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.io.*;
import java.util.*;

public class client1 {
	static String plainText;
	static String client1Public;
	static String client2Public;
	static String client1Private;
	static InetAddress address;
	static DatagramSocket socket;
	public static void main(String[] args) throws NoSuchPaddingException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, SignatureException, IOException {
		//Checks the amount of arguments
		if(args.length != 5){
			System.err.println("Please enter the correct number of arguments");
			System.exit(1);
		}
		//Checks to ensure the password is correct
		String password = args[0];
		if(password.length() != 16){
			System.err.println("Please enter a sixteen character password");
			System.exit(1);
		}	
		KeySalt keySalt = generateKey(password);
		Key key = keySalt.getKey();
		
		//The filename is relative to the directory of the executable.
		String filename = args[1];
		
		//This checks for a valid IP address
		String ipAddress = args[2];
		try {
			address = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e1) {
			System.out.println("Please enter a valid host or IP address");
			System.exit(1);
		}
		
		//Checks for the port number
		int port = 0;
		String portString = args[3];
		if(portString.contains(".")){
			System.err.println("Please enter a valid port number");
			System.exit(1);
		}
		try{
			port = Integer.parseInt(portString);
			if(port > 61000){
				System.err.println("Please enter a valid port number");
				System.exit(1);
			}
		}
		catch(Exception e){
			System.err.println("Please enter a valid port number");
			System.exit(1);
		}
		
		//The file with the RSA keys, the filename is relative to the directory of the executable
		String rsaFile = args[4];
		
		//Reading the contents of the plain text file and the RSA keys
		try{
			File plain = new File(filename);
			File rsa = new File(rsaFile);
			Scanner scan = new Scanner(plain);
			Scanner scanRSA = new Scanner(rsa);
			plainText = scan.useDelimiter("\\Z").next();
			client1Public = scanRSA.nextLine();
			client1Private = scanRSA.nextLine();
			client2Public = scanRSA.nextLine();
			scan.close();
			scanRSA.close();
			
		} catch(IOException e){
			System.out.println("Invalid input, please check either the text file or the RSA file");
			System.exit(1);
		}
		
		//Encrypting the plain text
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, key);
		AlgorithmParameters params = c.getParameters();
		byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		byte[] encryptedText = c.doFinal(plainText.getBytes("UTF-8"));
		byte[] salt = keySalt.getSalt();
		
		//Get the length of the encrypted Text
		int textLength = encryptedText.length;
		byte[] byteLength = ByteBuffer.allocate(8).putInt(textLength).array();
		//Encrypting the password and creating the signature
		PrivateKey privateKey = retrievePrivateKey(client1Private);
		PublicKey publicKey2 = retrievePublicKey(client2Public);
		byte[] signature = generateSignature(plainText, privateKey);
		byte[] signLength = ByteBuffer.allocate(8).putInt(signature.length).array();
		byte[] encryptPass = encryptPassword(password, publicKey2);
		
		byte[] buf = new byte[256];
		//Setting up the socket for writing to the server
		try{
		socket = new DatagramSocket(port);
		}catch(BindException e){
			System.out.println("Port is already in use.");
			System.exit(1);
		}
		//Sends all of the packets
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
		socket.send(packet);
		//It absorbs an extra message which is getting sent out.
		//packet = new DatagramPacket(buf, buf.length);
		//socket.receive(packet);
		//Ready to receive
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		packet = new DatagramPacket(iv, iv.length, address, port);
		socket.send(packet);
		packet = new DatagramPacket(salt, salt.length, address, port);
		socket.send(packet);
		packet = new DatagramPacket(encryptPass, encryptPass.length, address, port);
		socket.send(packet);
		packet = new DatagramPacket(byteLength, byteLength.length, address, port);
		socket.send(packet);
		packet = new DatagramPacket(encryptedText, encryptedText.length, address, port);
		socket.send(packet);
		packet = new DatagramPacket(signLength, signLength.length, address, port);
		socket.send(packet);
		packet = new DatagramPacket(signature, signature.length, address, port);
		socket.send(packet);

				
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		System.out.println("All data recieved, closing.");
		socket.close();
		

	}
	
	
	
	public static KeySalt generateKey(String pass) throws NoSuchAlgorithmException, InvalidKeySpecException{
		SecureRandom rng = new SecureRandom();
		byte[] salt = new byte[8];
		rng.nextBytes(salt);
		
		char[] passArray = pass.toCharArray();
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(passArray, salt, 1000, 128);
		SecretKey temp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(temp.getEncoded(), "AES");
		
		KeySalt temp2 = new KeySalt(secret, salt);
		return temp2;
				
	}

	public static byte[] generateSignature(String text, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		Signature sign = Signature.getInstance("SHA256withRSA");
		sign.initSign(privateKey);
		byte[] textArray = text.getBytes();
		sign.update(textArray);
		byte[] signature = sign.sign();
		return signature;
	}

	public static PrivateKey retrievePrivateKey(String key) throws InvalidKeySpecException, NoSuchAlgorithmException{
		byte[] keyData = DatatypeConverter.parseHexBinary(key);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(keyData));
		return privateKey;
	}
	
	public static PublicKey retrievePublicKey(String key) throws InvalidKeySpecException, NoSuchAlgorithmException{
		byte[] keyData = DatatypeConverter.parseHexBinary(key);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(keyData));
		return publicKey;
	}

	public static byte[] encryptPassword(String pass, PublicKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		byte[] cipherText = null;
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.ENCRYPT_MODE, key);
		cipherText = c.doFinal(pass.getBytes());
		return cipherText;
	}

	
}


