public class Assignment2 {

	/**
	 * Allocate a buffer of the correct size and initialize the
	 * ethernet header here.
	 */
	private static byte[] buildEtherHeader(byte[] dstmac, byte[] srcmac) {	
		byte[] ethernetHeader = new byte[12];
		int j = 0;
		Integer a = new Integer(0);
		for (int i = 0; i < ethernetHeader.length; i++) {
			if(i<dstmac.length){
				a = Integer.parseInt("ff", 16);
				ethernetHeader[i] = a.byteValue();
			}
			if(i>=dstmac.length){
				ethernetHeader[i] = srcmac[j];
				j++;
			}
		}
		return ethernetHeader;
	}

	/**
	 * Allocate a buffer of the correct size and initialize the WoL
	 * payload here.
	 */
	private static byte[] buildWolPayload(byte[] dstmac) {
		byte[] ploadWoL = new byte[104];	//2 + 6 + 16*6 = 8 + 96 = 104
		Integer a = new Integer(0);
		a = Integer.parseInt("08", 16);
		ploadWoL[0] = a.byteValue();
		a = Integer.parseInt("42", 16);
		ploadWoL[1] = a.byteValue();
		//ploadWoL[0] = 0x08;
		//ploadWoL[1] = 0x42;
		for (int i = 2; i < 8; i++){
			a = Integer.parseInt("ff", 16);
			ploadWoL[i] = a.byteValue();
		}
		for (int i = 8; i < ploadWoL.length; i++)
			ploadWoL[i] = dstmac[(i - 2)%6];			
		return ploadWoL;	}

	/**
	 * Parse the MAC address (string) and return the result as byte
	 * array for use in packet building. Return null on error.
	 */
	private static byte[] parseMAC(String hwaddr) {
		try{
		String mac[] = hwaddr.split(":");
		byte[] macInBytes = new byte[6];
		Integer a = new Integer(0);
		for (int i = 0; i < mac.length; i++) {
			a = Integer.parseInt(mac[i], 16);
			macInBytes[i] = a.byteValue();
		}
		return macInBytes;
		} catch(Exception e){
			return null;	
		}
	}
/*===========================================================================*/

	public static void run(GRNVS_RAW sock, String dst) {
		byte[] buffer = new byte[1514];
		byte[] dstmac;
		byte[] srcmac;
		byte[] header;
		byte[] payload;
		int length;

		if((dstmac = parseMAC(dst)) == null) {
			System.err.println("Your destination input format is broken, it should be: xx:xx:xx:xx:xx:xx");
			return;
		}
		
		//buildwrite  ether-header
		// -> use buildEtherHeader()
		// -> getMac() in GRNVS_RAW returns your source address
		// -> determine the correct ethertype and take care of endianess
		srcmac = sock.getMac();
		header = buildEtherHeader(dstmac, srcmac);

		//build WoL payload
		// -> use buildWolPayload()
		payload = buildWolPayload(dstmac);

		//Concatinate the ether header and the WoL payload into
		// buffer.
		// Determine the size of the WoL packet and set it here.
		for (int i = 0; i < 116 ; i++){ 
			buffer[i] = (i < header.length) ? header[i] : payload[i-12];
		}
		
		length = header.length  + payload.length;//header.length  + payload.length;
		sock.hexdump(buffer, length);
		sock.write(buffer, length);
/*===========================================================================*/
	}



	public static void main(String[] argv) {
		Arguments args = new Arguments(argv);
		GRNVS_RAW sock = null;
		try{
			sock = new GRNVS_RAW(args.iface, 3);
			run(sock, args.dst);
			sock.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
}
