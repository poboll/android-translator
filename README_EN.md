<div align="center">

# Translator (Android Translator)

_✨ AI-powered Intelligent Multilingual Translation App ✨_

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com/)
[![Language](https://img.shields.io/badge/language-Java-orange.svg)](https://www.java.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Version](https://img.shields.io/badge/version-1.0-informational)](https://github.com/poboll/android-translator/releases)

[简体中文](README.md) | [English](README_EN.md)

<img src="Screenshot_20250602_213110.png" width="200"> <img src="Screenshot_20250602_213150.png" width="200"> <img src="Screenshot_20250602_213308.png" width="200">
  <img src="https://cdn.jsdelivr.net/gh/poboll/android-translator@main/images/Screenshot_20250602_213150.png" width="200">
  <img src="https://cdn.jsdelivr.net/gh/poboll/android-translator@main/images/Screenshot_20250602_213308.png" width="200">
</div>
<div style="display: flex; flex-wrap: wrap; justify-content: center; gap: 10px;">
  <img src="https://cdn.jsdelivr.net/gh/poboll/android-translator@main/images/Screenshot_20250603_214235.png" width="200">
  <img src="https://cdn.jsdelivr.net/gh/poboll/android-translator@main/images/Screenshot_20250603_214245.png" width="200">
  <img src="https://cdn.jsdelivr.net/gh/poboll/android-translator@main/images/Screenshot_20250603_214438.png" width="200">
</div>

</div>

## 📝 Project Introduction

**Translator** is an intelligent multilingual translation application based on advanced AI technology, designed to provide fast, accurate, and completely free text translation services. Initially developed as an Android course design project, it has now evolved into a fully-featured translation tool that helps users easily overcome language barriers and achieve global communication.

The project is divided into two parts: Android client and PHP server, using a front-end and back-end separation architecture to provide stable and efficient translation services.

🌐 **Online Experience**: [https://translate.caiths.com/](https://translate.caiths.com/)

## ✨ Core Features

- **🌍 Global Language Support**: Supports translation between dozens of mainstream languages, covering most global communication needs
- **⚡ Efficient Translation Engine**: Optimized algorithms and powerful backend support, millisecond-level response, instant translation results
- **🎯 AI-Accurate Translation**: Integrates leading artificial intelligence translation engines to ensure more natural and accurate translation results
- **📱 Android Native Experience**: Specially designed for Android users, with a clean interface, convenient operation, and a smooth native application experience
- **🔒 Privacy Security**: Highly values user data security and privacy, does not save user translation records
- **💖 Completely Free**: All core functions are permanently free, no ad interference, pure experience

## 🛠️ Technical Architecture

### Client (Android)

- **Development Language**: Java
- **Minimum SDK Version**: 24 (Android 7.0)
- **Target SDK Version**: 30 (Android 11)
- **Build Tool**: Gradle
- **Main Dependencies**:
  - AndroidX
  - Material Design Components
  - OkHttp (Network Requests)
  - Baidu Voice SDK (Speech Recognition)

### Server (PHP)

- **Development Language**: PHP
- **Database**: MySQL
- **API Interfaces**:
  - Text Translation API
  - Image Translation API
  - Word Check API
  - Application Update API

## 📋 Feature List

- **Text Translation**: Supports text translation between multiple languages
- **Voice Recognition**: Supports voice input, automatically converts to text
- **Image Translation**: Supports recognition and translation of text in images
- **History Records**: Saves translation history for easy reference
- **Favorites Function**: Bookmark commonly used translation results
- **Offline Translation**: Supports offline translation for some languages
- **Automatic Updates**: In-app automatic detection and update prompts

## 🚀 Quick Start

### Client Installation

#### Method 1: Direct APK Download

1. Visit the [Official Website](https://translate.caiths.com/) to download the latest version APK
2. Install the APK file on your Android device
3. Open the app and start using it

#### Method 2: Build from Source

1. Clone the repository
   ```bash
   git clone https://github.com/poboll/android-translator.git
   ```

2. Open the `client` directory with Android Studio

3. Configure API keys
   - Configure related API keys in `app/src/main/res/values/strings.xml`

4. Build and run the application

### Server Deployment

1. Configure Web server environment (Apache/Nginx + PHP + MySQL)

2. Import the database
   ```bash
   mysql -u username -p database_name < server/legal_en_word.sql
   ```

3. Modify configuration files
   - Edit configuration items in `server/api/translator.php`
   - Set database connection information and API keys

4. Deploy files in the `server` directory to the Web server

## 🔧 Configuration Instructions

### Client Configuration

Configure in `app/src/main/res/values/strings.xml`:

```xml
<string name="api_base_url">https://your-server.com/api/</string>
<string name="api_key">YOUR_API_KEY</string>
```

### Server Configuration

Configure in `server/api/translator.php`:

```php
public static $BAIDU_APP_ID = 'YOUR_BAIDU_APP_ID';
public static $BAIDU_SEC_KEY = 'YOUR_BAIDU_SECRET_KEY';
public static $COMMON_AUTH_KEY = 'YOUR_AUTH_KEY';
// Other configuration items...
```

## 📱 Application Scenarios

- **🌍 International Travel**: Easily read menus, road signs, and communicate smoothly with locals
- **📚 Learning and Research**: Quickly translate foreign literature and academic papers, improving learning efficiency
- **💼 Business Communication**: Handle multilingual emails and documents, participate in international conferences
- **💬 Daily Social Interaction**: Chat and interact with friends from different countries, understand diverse cultures

## 🔌 Developer API

Translator provides a stable, efficient, and completely free text translation API that allows developers to easily integrate multilingual translation capabilities into their own applications.

### API Example (PHP)

```php
// POST Request Example
$url = 'https://translate.caiths.com/api/translate/common.php';
$data = [
    'auth_key' => 'YOUR_FREE_API_KEY',
    'query' => 'Hello, world!',
    'from' => 'auto',
    'to' => 'zh'
];

$options = [
    'http' => [
        'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
        'method'  => 'POST',
        'content' => http_build_query($data)
    ]
];

$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);

echo $result; // {"from":"en","to":"zh","trans_result":[{"src":"Hello, world!","dst":"你好，世界！"}]}
```

## 🤝 Contribution Guidelines

Contributions to the Translator project are welcome! Whether reporting issues, suggesting new features, or submitting code, we greatly appreciate it.

1. Fork the project
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Create a Pull Request

## 📄 Open Source License

This project is licensed under the [Apache License 2.0](LICENSE).

## 📞 Contact Us

- **Project Homepage**: [GitHub](https://github.com/poboll/android-translator)
- **Issue Feedback**: [Issues](https://github.com/poboll/android-translator/issues)
- **Official Website**: [https://translate.caiths.com/](https://translate.caiths.com/)

---

<div align="center">

**Translator** - Instant Translation, Connecting the World

</div>