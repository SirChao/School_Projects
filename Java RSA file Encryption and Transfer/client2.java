//Nicholas Chao nkc2116
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.*;
import java.util.Scanner;












import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;


public class client2 {
	static String client2Public;
	static String client2Private;
	static String client1Public;
	static InetAddress address;
	static DatagramSocket socket;
	static PublicKey publicKey1;
	static PrivateKey privateKey2;
	static byte[] text;
	public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, SignatureException {
		if(args.length != 3){
			System.err.println("Please enter the correct number of arguments");
			System.exit(1);
		}
		
		//This checks for a valid IP address
		String ipAddress = args[0];
		try {
			address = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e1) {
			System.out.println("Please enter a valid host or IP address");
			System.exit(1);
		}
		
		//Checks for the port number
		int port = 0;
		String portString = args[1];
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
		String rsaFile = args[2];
		
		try{
			File rsa = new File(rsaFile);
			Scanner scanRSA = new Scanner(rsa);
			client2Public = scanRSA.nextLine();
			client2Private = scanRSA.nextLine();
			client1Public = scanRSA.nextLine();
			scanRSA.close();
			
		} catch(IOException e){
			System.out.println("Invalid input, please check either the text file or the RSA file");
			System.exit(1);
		}
		
		//Loads the relevant public and private keys
		try{
		publicKey1 = retrievePublicKey(client1Public);
		privateKey2 = retrievePrivateKey(client2Private);
		}
		catch (InvalidKeySpecException e){
			System.out.println("Key is not valid");
			System.exit(1);
		}
		//Creates the socket and tells the server it's ready for information
		try{
			socket = new DatagramSocket(port);
			}catch(BindException e){
				System.out.println("Port is already in use.");
				System.exit(1);
			}		
		byte[] iv = new byte[16];
		byte[] salt = new byte[8];
		byte[] encryptPass = new byte[256];
		byte[] buf = new byte[256];
		byte[] byteLength = new byte[8];
		byte[] signLength = new byte[8];
		

		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
		socket.send(packet);
		
		System.out.println("Hi");
		//receives all the information
		packet = new DatagramPacket(iv, iv.length);
		socket.receive(packet);
		packet = new DatagramPacket(salt, salt.length);
		socket.receive(packet);
		packet = new DatagramPacket(encryptPass, encryptPass.length);
		socket.receive(packet);
		packet = new DatagramPacket(byteLength, byteLength.length);
		socket.receive(packet);
		int textLength = ByteBuffer.wrap(byteLength).getInt();
		byte[] encryptedText = new byte[textLength];
		packet = new DatagramPacket(encryptedText, encryptedText.length);
		socket.receive(packet);
		packet = new DatagramPacket(signLength, signLength.length);
		socket.receive(packet);
		int sLength = ByteBuffer.wrap(signLength).getInt();
		byte[] signature = new byte[sLength];
		packet = new DatagramPacket(signature, signature.length);
		socket.receive(packet);
		//Telling the server it's ok to close and closes the socket
		System.out.println("All data recieved");
		packet = new DatagramPacket(buf, buf.length, address, port);
		socket.send(packet);
		socket.close();
		
		//Recreates the key using the salt and the encrypted password
		String password = decryptPass(encryptPass, privateKey2);
		SecretKey key = generateKey(password, salt);
		//Decrypts the encrypted text file using the key and the iv
		try{
			text = decryptText(encryptedText, iv, key);
		}catch(Exception e){
			text = encryptedText;
		}
		
		//Checks the signature and displays the message
		boolean valid = checkSig(publicKey1, signature, text);
		if(valid){
			System.out.println("Verification Passed");
		}
		if(!valid){
			System.out.println("Verification Failed");
		}
		//prints the file out to client2data
		String plainText = new String(text, "UTF-8");
		PrintWriter write = new PrintWriter("client2data", "UTF-8");
		write.print(plainText);
		write.close();
		

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
	
	public static boolean checkSig(PublicKey key, byte[] sig, byte[] text) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initVerify(key);
		signature.update(text);
		boolean verifies = signature.verify(sig);
		return verifies;
	}

	public static SecretKey generateKey(String pass, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException{
		char[] passArray = pass.toCharArray();
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(passArray, salt, 1000, 128);
		SecretKey temp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(temp.getEncoded(), "AES");
		
		return secret;
	}

	public static byte[] decryptText(byte[] text, byte[] iv, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		byte[] plainText = c.doFinal(text);
		return plainText;
	}

	public static String decryptPass(byte[] pass, PrivateKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.DECRYPT_MODE,key);
		String s = new String(c.doFinal(pass));
		return s;
		
	}
}
