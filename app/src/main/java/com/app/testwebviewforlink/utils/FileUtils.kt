package com.app.testwebviewforlink.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
fun File.toBase64(): String? {
    if (!this.exists())
        return null
    return Base64.getEncoder().encodeToString(this.readBytes())
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.toBase64(): String? {
    if (this.isEmpty())
        return null
    val f = File(this);
    return f.toBase64();
}