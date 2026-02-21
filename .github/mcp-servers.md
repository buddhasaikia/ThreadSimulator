# MCP Servers Configuration for ThreadSimulator

This guide covers setting up Model Context Protocol (MCP) servers for enhanced Copilot CLI integration with Android development tools.

## What are MCP Servers?

MCP (Model Context Protocol) servers extend Copilot CLI's capabilities by providing access to specialized tools and services. They enable Copilot to interact with external systems like IDEs, build tools, and version control systems.

## Configuring MCP Servers

MCP servers can be configured at two levels:

### 1. User-level Configuration
Applies to all projects on your machine.

**File**: `~/.copilot/mcp-config.json`

### 2. Repository-level Configuration
Applies only to this project.

**File**: `.github/mcp-config.json` (create in repository root)

## Android Studio Integration

For ThreadSimulator, the following MCP servers are recommended:

### Available Android Development Tools

While GitHub doesn't provide a built-in Android Studio MCP server yet, you can leverage these alternatives:

#### Option 1: GitHub MCP Server (Built-in)
The GitHub MCP server is included by default and provides:
- Access to GitHub Actions workflows
- Pull request and issue management
- Repository information
- Build status checks

**No configuration needed** — this is available out of the box.

Use it in prompts like:
```
Use the GitHub MCP server to check the build status of recent workflows
```

#### Option 2: Custom Gradle MCP Server
Set up a custom MCP server to run Gradle commands directly.

**Repository-level configuration** (`.github/mcp-config.json`):

```json
{
  "mcpServers": {
    "gradle": {
      "command": "bash",
      "args": ["-c", "cd /Users/buddhasaikia/work/AndroidStudioProjects/ThreadSimulator && ./gradlew"],
      "env": {
        "GRADLE_OPTS": "-Xmx2g"
      }
    }
  }
}
```

This allows Copilot to:
- Run builds: `./gradlew build`
- Execute tests: `./gradlew test`
- Check code quality: `./gradlew lint`

#### Option 3: ADB (Android Debug Bridge) MCP Server
For device/emulator interaction:

```json
{
  "mcpServers": {
    "adb": {
      "command": "bash",
      "args": ["-c", "adb"]
    }
  }
}
```

Enables commands like:
- List connected devices: `adb devices`
- Install APK: `adb install app/build/outputs/apk/debug/app-debug.apk`
- Run instrumented tests: `adb shell am instrument ...`

## Managing MCP Servers from CLI

Within an interactive Copilot CLI session, use these commands:

```bash
# View and manage MCP servers
/mcp

# Available subcommands:
/mcp list          # List configured MCP servers
/mcp add           # Add a new MCP server
/mcp remove        # Remove an MCP server
/mcp test          # Test a server connection
```

## Example Usage

Once configured, reference MCP servers in your Copilot prompts:

```
"Use the gradle server to run all tests and show me any failures"
"Use the github server to create a PR for my current changes"
"Use the adb server to check if the emulator is running"
```

## Recommended Setup for ThreadSimulator

For optimal Copilot CLI + Android development experience:

1. **Keep GitHub server enabled** (default) for PR and workflow management
2. **Configure Gradle server** for build and test automation
3. **Add ADB server** if doing device testing
4. **Use LSP servers** (configured separately) for code intelligence

See `.github/lsp.json` for Language Server Protocol configuration if present.

## Troubleshooting

### MCP Server Not Responding
- Verify the command path is correct
- Check file permissions
- Test the command manually in terminal

### Permission Denied Errors
```bash
# Make scripts executable
chmod +x ./gradlew

# Verify Android tools are in PATH
which adb
which gradle
```

### Environment Variables Not Set
- Define variables in the MCP config's `env` section
- Or set them system-wide in your shell profile

## Documentation

For more information on MCP servers:
- GitHub Copilot CLI docs: https://docs.github.com/en/copilot/concepts/agents/about-copilot-cli
- MCP Protocol specs: https://modelcontextprotocol.io/

## Next Steps

1. Create `.github/mcp-config.json` in this repository if needed
2. Test MCP servers with `/mcp` command in Copilot CLI
3. Reference them in your prompts for enhanced capabilities
4. Report any issues or improvements to project maintainers
