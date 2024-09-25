package dk.sierrasoftware.nfcscanner.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Gson cannot deserialize UInt. Using Int istead.

@Parcelize
data class EntityDTO (val id: Int, val name: String, val tag_uid: String?, val parent_id: Int?, val parent_name: String?) :
    Parcelable

@Parcelize
data class EntityClosureDTO (val id: Int, val name: String, val tag_uid: String?, val parent_id: Int?, val parent_name: String?, val children: List<EntityClosureDTO>) :
    Parcelable

@Parcelize
data class PatchEntityDTO (val name: String? = null, val tag_uid: MaybeString? = null, val parent_id: MaybeInt? = null) :
    Parcelable

@Parcelize
data class CreateEntityDTO (val name: String, val tag_uid: String?, val parent_id: Int?) :
    Parcelable

@Parcelize
data class CheckForUpdateDTO(val version: String, val platform: String = "Android") :
    Parcelable

@Parcelize
data class CheckForUpdateResponseDTO(val update_mandatory: Boolean?, val update_recommended: Boolean?, val latestVersion: String?, val title: String?, val message: String?, val update_url: String?) :
    Parcelable

@Parcelize
data class LoginRequest(val username: String) :
    Parcelable

@Parcelize
data class LoginResponse(val token: String) :
    Parcelable


@Parcelize
data class MaybeInt(val value: Int?) :
    Parcelable

@Parcelize
data class MaybeString(val value: String?) :
    Parcelable
