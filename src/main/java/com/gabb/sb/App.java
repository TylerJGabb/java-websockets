package com.gabb.sb;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.server.ExportException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
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
	public static final byte[] TYLER = new byte[]{(byte) 0x81, 0x05, 0x74, 0x79, 0x6C, 0x65, 0x72};

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
		boolean success = handshake(in, out);
		if (success) LOGGER.info("Handshake Successful");
		else {
			LOGGER.info("Handshake Unsuccessful, aborting program");
			System.exit(1);
		}

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

		DataInputStream dataStream = new DataInputStream(in);
		new Thread(() -> {
			//section 5.2
			while (true) {
				try {
					int finop = dataStream.readUnsignedByte();
					int masklen = dataStream.readUnsignedByte();
					boolean fin = (finop & 0x80) > 0;
					int opcode = finop & 0xF;
					boolean masked = (masklen & 0x80) > 0;
					//TODO: if not masked, fail the connection
					byte len = (byte) (masklen & 0x7F);
					LOGGER.info("FRAME HEADER: {}", String.format("%02x %02x", finop, masklen));

					boolean shortPayload = len == 126;
					boolean longPayload = len == 127;
					long payloadLen = len;
					if (shortPayload) {
						//payload length is the next 16 bits (short)
						payloadLen = dataStream.readUnsignedShort();
					} else if (longPayload) {
						payloadLen = dataStream.readLong();
					}
					LOGGER.info("PAYLOAD LENGTH IS {}", payloadLen);

					byte[] mask = new byte[4];
					if (masked) {
						dataStream.read(mask, 0, 4);
						int maskVal = ByteBuffer.wrap(mask).getInt();
						LOGGER.info("MASK IS PRESENT: 0x{} ({})", Integer.toHexString(maskVal), maskVal);
					}

					StringBuilder payload = new StringBuilder();
					for (int i = 0; i < payloadLen; i++) {
						byte key = mask[i % 4];
						byte decoded = (byte) (dataStream.readUnsignedByte() ^ key);
						payload.append((char) decoded);
					}
					LOGGER.info("RECEIVED PAYLOAD:\n{}", payload);
					StringBuilder huge = new StringBuilder();
					Random r = new Random();
					for(int i = 0; i < 40000; i++){
						huge.append("A");
					}
					byte[] unmaskedPayload = buildUnmaskedPayload(huge.toString());
					out.write(unmaskedPayload);

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private static void howToReadWriteShort() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(out);
		output.writeShort(40000);
		// several network layers later....
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		DataInputStream dataIn = new DataInputStream(in);
		int len = dataIn.readUnsignedShort();
		int x = 0xff;
		System.out.println("====");
	}

	private static byte[] buildUnmaskedPayload(String payload) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(out);
		outputStream.writeByte(0x81);  // 10000001
		int len = payload.length();
		if (len < 125) {
			outputStream.writeByte(len & 0xFF);
		} else if ((len & 0xffff0000) == 0) { //len fits in a short
			// 01111110 = 126 = 0x7E
			outputStream.writeByte(0x7e);
			outputStream.writeShort(len);
		} else {
			// 01111111 = 127 = 0x7F
			outputStream.writeByte(0x7f);
			outputStream.writeLong(len);
		}
		outputStream.writeBytes(payload);
		return out.toByteArray();
	}

	/**
	 * HANDSHAKING
	 * actually doing the handshake is easier than understanding why its done this way
	 * read up on websocket handshaking
	 */
	private static boolean handshake(InputStream in, OutputStream out) throws NoSuchAlgorithmException, IOException {
		Scanner scannerIn = new Scanner(in, UTF_8).useDelimiter("\\r\\n\\r\\n");
		String data = scannerIn.next();
		Matcher get = Pattern.compile("^GET").matcher(data);
		if (!get.find()) return false;
		String webSocketAccept = getWebSocketAccept(data);
		byte[] response = buildResponse(webSocketAccept);
		out.write(response, 0, response.length);
		return webSocketAccept != null;
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
		if (!match.find()) return null;
		String webSocketKey = match.group(1);
		String linkedWebSocketKey = webSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		MessageDigest messageDigestSHA1 = MessageDigest.getInstance("SHA-1");
		return Base64.getEncoder().encodeToString(
				messageDigestSHA1.digest(linkedWebSocketKey.getBytes(StandardCharsets.UTF_8)));
	}
}
