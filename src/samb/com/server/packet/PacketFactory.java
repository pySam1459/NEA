package samb.com.server.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class PacketFactory {
	/* The PacketFactory contains static methods which can be called to convert Packets to and from bytes.
	 * These methods can be called from any class in the project and so can be used anywhere */
	
	public static Packet toPacket(byte[] data) {
		// This method converts a byte stream into a Packet object
		// If the data is corrupted, the Packet object will not be created
		//   and an error would occur (unsynchronized, etc)
		
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInputStream in = new ObjectInputStream(bis);
			return (Packet) in.readObject();
			
		} catch(IOException | ClassNotFoundException e) {
			// Exception is caused by corrupted data, or if the sent Packet was too large to find into 1 DatagramPacket
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] getBytes(Packet p) {
		// This method converts a Packet object into a byte array
		// This method will be called before a packet is to be sent across the Internet
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(p);
			return bos.toByteArray();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
