Nicholas Chao
nkc2116


Network Security Assignment 1

-Generation of the RSA keys was done through the use of the java's KeyGen function, which are then
written into two text files, RSA_Keys1.txt and RSA_Keys2.txt. I've included the class that generates
the keys in the source files. It also doesn't matter which client receives which set of Keys, only that
they recieve different sets of keys. What does matter is that the order of the RSA keys is maintained, 
in that the first key is the client's public key, the second key is the client's private key, and the 
last key is the other client's public key. 

-To run the programs you need to first compile all of them, javac *.java works, and then run the server.
After running the server, run client 1 then client 2. This order is important. The server is run by the
port number for client 1 then the port number for client 2, and then the mode. Client 1 takes in the password,
the filename relative to the driectory containing the executable, the server IP address, the port number, and then
the file containing the RSA keys in that order. Client 2 takes in the server IP address, the port number, and then
the file containing the RSA keys in that order. 

-Other than the rsaGenerator, the only helper class I have is called KeySalt, and it doesn't really do anything other than
hold a secret key and a salt. 
You still need to compile it though. 