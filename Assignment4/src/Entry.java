/**
 * Assignment 4
 * @author Svilen Stefanov
 */
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/* DO NOT!!! put a package statement here, that would break the build system */

/* This class is intended to be used as element to your data structure.
 * You use this in the template declaration if you use something from the
 * stdlib.
 *
 * Should you need more classes, you will have to modify the Makefile.
 * The makefile contains a line:
 * CLASSES+=Entry.class
 * This is a whitespace (' ') separated list of .class files that should be
 * packed into the jar. If you need e.g. Table.java, change the line to:
 * CLASSES+=Entry.class Table.class
 *
 * NOTE: You cannot declare private classes inside a .java file, because the
 * Makefile will not finde the .class file! Create a .java file for every new
 * class you need.
 */
public class Entry {
	private byte[] parseIP(String ipAddr) {
		try{
			return InetAddress.getByName(ipAddr).getAddress();
		}
		catch (UnknownHostException e) {
			return null;
		}
	}
	
	/**
	 * Check whether the requested address is from the corresponding (sub)network in the routing table 
	 * @param ipRouteInTable information in the i-th row from the routing table
	 * @param ip the ip address from the given file
	 * @return <b>true</b> if it is from the network <br><b>false</b> if not compatible with the network address
	 */
	public boolean isCompatible(String ipRouteInTable, String ip){		
		String prefix = ipRouteInTable.substring(ipRouteInTable.indexOf("/") + 1, ipRouteInTable.length());
		Scanner in = new Scanner(prefix);
		prefix = in.next();
		int pre = Integer.parseInt(prefix);
		String ipTable = ipRouteInTable.substring(0, ipRouteInTable.indexOf("/"));
		byte[] ipInTable = parseIP(ipTable);
		byte[] ipRequest = parseIP(ip);
		byte[] mask = null;
		if(pre%8==0)
			mask = new byte[pre/8];
		else mask = new byte[pre/8 + 1];
		int i = 0;
		for (i = 0; pre - 8 >= 0; i++) {
			mask[i] = (byte)0xFF;
			pre-=8;
		}
		if(i<mask.length)
			mask[i] = (byte)((byte)0xFF << (8-pre));
		in.close();
		
		checkMyIP(ipRequest, mask);
		checkMyIP(ipInTable, mask);
		if(Arrays.equals(ipRequest, ipInTable))	
			return true;
		else return false;
	}
	
	/**
	 * Method, used to find the network of the given ip route
	 * @param ip the address of the ip to be routed
	 * @param mask	the mask of the corresponding root in the table
	 */
	private void checkMyIP(byte[] ip,  byte[] mask){
		for (int i = 0; i < ip.length; i++) {
			if(i<mask.length)
				ip[i] = (byte)((byte)ip[i] & (byte)mask[i]);
			else ip[i] = (byte)0x00;
		}
	}
	
	/**
	 * Decide which is the right path in the routing table to send the packet to
	 * @param routingTable - list of the routes in the table(sorted)
	 * @param route - the request address
	 * @return <b>true</b> if it is from the network <br><b>false</b> if not compatible with the network address
	 */
	public String check(ArrayList<String> ipRoutingTable, String ip){	
		int i = 0;
		String nextHop = new String();
		String ether = new String();
		while(i < ipRoutingTable.size()){
			if(isCompatible(ipRoutingTable.get(i), ip)){
				nextHop = ipRoutingTable.get(i).substring(ipRoutingTable.get(i).indexOf("/") + 1, ipRoutingTable.get(i).length());
				Scanner in = new Scanner(nextHop);
				in.next();
				nextHop = in.next();
				ether = in.next();
				in.close();
				return nextHop + " " + ether;
			}
			else i++;
		}
		return "No route to host";
	}
}
