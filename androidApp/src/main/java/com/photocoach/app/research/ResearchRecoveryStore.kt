package com.photocoach.app.research

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Anonymous association only: no photograph, URI, landmarks, or participant identity. */
@Serializable
data class ResearchCaptureContext(val sessionId:String,val roundId:Int,val intent:String,
    val condition:String,val scene:String,val configurationId:String,val buildVersion:String)

/** Publication has been verified by recovery. Its original-session monotonic time is unknown. */
@Serializable
data class ResearchRecoveryReceipt(val captureId:String,val context:ResearchCaptureContext,
    val originalPublicationVerified:Boolean=true,val originalSessionPublicationElapsedMs:Long?=null)

class ResearchRecoveryStore(private val directory:File) {
    fun record(receipt:ResearchRecoveryReceipt) {
        require(receipt.captureId.matches(Regex("[a-zA-Z0-9_-]{1,100}")))
        require(receipt.originalPublicationVerified && receipt.originalSessionPublicationElapsedMs==null)
        check(directory.isDirectory || directory.mkdirs())
        val target=File(directory,"${receipt.captureId}.json")
        check(target.exists() || directory.listFiles().orEmpty().count {it.extension=="json"}<2000) {"research recovery receipt quota reached"}
        val temporary=File(directory,"${receipt.captureId}.tmp")
        try {
            temporary.outputStream().use {out -> out.write(Json.encodeToString(receipt).toByteArray(Charsets.UTF_8));out.fd.sync()}
            Files.move(temporary.toPath(),target.toPath(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE)
        } finally {temporary.delete()}
    }
    /** Invalid receipts are errors; callers must not silently exclude them. */
    fun readAll():List<ResearchRecoveryReceipt> = directory.listFiles().orEmpty().filter {it.extension=="json"}
        .sortedBy {it.name}.map {Json.decodeFromString<ResearchRecoveryReceipt>(it.readText(Charsets.UTF_8))}
}

data class ResearchRecoveryJoin(val recoveredCaptureIds:Set<String>,val issues:List<String>)
object ResearchRecoveryAssociation {
    fun join(events:List<ResearchEvent>,receipts:List<ResearchRecoveryReceipt>):ResearchRecoveryJoin {
        val issues=mutableListOf<String>();val recovered=mutableSetOf<String>()
        if(receipts.map {it.captureId}.distinct().size!=receipts.size)issues+="duplicate recovery receipt"
        receipts.forEach {receipt ->
            val accepted=events.filter {it.type=="capture_accepted" && it.captureId==receipt.captureId}.distinct()
            val owner=accepted.singleOrNull()
            val context=receipt.context
            if(owner==null || context!=owner.let {ResearchCaptureContext(it.sessionId,it.roundId,it.intent,it.condition,it.scene,it.configurationId,it.buildVersion)} ||
                !receipt.originalPublicationVerified || receipt.originalSessionPublicationElapsedMs!=null) {
                issues+="unlinked or invalid recovery receipt ${receipt.captureId}"
            } else {
                val own=events.filter {it.sessionId==owner.sessionId && it.roundId==owner.roundId && it.captureId==receipt.captureId}
                val beforePublication=own.takeWhile {it.type!="original_published"}
                if(beforePublication.any {it.type=="save" && it.result in setOf("failed_retryable","burst_failed_retryable")} &&
                    beforePublication.none {it.type=="save" && it.result in setOf("capture_failed","burst_failed")})recovered+=receipt.captureId
            }
        }
        return ResearchRecoveryJoin(if(issues.isEmpty())recovered else emptySet(),issues)
    }
}
