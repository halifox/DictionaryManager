import 'dart:convert';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

/// 紫光拼音文本格式解析器
/// 格式：word\tpin'yin\tfrequency
/// 编码：UTF-8
/// 文件头包含元数据（名称、作者、编辑）
class ZiguangTextParser extends TextParser {
  ZiguangTextParser() : super(ImeFormat.ziguangText);

  @override
  Encoding get encoding => utf8;

  @override
  Future<List<TableEntryData>> parseText(String content) async {
    final entries = <TableEntryData>[];
    final lines = content.split('\n');

    for (int i = 0; i < lines.length; i++) {
      final line = lines[i].trim();
      if (line.isEmpty) {
        continue;
      }

      final entry = parseLine(line);
      if (entry != null) {
        entries.add(entry);
      }
    }

    return entries;
  }

  @override
  TableEntryData? parseLine(String line) {
    line = line.trim();
    if (line.isEmpty) {
      return null;
    }

    // 跳过元数据行
    if (line.startsWith('名称=') ||
        line.startsWith('作者=') ||
        line.startsWith('编辑=')) {
      return null;
    }

    final parts = line.split('\t');
    if (parts.length < 2) {
      return null;
    }

    final word = parts[0];
    final pinyin = parts[1].replaceAll("'", '');
    final frequency = parts.length >= 3 ? int.tryParse(parts[2]) ?? 0 : 0;

    return TableEntryData(
      word: word,
      shortcut: pinyin,
      frequency: frequency,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.txt');
  }
}
