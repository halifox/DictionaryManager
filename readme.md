# 词典管理器

一个安卓端的词典管理器应用，支持管理不同来源的词库，包括本地词库和在线词库（搜狗词库）。

**本项目旨在解决 Gboard （或者其他会调用DictionaryAPI的安卓输入法）中文词库匮乏的问题。**

## 功能

- 管理多个本地词库。
- 在线词库同步（支持搜狗词库）。
- 搜索词汇、添加和删除词条。
- 提供高效的离线词典查询功能。
- 支持自定义词库格式的导入和导出。

## 设计与规范

- [Google Android 开发规范](https://developer.android.com/docs)
- [Material Design 3](https://m3.material.io/)
- [动态色彩](https://developer.android.com/develop/ui/views/theming/dynamic-colors)
- [自适应图标](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive)

## 截图

![](./screenshot/Screenshot.png)

## 安装

1. 从 [releases](https://github.com/halifox/DictionaryManager/releases) 下载 APK 文件。
2. 在安卓设备上启用安装来自未知来源的应用。
3. 安装APK文件。

## 使用

1. 打开应用后，您可以选择添加本地词库或连接在线词库（如搜狗）。
2. 在“词库管理”界面，您可以查看、编辑、删除词条。
3. 使用搜索框快速查找词汇。

## 贡献

我们欢迎社区的贡献！如果你有兴趣为该项目做贡献，请阅读 [贡献指南](CONTRIBUTING.md)，了解如何提交问题、请求功能或贡献代码。

## 许可证

本项目遵循 [GPL-3.0 License](LICENSE)。

## 致谢

感谢以下开源项目和库对本项目的帮助：

- [Android SDK](https://developer.android.com/studio)
- [搜狗词库](https://pinyin.sogou.com/dict/)
- [sogou_pinyin_dict.list](https://gist.githubusercontent.com/leiless/55eddb489c53500373a5bc46c75afc4b/raw/749f6e86373990f6739c19da33e59e138a2eb089/sogou_pinyin_dict.list)
- [Another SCEL Parser](https://github.com/alswl/ascel)
