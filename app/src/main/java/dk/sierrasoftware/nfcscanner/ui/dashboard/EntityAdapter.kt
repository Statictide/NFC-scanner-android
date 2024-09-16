package dk.sierrasoftware.nfcscanner.ui.dashboard;

import android.view.LayoutInflater
import android.view.View;
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import dk.sierrasoftware.nfcscanner.MobileNavigationDirections
import dk.sierrasoftware.nfcscanner.R
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO

interface OnEntitySelectedListener {
    fun onEntitySelected(entityId: Int)
}

class EntityAdapter(
    private val entities: List<EntityClosureDTO>,
    private val listener: OnEntitySelectedListener? = null,
    private val showName: Boolean = true
) :
    RecyclerView.Adapter<EntityAdapter.EntityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entity, parent, false)
        return EntityViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
        holder.bind(entities[position], listener, showName)
    }

    override fun getItemCount(): Int = entities.size

    class EntityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.entity_adapter_name)
        private val childrenGroup: View = itemView.findViewById(R.id.entity_adapter_children_group)
        private val childrenView: RecyclerView = itemView.findViewById(R.id.entity_adapter_children)

        fun bind(entity: EntityClosureDTO, listener: OnEntitySelectedListener?, showName: Boolean) {
            if (showName) {
                nameView.text = entity.name
                if (listener != null) {
                    nameView.setOnClickListener {
                        listener.onEntitySelected(entity.id)
                    }
                }
            } else {
                nameView.visibility = View.GONE
            }

            if (entity.children.isNotEmpty()) {
                childrenGroup.visibility = View.VISIBLE
                childrenView.layoutManager = LinearLayoutManager(itemView.context)
                childrenView.adapter = EntityAdapter(entity.children, listener)
            } else {
                childrenGroup.visibility = View.GONE
            }
        }
    }
}

