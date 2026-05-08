import 'package:flutter/services.dart';

import '../models/table_entry.dart';

class NativeService {
  static const _channel = MethodChannel('com.halifox.puredict/dictionary');

  static Future<bool> isImeEnabled() async {
    try {
      final bool result = await _channel.invokeMethod('isImeEnabled');
      return result;
    } on PlatformException catch (e) {
      throw Exception('检查输入法状态失败: ${e.message}');
    }
  }

  static Future<void> openImeSettings() async {
    try {
      await _channel.invokeMethod('openImeSettings');
    } on PlatformException catch (e) {
      throw Exception('打开输入法设置失败: ${e.message}');
    }
  }

  static Future<List<TableEntry>> queryUserDictionary() async {
    try {
      final List<dynamic> result = await _channel.invokeMethod(
        'queryUserDictionary',
      );
      return result
          .map(
            (item) => TableEntry(
              id: item['id'] as int?,
              word: item['word'] as String,
              frequency: item['frequency'] as int,
              locale: item['locale'] as String?,
              shortcut: item['shortcut'] as String?,
            ),
          )
          .toList();
    } on PlatformException catch (e) {
      throw Exception('查询用户词典失败: ${e.message}');
    }
  }

  static Future<List<int>> insertBatch(List<TableEntry> list) async {
    try {
      final words = list.map((entry) => entry.toMap()).toList();
      final result = await _channel.invokeMethod('insertBatch', {'words': words});
      return (result as List).cast<int>();
    } on PlatformException catch (e) {
      throw Exception('批量插入失败: ${e.message}');
    }
  }

  static Future<void> clearUserDictionary() async {
    try {
      await _channel.invokeMethod('clearUserDictionary');
    } on PlatformException catch (e) {
      throw Exception('清空用户词库失败: ${e.message}');
    }
  }

  static Future<int> deleteWordsByIds(List<int> ids) async {
    try {
      final result = await _channel.invokeMethod('deleteWordsByIds', {'ids': ids});
      return result as int;
    } on PlatformException catch (e) {
      throw Exception('删除词条失败: ${e.message}');
    }
  }

  static Future<List<TableEntry>> queryWordsByIds(List<int> ids) async {
    try {
      final List<dynamic> result = await _channel.invokeMethod(
        'queryWordsByIds',
        {'ids': ids},
      );
      return result
          .map(
            (item) => TableEntry(
              id: item['id'] as int?,
              word: item['word'] as String,
              frequency: item['frequency'] as int,
              locale: item['locale'] as String?,
              shortcut: item['shortcut'] as String?,
            ),
          )
          .toList();
    } on PlatformException catch (e) {
      throw Exception('根据ID查询词条失败: ${e.message}');
    }
  }
}
