package com.photocoach.app.research

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ResearchEvidenceBundle(val schemaVersion:Int=1,val eventsJsonl:String,
    val recoveryReceipts:List<ResearchRecoveryReceipt>)
data class ImportedResearchBundle(val log:ResearchLogImport,val receipts:List<ResearchRecoveryReceipt>) {
    fun replay(expected:Set<ResearchRoundKey>,deadlineMs:Long)=ResearchRecomputation.replay(log,expected,deadlineMs,receipts)
}
/** Research-side export contract; preserves logger bytes and durable anonymous recovery receipts. */
object ResearchEvidenceBundles {
    fun export(eventFile:File,recoveryStore:ResearchRecoveryStore):String = Json.encodeToString(
        ResearchEvidenceBundle(eventsJsonl=eventFile.readText(Charsets.UTF_8),recoveryReceipts=recoveryStore.readAll()))
    fun read(encoded:String):ImportedResearchBundle = try {
        val bundle=Json.decodeFromString<ResearchEvidenceBundle>(encoded)
        require(bundle.schemaVersion==1)
        val log=ResearchLogReader.read(bundle.eventsJsonl)
        val association=ResearchRecoveryAssociation.join(log.events,bundle.recoveryReceipts)
        ImportedResearchBundle(log.copy(issues=(log.issues+association.issues).distinct()),bundle.recoveryReceipts)
    } catch (_:Exception) {ImportedResearchBundle(ResearchLogImport(emptyList(),listOf("invalid research evidence bundle")),emptyList())}
}
