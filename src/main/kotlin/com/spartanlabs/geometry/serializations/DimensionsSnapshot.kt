package com.spartanlabs.geometry.serializations

// Organizational: Spartan Laboratories
import com.spartanlabs.geometry.Dimensions
// Intended Function
import kotlinx.serialization.Serializable

@Serializable
data class DimensionsSnapshot(val x:Double,val y:Double){
    companion object{
        infix fun from(size: Dimensions): DimensionsSnapshot = DimensionsSnapshot(size.width, size.height)
    }
}