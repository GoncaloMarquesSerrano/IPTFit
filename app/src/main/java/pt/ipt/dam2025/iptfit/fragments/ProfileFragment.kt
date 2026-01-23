package pt.ipt.dam2025.iptfit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import pt.ipt.dam2025.iptfit.R
import pt.ipt.dam2025.iptfit.databinding.FragmentProfileBinding
import pt.ipt.dam2025.iptfit.viewmodels.FitnessViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FitnessViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.userName.observe(viewLifecycleOwner, Observer { name ->
            binding.textUserName.text = name
            binding.textAvatar.text = name.take(2).uppercase()
        })

        viewModel.dailyGoal.observe(viewLifecycleOwner, Observer { goal ->
            binding.textDailyGoal.text = "$goal cal"
        })

        // Estatísticas (dados de exemplo)
        binding.textTotalWorkouts.text = "12"
        binding.textTotalCalories.text = "4500"
        binding.textTotalMinutes.text = "360"
    }

    private fun setupClickListeners() {
        binding.cardAvatar.setOnClickListener {
            showEditNameDialog()
        }

        binding.cardDailyGoal.setOnClickListener {
            showEditGoalDialog()
        }
    }

    private fun showEditNameDialog() {
        // Implementar diálogo para editar nome
    }

    private fun showEditGoalDialog() {
        // Implementar diálogo para editar meta
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}