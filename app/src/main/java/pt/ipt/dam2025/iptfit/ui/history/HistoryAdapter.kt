package pt.ipt.dam2025.iptfit.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.dam2025.iptfit.R
import pt.ipt.dam2025.iptfit.data.local.entity.Consumption
import pt.ipt.dam2025.iptfit.databinding.ItemConsumptionBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para RecyclerView do histórico de consumos
 */
class HistoryAdapter(
    private val onDeleteClick: (Consumption) -> Unit
) : ListAdapter<Consumption, HistoryAdapter.ConsumptionViewHolder>(ConsumptionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsumptionViewHolder {
        val binding = ItemConsumptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConsumptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConsumptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConsumptionViewHolder(
        private val binding: ItemConsumptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(consumption: Consumption) {
            // Nome e marca
            binding.tvItemProductName.text = consumption.productName
            binding.tvItemBrands.text = consumption.brands ?: "Marca desconhecida"

            // Calorias
            val calories = consumption.energyKcal ?: 0f
            binding.tvItemCalories.text = if (calories > 0) {
                "${String.format("%.0f", calories)} kcal"
            } else {
                "N/D"
            }

            // Data
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvItemDate.text = dateFormat.format(Date(consumption.consumedAt))

            // Imagem
            if (!consumption.imageUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(consumption.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(binding.ivProductThumb)
            } else {
                binding.ivProductThumb.setImageResource(R.drawable.ic_launcher_foreground)
            }

            // Botão eliminar
            binding.btnDelete.setOnClickListener {
                onDeleteClick(consumption)
            }
        }
    }

    /**
     * DiffUtil para otimizar atualizações da lista
     */
    class ConsumptionDiffCallback : DiffUtil.ItemCallback<Consumption>() {
        override fun areItemsTheSame(oldItem: Consumption, newItem: Consumption): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Consumption, newItem: Consumption): Boolean {
            return oldItem == newItem
        }
    }
}