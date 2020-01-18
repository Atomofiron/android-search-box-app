package ru.atomofiron.regextool

import android.util.Log

private var timestamp: Long = 0
private var nanotimestamp: Long = 0

fun Any.log(s: String) {
    Log.e("regextool", "[${this.javaClass.simpleName}] $s")
}

fun Any.tik(s: String) {
    val now = System.currentTimeMillis()
    val dif = now - timestamp
    timestamp = now
    Log.e("regextool", "[${this.javaClass.simpleName}] $dif $s")
}

fun Any.natik(s: String) {
    val now = System.nanoTime()
    val dif = now - nanotimestamp
    nanotimestamp = now
    Log.e("regextool", "[${this.javaClass.simpleName}] $dif $s")
}
