package com.github.forsety

/**
 * Created by Forsety on 27.08.2014.
 *
 * Rewrite to Kotlin by 160R
 */
enum class Density(
        val value: Int
) {

    LDPI(120),
    MDPI(160),
    TVDPI(213),
    HDPI(240),
    XHDPI(320),
    XXHDPI(480),
    XXXHDPI(640);

    val qualifierName  = name.toLowerCase()

}
