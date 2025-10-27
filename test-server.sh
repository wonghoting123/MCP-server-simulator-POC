#!/bin/bash

# Example script for testing the Solace MCP Server
# This script demonstrates how to interact with the server

JAR_FILE="build/libs/solace-mcp-server-1.0-SNAPSHOT-all.jar"

echo "=== Testing Solace MCP Server ==="
echo ""

# Test 1: Initialize
echo "1. Testing initialize..."
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","clientInfo":{"name":"test-client","version":"1.0.0"}}}' | java -jar $JAR_FILE
echo ""

# Test 2: List tools
echo "2. Testing tools/list..."
echo '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}' | java -jar $JAR_FILE
echo ""

# Test 3: Ping
echo "3. Testing ping..."
echo '{"jsonrpc":"2.0","id":3,"method":"ping","params":{}}' | java -jar $JAR_FILE
echo ""

# Test 4: Send message (will fail without a running Solace broker)
echo "4. Testing send_solace_message (will fail without running broker)..."
echo '{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"send_solace_message","arguments":{"host":"tcp://localhost:55555","vpn":"default","username":"admin","password":"admin","topic":"test/topic","message":"Hello from MCP!"}}}' | java -jar $JAR_FILE
echo ""

echo "=== Tests completed ==="
