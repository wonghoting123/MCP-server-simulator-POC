# Solace MCP Server

A Model Context Protocol (MCP) server implementation for sending messages to Solace PubSub+ brokers using Java and Gradle.

## Overview

This MCP server provides tools for interacting with Solace PubSub+ message brokers, allowing you to send messages to topics through a standardized MCP interface. It implements the Model Context Protocol specification and uses the Solace Java API (JCSMP) for messaging operations.

## Features

- **MCP Protocol Support**: Implements MCP 2024-11-05 specification
- **Solace Integration**: Send messages to Solace PubSub+ brokers
- **Flexible Configuration**: Support for various Solace broker configurations
- **Topic Publishing**: Publish text messages to any Solace topic
- **Connection Management**: Automatic connection handling per request

## Prerequisites

- Java 11 or higher
- Gradle 8.x (or use included wrapper)
- Access to a Solace PubSub+ broker (local, cloud, or enterprise)

## Building the Project

Build the project using Gradle:

```bash
./gradlew build
```

Create a fat JAR with all dependencies:

```bash
./gradlew fatJar
```

The fat JAR will be created in `build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar`

## Running the Server

Run directly with Gradle:

```bash
./gradlew run
```

Or run the fat JAR:

```bash
java -jar build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar
```

The server communicates via JSON-RPC 2.0 over stdin/stdout.

## MCP Tools

### send_solace_message

Send a message to a Solace PubSub+ broker topic.

**Parameters:**
- `host` (string, required): Solace broker host (e.g., `tcp://localhost:55555`)
- `vpn` (string, required): Message VPN name
- `username` (string, required): Username for authentication
- `password` (string, required): Password for authentication
- `topic` (string, required): Topic to publish message to
- `message` (string, required): Message content to send

**Example Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "send_solace_message",
    "arguments": {
      "host": "tcp://localhost:55555",
      "vpn": "default",
      "username": "admin",
      "password": "admin",
      "topic": "test/topic",
      "message": "Hello from MCP!"
    }
  }
}
```

## MCP Protocol Methods

The server implements the following MCP methods:

- `initialize`: Initialize the MCP connection
- `tools/list`: List available tools
- `tools/call`: Execute a tool
- `ping`: Health check

## Setting up Solace PubSub+

### Local Development

You can run Solace PubSub+ locally using Docker:

```bash
docker run -d -p 55555:55555 -p 8080:8080 -p 1883:1883 -p 8000:8000 -p 5672:5672 -p 9000:9000 \
  --shm-size=2g --env username_admin_globalaccesslevel=admin \
  --env username_admin_password=admin --name=solace solace/solace-pubsub-standard
```

Default credentials:
- Host: `tcp://localhost:55555`
- VPN: `default`
- Username: `admin`
- Password: `admin`

### Solace Cloud

Sign up for a free Solace Cloud account at https://console.solace.cloud/ and create a messaging service. Use the connection details provided in your service dashboard.

## Example Usage with MCP Client

```bash
# Initialize
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","clientInfo":{"name":"test-client","version":"1.0.0"}}}' | java -jar build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar

# List tools
echo '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}' | java -jar build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar

# Send a message
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"send_solace_message","arguments":{"host":"tcp://localhost:55555","vpn":"default","username":"admin","password":"admin","topic":"test/topic","message":"Hello Solace!"}}}' | java -jar build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar
```

## Project Structure

```
.
├── build.gradle                      # Gradle build configuration
├── settings.gradle                   # Gradle settings
├── src/
│   └── main/
│       ├── java/
│       │   └── com/solace/mcp/
│       │       ├── SolaceMcpServer.java           # Main MCP server implementation
│       │       └── SolaceMessagePublisher.java    # Solace messaging logic
│       └── resources/
│           └── solace-config.properties.template  # Configuration template
└── README.md
```

## Dependencies

- **Solace JCSMP**: Official Solace Java API for messaging
- **Gson**: JSON processing
- **SLF4J**: Logging framework

## Security Considerations

- Never commit credentials to version control
- Use environment variables or secure configuration management for sensitive data
- Consider using SSL/TLS connections (tcps://) for production environments
- Implement proper authentication and authorization in production deployments

## Troubleshooting

### Connection Issues

If you cannot connect to Solace:
1. Verify the broker is running and accessible
2. Check the host, VPN name, username, and password
3. Ensure firewall rules allow connections on the specified port
4. Check Solace broker logs for authentication errors

### Message Publishing Issues

If messages fail to publish:
1. Verify you have publish permissions on the topic
2. Check the topic name format (no wildcards in publish topics)
3. Review Solace broker logs for ACL or quota violations

## License

This project is provided as a proof of concept for MCP server implementation with Solace messaging.

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## Resources

- [Model Context Protocol Documentation](https://modelcontextprotocol.io/)
- [Solace PubSub+ Documentation](https://docs.solace.com/)
- [Solace Java API Reference](https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/index.html)
