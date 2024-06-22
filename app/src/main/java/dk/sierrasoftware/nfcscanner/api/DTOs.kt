package dk.sierrasoftware.nfcscanner.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EntityDTO (val id: UInt, val tag_uid: String, val name: String, val parent_id: UInt) :
    Parcelable

@Parcelize
data class EntityClosureDTO (val entity: EntityDTO, val parent: EntityDTO?, val children: List<EntityDTO>) :
    Parcelable

@Parcelize
data class PatchEntityDTO ( val parent_id: UInt) :
    Parcelable