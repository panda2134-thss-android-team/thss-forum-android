package site.panda2134.thssforum.ui.post

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.gson.JsonObject
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.gsonFireObject
import java.time.Instant
import java.util.*

/**
 * 所有带草稿的新动态页面的抽象基类
 * 草稿以 id 为键的形式存储在 DRAFT_SHARED_REF 内。值为JSON对象，含有 activityClassName / data 3个键值对
 * 其中 activityClassName 为创建这个草稿的类名，data 为草稿具体内容，由 DraftHolder 序列化得到。
 */
abstract class ActivityNewPostWithDraft<T: ActivityNewPostWithDraft.DraftHolder>: ActivityNewPost() {
    companion object {
        data class DeserializedDraft (
            val activityClassName: String,
            val data: JsonObject,
            val createdAt: Instant = Instant.now()
        )

        val EXTRA_DRAFT_ID = "draft_id"

        fun startNewPostActivityForDraftId (context: Context, draftId: String) {
            val pref = context.getSharedPreferences(context.getString(R.string.DRAFT_SHARED_PREF), MODE_PRIVATE)
            val jsonValue = pref.getString(draftId, null) ?: throw IllegalArgumentException("no such draft")
            val j = gsonFireObject.fromJson(jsonValue, DeserializedDraft::class.java)
            val clazz = Class.forName(j.activityClassName)
            context.startActivity(Intent(context, clazz).putExtra(EXTRA_DRAFT_ID, draftId))
        }

        fun getDraftList (context: Context): List<Pair<String, DeserializedDraft>> =
            with(context) {
                val pref = getSharedPreferences(getString(R.string.DRAFT_SHARED_PREF), MODE_PRIVATE)
                return pref.all.filter { it.value is String }.map {
                    try {
                        val valueParsed = gsonFireObject.fromJson(it.value as String, DeserializedDraft::class.java)
                        it.key to valueParsed
                    } catch (e: Throwable) {
                        null
                    }
                }.filterNotNull().sortedBy { it.second.createdAt }.reversed()
            }

    }

    abstract class DraftHolder {
        abstract val title: String
        abstract fun getActivityClassName(): String
    }

    private var draftId: String? = null
    protected var isPostSent = false
    protected abstract fun deserializeDraftJson(j: JsonObject): T
    protected abstract fun serializeDraftJson(holder: T): JsonObject
    protected var draftHolder: T? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getStringExtra(EXTRA_DRAFT_ID)?.let { draftId ->
            this.draftId = draftId
            val pref = getSharedPreferences(getString(R.string.DRAFT_SHARED_PREF), MODE_PRIVATE)
            val jsonValue = pref.getString(draftId, null) ?: return@let
            val j = gsonFireObject.fromJson(jsonValue, DeserializedDraft::class.java)
            draftHolder = deserializeDraftJson(j.data)
        }
    }

    abstract fun saveDraft ()

    override fun finish() {
        super.finish()
        if (!isPostSent) {
            this.saveDraft()
            draftHolder?.let {
                val activityClassName = it.getActivityClassName()
                val j = serializeDraftJson(it)
                val pref = getSharedPreferences(getString(R.string.DRAFT_SHARED_PREF), MODE_PRIVATE)
                with (pref.edit()) {
                    val jsonValue = gsonFireObject.toJson(
                        DeserializedDraft(
                            activityClassName = activityClassName,
                            data = j
                        )
                    )
                    putString(UUID.randomUUID().toString(), jsonValue)
                    apply()
                }
            }
        } else {
            // post already sent, remove the draft
            draftId?.let {
                val pref = getSharedPreferences(getString(R.string.DRAFT_SHARED_PREF), MODE_PRIVATE)
                with (pref.edit()) {
                    remove(it)
                    apply()
                }
            }
        }
    }
}