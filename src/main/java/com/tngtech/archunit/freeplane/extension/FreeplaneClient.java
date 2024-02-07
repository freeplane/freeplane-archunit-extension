/*
 * Created on 7 Feb 2024
 *
 * author dimitry
 */
package com.tngtech.archunit.freeplane.extension;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

class FreeplaneClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeplaneClient.class);
    private final String host;
    private final int port;


    public FreeplaneClient(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }


    void sendJson(EvaluatedRuleDto data) {
        Gson gson = new Gson();
        String jsonData = gson.toJson(data); // Serializing

        try (Socket socket = new Socket(host, port);
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

            writer.println(jsonData);
        } catch (IOException e) {
            LOGGER.error("Can't send data to Freeplane", e);
        }
    }
}
