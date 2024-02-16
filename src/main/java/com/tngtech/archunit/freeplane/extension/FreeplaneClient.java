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
import java.util.Collection;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class FreeplaneClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeplaneClient.class);
    private final String host;
    private final int port;
    private final Gson gson;


    public FreeplaneClient(String host, int port) {
        super();
        this.host = host;
        this.port = port;
        this.gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Collection.class, new CollectionAdapter())
                .registerTypeHierarchyAdapter(Map.class, new MapAdapter())
                .create();
    }

    boolean sendJson(ArchitectureViolations data) {

        try (Socket socket = new Socket(host, port);
                OutputStream outputStream = socket.getOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(gzipOutputStream, "UTF-8"), true)) {
            gson.toJson(data, writer);
            LOGGER.info("Sent data to Freeplane");
            return true;
        } catch (IOException e) {
            LOGGER.error("Can't send data to Freeplane");
            return false;
        }
    }
}
