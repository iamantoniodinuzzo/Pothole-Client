package com.indisparte.pothole.data.model

import java.util.*

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
class Pothole(
    val user: String,
    val lat: Double,
    val lon: Double,
    val `var`: Double
) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is Pothole) return false
        return user == o.user && lat == o.lat && lon == o.lon && `var` == o.`var`
    }

    override fun hashCode(): Int {
        return Objects.hash(user, lat, lon, `var`)
    }

    override fun toString(): String {
        return "Pothole{" +
                "user='" + user + '\'' +
                ", latitude=" + lat +
                ", longitude=" + lon +
                ", variation=" + `var` +
                '}'
    }
}