package dk.sierrasoftware.nfcscanner.ui.dashboard;

import android.view.LayoutInflater
import android.view.View;
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dk.sierrasoftware.nfcscanner.R

import dk.sierrasoftware.nfcscanner.api.EntityDTO;

class EntitiesAdapter(private val entities: List<EntityDTO>) : RecyclerView.Adapter<EntitiesAdapter.EntityViewHolder>() {
    class EntityViewHolder(view:View) : RecyclerView.ViewHolder(view) {
        val textViewName: TextView = view.findViewById(R.id.text_view_name)
        val textViewTag: TextView = view.findViewById(R.id.text_view_tag_uid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entity, parent, false)
        return EntityViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
        val entity = entities[position]
        holder.textViewName.text = entity.name
        holder.textViewTag.text = entity.tag_uid
    }

    override fun getItemCount(): Int = entities.size
}
