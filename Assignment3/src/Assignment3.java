/* DO NOT!!! put a package statement here, that would break the build system */
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
/**
 * Assignment 3
 * @author Svilen Stefanov
 */
public class Assignment3 {

	/**
	 * This function parses the ip address from a string into a network
	 * byte representation
	 *
	 * @param hwaddr The ip address as string
	 *
	 * @return A newly allocated byte array filled with the ip address,
	 *         null on error
	 */
	private static byte[] parseIP(String hwaddr) {	
		InetAddress ipAdd = null;
		try { ipAdd = InetAddress.getByName(hwaddr); } 
		catch (UnknownHostException e) { e.printStackTrace(); }
		byte[] ip = ipAdd.getAddress();
		return ip;
	}
	
	/**
	 * This method gives a byte array of the mac address, given as a String
	 * @param hwaddr mac address as a string
	 * @return mac address as byte array
	 */
	private static byte[] parseMAC(String hwaddr) {
		byte[] parsedMac = new byte[6];
		int k=0;
		for(int i = 0; i<hwaddr.length();i+=3)
			parsedMac[k++] = (byte) Integer.parseInt(hwaddr.substring(i,i+2), 16);

		return parsedMac;
	}

	/**
	 * This function initializes an ethernet header
	 *
	 * @param dstmac The destination mac
	 * @param srcmac The mac of this host
	 * @param ethertype The ethernet type for the header
	 *
	 * @return A newly allocated byte array filled with the header
	 */
	private static byte[] buildEtherHeader(byte[] dstmac, byte[] srcmac,
	                                       int ethertype) {
		byte[] ethernetHeader = new byte[dstmac.length + srcmac.length + 2];
		System.arraycopy(dstmac, 0, ethernetHeader, 0, dstmac.length);
		System.arraycopy(srcmac, 0, ethernetHeader, dstmac.length, srcmac.length);
		byte[] etherType = ByteBuffer.allocate(4).putInt(ethertype).array();
		System.arraycopy(etherType, 2, ethernetHeader, dstmac.length + srcmac.length, etherType.length-2);
		return ethernetHeader;
	}

	/**
	 * This function initializes an ipv6 header
	 * This encompasses the version and everything give as parameter
	 *
	 * @param dstIp the destination ip
	 * @param srcIp the ip of this host
	 * @param next The value for the next header field
	 * @param len The length of the ip payload
	 * @param hlim The hoplimit for the packet
	 *
	 * @return A newly allocated byte array filled with the ip header
	 */
	private static byte[] buildIP6Header(byte[] dstIp, byte[] srcIp,
                                             byte next, short len, byte hlim) {
		byte[] ipHeader = new byte[40];
		ipHeader[0] = (byte)0x60;
		for (int i = 1; i < 4; i++)
			ipHeader[i] = (byte)0x00;
		
		byte payload1 = (byte)(len & 0xFF);
		byte payload2 = (byte)((len >> 8) & 0xFF); 
		
		ipHeader[4] = payload2;
		ipHeader[5] = payload1;
		ipHeader[6] = next;
		ipHeader[7] = hlim;
		System.arraycopy(srcIp, 0, ipHeader, 8, srcIp.length);
		
		System.arraycopy(dstIp, 0, ipHeader, 24, dstIp.length);
		
		return ipHeader;
	}

	/**
	 * This function initializes the icmpv6 header and the neighbor
	 * discovery payload
	 *
	 * @param dstIp the destination ip
	 * @param srcMac the mac of this host
	 *
	 * @return A newly allocated byte array filled with the payload
	 */
	private static byte[] buildIcmp6Ndisc(byte[] dstIp, byte[] srcMac) {	
		byte[] icmpHeader = new byte[32];	
		icmpHeader[0] = (byte)0x87;
		for (int i = 1; i < 8; i++) 
			icmpHeader[i] = (byte)0x00;
		
		System.arraycopy(dstIp, 0, icmpHeader, 8, dstIp.length);		
		for (int i = 24; i <= 25; i++)
			icmpHeader[i] = (byte)0x01;
		System.arraycopy(srcMac, 0, icmpHeader, 26, srcMac.length);
		
		return icmpHeader;
	}

