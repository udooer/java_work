package icListenPluginDaqBETA3_4;

class StreamMessage {
	/*** Constants ***/
	final char SYNC_VAL = 0x2A;

	final char MIN_MESSAGE_LENGTH = 4;
	final int PAYLOADLEN_ARRAY_LENGTH = 2;

	/*** Fields ***/

	/* the type of Character */
	char type;
	/* Sync character */
	char sync;
	/* Length of the data payload */
	char[] payloadLength;
	/* Contains the data (if applicable). It is 32 bit aligned */
	char[] payload;

	/**
	 * Standard constructor using individual message components
	 * 
	 */
	public StreamMessage(char argType, char[] argPayloadLength, char[] argPayload) {
		this.type = argType;
		this.sync = SYNC_VAL;
		this.payloadLength = argPayloadLength;
		this.payload = argPayload;
	}

	/**
	 * A constructor using a properly formated byte buffer Useful with returned
	 * messages, CRC will need to be checked
	 * 
	 * @param buf
	 */
	public StreamMessage(byte[] buf) {
		int payloadLength;
		int bufferIndex;

		/* get command */
		this.type = (char) buf[0];

		/* get sync byte */
		this.sync = (char) buf[1];

		/* get payload length */
		this.payloadLength = new char[PAYLOADLEN_ARRAY_LENGTH];
		this.payloadLength[0] = (char) buf[2];
		this.payloadLength[1] = (char) buf[3];
		payloadLength = (int) ((this.payloadLength[1] & 0xFF) | ((this.payloadLength[0] & 0xFF) << 8));

		/* get payload */
		this.payload = new char[payloadLength];
		for (bufferIndex = MIN_MESSAGE_LENGTH; bufferIndex < (payloadLength + MIN_MESSAGE_LENGTH)
				&& bufferIndex < IcListenParams.MAX_MESSAGE_SIZE; bufferIndex++) {
			this.payload[bufferIndex - MIN_MESSAGE_LENGTH] = (char) buf[bufferIndex];
		}
	}

}