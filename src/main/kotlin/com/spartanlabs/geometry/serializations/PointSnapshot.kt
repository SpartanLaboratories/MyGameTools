package com.spartanlabs.geometry.serializations

// Organizational: Spartan Laboratories
import com.spartanlabs.geometry.Point
// Intended Function
import kotlinx.serialization.Serializable

@Serializable
data class PointSnapshot(val x:Double,val y:Double){
    companion object{
        infix fun from(point: Point): PointSnapshot = PointSnapshot(point.x,point.y)
    }
}