	/**
	 * This function checks whether the ethernet header may be for a
	 * neighbor advertisement which is sent as response to our neighbor
	 * discovery
	 *
	 * @param buffer A buffer containing the received frame
	 * @param offset The offset of the frame in the buffer
	 * @param mymac A byte array containing the mac of this host
	 *
	 * @result true on success
	 *	   false on fail
	 */
	private static boolean myEtherHeader(byte[] buffer, int offset,
	                                     byte[] mymac) {
		byte[] comparableArray = new byte[6];
		System.arraycopy(buffer, offset, comparableArray, 0, 6);
		return Arrays.equals(mymac, comparableArray) && buffer[offset +12] == (byte)0x86 && buffer[offset+13] == (byte)0xdd;
	}

	/**
	 * This function check whether the ip ethernet payload is an ipv6 header
	 * and if it may be a neighbor advertisement sent in response to our
	 * neighbor discovery
	 *
	 * @param buffer A byte array containing the received packet
	 * @param offset The offset of the packet in the buffer
	 * @param myIp A byte array containing the ip of this host
	 * @param dstIp A byte array containing the destination ip
	 *
	 * @return true on success
	 *	   false on fail
	 */
	private static boolean myIP6Header(byte[] buffer, int offset, int len,
	                                   byte[] myIp, byte[] dstIp) {
		if( (byte) ((buffer[offset] & 0xF0)) == (byte)0x60 && buffer[offset + 6] == (byte)0x3a &&  buffer[offset + 7] == (byte)255){
			byte len1 = (byte)(len & 0xFF);
			byte len2 = (byte)((len >> 8) & 0xFF); 
			if(len1 == buffer[offset+5] && len2 == buffer[offset+4]){
				byte[] comparableArray = new byte[myIp.length];
				byte[] comparableArray2 = new byte[dstIp.length];
				System.arraycopy(buffer, offset+8, comparableArray2, 0, dstIp.length);
				System.arraycopy(buffer, offset+24, comparableArray, 0, myIp.length);		

				return Arrays.equals(myIp, comparableArray) && Arrays.equals(dstIp, comparableArray2);
			} else return false;
		}
		return false;
	}

	/**
	 * This function checks whether the ip payload is a neighbor
	 * advertisement sent as a response to us
	 *
	 * @param buffer A byte array containing the received packet
	 * @param offset The offset of the ip payload
	 * @param dstIp The destination ip
	 *
	 * @return true on success
	 *	   false on fail
	 */
	private static boolean myNeighborAdvert(byte[] buffer, int offset,
	                                        int len, byte[] dstIp) {
		if(buffer[offset] == (byte)0x88 && buffer[offset+1] == (byte)0x00 && (buffer[offset+4] & (byte)0x40) == 64){
			byte[] comparableArray = new byte[16];
			System.arraycopy(buffer, offset + 8, comparableArray, 0, 16);
			return Arrays.equals(dstIp, comparableArray) && buffer[offset+24] == (byte)0x02 && buffer[offset+25] == (byte)0x01;
		} else return false;
	}

