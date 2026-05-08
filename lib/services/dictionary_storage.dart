import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/installed_dictionary.dart';

class DictionaryStorage {
  static const _key = 'installed_dictionaries';

  static Future<List<InstalledDictionary>> getInstalledDictionaries() async {
    final prefs = await SharedPreferences.getInstance();
    final jsonString = prefs.getString(_key);
    if (jsonString == null) return [];

    final List<dynamic> jsonList = json.decode(jsonString);
    return jsonList.map((json) => InstalledDictionary.fromJson(json)).toList();
  }

  static Future<void> saveDictionary(InstalledDictionary dictionary) async {
    final dictionaries = await getInstalledDictionaries();
    dictionaries.removeWhere((d) => d.name == dictionary.name);
    dictionaries.add(dictionary);

    final prefs = await SharedPreferences.getInstance();
    final jsonString = json.encode(dictionaries.map((d) => d.toJson()).toList());
    await prefs.setString(_key, jsonString);
  }

  static Future<void> removeDictionary(String name) async {
    final dictionaries = await getInstalledDictionaries();
    dictionaries.removeWhere((d) => d.name == name);

    final prefs = await SharedPreferences.getInstance();
    final jsonString = json.encode(dictionaries.map((d) => d.toJson()).toList());
    await prefs.setString(_key, jsonString);
  }

  static Future<bool> isDictionaryInstalled(String name) async {
    final dictionaries = await getInstalledDictionaries();
    return dictionaries.any((d) => d.name == name);
  }

  static Future<InstalledDictionary?> getDictionary(String name) async {
    final dictionaries = await getInstalledDictionaries();
    try {
      return dictionaries.firstWhere((d) => d.name == name);
    } catch (e) {
      print('获取词库失败 ($name): $e');
      return null;
    }
  }
}
