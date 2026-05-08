import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:path_provider/path_provider.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'dictionary_download_provider.g.dart';

class DownloadProgress {
  final int downloaded;
  final int total;
  final double progress;

  DownloadProgress({
    required this.downloaded,
    required this.total,
    required this.progress,
  });
}

@riverpod
class DictionaryDownloader extends _$DictionaryDownloader {
  @override
  Stream<DownloadProgress> build(String fileName) async* {
    final url = 'https://puredict.halifox.top/pyim/$fileName';
    final dir = await getApplicationDocumentsDirectory();
    final filePath = '${dir.path}/$fileName';

    final file = File(filePath);
    if (await file.exists()) {
      yield DownloadProgress(downloaded: 1, total: 1, progress: 1.0);
      return;
    }

    final request = http.Request('GET', Uri.parse(url));
    final response = await request.send();

    if (response.statusCode != 200) {
      throw Exception('下载失败: ${response.statusCode}');
    }

    final total = response.contentLength ?? 0;
    int downloaded = 0;

    final sink = file.openWrite();

    await for (final chunk in response.stream) {
      sink.add(chunk);
      downloaded += chunk.length;

      if (total > 0) {
        yield DownloadProgress(
          downloaded: downloaded,
          total: total,
          progress: downloaded / total,
        );
      }
    }

    await sink.close();

    // 下载完成后，刷新下载状态
    ref.invalidate(isDictionaryDownloadedProvider(fileName));

    yield DownloadProgress(downloaded: total, total: total, progress: 1.0);
  }
}

@riverpod
class SogouDictionaryDownloader extends _$SogouDictionaryDownloader {
  @override
  Stream<DownloadProgress> build(String fileName, String downloadUrl) async* {
    final dir = await getApplicationDocumentsDirectory();
    final filePath = '${dir.path}/$fileName';

    final file = File(filePath);
    if (await file.exists()) {
      yield DownloadProgress(downloaded: 1, total: 1, progress: 1.0);
      return;
    }

    final request = http.Request('GET', Uri.parse(downloadUrl));
    final response = await request.send();

    if (response.statusCode != 200) {
      throw Exception('下载失败: ${response.statusCode}');
    }

    final total = response.contentLength ?? 0;
    int downloaded = 0;

    final sink = file.openWrite();

    await for (final chunk in response.stream) {
      sink.add(chunk);
      downloaded += chunk.length;

      if (total > 0) {
        yield DownloadProgress(
          downloaded: downloaded,
          total: total,
          progress: downloaded / total,
        );
      }
    }

    await sink.close();

    // 下载完成后，刷新下载状态
    ref.invalidate(isSogouDictionaryDownloadedProvider(fileName));

    yield DownloadProgress(downloaded: total, total: total, progress: 1.0);
  }
}

@riverpod
Future<String> getDictionaryPath(Ref ref, String fileName) async {
  final dir = await getApplicationDocumentsDirectory();
  return '${dir.path}/$fileName';
}

@riverpod
Future<bool> isDictionaryDownloaded(Ref ref, String fileName) async {
  final dir = await getApplicationDocumentsDirectory();
  final file = File('${dir.path}/$fileName');
  return await file.exists();
}

@riverpod
Future<String> getSogouDictionaryPath(Ref ref, String fileName) async {
  final dir = await getApplicationDocumentsDirectory();
  return '${dir.path}/$fileName';
}

@riverpod
Future<bool> isSogouDictionaryDownloaded(Ref ref, String fileName) async {
  final dir = await getApplicationDocumentsDirectory();
  final file = File('${dir.path}/$fileName');
  return await file.exists();
}
