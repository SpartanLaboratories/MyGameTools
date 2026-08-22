import kotlinx.serialization.Serializable

/** The kind of mouse event that occurred. */
@Serializable
enum class MouseActionType { MOVE, PRESS, RELEASE }

/**
 * A single mouse input event, in window pixel coordinates (origin top-left).
 * [button] is the GLFW mouse button code (e.g. GLFW_MOUSE_BUTTON_LEFT = 0)
 * for PRESS/RELEASE events, or -1 for MOVE events where no button is involved.
 */
@Serializable
data class MouseAction(
    val type: MouseActionType,
    val button: Int,
    val x: Double,
    val y: Double
)
 