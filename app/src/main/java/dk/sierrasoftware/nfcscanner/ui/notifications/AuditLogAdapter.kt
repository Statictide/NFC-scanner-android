package dk.sierrasoftware.nfcscanner.ui.notifications;

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View;
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dk.sierrasoftware.nfcscanner.R
import dk.sierrasoftware.nfcscanner.api.AuditLogEntry

class AuditLogAdapter(
    private val logEntries: List<AuditLogEntry>,
) :
    RecyclerView.Adapter<AuditLogAdapter.EntityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return EntityViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
        holder.bind(logEntries[position])
    }

    override fun getItemCount(): Int = logEntries.size

    class EntityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.audit_log_adapter_name)

        @SuppressLint("SetTextI18n")
        fun bind(entry: AuditLogEntry) {
            nameView.text = entry.entityName + ": " + entry.oldParentName + " -> " + entry.newParentName
        }
    }
}


