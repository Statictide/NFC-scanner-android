package dk.sierrasoftware.nfcscanner.ui.dashboard;

import android.view.LayoutInflater
import android.view.View;
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dk.sierrasoftware.nfcscanner.R
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO

class EntityAdapter(private val entities: List<EntityClosureDTO>) : RecyclerView.Adapter<EntityAdapter.EntityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entity, parent, false)
        return EntityViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
        holder.bind(entities[position])
    }

    override fun getItemCount(): Int = entities.size

    class EntityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvEntityName: TextView = itemView.findViewById(R.id.tvEntityName)
        private val rvChildEntities: RecyclerView = itemView.findViewById(R.id.rvChildEntities)

        fun bind(entity: EntityClosureDTO) {
            tvEntityName.text = entity.name

            // Set OnClickListener to show a Toast with the entity ID
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "Entity ID: ${entity.id}", Toast.LENGTH_SHORT).show()
            }

            if (entity.children.isNotEmpty()) {
                rvChildEntities.visibility = View.VISIBLE
                rvChildEntities.layoutManager = LinearLayoutManager(itemView.context)
                rvChildEntities.adapter = EntityChildAdapter(entity.children)
            } else {
                rvChildEntities.visibility = View.GONE
            }
        }
    }
}

