package icListenPluginDaqBETA3_4;
/**
 * 
 * @see the appropriate document for more information about the command message
 *
 */
public class commandMessage {
	private static final int PAYLOAD_LEN = 2;
	private static final int CRC_LEN = 2;
	private static final int MAX_PAYLOAD = 6700;

	// the minimum payload length is 4 without including the CRC field.
	private static final int MIN_PAYLOAD_OFFSET = 4;
	char sync;
	char messageType;
	// the payload length took 2 bytes field.
	char[] payloadLength;

	char[] payload = new char[MAX_PAYLOAD];
	char[] CRC = new char[2];

	// The CRC-16 lookup table
	char crc16_table[] = { 0x0000, 0xc0c1, 0xc181, 0x0140, 0xc301, 0x03c0, 0x0280, 0xc241, 0xc601, 0x06c0, 0x0780,
			0xc741, 0x0500, 0xc5c1, 0xc481, 0x0440, 0xcc01, 0x0cc0, 0x0d80, 0xcd41, 0x0f00, 0xcfc1, 0xce81, 0x0e40,
			0x0a00, 0xcac1, 0xcb81, 0x0b40, 0xc901, 0x09c0, 0x0880, 0xc841, 0xd801, 0x18c0, 0x1980, 0xd941, 0x1b00,
			0xdbc1, 0xda81, 0x1a40, 0x1e00, 0xdec1, 0xdf81, 0x1f40, 0xdd01, 0x1dc0, 0x1c80, 0xdc41, 0x1400, 0xd4c1,
			0xd581, 0x1540, 0xd701, 0x17c0, 0x1680, 0xd641, 0xd201, 0x12c0, 0x1380, 0xd341, 0x1100, 0xd1c1, 0xd081,
			0x1040, 0xf001, 0x30c0, 0x3180, 0xf141, 0x3300, 0xf3c1, 0xf281, 0x3240, 0x3600, 0xf6c1, 0xf781, 0x3740,
			0xf501, 0x35c0, 0x3480, 0xf441, 0x3c00, 0xfcc1, 0xfd81, 0x3d40, 0xff01, 0x3fc0, 0x3e80, 0xfe41, 0xfa01,
			0x3ac0, 0x3b80, 0xfb41, 0x3900, 0xf9c1, 0xf881, 0x3840, 0x2800, 0xe8c1, 0xe981, 0x2940, 0xeb01, 0x2bc0,
			0x2a80, 0xea41, 0xee01, 0x2ec0, 0x2f80, 0xef41, 0x2d00, 0xedc1, 0xec81, 0x2c40, 0xe401, 0x24c0, 0x2580,
			0xe541, 0x2700, 0xe7c1, 0xe681, 0x2640, 0x2200, 0xe2c1, 0xe381, 0x2340, 0xe101, 0x21c0, 0x2080, 0xe041,
			0xa001, 0x60c0, 0x6180, 0xa141, 0x6300, 0xa3c1, 0xa281, 0x6240, 0x6600, 0xa6c1, 0xa781, 0x6740, 0xa501,
			0x65c0, 0x6480, 0xa441, 0x6c00, 0xacc1, 0xad81, 0x6d40, 0xaf01, 0x6fc0, 0x6e80, 0xae41, 0xaa01, 0x6ac0,
			0x6b80, 0xab41, 0x6900, 0xa9c1, 0xa881, 0x6840, 0x7800, 0xb8c1, 0xb981, 0x7940, 0xbb01, 0x7bc0, 0x7a80,
			0xba41, 0xbe01, 0x7ec0, 0x7f80, 0xbf41, 0x7d00, 0xbdc1, 0xbc81, 0x7c40, 0xb401, 0x74c0, 0x7580, 0xb541,
			0x7700, 0xb7c1, 0xb681, 0x7640, 0x7200, 0xb2c1, 0xb381, 0x7340, 0xb101, 0x71c0, 0x7080, 0xb041, 0x5000,
			0x90c1, 0x9181, 0x5140, 0x9301, 0x53c0, 0x5280, 0x9241, 0x9601, 0x56c0, 0x5780, 0x9741, 0x5500, 0x95c1,
			0x9481, 0x5440, 0x9c01, 0x5cc0, 0x5d80, 0x9d41, 0x5f00, 0x9fc1, 0x9e81, 0x5e40, 0x5a00, 0x9ac1, 0x9b81,
			0x5b40, 0x9901, 0x59c0, 0x5880, 0x9841, 0x8801, 0x48c0, 0x4980, 0x8941, 0x4b00, 0x8bc1, 0x8a81, 0x4a40,
			0x4e00, 0x8ec1, 0x8f81, 0x4f40, 0x8d01, 0x4dc0, 0x4c80, 0x8c41, 0x4400, 0x84c1, 0x8581, 0x4540, 0x8701,
			0x47c0, 0x4680, 0x8641, 0x8201, 0x42c0, 0x4380, 0x8341, 0x4100, 0x81c1, 0x8081, 0x4040 };

