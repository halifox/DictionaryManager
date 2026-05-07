import 'package:flutter/material.dart';

enum ImeFormat {
  // 二进制格式
  sougouScel,
  qqQpyd,
  baiduBdict,
  lingoesLd2,
  ziguangUwl,
  qqQcel,
  win10MicrosoftPinyin,
  win10MicrosoftWubi,
  baiduBcd,
  gboard,
  macOsPlist,

  // 文本格式
  sougouText,
  qqText,
  baiduText,
  rime,
  pyimTable,
  googlePinyin,
  libpinyin,
  ziguangText,
  fit,
  bing,
  shouxin,
  pinyinJiajia,
  sinaPinyin,
  microsoftPinyin2010,
  macosNative,
  qqPhonePinyin,
  baiduPhonePinyin,

  // 五笔/郑码
  wubi86,
  wubi98,
  wubiNewAge,
  qqWubi,
  sougouWubi,
  jidianWubi,
  jidianZhengma,
  xiaoyaWubi,

  // 其他
  cangjiePlatform,
  yahooKeyKey,
  xiaoxiao,
  emoji,
  selfDefining,
  unknown,
}

class ImeFormatInfo {
  final ImeFormat format;
  final String displayName;
  final List<String> extensions;
  final IconData icon;
  final String? wikiUrl;

  const ImeFormatInfo({
    required this.format,
    required this.displayName,
    required this.extensions,
    required this.icon,
    this.wikiUrl,
  });

  static const List<ImeFormatInfo> allFormats = [
    // PC端输入法
    ImeFormatInfo(
      format: ImeFormat.pyimTable,
      displayName: 'Chinese-pyim (Linux)',
      extensions: ['.table'],
      icon: Icons.keyboard_outlined,
    ),
    ImeFormatInfo(
      format: ImeFormat.fit,
      displayName: 'FIT 输入法 (Mac)',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/FIT',
    ),
    ImeFormatInfo(
      format: ImeFormat.libpinyin,
      displayName: 'libpinyin (Linux)',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
    ),
    ImeFormatInfo(
      format: ImeFormat.macosNative,
      displayName: 'MacOS 自带简体拼音',
      extensions: ['.plist'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/MacPlist',
    ),
    ImeFormatInfo(
      format: ImeFormat.qqText,
      displayName: 'QQ 拼音（文本词库和 qpyd 格式分类词库）',
      extensions: ['.txt', '.qpyd'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/QQ_Pinyin_Win',
    ),
    ImeFormatInfo(
      format: ImeFormat.qqWubi,
      displayName: 'QQ 五笔',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/QQ_Wubi',
    ),
    ImeFormatInfo(
      format: ImeFormat.rime,
      displayName: 'Rime 输入法 (Linux 中州韻、Windows 小狼毫、Mac OS 鼠鬚管)',
      extensions: ['.dict.yaml', '.yaml'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Rime',
    ),
    ImeFormatInfo(
      format: ImeFormat.win10MicrosoftPinyin,
      displayName: 'Win10 微软拼音',
      extensions: ['.dat'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Win10Ms_Pinyin',
    ),
    ImeFormatInfo(
      format: ImeFormat.win10MicrosoftWubi,
      displayName: 'Win10 微软五笔',
      extensions: ['.dat'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Win10Ms_Wubi',
    ),
    ImeFormatInfo(
      format: ImeFormat.baiduText,
      displayName: '百度拼音 PC（文本词库、bdict 格式）',
      extensions: ['.txt', '.bdict'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Baidu_PC',
    ),
    ImeFormatInfo(
      format: ImeFormat.bing,
      displayName: '必应输入法',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Engkoo',
    ),
    ImeFormatInfo(
      format: ImeFormat.cangjiePlatform,
      displayName: '仓颉平台',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
    ),
    ImeFormatInfo(
      format: ImeFormat.googlePinyin,
      displayName: '谷歌拼音',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Google_Pinyin',
    ),
    ImeFormatInfo(
      format: ImeFormat.jidianWubi,
      displayName: '极点五笔',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Jidian',
    ),
    ImeFormatInfo(
      format: ImeFormat.jidianZhengma,
      displayName: '极点郑码',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Jidian',
    ),
    ImeFormatInfo(
      format: ImeFormat.lingoesLd2,
      displayName: '灵格斯词库 ld2',
      extensions: ['.ld2'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Lingoes_Ld2',
    ),
    ImeFormatInfo(
      format: ImeFormat.pinyinJiajia,
      displayName: '拼音加加',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Pinyin_Jiajia',
    ),
    ImeFormatInfo(
      format: ImeFormat.shouxin,
      displayName: '手心输入法',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
    ),
    ImeFormatInfo(
      format: ImeFormat.sougouText,
      displayName: '搜狗拼音（文本词库、Bin 格式备份词库和 scel 格式细胞词库）',
      extensions: ['.txt', '.bin', '.scel'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Sougou_Pinyin',
    ),
    ImeFormatInfo(
      format: ImeFormat.sougouWubi,
      displayName: '搜狗五笔',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Sougou_Wubi',
    ),
    ImeFormatInfo(
      format: ImeFormat.microsoftPinyin2010,
      displayName: '微软拼音 2010',
      extensions: ['.xml'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Ms_Pinyin',
    ),
    ImeFormatInfo(
      format: ImeFormat.xiaoxiao,
      displayName: '小小输入法（拼音、五笔、郑码、二笔）',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Xiaoxiao',
    ),
    ImeFormatInfo(
      format: ImeFormat.xiaoyaWubi,
      displayName: '小鸭五笔',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Xiaoya_Wubi',
    ),
    ImeFormatInfo(
      format: ImeFormat.sinaPinyin,
      displayName: '新浪拼音',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Sina_Pinyin',
    ),
    ImeFormatInfo(
      format: ImeFormat.yahooKeyKey,
      displayName: '雅虎奇摩输入法（注音）',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Yahoo',
    ),
    ImeFormatInfo(
      format: ImeFormat.ziguangText,
      displayName: '紫光拼音（文本词库和 uwl 格式分类词库）',
      extensions: ['.txt', '.uwl'],
      icon: Icons.keyboard_outlined,
      wikiUrl: 'https://github.com/studyzy/imewlconverter/wiki/Ziguang_Pinyin',
    ),

    // 手机端
    ImeFormatInfo(
      format: ImeFormat.qqPhonePinyin,
      displayName: 'QQ 手机拼音',
      extensions: ['.txt'],
      icon: Icons.keyboard_outlined,
    ),
    ImeFormatInfo(
      format: ImeFormat.baiduPhonePinyin,
      displayName: '百度手机拼音（文本词库和 bcd 格式）',
      extensions: ['.txt', '.bcd'],
      icon: Icons.keyboard_outlined,
    ),
    ImeFormatInfo(
      format: ImeFormat.gboard,
      displayName: '谷歌拼音输入法',
      extensions: ['.zip'],
      icon: Icons.keyboard_outlined,
    ),
  ];
}
