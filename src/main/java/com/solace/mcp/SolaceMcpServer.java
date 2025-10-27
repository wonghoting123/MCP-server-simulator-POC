package com.solace.mcp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * MCP Server for Solace Messaging
 * This server implements the Model Context Protocol (MCP) to provide tools for sending messages
 * to Solace PubSub+ brokers.
 */
public class SolaceMcpServer {
    private static final Logger logger = LoggerFactory.getLogger(SolaceMcpServer.class);
    private static final Gson gson = new Gson();
    
    private final SolaceMessagePublisher publisher;
    private boolean running = true;

    public SolaceMcpServer() {
        this.publisher = new SolaceMessagePublisher();
    }

    public static void main(String[] args) {
        SolaceMcpServer server = new SolaceMcpServer();
        server.start();
    }

    public void start() {
        logger.info("Starting Solace MCP Server");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (running) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                
                try {
                    JsonObject request = JsonParser.parseString(line).getAsJsonObject();
                    handleRequest(request);
                } catch (Exception e) {
                    logger.error("Error processing request: {}", e.getMessage());
                    sendError(-32700, "Parse error", null);
                }
            }
        } catch (IOException e) {
            logger.error("IO error: {}", e.getMessage());
        }
        
        publisher.close();
        logger.info("Solace MCP Server stopped");
    }

    private void handleRequest(JsonObject request) {
        String method = request.has("method") ? request.get("method").getAsString() : null;
        Object id = request.has("id") ? request.get("id") : null;
        JsonObject params = request.has("params") ? request.getAsJsonObject("params") : new JsonObject();

        if (method == null) {
            sendError(-32600, "Invalid Request", id);
            return;
        }

        switch (method) {
            case "initialize":
                handleInitialize(id, params);
                break;
            case "tools/list":
                handleToolsList(id);
                break;
            case "tools/call":
                handleToolsCall(id, params);
                break;
            case "ping":
                handlePing(id);
                break;
            default:
                sendError(-32601, "Method not found", id);
        }
    }

    private void handleInitialize(Object id, JsonObject params) {
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        result.put("serverInfo", Map.of(
            "name", "solace-mcp-server",
            "version", "1.0.0"
        ));
        result.put("capabilities", Map.of(
            "tools", Map.of()
        ));
        
        sendResponse(id, result);
    }

    private void handleToolsList(Object id) {
        Map<String, Object> result = new HashMap<>();
        result.put("tools", new Object[] {
            Map.of(
                "name", "send_solace_message",
                "description", "Send a message to a Solace PubSub+ broker topic",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "host", Map.of(
                            "type", "string",
                            "description", "Solace broker host (e.g., tcp://localhost:55555)"
                        ),
                        "vpn", Map.of(
                            "type", "string",
                            "description", "Message VPN name"
                        ),
                        "username", Map.of(
                            "type", "string",
                            "description", "Username for authentication"
                        ),
                        "password", Map.of(
                            "type", "string",
                            "description", "Password for authentication"
                        ),
                        "topic", Map.of(
                            "type", "string",
                            "description", "Topic to publish message to"
                        ),
                        "message", Map.of(
                            "type", "string",
                            "description", "Message content to send"
                        )
                    ),
                    "required", new String[] {"host", "vpn", "username", "password", "topic", "message"}
                )
            )
        });
        
        sendResponse(id, result);
    }

    private void handleToolsCall(Object id, JsonObject params) {
        if (!params.has("name") || !params.has("arguments")) {
            sendError(-32602, "Invalid params", id);
            return;
        }

        String toolName = params.get("name").getAsString();
        JsonObject arguments = params.getAsJsonObject("arguments");

        if ("send_solace_message".equals(toolName)) {
            handleSendSolaceMessage(id, arguments);
        } else {
            sendError(-32601, "Tool not found", id);
        }
    }

    private void handleSendSolaceMessage(Object id, JsonObject arguments) {
        try {
            String host = arguments.get("host").getAsString();
            String vpn = arguments.get("vpn").getAsString();
            String username = arguments.get("username").getAsString();
            String password = arguments.get("password").getAsString();
            String topic = arguments.get("topic").getAsString();
            String message = arguments.get("message").getAsString();

            boolean success = publisher.sendMessage(host, vpn, username, password, topic, message);
            
            Map<String, Object> result = new HashMap<>();
            if (success) {
                result.put("content", new Object[] {
                    Map.of(
                        "type", "text",
                        "text", String.format("Successfully sent message to topic '%s'", topic)
                    )
                });
            } else {
                result.put("content", new Object[] {
                    Map.of(
                        "type", "text",
                        "text", "Failed to send message"
                    )
                });
                result.put("isError", true);
            }
            
            sendResponse(id, result);
        } catch (Exception e) {
            logger.error("Error sending Solace message: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("content", new Object[] {
                Map.of(
                    "type", "text",
                    "text", "Error: " + e.getMessage()
                )
            });
            result.put("isError", true);
            sendResponse(id, result);
        }
    }

    private void handlePing(Object id) {
        sendResponse(id, Map.of());
    }

    private void sendResponse(Object id, Object result) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("result", result);
        
        String json = gson.toJson(response);
        System.out.println(json);
        System.out.flush();
    }

    private void sendError(int code, String message, Object id) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("error", Map.of(
            "code", code,
            "message", message
        ));
        
        String json = gson.toJson(response);
        System.out.println(json);
        System.out.flush();
    }
}
