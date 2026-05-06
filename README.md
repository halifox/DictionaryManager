# 词典解析器 (PureDict)

一个用于解析 .table 格式词典文件并安装到Android用户词库的 Flutter 应用。

## 功能

- 选择并解析 .table 格式的词典文件
- 显示所有词条详情（拼音、汉字、ID、频率）
- 批量安装词条到Android系统用户词库
- 实时显示安装进度
- 使用ContentResolver批量插入，性能优化

## 技术栈

- **Flutter**: UI 框架
- **Riverpod**: 状态管理（使用 riverpod_annotation 和代码生成）
- **Flutter Hooks**: 简化状态逻辑
- **file_picker**: 文件选择
- **MethodChannel**: Flutter与Android原生通信
- **ContentResolver**: Android批量插入API

## 项目结构

```
lib/
├── main.dart                                    # 应用入口和主页
├── models/
│   └── table_entry.dart                        # 词条数据模型
├── services/
│   └── table_parser.dart                       # .table 文件解析服务
├── providers/
│   ├── table_provider.dart                     # 文件选择和解析状态管理
│   ├── table_provider.g.dart                   # 自动生成
│   ├── dictionary_installer_provider.dart      # 词库安装状态管理
│   └── dictionary_installer_provider.g.dart    # 自动生成
└── pages/
    ├── entry_list_page.dart                    # 词条列表页面
    └── install_progress_page.dart              # 安装进度页面

android/app/src/main/kotlin/com/halifox/puredict/
└── MainActivity.kt                              # Android原生代码
```

## .table 文件格式

每行包含 4 个字段，用制表符（Tab）分隔：

```
拼音(shortcut)\t汉字(word)\t词条ID\t频率权重(frequency)
```

映射到 Android UserDictionary.Words 字段：
- **word**: 词条内容（汉字）
- **shortcut**: 快捷键（拼音）
- **frequency**: 使用频率
- **locale**: 语言区域（默认 zh_CN）

示例：
```
a	啊	16777217	60751
er'huang	二簧	16777218	100
```

## 性能优化

1. **批量插入**: 使用 `ContentResolver.bulkInsert()` 每次插入500条词条
2. **分批处理**: 避免一次性加载所有数据到内存
3. **异步操作**: 使用 Kotlin 协程和 Flutter 异步处理
4. **进度反馈**: 实时更新UI显示安装进度

## 运行项目

1. 安装依赖：
```bash
flutter pub get
```

2. 生成代码：
```bash
dart run build_runner build
```

3. 运行应用（仅支持Android）：
```bash
flutter run
```

## 使用说明

1. **主页**: 点击"选择 .table 文件"按钮
2. **选择文件**: 从文件系统中选择一个 .table 文件
3. **词条列表**: 查看所有解析的词条，点击"安装词库"按钮
4. **安装进度**: 等待安装完成，显示实时进度和已安装数量
5. **完成**: 安装完成后词条会出现在系统输入法的用户词库中

## 权限说明

应用需要以下Android权限：
- `READ_EXTERNAL_STORAGE`: 读取文件（Android 12及以下）
- `READ_MEDIA_*`: 读取媒体文件（Android 13+）
- `WRITE_USER_DICTIONARY`: 写入用户词典
- `READ_USER_DICTIONARY`: 读取用户词典

## 注意事项

- 仅支持Android平台
- 需要Android 5.0 (API 21) 或更高版本
- 安装过程中请勿退出应用
- 大文件可能需要较长时间安装
