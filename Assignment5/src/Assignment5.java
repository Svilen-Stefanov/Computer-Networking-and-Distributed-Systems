import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

/**
 * Assignment 5
 * @author Svilen Stefanov
 */
public class Assignment5 {

    /**
     * This function returns the IP address from an address string or host name
     *
     * @param host The address string or host
     * @return A newly allocated byte array filled with the ip address
     * Or null if the String is not an ipv6-address
     */
    private static InetAddress getip(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return null;
        }
    }


    /**
     * Encode a string to an UTF-8 byte array using the standard library
     *
     * @param str String to Encode
     * @return UTF-8 encoded str, or null on error
     */
    static byte[] encodeUTF8(String str) {
		return str.getBytes(Charset.forName("UTF-8"));
    }


    /**
     * Decode a byte buffer as UTF-8 using the standard library
     *
     * @param buf buffer to decode
     * @return decoded string or null on error
     */
    static String decodeUTF8(byte[] buf) {
		return new String(buf, Charset.forName("UTF-8"));
    }


    /**
     * Convert an input byte array to a netstring and write it to an output
     * stream.
     *
     * The input byte array must first be encoded as netstring:
     * <length of data in decimal>":"<data>","
     *
     * @param data The data to encode
     * @param out  Steam to write to
     */
    static void sendNetstring(OutputStream out, byte[] data) throws IOException {
    	String length = data.length + ":";
    	byte [] prefix = encodeUTF8(length);
    	byte[] all = new byte[prefix.length + data.length + 1];
    	System.arraycopy(prefix, 0, all, 0, prefix.length);
    	System.arraycopy(data, 0, all, prefix.length, data.length);
    	all[prefix.length + data.length] = (byte)',';
    	out.write(all);
    }

    /**
     * Read a netstring from an input stream and return its content as a
     * byte array.
     * The netstring may be fragmented, so this function might need to
     * call read() multiple times.
     * Since TCP is stream oriented, there may also be more than one
     * netstring buffered. This function must NOT read into the next
     * netstring, so it can be read later without problems.
     * This function must validate the netstring and strip the length
     * (including the ':') and the trailing ','.
     * It returns the contents as a byte array.
     *
     * @param reader The input stream to read from
     * @return The data of the netstring as newly allocated byte array or
     *         null on error
     */
    static byte[] recvNetstring(InputStream reader) {
    	try {
    		byte[] length = new byte[3];
    		byte check = 0;
    		int i;
    		for (i = 0; i < 3; i++) {			//liest die Laenge der Daten ab
    			check = (byte)reader.read();		//gibt es immer 3 Byte davor?!
    			if(check!=(byte)':')
    				length[i] = check;
    			else break;
			}
    		
    		if(check!=(byte)':'){		//ueberprueft ob danach Doppelpunkt kommt
    			check = (byte)reader.read();
    			if(check!=(byte)':'){
    				System.err.println("Invalid Netstring!");
					return null;
    			}
    		}
    		
    		byte[] realLength = new byte[i];		//i ist schon erhoeht, beginnt aber ab 0 an
    		System.arraycopy(length, 0, realLength, 0, realLength.length);
			
    		String lengthData = decodeUTF8(realLength);			//berechnet die Groesse der Nachricht
			
			byte[] data = new byte[Integer.parseInt(lengthData)];		//nicht i sonders den Wert von 
			byte checkLength = 0;
			for (int j = 0; j < data.length; j++){
				checkLength = (byte)reader.read();
				data[j] = checkLength;
				/*if(checkLength != (byte)',')		//трябва да се провери само накрая
					data[j] = checkLength;
				else {
					System.err.println("Invalid Netstring!");
					return null;
				}*/
			}
			
			byte comma = (byte)reader.read();
			if(comma != (byte)','){
				System.err.println("Invalid Netstring!");
				return null;
			} else {
				byte[] recvString = new byte[realLength.length + data.length + 2];
				System.arraycopy(realLength, 0, recvString, 0, realLength.length);
				recvString[realLength.length] = (byte)':';
				System.arraycopy(data, 0, recvString, realLength.length+1, data.length);
				recvString[realLength.length + data.length + 1] = (byte)',';
				return recvString;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	//BufferedReader buffer = new BufferedReader();
    	//overhead mitgeliefert - Komma und doppelpunkt stehen am Anfang und Ende 
        return null;
    }
    
    static String requestTransfer(String nick, OutputStream out, InputStream in) throws UnknownHostException, IOException {
    	byte[] init = encodeUTF8("C GRNVS V:1.0");
    	sendNetstring(out, init);

    	byte[] answer = recvNetstring(in);
    	byte[] expAnswer = encodeUTF8("S GRNVS V:1.0");
    	if(Arrays.equals(answer, expAnswer)){
        	byte[] conf = encodeUTF8("C " + nick);
        	sendNetstring(out, conf);
        	byte[] token = recvNetstring(in);		     //erwartet "S <token>" - muss fuer send gespeichert werden
        	if(decodeUTF8(token).substring(0,2).equals("S ")){
        		String recvToken = decodeUTF8(token).substring(2, decodeUTF8(token).length());
            	return recvToken;
        	} else { System.err.format("Wrong conformation!"); return null; }
    	} else {
    		System.err.format("Answer was wrong!");
            return null;
    	}
    }
    
    static String sendMessage(String nick,  OutputStream out, InputStream in, String msg, String token) throws IOException {
    	byte[] conf = encodeUTF8("D " + nick);
        sendNetstring(out, conf);
        byte[] verToken = recvNetstring(in);
        if(decodeUTF8(verToken).substring(0,2).equals("S ")){		//ist es wieder mit S?!
	        String recvToken = decodeUTF8(verToken).substring(2, decodeUTF8(verToken).length());
	        if(recvToken.equals(token)){
	        	sendNetstring(out, encodeUTF8("D " + msg));
	        	byte[] secToken = recvNetstring(in);		     //erwartet "T <token>" - muss fuer send gespeichert werden
	        	if(decodeUTF8(secToken).substring(0,2).equals("T ")){
	        	recvToken = decodeUTF8(secToken).substring(2, decodeUTF8(secToken).length());
	        	return recvToken;
	        	} else {System.err.format("Wrong token verification!"); return null;}  
	        } else  {System.err.format("Wrong verification!"); return null;}    	
        } else {System.err.format("Wrong token conformation!"); return null;}
    }
    
    static void commitMessage(OutputStream out, InputStream in, String msg, String token, Socket socket) throws IOException{
    	byte[] answer = recvNetstring(in);
    	if(decodeUTF8(answer).substring(0,2).equals("S ")){
	    	int length = Integer.parseInt(decodeUTF8(answer).substring(2, decodeUTF8(answer).length()));
	    	if(length == msg.length()){
	    		byte[] conf = encodeUTF8("C " + token);
	    		sendNetstring(out, conf);
	    		byte[] lastMsg = recvNetstring(in);	
	    		if(decodeUTF8(lastMsg).equals("S ACK")){
	    			socket.close();
	    		} else {System.err.format("Conformation failed!"); return;}
	    	} else {System.err.format("Wrong verification!"); return;}    
    	}
    	//Server sendet die Laenge der Nachricht in Form "S + <msglen>" (muss ueberprueft werden)
    	//sende "C + <dtoken>", wenn die Laenge stimmt
    	//"S ACK" bestaetigt alles und schliesst den Kanal
    	
    }
    
    static void sendErrorMessage(){
    	
    }
/*===========================================================================*/

    /**
     * This is the entry function for asciiclient.
     * It establishes the connection to the server and opens a listening
     * port.
     * It uses requestTransfer, sendMessage and commitMessage to post a
     * unicode message to the server.
     *
     * @param dst  The IP of the server in ASCII representation. IPv6 MUST be
     *             supported, support for IPv4 is optional.
     * @param port The server port to connect to
     * @param nick The nickname to use in the protocol
     * @param msg  The message to send
     * @throws IOException 
     */
    public static void run(String dst, int port, String nick, String msg) throws IOException {
    	InetAddress dstIp;
    	
        if ((dstIp = getip(dst)) == null) {
            System.err.println("Could not get IP address for destination");
            return;
        }
        
        Socket socket = new Socket(dstIp, 1337);
        ServerSocket serverSocket = new ServerSocket(0);
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        
        String token = requestTransfer(nick, out, in);		//gibt das vom Server bekommenen Token
        sendNetstring(out, encodeUTF8("C " + socket.getPort()));		//schickt port
        
        Socket dataSocket = serverSocket.accept();			//wartet bis sich das Server verbindet
        byte[] dataResponse = recvNetstring(dataSocket.getInputStream());
        out = dataSocket.getOutputStream();
        in = dataSocket.getInputStream();
        String secToken = null;
        if(decodeUTF8(dataResponse).equals("T GRNVS V:1.0"))
        	secToken = sendMessage(nick, out, in, msg, token);
        else {System.err.format("Wrong verification!");}
        dataSocket.close();
        
        out = socket.getOutputStream();
        in = socket.getInputStream();
        commitMessage(out, in, msg, secToken, socket);
        
        //varni se i proverqvai bukvite navsqkade
        

        
        //Socket server = new Socket(dst, port);		//connectivity to master
	/*	ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
		out.writeObject(msg);
		out.flush();
*/

    }


    public static void main(String[] argv) {
        Arguments args = new Arguments(argv);
        String msg = args.msg;
        if (args.file != null) {
            try {
                msg = new String(Files.readAllBytes(Paths.get(args.file)));
            } catch (IOException e) {
                System.err.format("Could not open the file: %s\n", e.getMessage());
                return;
            }
        }

        try {
            run(args.dst, args.port, args.nick, msg);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
