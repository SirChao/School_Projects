//Nicholas Chao nkc2116
import java.security.Key;


public class KeySalt{
		public Key k;
		public byte[] salt;
		
		public KeySalt(Key key, byte[] s){
			this.k = key;
			this.salt = s;
		}
		
		public Key getKey(){
			return this.k;
		}
		public byte[] getSalt(){
			return this.salt;
		}
	}
