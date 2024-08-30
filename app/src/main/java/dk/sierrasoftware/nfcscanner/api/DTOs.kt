package dk.sierrasoftware.nfcscanner.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EntityDTO (val id: UInt, val tag_uid: String, val name: String, val parent_id: UInt?, val parent_name: String?) :
    Parcelable

@Parcelize
data class EntityClosureDTO (val id: UInt, val tag_uid: String, val name: String, val parent_id: UInt, val parent_name: String, val children: List<EntityClosureDTO>) :
    Parcelable

@Parcelize
data class PatchEntityDTO ( val parent_id: UInt) :
    Parcelable