# Contributing

感谢你对项目的关注和贡献！为了确保我们能够高效地合作，请遵循以下准则：

## 如何开始

1. **Fork 项目**：点击右上角的 "Fork" 按钮，将项目克隆到自己的 GitHub 帐号。
2. **克隆仓库**：使用 `git clone` 命令克隆你自己的 Fork：
   ```bash
   git clone https://github.com/yourusername/your-app.git
   ```
3. **创建分支**：在开始新特性或修复 Bug 之前，创建一个新的分支：
   ```bash
   git checkout -b feature/your-feature-name
   ```
4. **安装依赖**：使用 Gradle 安装必要的依赖：
   ```bash
   ./gradlew build
   ```

## 编码规范

- 请遵循 Java 或 Kotlin 编码规范，参考 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) 或 [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)。
- 所有新的代码必须覆盖单元测试，并通过 CI 测试。
- 确保你的代码符合 Android 开发最佳实践，如使用 `ViewModel` 和 `LiveData` 进行数据管理，避免内存泄漏。

## 提交代码

1. **提交信息**：使用简洁清晰的提交信息，描述所做的更改：
   ```
   fix: 修复用户登录时的崩溃问题
   feat: 添加新的主题切换功能
   ```
2. **推送分支**：将你的更改推送到 GitHub：
   ```bash
   git push origin feature/your-feature-name
   ```

## 提交 Pull Request

1. **描述清晰**：确保你的 PR 描述清晰、简洁，包含解决的问题和做出的更改。
2. **请求审核**：提交 PR 后，团队会进行代码审核。根据反馈，进行必要的修改。
3. **保持同步**：确保你的分支是最新的，避免出现冲突。若有冲突，使用以下命令更新分支：
   ```bash
   git pull origin main
   ```

## 代码审核流程

- 代码提交后，团队成员会进行审核。
- 通过审核后，PR 将被合并到主分支。
- 如果需要修改，请根据反馈进行调整并重新提交。

## 讨论和支持

- **问题和建议**：请在 [Issues](https://github.com/yourusername/your-app/issues) 中提交问题和功能请求。
- **Slack/Discussions**：加入我们的 Slack 频道或 Discussions 提交问题和建议。

## 许可

项目遵循 [MIT License](LICENSE)。

感谢你的贡献！ 🎉