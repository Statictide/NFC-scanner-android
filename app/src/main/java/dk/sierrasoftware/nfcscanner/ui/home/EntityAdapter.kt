package dk.sierrasoftware.nfcscanner.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dk.sierrasoftware.nfcscanner.MobileNavigationDirections
import dk.sierrasoftware.nfcscanner.R
import dk.sierrasoftware.nfcscanner.api.EntityDTO

class EntityAdapter(private val entities: List<EntityDTO>) : RecyclerView.Adapter<EntityAdapter.EntityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entity, parent, false)
        return EntityViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
        holder.bind(entities[position])
    }

    override fun getItemCount(): Int = entities.size

    class EntityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.name)
        private val childrenView: RecyclerView = itemView.findViewById(R.id.children)


        fun bind(entity: EntityDTO) {
            nameView.text = entity.name

            // Set OnClickListener to show a Toast with the entity ID
            itemView.setOnClickListener {
                val actionNavigationHome = MobileNavigationDirections.actionNavigationHomeWithTagUid(entity.tag_uid, 0)
                val navController = Navigation.findNavController(itemView)
                navController.navigate(actionNavigationHome)
            }

            childrenView.visibility = View.GONE

        }
    }
}

