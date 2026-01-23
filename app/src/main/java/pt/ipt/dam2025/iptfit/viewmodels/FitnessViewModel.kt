package pt.ipt.dam2025.iptfit.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pt.ipt.dam2025.iptfit.models.Workout
import java.util.*

class FitnessViewModel : ViewModel() {

    private val _workouts = MutableLiveData<List<Workout>>().apply {
        value = listOf(
            Workout("1", "Corrida", 30, 10),
            Workout("2", "Musculação", 45, 8),
            Workout("3", "HIIT", 20, 15),
            Workout("4", "Caminhada", 40, 5)
        )
    }

    private val _userName = MutableLiveData<String>().apply { value = "Atleta" }
    private val _dailyGoal = MutableLiveData<Int>().apply { value = 500 }
    private val _todayCalories = MutableLiveData<Int>().apply { value = 250 }

    val workouts: LiveData<List<Workout>> = _workouts
    val userName: LiveData<String> = _userName
    val dailyGoal: LiveData<Int> = _dailyGoal
    val todayCalories: LiveData<Int> = _todayCalories

    fun updateWorkoutCompletion(workoutId: String, completed: Boolean) {
        val currentWorkouts = _workouts.value?.toMutableList() ?: return
        val workoutIndex = currentWorkouts.indexOfFirst { it.id == workoutId }

        if (workoutIndex != -1) {
            currentWorkouts[workoutIndex] = currentWorkouts[workoutIndex].copy(
                completed = completed,
                actualDuration = if (completed) currentWorkouts[workoutIndex].duration else 0,
                caloriesBurned = if (completed)
                    currentWorkouts[workoutIndex].duration * currentWorkouts[workoutIndex].caloriesPerMinute
                else 0,
                completedDate = if (completed) System.currentTimeMillis() else null
            )
            _workouts.value = currentWorkouts
        }
    }

    fun updateUserName(name: String) {
        _userName.value = name
    }

    fun updateDailyGoal(goal: Int) {
        _dailyGoal.value = goal
    }

    fun updateTodayCalories(calories: Int) {
        _todayCalories.value = calories
    }
}