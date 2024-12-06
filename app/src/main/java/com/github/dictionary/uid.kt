package com.github.dictionary

import android.content.Context
import android.content.pm.PackageManager

fun getUid(context: Context): Int {
    try {
        val info = context.packageManager.getApplicationInfo(context.packageName, 0)
        return info.uid
    } catch (e: PackageManager.NameNotFoundException) {
        return 0
    }
}