	/**
	 * standard constructor used for command message
	 * 
	 * @param sync
	 * @param messageType
	 * @param payloadLength
	 * @param payload
	 */
	public commandMessage(char sync, char messageType, char[] payloadLength, char[] payload) {
		this.sync = sync;
		this.messageType = messageType;
		this.payloadLength = payloadLength;
		// int payLoadLength = (payloadLength[0])|(payloadLength[1]<<8);
		/*
		 * payload = new char[payLoadLength]; for(int i = 0 ; i < payLoadLength;i++) {
		 * this.payload[i] = payload[i]; }
		 */
		this.payload = payload;
		this.CRC = calculateTxCrc(this);
	}

	/**
	 * another constructor for command message field.
	 * 
	 * @param buffer
	 */
	public commandMessage(char buffer[]) {
		/*
		 * sync = buffer[0]; messageType = buffer[1]; int payLoadLength;
		 * payloadLength[0] = buffer[2]; payloadLength[1] = buffer[3];
		 * 
		 * int bufferIndex; payLoadLength = (payloadLength[0]<<8)|(payloadLength[1]);
		 * for(bufferIndex = PAYLOAD_OFFSET ; bufferIndex <
		 * (payLoadLength+PAYLOAD_OFFSET);bufferIndex++) { this.payload[bufferIndex] =
		 * buffer[bufferIndex + PAYLOAD_OFFSET]; } this.CRC= new char[CRC_LEN];
		 * 
		 * this.CRC[0] = (char)buffer[bufferIndex]; this.CRC[1] =
		 * (char)buffer[bufferIndex+1];
		 */

		int payloadLength;
		int bufferIndex;

		this.sync = (char) buffer[0];

		this.messageType = (char) buffer[1];
		this.payloadLength = new char[2];

		this.payloadLength[0] = buffer[2];
		this.payloadLength[1] = buffer[3];

		payloadLength = (this.payloadLength[0] & 0xFF) | (this.payloadLength[1] & 0xFF << 8);

		this.payload = new char[payloadLength];
		for (bufferIndex = MIN_PAYLOAD_OFFSET; bufferIndex < (payloadLength + MIN_PAYLOAD_OFFSET); bufferIndex++)
			this.payload[bufferIndex - MIN_PAYLOAD_OFFSET] = (char) buffer[bufferIndex];
		this.CRC = new char[CRC_LEN];
		this.CRC[0] = (char) buffer[bufferIndex];
		this.CRC[1] = (char) buffer[bufferIndex + 1];
	}

	/**
	 * This method will calculate the CRC field.
	 * 
	 * @param msg
	 * @return crc
	 */
	public char[] calculateTxCrc(commandMessage msg) {
		char[] crc = new char[CRC_LEN];
		char checksum = 0;
		int payloadIndex;
		int payloadLen;

		checksum = crc_update(msg.sync, checksum);
		checksum = crc_update(msg.messageType, checksum);
		checksum = crc_update(msg.payloadLength[0], checksum);
		checksum = crc_update(msg.payloadLength[1], checksum);
		payloadLen = (msg.payloadLength[0] & 0xFF) | (msg.payloadLength[1] & 0xFF << 8);
		for (payloadIndex = 0; payloadIndex < payloadLen; payloadIndex++) {
			checksum = crc_update(msg.payload[payloadIndex], checksum);
		}
		crc[0] = (char) (checksum & 0xFF);
		crc[1] = (char) ((checksum >> 8) & 0xFF);

		return crc;

	}

	/**
	 * make sure the icListen gets the correct data field.
	 * 
	 * @param data
	 * @param accum
	 * @return
	 */
	public char crc_update(char data, char accum) {
		int comb_value;

		comb_value = (int) (((accum & 0xFF) ^ (data & 0xFF)) & 0xFF);
		accum = (char) ((accum >> 8) ^ crc16_table[comb_value & 0xFF]);

		return accum;
	}

}
