import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:dynamic_color/dynamic_color.dart';
import 'pages/home_page.dart';

void main() {
  runApp(const ProviderScope(child: MyApp()));
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return DynamicColorBuilder(
      builder: (lightDynamic, darkDynamic) {
        ColorScheme lightColorScheme;
        ColorScheme darkColorScheme;

        if (lightDynamic != null && darkDynamic != null) {
          lightColorScheme = lightDynamic.harmonized();
          darkColorScheme = darkDynamic.harmonized();
        } else {
          lightColorScheme = ColorScheme.fromSeed(
            seedColor: Colors.deepPurple,
            brightness: Brightness.light,
          );
          darkColorScheme = ColorScheme.fromSeed(
            seedColor: Colors.deepPurple,
            brightness: Brightness.dark,
          );
        }

        return MaterialApp(
          title: '词典管理器',
          theme: ThemeData(
            colorScheme: lightColorScheme,
            useMaterial3: true,
            appBarTheme: const AppBarTheme(
              centerTitle: true,
            ),
          ),
          darkTheme: ThemeData(
            colorScheme: darkColorScheme,
            useMaterial3: true,
            appBarTheme: const AppBarTheme(
              centerTitle: true,
            ),
          ),
          home: const HomePage(),
        );
      },
    );
  }
}
