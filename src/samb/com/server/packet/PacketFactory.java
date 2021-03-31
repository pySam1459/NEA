package samb.com.server.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class PacketFactory {
	/* PacketFactory is a class containing static methods which other classes can call to convert Packets from and to bytes.
	 * These methods can be called from any class in the project and so can be used anywhere */
	
	//
	public static Packet toPacket(byte[] data) {
		/* This method uses a ByteArrayInputStream and an ObjectInputStream to convert a byte array
		 * (containing the packet object data) into a Packet Object 
		 * This byte data will have been sent across the Internet, so there is always a chance that the data may have been corrupted */
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInputStream in = new ObjectInputStream(bis);
			return (Packet) in.readObject();
			
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace(); // If the byte data is corrupted, an IOException will be called and a runtime error produced.
		}
		return null;
	}
	
	public static byte[] getBytes(Packet p) {
		/* This method uses a ByteArrayOutputStream and an ObjectOutputStream to convert a 'Packet' object
		 * into a byte array, which will then be sent across the Internet to a host Server, which will then be dealt with */
		
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
