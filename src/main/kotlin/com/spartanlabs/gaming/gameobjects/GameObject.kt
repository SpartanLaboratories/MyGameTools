package com.spartanlabs.gaming.gameobjects

// Organizational
// Spartan Laboratories
import com.spartanlabs.geometry.Point
// Spartan Gaming
import com.spartanlabs.geometry.serializations.PointSnapshot

// Utility
import kotlinx.serialization.Serializable

// Programming Generics (Tests, Logging, Profiling)
import org.slf4j.LoggerFactory
import org.slf4j.Logger
val log: Logger = LoggerFactory.getLogger("GameObject")

abstract class GameObject (val location: Point = Point() ) {
    constructor(x:Double, y:Double):this(Point(x=x,y=y))
    open fun tick(){
        onUpdate()
    }
    abstract fun onUpdate()
}

@Serializable
data class GameObjectSnapshot(val location: PointSnapshot){
    companion object{
    infix fun from(gameObject: GameObject): GameObjectSnapshot = GameObjectSnapshot(PointSnapshot.from(gameObject.location))
    }
}