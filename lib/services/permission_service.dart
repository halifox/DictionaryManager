import 'package:permission_handler/permission_handler.dart';

class PermissionService {
  Future<bool> checkUserDictionaryPermission() async {
    final status = await Permission.contacts.status;
    return status.isGranted;
  }

  Future<PermissionStatus> requestUserDictionaryPermission() async {
    return await Permission.contacts.request();
  }

  Future<bool> isPermissionPermanentlyDenied() async {
    final status = await Permission.contacts.status;
    return status.isPermanentlyDenied;
  }

  Future<void> openAppSettings() async {
    await openAppSettings();
  }
}
