package com.gabb.sb;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
		Scanner scannerIn = new Scanner(in, UTF_8);

		/**
		 * HANDSHAKING
		 * actually doing the handshake is easier than understanding why its done this way
		 * read up on websocket handshaking
		 */
		
		String data = scannerIn.useDelimiter("\\r\\n\\r\\n").next();
		LOGGER.info("Received input: {}", data);
		Matcher get = Pattern.compile("^GET").matcher(data);

		if (get.find()) {
			LOGGER.info("Received GET");
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
		if (!match.find()) return null;
		String webSocketKey = match.group(1);
		String linkedWebSocketKey = webSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		MessageDigest messageDigestSHA1 = MessageDigest.getInstance("SHA-1");
		return Base64.getEncoder().encodeToString(
				messageDigestSHA1.digest(linkedWebSocketKey.getBytes(StandardCharsets.UTF_8)));
	}
}
