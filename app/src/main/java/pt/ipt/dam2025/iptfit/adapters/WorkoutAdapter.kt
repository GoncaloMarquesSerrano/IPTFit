package pt.ipt.dam2025.iptfit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam2025.iptfit.R
import pt.ipt.dam2025.iptfit.models.Workout

class WorkoutAdapter(
    private val workouts: List<Workout>,
    private val onWorkoutClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.text_workout_name)
        val textDetails: TextView = itemView.findViewById(R.id.text_workout_details)
        val buttonStart: Button = itemView.findViewById(R.id.button_start)
        val buttonRedo: Button = itemView.findViewById(R.id.button_redo)
        val layoutCompleted: View = itemView.findViewById(R.id.layout_completed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]

        holder.textName.text = workout.name

        if (workout.completed) {
            holder.textDetails.text = "${workout.actualDuration} min • ${workout.caloriesBurned} cal"
            holder.buttonStart.visibility = View.GONE
            holder.buttonRedo.visibility = View.VISIBLE
            holder.layoutCompleted.visibility = View.VISIBLE
        } else {
            holder.textDetails.text = "${workout.duration} min • ~${workout.duration * workout.caloriesPerMinute} cal"
            holder.buttonStart.visibility = View.VISIBLE
            holder.buttonRedo.visibility = View.GONE
            holder.layoutCompleted.visibility = View.GONE
        }

        holder.buttonStart.setOnClickListener {
            onWorkoutClick(workout)
        }

        holder.buttonRedo.setOnClickListener {
            onWorkoutClick(workout.copy(completed = false))
        }
    }

    override fun getItemCount() = workouts.size
}