# Copilot Setup Guide for ThreadSimulator

This directory contains configuration files for GitHub Copilot CLI integration. Use this guide to set up and configure Copilot for optimal productivity.

## 📋 Configuration Files

### 1. **copilot-instructions.md** (Primary Reference)
Main instruction file for Copilot CLI. Contains:
- Build, test, and lint commands
- Complete architecture overview
- Key conventions and patterns
- Known limitations and roadmap
- Common workflows for contributors

**Used by**: Copilot CLI automatically reads this file

### 2. **mcp-servers.md** (MCP Configuration)
Comprehensive guide for Model Context Protocol servers. Covers:
- What MCP servers are and why you need them
- Three options for Android development (GitHub, Gradle, ADB)
- Configuration at user and repository level
- Example usage and troubleshooting

**Use when**: Setting up build automation or device management with Copilot

### 3. **mcp-config.json.example** (Configuration Template)
Ready-to-use MCP server configuration template.

**To use**:
```bash
cp .github/mcp-config.json.example .github/mcp-config.json
```

**Contains**: Gradle and ADB server definitions with proper environment setup

## 🚀 Getting Started

### Minimal Setup (5 minutes)
```bash
# 1. Start Copilot CLI in the project directory
copilot

# 2. Inside Copilot CLI, verify GitHub MCP server is available
/mcp list
```

### Full Setup (10 minutes)
```bash
# 1. Copy MCP configuration template
cp .github/mcp-config.json.example .github/mcp-config.json

# 2. Start Copilot CLI
copilot

# 3. Verify all servers
/mcp list

# 4. Test connections (if needed)
/mcp test gradle
/mcp test adb
```

## 💡 Common Use Cases

### Build & Test Automation
```
"Use the gradle server to run all tests"
"Build a debug APK using ./gradlew assembleDebug"
"Check code quality with ./gradlew lint"
```

### GitHub Integration
```
"Check the status of recent workflow runs"
"Show me open pull requests assigned to me"
"Create a PR for these changes"
```

### Device Management
```
"List connected Android devices using adb"
"Install the app on the emulator"
"Run instrumented tests on the device"
```

## 🔧 Customization

### Adding Custom MCP Servers
Edit `.github/mcp-config.json` to add more servers:

```json
{
  "mcpServers": {
    "my-server": {
      "command": "bash",
      "args": ["-c", "your-command-here"],
      "env": {
        "VAR": "value"
      }
    }
  }
}
```

### User-level Configuration
For settings that apply to all your projects:

Edit `~/.copilot/mcp-config.json` (create if it doesn't exist)

## 📚 References

- **Copilot Instructions**: See `copilot-instructions.md`
- **MCP Server Setup**: See `mcp-servers.md`
- **GitHub Docs**: https://docs.github.com/en/copilot
- **MCP Protocol**: https://modelcontextprotocol.io/

## ❓ FAQ

**Q: Do I need to set up MCP servers?**  
A: No, they're optional. GitHub MCP server (built-in) works out of the box. Add Gradle/ADB servers if you want automation.

**Q: How do I know if MCP servers are working?**  
A: Run `/mcp list` in Copilot CLI to see configured servers.

**Q: Can I customize the MCP configuration?**  
A: Yes! Edit `.github/mcp-config.json` with your custom commands.

**Q: What's the difference between user and repository config?**  
A: Repository config (.github/mcp-config.json) only applies to this project. User config (~/.copilot/mcp-config.json) applies globally.

## 📝 Notes

- All paths in `mcp-config.json.example` are pre-configured for this repository
- MCP servers require the underlying tools to be installed (gradle, adb)
- Test servers with `/mcp test <server-name>` before using in prompts
- Check `MCP_SETUP_SUMMARY.md` for a quick reference guide

---

**Last Updated**: February 21, 2026  
**For issues or improvements**: Refer to `IMPROVEMENTS.md`
