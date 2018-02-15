
import java.util.HashMap;
import java.util.Map;

//TODO test update records
//TODO error checking for interruptions

public class Test {

	public static void main(String[] args) {
		byte[] b = {(byte) 224, (byte) 0, (byte) 31, (byte) 164, (byte) 249, (byte) 248, (byte) 153,  (byte) 203,  (byte) 255,  (byte) 255, (byte) 254};
		Map<Integer, Record> records = new HashMap<Integer, Record>();
		
		DataManager d = new DataManager(b);
		
		Unpacker p = new Unpacker(d);
		
		int i = 0;
		int recordNum = 1;
		while( i < b.length - 1 ) {
			p.readHeader(b);
			d.setRecordData(i, (i += Math.ceil(d.getRecordBits())));
			
			records.put(recordNum, p.decode(d.getRecordData()));
			recordNum++;
		}
		
		for(Map.Entry<Integer, Record> entry : records.entrySet()) {
			Record rec = entry.getValue();
			System.out.println("Key: " + entry.getKey());
			System.out.println(rec.getTemperature());	
			System.out.println(rec.getPressure());	
			System.out.println(rec.getHumidity());	
		}
	}

}

