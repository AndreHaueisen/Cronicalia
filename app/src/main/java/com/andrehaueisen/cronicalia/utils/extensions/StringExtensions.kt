package com.andrehaueisen.cronicalia.utils.extensions

/**
 * Created by andre on 2/19/2018.
 */
fun String.encodeEmail() = replace('.', ',')
fun String.decodeEmail() = replace(',', '.')