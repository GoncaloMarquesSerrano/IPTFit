package pt.ipt.dam2025.iptfit.models

data class Workout(
    val id: String,
    val name: String,
    val duration: Int, // in minutes
    val caloriesPerMinute: Int,
    val completed: Boolean = false,
    val actualDuration: Int = 0,
    val caloriesBurned: Int = 0,
    val completedDate: Long? = null
)