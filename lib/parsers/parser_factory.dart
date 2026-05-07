import 'package:path/path.dart' as path;

import '../models/ime_format.dart';
import 'base/base_parser.dart';
import 'binary/baidu_bcd_parser.dart';
import 'binary/baidu_bdict_parser.dart';
import 'binary/gboard_parser.dart';
import 'binary/lingoes_ld2_parser.dart';
import 'binary/macos_plist_parser.dart';
import 'binary/qq_qcel_parser.dart';
import 'binary/qq_qpyd_parser.dart';
import 'binary/sougou_scel_parser.dart';
import 'binary/win10_microsoft_pinyin_parser.dart';
import 'binary/win10_microsoft_wubi_parser.dart';
import 'binary/ziguang_uwl_parser.dart';
import 'text/baidu_phone_pinyin_parser.dart';
import 'text/baidu_text_parser.dart';
import 'text/bing_parser.dart';
import 'text/emoji_parser.dart';
import 'text/fit_parser.dart';
import 'text/google_pinyin_parser.dart';
import 'text/libpinyin_parser.dart';
import 'text/macos_native_parser.dart';
import 'text/microsoft_pinyin_2010_parser.dart';
import 'text/pinyin_jiajia_parser.dart';
import 'text/qq_phone_pinyin_parser.dart';
import 'text/qq_text_parser.dart';
import 'text/rime_parser.dart';
import 'text/self_defining_parser.dart';
import 'text/shouxin_parser.dart';
import 'text/sina_pinyin_parser.dart';
import 'text/sougou_text_parser.dart';
import 'text/yahoo_keykey_parser.dart';
import 'text/ziguang_text_parser.dart';
import 'text/cangjie_parser.dart';
import 'wubi/jidian_wubi_parser.dart';
import 'wubi/jidian_zhengma_parser.dart';
import 'wubi/qq_wubi_parser.dart';
import 'wubi/sougou_wubi_parser.dart';
import 'wubi/wubi86_parser.dart';
import 'wubi/wubi98_parser.dart';
import 'wubi/wubi_new_century_parser.dart';
import 'wubi/xiaoxiao_parser.dart';
import 'wubi/xiaoya_wubi_parser.dart';

class ParserFactory {
  static final Map<String, BaseParser Function()> _parsers = {
    '.scel': () => SougouScelParser(),
    '.qpyd': () => QQQpydParser(),
    '.bdict': () => BaiduBdictParser(),
    '.ld2': () => LingoesLd2Parser(),
    '.uwl': () => ZiguangUwlParser(),
    '.qcel': () => QqQcelParser(),
    '.dat': () => Win10MicrosoftPinyinParser(),
    '.bcd': () => BaiduBcdParser(),
    '.zip': () => GboardParser(),
    '.plist': () => MacOsPlistParser(),
    '.txt': () => SougouTextParser(),
    '.yaml': () => RimeParser(),
    '.dict.yaml': () => RimeParser(),
    '.xml': () => MicrosoftPinyin2010Parser(),
  };

  static BaseParser? createParser(String filePath) {
    final ext = path.extension(filePath).toLowerCase();
    return _parsers[ext]?.call();
  }

  static BaseParser? createParserByFormat(ImeFormat format) {
    switch (format) {
      case ImeFormat.sougouScel:
        return SougouScelParser();
      case ImeFormat.qqQpyd:
        return QQQpydParser();
      case ImeFormat.baiduBdict:
        return BaiduBdictParser();
      case ImeFormat.lingoesLd2:
        return LingoesLd2Parser();
      case ImeFormat.ziguangUwl:
        return ZiguangUwlParser();
      case ImeFormat.qqQcel:
        return QqQcelParser();
      case ImeFormat.win10MicrosoftPinyin:
        return Win10MicrosoftPinyinParser();
      case ImeFormat.win10MicrosoftWubi:
        return Win10MicrosoftWubiParser();
      case ImeFormat.baiduBcd:
        return BaiduBcdParser();
      case ImeFormat.gboard:
        return GboardParser();
      case ImeFormat.macOsPlist:
        return MacOsPlistParser();
      case ImeFormat.sougouText:
        return SougouTextParser();
      case ImeFormat.qqText:
        return QQTextParser();
      case ImeFormat.baiduText:
        return BaiduTextParser();
      case ImeFormat.rime:
        return RimeParser();
      case ImeFormat.ziguangText:
        return ZiguangTextParser();
      case ImeFormat.googlePinyin:
        return GooglePinyinParser();
      case ImeFormat.libpinyin:
        return LibpinyinParser();
      case ImeFormat.fit:
        return FitParser();
      case ImeFormat.bing:
        return BingParser();
      case ImeFormat.shouxin:
        return ShouxinParser();
      case ImeFormat.pinyinJiajia:
        return PinyinJiaJiaParser();
      case ImeFormat.sinaPinyin:
        return SinaPinyinParser();
      case ImeFormat.microsoftPinyin2010:
        return MicrosoftPinyin2010Parser();
      case ImeFormat.macosNative:
        return MacOsNativeParser();
      case ImeFormat.qqPhonePinyin:
        return QqPhonePinyinParser();
      case ImeFormat.baiduPhonePinyin:
        return BaiduPhonePinyinParser();
      case ImeFormat.selfDefining:
        return SelfDefiningParser();
      case ImeFormat.qqWubi:
        return QqWubiParser();
      case ImeFormat.sougouWubi:
        return SougouWubiParser();
      case ImeFormat.jidianWubi:
        return JidianWubiParser();
      case ImeFormat.jidianZhengma:
        return JidianZhengmaParser();
      case ImeFormat.xiaoyaWubi:
        return XiaoyaWubiParser();
      case ImeFormat.xiaoxiao:
        return XiaoxiaoParser();
      case ImeFormat.wubi86:
        return Wubi86Parser();
      case ImeFormat.wubi98:
        return Wubi98Parser();
      case ImeFormat.wubiNewAge:
        return WubiNewCenturyParser();
      case ImeFormat.cangjiePlatform:
        return CangjieParser();
      case ImeFormat.yahooKeyKey:
        return YahooKeyKeyParser();
      case ImeFormat.emoji:
        return EmojiParser();
      default:
        return null;
    }
  }

  static Future<BaseParser?> detectParser(String filePath) async {
    final parser = createParser(filePath);
    if (parser != null && await parser.canParse(filePath)) {
      return parser;
    }

    for (final factory in _parsers.values) {
      final p = factory();
      if (await p.canParse(filePath)) {
        return p;
      }
    }

    return null;
  }

  static List<String> getSupportedExtensions() {
    return _parsers.keys.toList();
  }
}
