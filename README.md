# External Tool Opener

[![Version](https://img.shields.io/badge/version-1.1.3-blue.svg)](https://github.com/Lemon695/external-tool-opener)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange.svg)](https://plugins.jetbrains.com/)

A powerful IntelliJ Platform plugin that allows you to open files with external applications directly from the IDE context menu.

[中文文档](README_CN.md)

## ✨ Features

- **🎯 Smart Context Menu** - Right-click any file to open with configured external tools
- **🔍 Auto-Detection** - Automatically detect installed applications on your system
- **⚡ Intelligent Pre-selection** - Popular tools are pre-selected based on priority
- **🎨 Flexible Configuration** - Configure unlimited external applications
- **📁 File Type Association** - Link specific file extensions to preferred tools
- **🌍 Cross-Platform** - Supports Windows, macOS, and Linux
- **🔄 Smart Merge** - Preserves your settings when detecting new tools

## 🚀 Quick Start

### Installation

1. Download the latest release from [Releases](https://github.com/Lemon695/external-tool-opener/releases)
2. Open your IDE: **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk**
3. Select the downloaded `.zip` file
4. Restart your IDE

### First Use

1. **Auto-Detect Tools**:
   - Go to **Settings** → **Tools** → **External Tool Opener**
   - Click **"Detect Tools"** button
   - Review detected tools and enable the ones you want

2. **Manual Configuration**:
   - Click **"+"** to add a new tool
   - Set name, executable path, and supported extensions
   - Click **OK** to save

3. **Use It**:
   - Right-click any file in Project View or Editor
   - Select **"Open With..."**
   - Choose your external tool

## 📋 Supported IDEs

Compatible with **all JetBrains IDEs** (2020.1+):

- ✅ IntelliJ IDEA
- ✅ PyCharm
- ✅ WebStorm
- ✅ PhpStorm
- ✅ GoLand
- ✅ RubyMine
- ✅ CLion
- ✅ Android Studio
- ✅ Rider
- ✅ DataGrip

## 🛠️ Pre-configured Tools

The plugin includes detection for 10 popular tools:

| Tool | Category | Priority | Platforms |
|------|----------|----------|-----------|
| Visual Studio Code | Editor | ⭐⭐⭐ | Win/Mac/Linux |
| Sublime Text | Editor | ⭐⭐ | Win/Mac/Linux |
| Typora | Markdown | ⭐⭐⭐ | Win/Mac/Linux |
| Trae | Markdown | ⭐⭐⭐ | Mac |
| Kiro | Markdown | ⭐⭐⭐ | Mac |
| IntelliJ IDEA | IDE | ⭐⭐ | Win/Mac/Linux |
| Notepad++ | Editor | ⭐ | Windows |
| Atom | Editor | ⭐ | Win/Mac/Linux |
| Vim/MacVim | Editor | ⭐ | Mac/Linux |

## 🎨 Screenshots

### Context Menu
![Context Menu](docs/screenshots/context-menu.png)

### Auto-Detection Dialog
![Auto Detection](docs/screenshots/auto-detection.png)

### Settings Panel
![Settings](docs/screenshots/settings.png)

## 🔧 Configuration

### Tool Properties

- **Name**: Display name in the menu
- **Executable Path**: Full path to the application
- **Supported Extensions**: File types (e.g., `.md`, `.js`)
- **Enabled**: Toggle tool visibility
- **Priority**: Used for smart pre-selection (1-10)

### Smart Detection

The auto-detection system:
1. Scans common installation paths
2. Verifies tool accessibility
3. Pre-selects tools with priority ≥ 8
4. Merges with existing configuration
5. New tools are disabled by default

### Path Templates

Supports dynamic path expansion:
- `{user}` - Current username
- `{home}` - User home directory

Example: `C:\Users\{user}\AppData\Local\Programs\...`

## 📖 Usage Examples

### Open Markdown Files with Typora

1. Configure Typora:
   - Path: `/Applications/Typora.app` (Mac)
   - Extensions: `.md`, `.markdown`
   - Enabled: ✓

2. Right-click any `.md` file → **Open With...** → **Typora**

### Open Code with VS Code

1. Auto-detect or manually add VS Code
2. Right-click project files → **Open With...** → **Visual Studio Code**

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Setup

```bash
# Clone the repository
git clone https://github.com/Lemon695/external-tool-opener.git
cd external-tool-opener

# Build the plugin
./gradlew buildPlugin

# Run in IDE
./gradlew runIde
```

### Project Structure

```
external-tool-opener-plugin/
├── src/main/
│   ├── java/com/lemon/externaltool/
│   │   ├── action/          # Context menu actions
│   │   ├── config/          # Settings UI
│   │   ├── model/           # Data models
│   │   ├── service/         # Core services
│   │   └── ui/              # UI components
│   └── resources/
│       ├── META-INF/
│       │   └── plugin.xml   # Plugin configuration
│       └── tool-registry.yaml  # Tool definitions
├── build.gradle.kts         # Build configuration
└── README.md
```

## 📝 Changelog

### Version 1.1.3 (2026-01-04)

- ✨ Added intelligent tool selection dialog
- ✨ Implemented auto-detection for 10 popular tools
- ✨ Added Trae and Kiro support
- 🔧 Optimized menu position (after "Reveal In")
- 🔧 Extended IDE compatibility to 2020.1+
- 🐛 Fixed compatibility issues with PyCharm 2025.3

### Version 1.0.0

- 🎉 Initial release
- ✨ Basic external tool configuration
- ✨ Context menu integration
- ✨ File type association

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Thanks to all contributors
- Inspired by the need for better external tool integration
- Built with [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/)

## 📞 Support

- 🐛 [Report Issues](https://github.com/Lemon695/external-tool-opener/issues)
- 💡 [Feature Requests](https://github.com/Lemon695/external-tool-opener/issues/new)
- 📧 Email: -

## ⭐ Star History

If you find this plugin useful, please consider giving it a star!

---

Made with ❤️ by [Lemon695](https://github.com/Lemon695)
