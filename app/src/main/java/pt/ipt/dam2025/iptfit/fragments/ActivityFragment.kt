package pt.ipt.dam2025.iptfit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pt.ipt.dam2025.iptfit.R
import pt.ipt.dam2025.iptfit.databinding.FragmentActivityBinding

class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar gráfico semanal
        setupWeeklyChart()
    }

    private fun setupWeeklyChart() {
        // Dados de exemplo para o gráfico
        val weeklyData = listOf(120, 180, 90, 210, 150, 240, 300)
        val maxValue = weeklyData.maxOrNull() ?: 1

        val bars = listOf(
            binding.barMon, binding.barTue, binding.barWed,
            binding.barThu, binding.barFri, binding.barSat, binding.barSun
        )

        val labels = listOf(
            binding.labelMon, binding.labelTue, binding.labelWed,
            binding.labelThu, binding.labelFri, binding.labelSat, binding.labelSun
        )

        val valueLabels = listOf(
            binding.valueMon, binding.valueTue, binding.valueWed,
            binding.valueThu, binding.valueFri, binding.valueSat, binding.valueSun
        )

        weeklyData.forEachIndexed { index, value ->
            val percentage = (value.toFloat() / maxValue * 100).coerceIn(0f, 100f)

            bars[index].layoutParams.height = (percentage * 2).toInt()
            bars[index].requestLayout()

            valueLabels[index].text = value.toString()
            valueLabels[index].visibility = if (value > 0) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}