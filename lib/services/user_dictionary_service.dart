import 'package:flutter/services.dart';

class UserDictionaryService {
  static const platform = MethodChannel('com.halifox.puredict/dictionary');

  Future<List<Map<String, dynamic>>> queryUserDictionary() async {
    try {
      final List<dynamic> result = await platform.invokeMethod('queryUserDictionary');
      return result.map((item) => Map<String, dynamic>.from(item)).toList();
    } on PlatformException catch (e) {
      throw Exception('查询用户词典失败: ${e.message}');
    }
  }
}
