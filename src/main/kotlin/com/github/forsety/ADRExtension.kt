package com.github.forsety

/***
 * Created by Forsety on 28.08.2014.
 *
 * Rewrite to Kotlin by 160R
 */
open class ADRExtension {

    var maxDensity = Density.XXHDPI.name
    var minDensity = Density.MDPI.name
    var exclude = mutableListOf("tvdpi")

    lateinit var maxDensityInfo: Density
    lateinit var minDensityInfo: Density
    lateinit var excludeInfo: List<Density>


    fun validate() {

        maxDensityInfo = try {
            Density.valueOf(maxDensity.toUpperCase())
        } catch (e: IllegalArgumentException) {
            throw InvalidConfigurationException("\"$maxDensity\" is not valid max density")
        }

        minDensityInfo = try {
            Density.valueOf(minDensity.toUpperCase())
        } catch (e: IllegalArgumentException) {
            throw InvalidConfigurationException("\"$minDensity\" is not valid min density")
        }

        excludeInfo = exclude.map {
            try {
                Density.valueOf(it.toUpperCase())
            } catch (e: IllegalArgumentException) {
                throw InvalidConfigurationException("\"$it\" is not valid density name")
            }
        }

        if (minDensityInfo >= maxDensityInfo) {
            throw InvalidConfigurationException("Min density must be less than base density")
        }

        if (exclude.contains(minDensity)) {
            throw InvalidConfigurationException("Can't exclude min density")
        }

        if (exclude.contains(maxDensity)) {
            throw InvalidConfigurationException("Can't exclude max density")
        }
    }
}
