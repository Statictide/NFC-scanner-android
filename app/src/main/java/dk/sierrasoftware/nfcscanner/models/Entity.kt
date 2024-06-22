package dk.sierrasoftware.nfcscanner.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Entity (val id: UInt?, val tag_uid: String?, val name: String, val parent_id: UInt?) :
    Parcelable

@Parcelize
data class EntityClosure (val entity: Entity, val parent: Entity?, val children: List<Entity>) :
    Parcelable
