import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

class PermissionRequestPage extends StatelessWidget {
  final String title;
  final String description;
  final IconData icon;
  final VoidCallback onPermissionGranted;

  const PermissionRequestPage({
    super.key,
    required this.title,
    required this.description,
    required this.icon,
    required this.onPermissionGranted,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('需要权限'),
        centerTitle: true,
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(32.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                width: 120,
                height: 120,
                decoration: BoxDecoration(
                  color: Theme.of(context).colorScheme.errorContainer,
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  icon,
                  size: 64,
                  color: Theme.of(context).colorScheme.error,
                ),
              ),
              const SizedBox(height: 48),
              Text(
                title,
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 16),
              Text(
                description,
                style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 48),
              FilledButton.icon(
                onPressed: () async {
                  final status = await Permission.contacts.request();
                  if (status.isGranted) {
                    if (context.mounted) {
                      Navigator.of(context).pop();
                      onPermissionGranted();
                    }
                  } else if (status.isPermanentlyDenied) {
                    if (context.mounted) {
                      _showSettingsDialog(context);
                    }
                  }
                },
                icon: const Icon(Icons.check_circle_outline),
                label: const Text('授予权限'),
                style: FilledButton.styleFrom(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 32,
                    vertical: 20,
                  ),
                ),
              ),
              const SizedBox(height: 16),
              TextButton(
                onPressed: () {
                  Navigator.of(context).pop();
                },
                child: const Text('稍后'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _showSettingsDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        icon: Icon(
          Icons.settings_outlined,
          size: 48,
          color: Theme.of(context).colorScheme.primary,
        ),
        title: const Text('需要在设置中授权'),
        content: const Text('您已拒绝该权限，请在系统设置中手动授予权限。'),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
            },
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () async {
              await openAppSettings();
              if (context.mounted) {
                Navigator.of(context).pop();
              }
            },
            child: const Text('去设置'),
          ),
        ],
      ),
    );
  }
}
