#!/usr/bin/env python3
"""
Example Python client for Solace MCP Server
This demonstrates how to interact with the MCP server programmatically
"""

import json
import subprocess
import sys

class SolaceMcpClient:
    def __init__(self, jar_path="build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar"):
        self.jar_path = jar_path
        self.request_id = 0
    
    def send_request(self, method, params=None):
        """Send a JSON-RPC request to the MCP server"""
        self.request_id += 1
        request = {
            "jsonrpc": "2.0",
            "id": self.request_id,
            "method": method,
            "params": params or {}
        }
        
        # Convert to JSON and send to the server
        request_json = json.dumps(request)
        
        try:
            # Run the Java server with the request as input
            result = subprocess.run(
                ["java", "-jar", self.jar_path],
                input=request_json,
                capture_output=True,
                text=True,
                timeout=10
            )
            
            # Parse the response (filter out log lines)
            lines = result.stdout.strip().split('\n')
            for line in lines:
                if line.startswith('{'):
                    return json.loads(line)
            
            return None
        except subprocess.TimeoutExpired:
            print("Request timed out")
            return None
        except Exception as e:
            print(f"Error: {e}")
            return None
    
    def initialize(self):
        """Initialize the MCP connection"""
        return self.send_request("initialize", {
            "protocolVersion": "2024-11-05",
            "clientInfo": {
                "name": "python-mcp-client",
                "version": "1.0.0"
            }
        })
    
    def list_tools(self):
        """List available tools"""
        return self.send_request("tools/list", {})
    
    def send_solace_message(self, host, vpn, username, password, topic, message):
        """Send a message to Solace"""
        return self.send_request("tools/call", {
            "name": "send_solace_message",
            "arguments": {
                "host": host,
                "vpn": vpn,
                "username": username,
                "password": password,
                "topic": topic,
                "message": message
            }
        })
    
    def ping(self):
        """Ping the server"""
        return self.send_request("ping", {})

def main():
    print("=== Solace MCP Client Example ===\n")
    
    client = SolaceMcpClient()
    
    # Test 1: Initialize
    print("1. Initializing...")
    response = client.initialize()
    if response:
        print(f"   Server: {response['result']['serverInfo']['name']} v{response['result']['serverInfo']['version']}")
        print(f"   Protocol: {response['result']['protocolVersion']}")
    print()
    
    # Test 2: List tools
    print("2. Listing tools...")
    response = client.list_tools()
    if response and 'result' in response:
        for tool in response['result']['tools']:
            print(f"   - {tool['name']}: {tool['description']}")
    print()
    
    # Test 3: Ping
    print("3. Pinging server...")
    response = client.ping()
    if response and 'result' in response:
        print("   Pong!")
    print()
    
    # Test 4: Send message (example - will fail without running broker)
    print("4. Sending test message (will fail without running Solace broker)...")
    response = client.send_solace_message(
        host="tcp://localhost:55555",
        vpn="default",
        username="admin",
        password="admin",
        topic="test/topic",
        message="Hello from Python client!"
    )
    if response:
        if 'result' in response:
            for content in response['result'].get('content', []):
                print(f"   {content.get('text', '')}")
    print()
    
    print("=== Tests completed ===")

if __name__ == "__main__":
    main()
