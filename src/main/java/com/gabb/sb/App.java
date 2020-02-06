package com.gabb.sb;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Writing a web socket
 * https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java
 * https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
 * https://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-17#section-1.3
 * https://en.wikipedia.org/wiki/Augmented_Backus%E2%80%93Naur_form
 * https://chrome.google.com/webstore/detail/smart-websocket-client/omalebghpgejjiaoknljcfmglgbpocdp?utm_source=chrome-app-launcher-info-dialog
 */
public class App {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	public static final String UTF_8 = "UTF-8";

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		/**
		 * When a client connects to the server, it will send a GET request to upgrade the connection
		 * to a websocket from a simple HTTP request. This is known as handshaking
		 */
		ServerSocket server = new ServerSocket(8084);
		int port = server.getLocalPort();
		LOGGER.info("Server started on 127.0.0.1:{}, waiting for connection...", port);
		Socket client = server.accept();
		LOGGER.info("Client connected: {}", client);
		InputStream in = client.getInputStream();
		OutputStream out = client.getOutputStream();
		handshake(in, out);

		/**
		 *       0                   1                   2                   3
		 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
		 *      +-+-+-+-+-------+-+-------------+-------------------------------+
		 *      |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
		 *      |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
		 *      |N|V|V|V|       |S|             |   (if payload len==126/127)   |
		 *      | |1|2|3|       |K|             |                               |
		 *      +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
		 *      |     Extended payload length continued, if payload len == 127  |
		 *      + - - - - - - - - - - - - - - - +-------------------------------+
		 *      |                               |Masking-key, if MASK set to 1  |
		 *      +-------------------------------+-------------------------------+
		 *      | Masking-key (continued)       |          Payload Data         |
		 *      +-------------------------------- - - - - - - - - - - - - - - - +
		 *      :                     Payload Data continued ...                :
		 *      + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
		 *      |                     Payload Data continued ...                |
		 *      +---------------------------------------------------------------+
		 */
		
		//Maybe DataInputStream?
		new Thread(() -> {
			//section 5.2
			while (true)
				try {
					int size = 256;
					byte[] buf = new byte[size];
					int offset = 0;
					int readCount = in.read(buf, offset, size);
					LOGGER.info("read {} bytes", readCount);
					byte one = buf[0];
					byte two = buf[1]; //why is this always 126...
					//ANSWER: Its not, it only surpasses 126 when the payload length
					// surpasses a number that can be represented by 2 bytes
					byte thr = buf[2];
					byte fou = buf[3];
					int fin = (one & 0b10000000) >> 7;
					int rsv1 = (one & 0b01000000) >> 6;
					int rsv2 = (one & 0b00100000) >> 5;
					int rsv3 = (one & 0b00010000) >> 4;
					int opcode = (one & 0b00001111); //or 0XF
					int masked = (two & 0b10000000) >> 7;


					LOGGER.info("FIN={}", fin);
					LOGGER.info("RSV 1,2,3 = {},{},{}", rsv1, rsv2, rsv3);
					LOGGER.info("opcode = {}", opcode);
					LOGGER.info("masked = {}", masked);

					// now we need to get the payload length. by default this value is 7 bits
					// 01111111 = 127  --> length is the 8 bytes after the initial payload length
					// 01111110 = 126  --> length is the 2 bytes after
					String twobin = String.format("%8s", Integer.toBinaryString(two & 0XFF));

					boolean payloadExtended = false;
					boolean longPayload = false;
					byte payloadMask = 0b01111111;
					int payloadLen = (two & payloadMask);
					if (payloadLen > 125) {
						payloadExtended = true;
						if (payloadLen == 126) {
							//payload length is the next 16 bits (short)
							payloadLen = (thr << 8) | (fou & 0XFF);
						} else {
							longPayload = true;
							// | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
							//payload length is the next 64 bits (long)
							payloadLen = 0;
							for (int i = 2; i < 10; i++) {
								//better practice to bitwise or to concatenate numbers
								payloadLen |= (buf[i] << (9 - i)); 
							}
						}
					}

					LOGGER.info("PAYLOAD LENGTH IS {}", payloadLen);
					
					int nextByte = payloadExtended ? longPayload ? 10 : 4 : 2;

					byte[] mask = new byte[4];
					if(masked == 1){
						for(int i = 0; i < 4 ; i++ ){
							int bufPos = i + nextByte;
							mask[i] = buf[bufPos];
						}
						LOGGER.info("MASK IS PRESENT AND IS {}", ByteBuffer.wrap(mask).getInt());
						nextByte += 4;
					}
					
					for(int i = 0; i < payloadLen; i++){
						byte key = mask[i % 4];
						int decoded = buf[i + nextByte] ^ key;
						System.out.print((char) decoded);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
		}).start();

	}

	/**
	 * HANDSHAKING
	 * actually doing the handshake is easier than understanding why its done this way
	 * read up on websocket handshaking
	 */
	private static void handshake(InputStream in, OutputStream out) throws NoSuchAlgorithmException, IOException {
		Scanner scannerIn = new Scanner(in, UTF_8).useDelimiter("\\r\\n\\r\\n");
		String data = scannerIn.next();
		Matcher get = Pattern.compile("^GET").matcher(data);

		if (get.find()) {
			String webSocketAccept = getWebSocketAccept(data);
			byte[] response = buildResponse(webSocketAccept);
			out.write(response, 0, response.length);
		}
	}

	private static byte[] buildResponse(String webSocketAccept) {
		return ("HTTP/1.1 101 Switching Protocols\r\n"
				+ "Connection: Upgrade\r\n"
				+ "Upgrade: websocket\r\n"
				+ "Sec-WebSocket-Accept: " + webSocketAccept
				+ "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
	}

	private static String getWebSocketAccept(String data) throws NoSuchAlgorithmException {
		Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
		if (!match.find()) return "REQUIRES HEADER Sec-WebSocket-Key";
		String webSocketKey = match.group(1);
		String linkedWebSocketKey = webSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		MessageDigest messageDigestSHA1 = MessageDigest.getInstance("SHA-1");
		return Base64.getEncoder().encodeToString(
				messageDigestSHA1.digest(linkedWebSocketKey.getBytes(StandardCharsets.UTF_8)));
	}
}
