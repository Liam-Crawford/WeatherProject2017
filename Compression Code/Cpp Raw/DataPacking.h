/*
 * DataPacking.h
 *
 *  Created on: 10/09/2017
 *      Author: Jesse
 *	Desc: Header file for DataPacking.cpp
 */

#ifndef DATAPACKING_H_
#define DATAPACKING_H_

#include <stdint.h>

class DataPacking{

	//Numerical minimums for sensors, used to scale numbers appropriately.
			static const double MINHPA = 300.0;
			static const double MINTMP = -40.0;
			static const double MINHMD = 0;
			//const double MINTEST = 0;
			//const double MINHMD = -300.0;
			//const double MINHMD = -300.0;

			//Maximum bits during normal temperature reading
			static const int HPABITS = 13;
			static const int TMPBITS = 11;
			static const int HMDBITS = 10;
			//const int TESTBITS = -300.0;
			//const int HMDBITS = -300.0;
			//const int HMDBITS = -300.0;

			//Maximum bits during normal temperature reading
			static const int HPABITSUPD = 14;
			static const int TMPBITSUPD = 12;
			static const int HMDBITSUPD = 11;
			//const int TESTBITS = -300.0;
			//const int HMDBITS = -300.0;
			//const int HMDBITS = -300.0;

public:

		DataPacking();

		/* wasteShift: 	 the amount of bits that number needs to shift left in order to be 'left side aligned'
		 * counterVaule: the amount of bits, rounded up to the nearest byte, that a number occupies.  Used as a counter
		 * 				 when bit shifting.
		 * overflow:	 the number of bits that a number exceeds a byte (8 bits).  Used when calculating the bit offset.
		 */
		struct shiftVals{
					int wasteShift;
					int counterValue;
					int overflow;
				};

		/* shiftAmount: the number of bits needed to be shifted
		 * shiftType:	the direction in which the bits need to be shifted
		 * 				(true == left, false == right)
		 */
		struct shiftingInstructions{
			int shiftAmount;
			bool shiftType;
		};

	/* full records of each sensor reading, stored when the last full reading was taken.
	 */
	struct fullRecord{
		int fullTmp;
		int fullHPA;
		int fullHmd;
	};

	/* shiftAmount: the number of bits needed to be shifted
	 * shiftType:	the direction in which the bits need to be shifted
	 * 				(true == left, false == right)
	 */
	struct arrayManagement{
		int offset;
		int arrayIndex;
	};

	arrayManagement storeUpdate(double temperature, double pressure, double humid, uint8_t recordArray[], DataPacking::arrayManagement offsetAndIndex, DataPacking::fullRecord &lastFullRecord);

	arrayManagement storeRecord(double rawTempRead, double rawPresRead, double rawHumidRead, uint8_t recordArray[], DataPacking::arrayManagement offsetAndIndex, DataPacking::fullRecord &fullRecord);

private:
	int storeValue(int value, int offset, DataPacking::shiftVals shiftValues, DataPacking::shiftingInstructions instr, int arrayIndex, uint8_t recordArray[]);

	int buildHeader(bool temperature, bool pressure, bool humidity, bool isUpdate, int &index, uint8_t recordArray[], int offset);

	int storeOffsetData(int remainingBits, int temporaryValue, uint8_t recordArray[], int index);

	int storeRemainder(int remainingBits, uint8_t remainder, uint8_t recordArray[], int index);

	int storeData(int remainingBits, int temporaryValue, uint8_t recordArray[], int index);

	void setShiftValues(DataPacking::shiftVals &shiftValues, int bits);

	void setShiftInstructions(DataPacking::shiftingInstructions &instr, int offset, int waste);

	int offsetValue(int offset, int overflow);

	int scaleNumber(double number, double lowestPoint);
};



#endif /* DATAPACKING_H_ */
