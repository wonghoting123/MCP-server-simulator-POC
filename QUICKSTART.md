# Quick Start Guide

This guide will help you get started with the Solace MCP Server in minutes.

## Prerequisites

- Java 11+ installed
- Git (to clone the repository)
- Docker (optional, for running Solace locally)

## Step 1: Build the Server

```bash
# Navigate to the project directory
cd MCP-server-simulator-POC

# Build the project
./gradlew build

# Create the fat JAR with all dependencies
./gradlew fatJar
```

The server JAR will be created at: `build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar`

## Step 2: Start a Solace Broker (Local Development)

Using Docker Compose:

```bash
# Start Solace broker
docker-compose up -d

# Wait ~30 seconds for the broker to start
# Access the web UI at http://localhost:8080 (admin/admin)
```

Or using Docker directly:

```bash
docker run -d -p 55555:55555 -p 8080:8080 \
  --shm-size=2g \
  --env username_admin_globalaccesslevel=admin \
  --env username_admin_password=admin \
  --name=solace \
  solace/solace-pubsub-standard
```

## Step 3: Test the Server

### Option A: Use the Bash Test Script

```bash
./test-server.sh
```

### Option B: Use the Python Client

```bash
python3 example-client.py
```

### Option C: Manual Testing

Test individual commands:

```bash
# Initialize
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","clientInfo":{"name":"test","version":"1.0.0"}}}' | java -jar build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar

# List tools
echo '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}' | java -jar build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar

# Send a message
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"send_solace_message","arguments":{"host":"tcp://localhost:55555","vpn":"default","username":"admin","password":"admin","topic":"test/hello","message":"Hello Solace!"}}}' | java -jar build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar
```

## Step 4: Integrate with MCP Clients

### Claude Desktop

Add to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "solace": {
      "command": "java",
      "args": [
        "-jar",
        "/full/path/to/build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar"
      ]
    }
  }
}
```

### Other MCP Clients

Use the example in `mcp-config.example.json` and update the path to match your installation.

## Verify Message Delivery

You can verify messages are being published using:

1. **Solace Web UI**: Navigate to http://localhost:8080, login (admin/admin), and go to Message VPN → Try Me! to subscribe to your topic

2. **Command-line subscriber** (if you have Solace tools installed):
   ```bash
   # Subscribe to all topics under test/
   solace subscribe --host tcp://localhost:55555 --vpn default \
     --username admin --password admin --topic "test/>"
   ```

## Troubleshooting

### Build Fails

- Ensure Java 11+ is installed: `java -version`
- Try cleaning the build: `./gradlew clean build`

### Cannot Connect to Solace

- Verify the broker is running: `docker ps | grep solace`
- Check broker logs: `docker logs solace`
- Ensure port 55555 is not blocked by firewall
- Wait 30-60 seconds after starting the broker

### Message Not Received

- Verify you're subscribing to the correct topic
- Check topic permissions in Solace (ACL profiles)
- Review Solace broker logs for errors

## Next Steps

- Read the full [README.md](README.md) for detailed documentation
- Explore the Solace PubSub+ features at https://docs.solace.com/
- Customize the server for your specific use case
- Add authentication and security configurations

## Common Connection Strings

- **Local Docker**: `tcp://localhost:55555`
- **Solace Cloud**: `tcps://your-service.messaging.solace.cloud:55443`
- **Enterprise Broker**: `tcp://your-broker-hostname:55555`

Remember to update VPN name, username, and password based on your Solace configuration!
