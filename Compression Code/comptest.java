/**
 * --BEFORE RUNNING PLEASE NOTE:--
 * 
 * 20-7-17 The code currently doesn't decode characters correctly due to a discovered flaw with the table system, it also maintains an array size of 15 because a system to trim the array
 * has not been implemented. It's also really ugly and inefficient.
 * 
 * DECODING: Starts on line 166
 */

import java.util.HashMap;

public class comptest{

	public static void main(String[] args) {
		
		/**--Enter string to encode then decode. CHARACTER DECODING NON-FUCNTIONAL--*/ String text = "123,";  /**Enter string to encode then decode. --CHARACTER DECODING NON-FUCNTIONAL--*/
		char characters[] = text.toCharArray();		//turn string into array to iterate through
		int temp = 0; //used to store character to encode
		int prevChar = 0; //used when encode sequential characters --CHARACTER DECODING NON-FUCNTIONAL--
		
		int bitCounter = 2;//indicates how empty the buffer is 2=empty 1= half full 0=full
		int arrayCounter = 0;
		byte toStore = 0; //The buffer
		
		byte[] array = new byte[15]; //Array for encoded bytes
		char string[] = new char[array.length]; //Array of characters for decoded bytes
		
		/**
		 * This HashMap is used to select appropriate byte when encoding special characters defined on the approved table
		*/
		HashMap<Character, Byte> table = new HashMap<Character, Byte>();
		table.put(',', (byte) 11);
		table.put('-', (byte) 12);
		table.put('.', (byte) 13);
		
		/**
		 * Table used to decode characters from approved table
		 */		
		HashMap<Byte, Character> decodeTable = new HashMap<Byte, Character>();
		decodeTable.put((byte) 1, '0');
		decodeTable.put((byte) 2, '1');
		decodeTable.put((byte) 3, '2');
		decodeTable.put((byte) 4, '3');
		decodeTable.put((byte) 5, '4');
		decodeTable.put((byte) 6, '5');
		decodeTable.put((byte) 7, '6');
		decodeTable.put((byte) 8, '7');
		decodeTable.put((byte) 9, '8');
		decodeTable.put((byte) 10, '9');
		decodeTable.put((byte) 11, ',');
		decodeTable.put((byte) 12, '-');
		decodeTable.put((byte) 13, '+');
		
		for(int i = 0; i < text.length(); i ++){ //Iterating through each character in array
			
			if(bitCounter == 0){ //If not currently writing to a byte reset
				arrayCounter++;
				bitCounter = 2;
				toStore = 0;
			}
			
			if(Character.isDigit(characters[i])){
				temp = Character.getNumericValue(characters[i]); //get character
				
				if(bitCounter == 2){ //If buffer is empty
					toStore = (byte) (temp + 1 << 4); //Move bits to start of buffer (bit shift 4 bits left)
				}else if(bitCounter == 1){//If buffer is half full
					toStore = (byte) (toStore | temp +1);//Fill rest of buffer
					array[arrayCounter] = toStore; // Store byte in array
				}
				bitCounter--;
			}
			
			if(Character.isLetter(characters[i])){
				prevChar = temp; //Store previous character
				temp = Character.valueOf(characters[i]);
				
				if(bitCounter == 2){ //If buffer is empty
					if(Character.isLetter(prevChar)) { //If previous character in string was a letter
						
						//Read previous byte in array to buffer
						arrayCounter--;
						toStore = array[arrayCounter];
						
						//Remove end characters byte indicator (1110) and replace them with the first four bits of the new character to be written 
						toStore = (byte) ((toStore & 240) + (temp >> 4));
						
						array[arrayCounter] = toStore;
						arrayCounter++;
						
						//Empty buffer
						toStore = 0; 
						
						//Store last four bits of character into the first four bits of the buffer and add end bytes indicator (1110) to the end of the byte
						toStore = (byte) ((temp << 4) + 14);
						
						array[arrayCounter] = toStore;
					}
					else{//If previous character was not letter
						//Shift first four bits of character to last four bits and add the start character indicator (1111)
						toStore = (byte)((temp >> 4) + 240);
						
						//Store in array & clear buffer --Need to revisit this line--
						array[arrayCounter] = (byte) (toStore & 255);
						toStore = 0;
						arrayCounter++;
						
						//Shift last four bits of character to first four bits of buffer and add end character indicator (1110)
						toStore = (byte)((temp << 4) + 14); 
						
						array[arrayCounter] = toStore;
					}
					bitCounter = 0; //Reset bit counter as net change of bits results in full buffer
					
				}else if(bitCounter == 1){//If buffer is half full
					if(Character.isLetter(prevChar)) {
						toStore = 0; //Empty buffer
						toStore = (byte) temp; //Put character into buffer
						array[arrayCounter] = toStore; //Store in array
						arrayCounter++;
						toStore = (byte) 224; //Add end character indicator to first four bits of buffer (1110)
					}else{
						toStore = (byte) (toStore + 15);//Fill rest of buffer with start character indicator (1111)
						array[arrayCounter] = toStore; // Store byte
						arrayCounter++;
						toStore = (byte) temp;
						array[arrayCounter] = toStore;
						arrayCounter++;
						toStore = (byte) 224;
					}
				}
				
			}
			
			if(table.containsKey(characters[i])){ //If approved special character
				if(bitCounter == 1){//If buffer half full
					//Get representative character from HashMap and store in array
					toStore = (byte) (toStore + table.get(characters[i]));
					array[arrayCounter] = toStore;
				}else{
					toStore = (byte) (table.get(characters[i]) << 4); //Get representative character and move to start of buffer
				}
				bitCounter--;
			}
			
		}//If encoding loop stops while data is still in the buffer but hasn't been written, write the data to the array.
		if(bitCounter == 1 || bitCounter == 0) {
			array[arrayCounter] = toStore;
		}
		
		
		/**
		 * Uncomment these lines for view of raw binary data after encoding
		 * 
		for( int i = 0; i < array.length; i++){
			System.out.println(String.format("%8s", Integer.toBinaryString(array[i] & 255)).replace(' ', '0'));
		}	    
		for( int i = 0; i < array.length; i++){
			System.out.println( Integer.toBinaryString(array[i] & 255));
		}	
		*/
		
		/**
		 * The below code decodes the array
		 */
		int counter = 0;
		for(int i = 0; i < array.length; i++){
			byte byyte = array[i]; //Get character from array 
			byte firstNib = (byte) ((byyte & 240) >> 4); //Get the first four bits by shifting them into the last four bits of the variable and removing the rest
			
			if(decodeTable.containsKey(firstNib)) {
				string[counter] = decodeTable.get(firstNib); //Get character from Map and store in string array
				counter++;
			}
			
			byte secondNib = (byte) (byyte & 15); //Get the last four bits removing the first four
			if(decodeTable.containsKey(secondNib)) {
				string[counter] = decodeTable.get(secondNib); //Get character and store
				counter++;
			}
			
			/**
			 * Uncomment these lines to see incoming bytes and resultant nibbles
			 * 
			 * 
			System.out.println(String.format("%8s", Integer.toBinaryString(byyte & 255)).replace(' ', '0') + " byte");
			System.out.println(String.format("%8s", Integer.toBinaryString(firstNib & 255)).replace(' ', '0') + " 1 nib");
			System.out.println(String.format("%8s", Integer.toBinaryString(secondNib & 255)).replace(' ', '0') + " 2 nib");
			System.out.println();
			*/
		}
		
		
		/**
		 * Below loop prints the string
		 */
		for(int i = 0; i < string.length; i++){
			System.out.println(string[i]);
		}
	}
}
