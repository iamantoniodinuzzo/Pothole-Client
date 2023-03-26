package com.indisparte.pothole.data.model

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
data class Pothole(
    val user: String,
    val lat: Double,
    val lon: Double,
    val variation: Double
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Pothole) return false

        if (user != other.user) return false
        if (lat != other.lat) return false
        if (lon != other.lon) return false
        if (variation != other.variation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + lat.hashCode()
        result = 31 * result + lon.hashCode()
        result = 31 * result + variation.hashCode()
        return result
    }
}