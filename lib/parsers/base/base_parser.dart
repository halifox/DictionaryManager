import '../../models/ime_format.dart';
import '../../models/parse_result.dart';

abstract class BaseParser {
  final ImeFormat format;

  BaseParser(this.format);

  Future<ParseResult> parseFile(
    String filePath, {
    void Function(ParseProgress)? onProgress,
  });

  Future<bool> canParse(String filePath);
}
