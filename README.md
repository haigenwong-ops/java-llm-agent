# Java LLM Agent

A complete, production-ready Java-based AI agent that runs locally with Ollama. Features include:

- ✅ **Memory Management** - Conversation history with context window management
- ✅ **Tool Calling** - Execute custom tools (calculator, web search, code execution)
- ✅ **Error Handling** - Comprehensive exception handling and retry logic
- ✅ **Local LLM Integration** - Works with Ollama for completely local operation
- ✅ **Extensible Architecture** - Easy to add new tools and features

## Quick Start

### Prerequisites
- Java 11+
- Maven 3.6+
- Ollama installed (https://ollama.ai)

### Installation

1. **Install and start Ollama:**
```bash
# Install Ollama
ollama pull llama2
ollama serve
```

2. **Clone and build:**
```bash
git clone https://github.com/haigenwong-ops/java-llm-agent.git
cd java-llm-agent
mvn clean compile
```

3. **Run the agent:**
```bash
mvn exec:java -Dexec.mainClass="com.agent.Main"
```

## Usage

```
Welcome to Java LLM Agent!
You: What is 2 + 2?
Agent: 2 + 2 = 4

You: Calculate 10 * 5
Agent: 10 * 5 = 50

You: exit
Agent stopped. Goodbye!
```

## Architecture

```
java-llm-agent/
├── src/main/java/com/agent/
│   ├── core/
│   │   ├── Agent.java              # Main agent orchestrator
│   │   ├── AgentConfig.java        # Configuration builder
│   │   └── AgentResponse.java      # Response model
│   ├── llm/
│   │   ├── LLMClient.java          # LLM interface
│   │   ├── LLMResponse.java        # LLM response model
│   │   └── OllamaClient.java       # Ollama implementation
│   ├── memory/
│   │   ├── Memory.java             # Memory interface
│   │   ├── Message.java            # Message model
│   │   └── ConversationMemory.java # Memory implementation
│   ├── tools/
│   │   ├── Tool.java               # Tool interface
│   │   ├── ToolRegistry.java       # Tool management
│   │   └── tools/
│   │       ├── CalculatorTool.java # Math calculator
│   │       ├── WebSearchTool.java  # Web search (stub)
│   │       └── CodeExecutionTool.java # Code executor
│   ├── exception/
│   │   ├── AgentException.java
│   │   └── ToolExecutionException.java
│   └── Main.java                   # Entry point
└── pom.xml
```

## Features

### 1. Memory Management
- Stores conversation history
- Context window with token limits
- Automatic cleanup of old messages

### 2. Tool Calling
The agent can call tools to:
- Perform calculations
- Search information
- Execute code

### 3. Error Handling
- Automatic retry with exponential backoff
- Comprehensive exception handling
- Tool execution validation

### 4. Extensibility
Easy to add new tools:

```java
public class MyCustomTool implements Tool {
    @Override
    public String getName() { return "my_tool"; }
    
    @Override
    public String getDescription() { return "My custom tool"; }
    
    @Override
    public String execute(JsonObject input) throws Exception {
        // Implementation
        return result;
    }
}

// Register
agent.registerTool(new MyCustomTool());
```

## Configuration

```java
AgentConfig config = new AgentConfig.Builder()
    .llmBaseUrl("http://localhost:11434")
    .llmModel("llama2")        // Change model
    .maxRetries(3)             // Retry attempts
    .maxMemoryMessages(100)    // Max history size
    .contextTokenLimit(4000)   // Context window
    .enableToolCalls(true)     // Enable tools
    .build();

Agent agent = new Agent(config);
```

## Supported Models

Work with any model available in Ollama:
- `llama2` - Default, good all-around performance
- `llama3` - Latest Llama model
- `phi3` - Fast and lightweight
- `mistral` - Good for coding tasks
- `neural-chat` - Optimized for conversations

## Advanced Usage

### Programmatic Use

```java
Agent agent = new Agent(config);
agent.registerTool(new CalculatorTool());

AgentResponse response = agent.processInput("What is 2+2?");

if (response.isSuccess()) {
    System.out.println(response.getMessage());
} else if (response.isError()) {
    System.out.println("Error: " + response.getMessage());
}

// Get conversation history
System.out.println(agent.getConversationHistory());
```

### Tool Results

```java
AgentResponse response = agent.processInput("Calculate 10*5");

if (response.isToolCall()) {
    System.out.println("Tool: " + response.getToolName());
    System.out.println("Time: " + response.getExecutionTimeMs() + "ms");
}
```

## Troubleshooting

### Connection Error
Ensure Ollama is running:
```bash
ollama serve
```

### Model Not Found
Download the model first:
```bash
ollama pull llama2
```

### Memory Issues
Adjust max messages in config:
```java
.maxMemoryMessages(50)  // Reduce from 100
```

## Performance Tips

1. **Use smaller models** for faster responses
2. **Limit context window** to reduce processing time
3. **Batch tool calls** when possible
4. **Monitor memory** usage with large history

## Contributing

Feel free to extend with:
- New tools (database, file operations, APIs)
- Better tool calling patterns
- Advanced memory strategies (RAG, semantic search)
- Performance optimizations

## License

MIT License

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review tool schemas
3. Increase logging verbosity

## Next Steps

- [ ] Add database integration tools
- [ ] Implement semantic search memory
- [ ] Add streaming response support
- [ ] Create plugin system
- [ ] Add metrics/monitoring
