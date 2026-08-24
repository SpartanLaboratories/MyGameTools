package com.spartanlabs.geometry.serializations

// Organizational: Spartan Laboratories
import com.spartanlabs.geometry.TwoDoubles
// Intended Function
import kotlinx.serialization.Serializable

@Serializable
data class TwoDoublesSnapshot(val x: Double, val y: Double){
    companion object{
        infix fun from(twoDoubles: TwoDoubles):TwoDoublesSnapshot = TwoDoublesSnapshot(twoDoubles.first, twoDoubles.second)
    }
}