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
data class PatchEntityDTO (val tag_uid: String? = null, val name: String? = null, val parent_id: MaybeInt? = null) :
    Parcelable

@Parcelize
data class CreateEntityDTO ( val tag_uid: String, val name: String, val parent_id: UInt?) :
    Parcelable

@Parcelize
data class MaybeInt(val value: Int?) :
    Parcelable

@Parcelize
data class CheckForUpdateDTO(val version: String, val platform: String = "Android") :
    Parcelable

@Parcelize
data class CheckForUpdateResponseDTO(val update_required: Boolean, val update_recommended: Boolean?, val latestVersion: String?, val message: String?) :
    Parcelable

