package pt.ipt.dam2025.iptfit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import pt.ipt.dam2025.iptfit.R
import pt.ipt.dam2025.iptfit.adapters.WorkoutAdapter
import pt.ipt.dam2025.iptfit.databinding.FragmentHomeBinding
import pt.ipt.dam2025.iptfit.viewmodels.FitnessViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FitnessViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupWorkouts()
    }

    private fun setupObservers() {
        viewModel.userName.observe(viewLifecycleOwner, Observer { name ->
            binding.textGreeting.text = "Olá, $name!"
        })

        viewModel.todayCalories.observe(viewLifecycleOwner, Observer { calories ->
            binding.statCalories.text = calories.toString()
        })

        viewModel.dailyGoal.observe(viewLifecycleOwner, Observer { goal ->
            updateGoalProgress()
        })
    }

    private fun updateGoalProgress() {
        val current = viewModel.todayCalories.value ?: 0
        val goal = viewModel.dailyGoal.value ?: 500
        val percentage = if (goal > 0) (current.toFloat() / goal * 100).toInt() else 0

        binding.textGoal.text = "$current / $goal calorias"
        binding.textGoalPercentage.text = "$percentage%"
        binding.progressGoal.progress = percentage

        // Atualizar treinos de hoje
        val todayWorkouts = viewModel.workouts.value?.count { it.completed } ?: 0
        binding.statWorkouts.text = todayWorkouts.toString()
    }

    private fun setupWorkouts() {
        viewModel.workouts.observe(viewLifecycleOwner, Observer { workouts ->
            val adapter = WorkoutAdapter(workouts) { workout ->
                // Marcar como completado
                viewModel.updateWorkoutCompletion(workout.id, true)
                // Atualizar calorias
                val currentCalories = viewModel.todayCalories.value ?: 0
                val newCalories = currentCalories + (workout.duration * workout.caloriesPerMinute)
                viewModel.updateTodayCalories(newCalories)
            }

            binding.recyclerWorkouts.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerWorkouts.adapter = adapter
            updateGoalProgress()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}