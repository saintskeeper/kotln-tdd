package com.ubertob.unlearnoop.zettai.fp


fun <U : Any> CharSequence?.ifNotNullOrEmpty(f: (CharSequence) -> U): U? =
    if (this.isNullOrEmpty()) null else f(this)
