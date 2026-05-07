import 'package:flutter/material.dart';

class StateView extends StatelessWidget {
  final String title;
  final String message;
  final Widget icon;
  final String? actionLabel;
  final VoidCallback? onAction;

  const StateView({
    super.key,
    this.title = '',
    this.message = '',
    required this.icon,
    this.actionLabel,
    this.onAction,
  });

  factory StateView.error({
    String title = '出错了',
    required String message,
    String? actionLabel,
    VoidCallback? onAction,
  }) {
    return StateView(
      title: title,
      message: message,
      icon: const Icon(Icons.error_outline_rounded, size: 64),
      actionLabel: actionLabel,
      onAction: onAction,
    );
  }

  factory StateView.loading({
    String title = '',
    String message = '加载中...',
  }) {
    return StateView(
      title: title,
      message: message,
      icon: const SizedBox(
        height: 64,
        width: 64,
        child: CircularProgressIndicator(
          strokeWidth: 8,
          strokeCap: StrokeCap.round,
        ),
      ),
    );
  }

  factory StateView.empty({
    String title = '暂无内容',
    String message = '这里空空如也',
    String? actionLabel,
    VoidCallback? onAction,
  }) {
    return StateView(
      title: title,
      message: message,
      icon: const Icon(Icons.inbox_outlined, size: 64),
      actionLabel: actionLabel,
      onAction: onAction,
    );
  }

  factory StateView.finish({
    String title = '完成',
    String message = '操作已完成',
    String? actionLabel,
    VoidCallback? onAction,
  }) {
    return StateView(
      title: title,
      message: message,
      icon: const Icon(Icons.check_circle_outline_rounded, size: 64),
      actionLabel: actionLabel,
      onAction: onAction,
    );
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: colorScheme.surfaceContainerLow,
                shape: BoxShape.circle,
              ),
              child: icon,
            ),
            const SizedBox(height: 32),
            if (title.isNotEmpty)
              Text(
                title,
                textAlign: TextAlign.center,
                style: textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.w600,
                  color: colorScheme.onSurface,
                ),
              ),
            if (title.isNotEmpty) const SizedBox(height: 16),
            if (message.isNotEmpty)
              Text(
                message,
                textAlign: TextAlign.center,
                style: textTheme.bodyLarge?.copyWith(
                  color: colorScheme.onSurfaceVariant,
                ),
              ),
            const SizedBox(height: 32),
            if (onAction != null && actionLabel != null)
              FilledButton.icon(
                onPressed: onAction,
                icon: const Icon(Icons.add),
                label: Text(actionLabel!),
                style: FilledButton.styleFrom(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 24,
                    vertical: 16,
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
