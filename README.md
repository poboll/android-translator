<div align="center">

# 翻译君 (Android Translator)

_✨ 基于AI技术的智能多语言翻译应用 ✨_

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com/)
[![Language](https://img.shields.io/badge/language-Java-orange.svg)](https://www.java.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Version](https://img.shields.io/badge/version-1.0-informational)](https://github.com/poboll/android-translator/releases)

[简体中文](README.md) | [English](README_EN.md)

<img src="Screenshot_20250602_213110.png" width="200"> <img src="Screenshot_20250602_213150.png" width="200"> <img src="Screenshot_20250602_213308.png" width="200">

</div>

## 📝 项目介绍

**翻译君**是一款基于先进AI技术的智能多语言翻译应用，旨在提供快速、精准、完全免费的文本翻译服务。本项目最初作为安卓课程设计项目开发，现已发展成为一个功能完善的翻译工具，帮助用户轻松跨越语言障碍，实现全球沟通。

项目分为安卓客户端和PHP服务器端两部分，采用前后端分离架构，提供稳定高效的翻译服务。

🌐 **在线体验**: [https://translate.caiths.com/](https://translate.caiths.com/)

## ✨ 核心特性

- **🌍 全球语言支持**: 支持数十种主流语言互译，覆盖全球大部分沟通需求
- **⚡ 高效翻译引擎**: 优化算法与强大后端支撑，毫秒级响应，即时翻译结果
- **🎯 AI精准翻译**: 集成领先的人工智能翻译引擎，确保翻译结果更自然、更精准
- **📱 安卓原生体验**: 专为安卓用户打造，界面清爽，操作便捷，提供流畅的原生应用体验
- **🔒 注重隐私安全**: 高度重视用户数据安全与隐私，不保存用户翻译记录
- **💖 完全免费**: 所有核心功能永久免费，无广告干扰，纯净体验

## 🛠️ 技术架构

### 客户端 (Android)

- **开发语言**: Java
- **最低SDK版本**: 24 (Android 7.0)
- **目标SDK版本**: 30 (Android 11)
- **构建工具**: Gradle
- **主要依赖**:
  - AndroidX
  - Material Design Components
  - OkHttp (网络请求)
  - 百度语音SDK (语音识别)

### 服务器端 (PHP)

- **开发语言**: PHP
- **数据库**: MySQL
- **API接口**:
  - 文本翻译API
  - 图片翻译API
  - 单词检查API
  - 应用更新API

## 📋 功能列表

- **文本翻译**: 支持多种语言间的文本互译
- **语音识别**: 支持语音输入，自动转换为文字
- **图片翻译**: 支持图片中文字的识别与翻译
- **历史记录**: 保存翻译历史，方便查询
- **收藏功能**: 收藏常用翻译结果
- **离线翻译**: 支持部分语言的离线翻译功能
- **自动更新**: 应用内自动检测并提示更新

## 🚀 快速开始

### 客户端安装

#### 方法一：直接下载APK

1. 访问 [官方网站](https://translate.caiths.com/) 下载最新版本APK
2. 在Android设备上安装APK文件
3. 打开应用，开始使用

#### 方法二：从源码构建

1. 克隆仓库
   ```bash
   git clone https://github.com/poboll/android-translator.git
   ```

2. 使用Android Studio打开`client`目录

3. 配置API密钥
   - 在`app/src/main/res/values/strings.xml`中配置相关API密钥

4. 构建并运行应用

### 服务器端部署

1. 配置Web服务器环境(Apache/Nginx + PHP + MySQL)

2. 导入数据库
   ```bash
   mysql -u username -p database_name < server/legal_en_word.sql
   ```

3. 修改配置文件
   - 编辑`server/api/translator.php`中的配置项
   - 设置数据库连接信息和API密钥

4. 将`server`目录下的文件部署到Web服务器

## 🔧 配置说明

### 客户端配置

在`app/src/main/res/values/strings.xml`中配置：

```xml
<string name="api_base_url">https://your-server.com/api/</string>
<string name="api_key">YOUR_API_KEY</string>
```

### 服务器端配置

在`server/api/translator.php`中配置：

```php
public static $BAIDU_APP_ID = 'YOUR_BAIDU_APP_ID';
public static $BAIDU_SEC_KEY = 'YOUR_BAIDU_SECRET_KEY';
public static $COMMON_AUTH_KEY = 'YOUR_AUTH_KEY';
// 其他配置项...
```

## 📱 应用场景

- **🌍 跨国旅行**: 轻松阅读菜单、路牌，与当地人顺畅交流
- **📚 学习研究**: 快速翻译外语文献、学术论文，提升学习效率
- **💼 商务沟通**: 处理多语言邮件、文档，参与国际会议
- **💬 日常社交**: 与不同国家的朋友聊天互动，了解多元文化

## 🔌 开发者API

翻译君提供了稳定、高效、完全免费的文本翻译API，开发者可以轻松将多语言翻译能力集成到自己的应用中。

### API示例 (PHP)

```php
// POST请求示例
$url = 'https://translate.caiths.com/api/translate/common.php';
$data = [
    'auth_key' => 'YOUR_FREE_API_KEY',
    'query' => '你好，世界！',
    'from' => 'auto',
    'to' => 'en'
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

echo $result; // {"from":"zh","to":"en","trans_result":[{"src":"你好，世界！","dst":"Hello, world!"}]}
```

## 🤝 贡献指南

欢迎为翻译君项目做出贡献！无论是报告问题、提出新功能建议，还是提交代码，我们都非常感谢。

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request

## 📄 开源许可

本项目采用 [Apache License 2.0](LICENSE) 许可证。

## 📞 联系我们

- **项目主页**: [GitHub](https://github.com/poboll/android-translator)
- **问题反馈**: [Issues](https://github.com/poboll/android-translator/issues)
- **官方网站**: [https://translate.caiths.com/](https://translate.caiths.com/)

---

<div align="center">

**翻译君** - 即时翻译，连接世界

</div>