	/**
	 * This function checks if the frame received is a neighbor advertisement
	 * set as response to our neighbor discovery. If it is, it returns a
	 * byte array filled with the target mac
	 * It calls the more specialized functions in succession to check their
	 * parts and does tests that require multiple layers of the network
	 * stack (i.e. macs, icmp checksum)
	 *
	 * @param buffer A byte array filled with the received frame
	 * @param length The size of the received frame (<= buffer.length)
	 * @param srcmac The mac of this host
	 * @param srcip The ip of this host
	 * @param dstip The destination ip
	 *
	 * @return null on fail
	 *	   A newly allocated byte array filled with the mac address of
	 *	   the destination node
	 */
	private static byte[] check_answer(byte[] buffer, int length,
	                                   byte[] srcmac, byte[] srcip,
	                                   byte[] dstip) {
		byte[] mac = new byte[6];
		byte[] checksumBuffer = new byte[2];		
		byte[] checksum = new byte[2];			
		byte[] ipHeader = new byte[40];
		byte[] icmp = new byte[length];
		byte[] sourceMac = new byte[6];
		byte[] icmpMac = new byte[6];
		System.arraycopy(buffer, 14, ipHeader, 0, 40);
		System.arraycopy(buffer, 6, sourceMac, 0, 6);		
		if(myEtherHeader(buffer, 0, srcmac) && myIP6Header(buffer, 14, 32, srcip, dstip)){
			int k = 20;
			while(buffer[k] != (byte)0x3a)
				k+=32;
			System.arraycopy(buffer, 34 + k, icmp, 0, length);
			System.arraycopy(icmp, 2, checksumBuffer, 0, 2);		
			icmp[2] = 0; icmp[3] = 0;
			checksum = GRNVS_RAW.checksum(ipHeader, 0, icmp, 0, 32);
			if(myNeighborAdvert(buffer, 34+k, 32, dstip)){
				for (int i = k; i < length; i=k) {		
					if(buffer[58+i] == (byte)0x02 && buffer[59+i] == (byte)0x01){
					System.arraycopy(buffer, 60+i, icmpMac, 0, 6);
					break;
					} else k += buffer[59+i]*8;
				}
				if(Arrays.equals(checksum, checksumBuffer) && Arrays.equals(icmpMac, sourceMac)){
					System.arraycopy(buffer, 6, mac, 0, 6);
					return mac;
				} else return null;
			} else return null;
		} else return null;
	}

/*===========================================================================*/
	public static void run(GRNVS_RAW sock, String dst, int timeout) {
		Timeout time = new Timeout(timeout*1000);
		byte[] recbuffer = new byte[1514];
		byte[] buffer = new byte[1514];
		byte[] dstip = null;
		byte[] srcip = null;
		byte[] dstmac = null;
		byte[] srcmac = null;
		byte[] ether_header = null;
		byte[] ip_header = null;
		byte[] payload = null;
		byte[] checksum;	
		int ret;			
		int length;
		byte[] solicitIp = new byte[16]; 	

		if((dstip = parseIP(dst)) == null) {
			System.err.println("Your destination input format is broken, it should be: xx:xx:xx:xx:xx:xx");
			return;
		}
		
		srcmac = sock.getMac();
		srcip = sock.getIPv6();
		dstip = parseIP(dst);
		
		dstmac = new byte[6];
		dstmac[0] = (byte)0x33;
		dstmac[1] = (byte)0x33;
		dstmac[2] = (byte)0xff;
		System.arraycopy(dstip, 13, dstmac, 3, 3);		//the last 3 bytes from dst
		
		solicitIp = parseIP("ff02::1:ff00:0000");		//00:0000 will be overriden
		System.arraycopy(dstip, 13, solicitIp, 13, 3);		//override with the last 3 bytes from the ip address
		
		ether_header = buildEtherHeader(dstmac, srcmac, 34525);
		ip_header = buildIP6Header(solicitIp, srcip, (byte)0x3a, (short)32, (byte)255);
		payload = buildIcmp6Ndisc(dstip, srcmac);
		checksum = GRNVS_RAW.checksum(ip_header, 0, payload, 0, payload.length);
		System.arraycopy(checksum, 0, payload, 2, 2);			//override the checksum in the icmp-header
		
		System.arraycopy(ether_header, 0,buffer,0,ether_header.length);
		System.arraycopy(ip_header, 0, buffer, ether_header.length, ip_header.length);
		System.arraycopy(payload, 0, buffer, ip_header.length + ether_header.length, payload.length);
		
		length = ether_header.length + ip_header.length + payload.length;
		sock.hexdump(buffer, length);
		sock.write(buffer, length);

		while((ret = sock.read(recbuffer, time)) != 0) {
			if((dstmac = check_answer(recbuffer, ret, srcmac, srcip,
								dstip)) != null)
				break;
		}
		
		if(ret == 0) {
			//This is supposed to go to stdout, the tester needs
			//this. Do NOT change this
			System.out.print("Timeout\n");
			return;
		}
		
		//print the mac address in the necessary format
		System.out.println(dst + " is at " + printMac(dstmac));
	}
	
	/**
	 * This method print the mac address
	 * @param mac mac address to be printed
	 */
	private static String printMac(byte[] mac){
		String macPrint = Arrays.toString(javax.xml.bind.DatatypeConverter.printHexBinary(mac).split("(?<=\\G.{2})")).toLowerCase();
		return macPrint = macPrint.substring(1, macPrint.length() - 1).replaceAll("[,] ", ":");
	}


	public static void main(String[] argv) {
		Arguments args = new Arguments(argv);
		GRNVS_RAW sock = null;
		try{
			sock = new GRNVS_RAW(args.iface, 3);
			run(sock, args.dst, args.timeout);
			sock.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
}
