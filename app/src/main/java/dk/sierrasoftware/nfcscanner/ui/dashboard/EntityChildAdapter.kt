package dk.sierrasoftware.nfcscanner.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import dk.sierrasoftware.nfcscanner.R
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO

class EntityChildAdapter(private val entities: List<EntityClosureDTO>) : RecyclerView.Adapter<EntityChildAdapter.ChildViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entity_child, parent, false)
        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(entities[position])
    }

    override fun getItemCount(): Int = entities.size

    class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvChildName: TextView = itemView.findViewById(R.id.tvChildName)

        fun bind(entity: EntityClosureDTO) {
            tvChildName.text = entity.name

            // Set OnClickListener to show a Toast with the entity ID
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "Entity ID: ${entity.id}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
