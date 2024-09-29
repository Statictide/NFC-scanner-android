package dk.sierrasoftware.nfcscanner.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

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
data class MaybeInt(val value: Int?) :
    Parcelable


@Parcelize
data class MaybeString(val value: String?) :
    Parcelable


@Parcelize
data class AuditLogResponse(val parent_history: List<AuditLogEntry>) :
    Parcelable
@Parcelize
data class AuditLogEntry(
    @SerializedName("parent_history_id") val parentHistoryId: Int,
    @SerializedName("entity_name") val entityName: String,
    @SerializedName("old_parent_name") val oldParentName: String?,
    @SerializedName("new_parent_name") val newParentName: String?,
    @SerializedName("created_at") val createdAt: String
) :
    Parcelable

/**
 * @Parcelize
 * data class AuditLogEntry(val id: Int, val entity_id: Int, val entity_name: String, val action: String, val created_at: String) :
 *     Parcelable
 *
 */
