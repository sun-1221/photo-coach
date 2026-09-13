package com.photocoach.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.photocoach.app.camera.*
import com.photocoach.app.beauty.BeautyPreset
import java.io.File
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Real Activity/CameraX flows use platform accessibility, without a virtual Compose clock. */
class ActivityLifecycleAcceptanceTest {
    @Test(timeout=120000) fun revokedPermissionShowsRecoveryAndActualSettingsGrantReturnsToWorkingCamera() {
        refresh()
        assertEquals(android.content.pm.PackageManager.PERMISSION_DENIED,activity.checkSelfPermission(android.Manifest.permission.CAMERA))
        assertNotNull(locate("需要相机权限"));assertEquals(0L,vm.deliveredAnalysisFrames)
        click("打开设置")
        fun settingsClick(label:String) {
            awaitCondition(20000) {
                var target=node(label)
                repeat(6){if(target?.isClickable==false)target=target?.parent}
                target?.let {it.isEnabled && it.isClickable && it.performAction(AccessibilityNodeInfo.ACTION_CLICK)}==true
            }
        }
        settingsClick("Permissions");settingsClick("Camera");settingsClick("Allow only while using the app")
        var backSteps=0
        awaitCondition(15000) {
            val returned=instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString()==activity.packageName
            if(!returned && backSteps++<5) {
                android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand("input keyevent 4")).use {it.readBytes()}
                SystemClock.sleep(800)
            }
            returned
        }
        awaitCondition(30000){vm.deliveredAnalysisFrames>0 && vm.ui.value.cameraError==null}
        assertEquals(android.content.pm.PackageManager.PERMISSION_GRANTED,activity.checkSelfPermission(android.Manifest.permission.CAMERA))
        val before=vm.ui.value.recentPhoto
        click("拍照快门")
        awaitCondition(30000){vm.ui.value.recentPhoto!=null && vm.ui.value.recentPhoto!=before}
        val uri=android.net.Uri.parse(vm.ui.value.recentPhoto!!)
        try {
            activity.contentResolver.openInputStream(uri)!!.use {val bitmap=android.graphics.BitmapFactory.decodeStream(it);assertNotNull(bitmap);bitmap!!.recycle()}
            evidence("权限设置返回证据.txt","initialPermission=denied; initialAnalysis=0; realAppSettingsButton=true; realSystemCameraGrant=true; returnedAnalysis=true; actualJpegDecode=true")
        } finally {activity.contentResolver.delete(uri,null,null)}
    }
    @Test(timeout=240000) fun actualCameraPhotoSurvivesTwoUiEditingAndExportRounds() {
        prepare()
        val owned=mutableListOf<android.net.Uri>()
        fun findLabel(current:AccessibilityNodeInfo?,label:String):AccessibilityNodeInfo? {
            if(current==null)return null
            val normalized=label.replace(Regex("\\s+"),"")
            if(current.text?.toString()?.lineSequence()?.firstOrNull()?.replace(Regex("\\s+"),"")==normalized || current.contentDescription?.toString()?.replace(Regex("\\s+"),"")==normalized)return current
            repeat(current.childCount){findLabel(current.getChild(it,AccessibilityNodeInfo.FLAG_PREFETCH_DESCENDANTS_HYBRID),label)?.let {return it}}
            return null
        }
        fun scrollable(current:AccessibilityNodeInfo?):AccessibilityNodeInfo? {
            if(current==null)return null
            val slider=current.rangeInfo!=null ||
                current.actionList.any {it.id==AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.id}
            if(current.isScrollable && !slider &&
                current.className?.toString()?.contains("Horizontal")!=true)return current
            repeat(current.childCount){scrollable(current.getChild(it,AccessibilityNodeInfo.FLAG_PREFETCH_DESCENDANTS_HYBRID))?.let {return it}}
            return null
        }
        fun swipeEditor(revealBelow:Boolean) {
            val container=scrollable(instrumentation.uiAutomation.rootInActiveWindow) ?: return
            val bounds=android.graphics.Rect().also(container::getBoundsInScreen)
            bounds.intersect(activity.windowManager.currentWindowMetrics.bounds)
            if(bounds.height()<80)return
            val down=SystemClock.uptimeMillis()
            val x=bounds.right-bounds.width()*.08f
            val startY=if(revealBelow) bounds.bottom-bounds.height()*.1f else bounds.top+bounds.height()*.1f
            val endY=if(revealBelow) bounds.top+bounds.height()*.1f else bounds.bottom-bounds.height()*.1f
            for(step in 0..10) {
                val action=when(step){0->android.view.MotionEvent.ACTION_DOWN;10->android.view.MotionEvent.ACTION_UP;else->android.view.MotionEvent.ACTION_MOVE}
                val event=android.view.MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,startY+(endY-startY)*step/10f,0)
                event.source=android.view.InputDevice.SOURCE_TOUCHSCREEN
                try {instrumentation.uiAutomation.injectInputEvent(event,false)} finally {event.recycle()}
                SystemClock.sleep(16)
            }
            SystemClock.sleep(200)
        }
        fun editorNode(vararg labels:String):AccessibilityNodeInfo {
            var target:AccessibilityNodeInfo?=null
            val footer=labels.any {it in setOf("撤销","重做","重置","对比原图","另存新副本")}
            var misses=0
            try {awaitCondition(20000) {
                val root=instrumentation.uiAutomation.rootInActiveWindow
                val container=scrollable(root)
                target=labels.firstNotNullOfOrNull {label ->
                    findLabel(container,label) ?: findLabel(root,label) ?: root?.findAccessibilityNodeInfosByText(label)?.firstOrNull {node ->
                        val normalized=label.replace(Regex("\\s+"),"")
                        node.text?.toString()?.lineSequence()?.firstOrNull()?.replace(Regex("\\s+"),"")==normalized ||
                            node.contentDescription?.toString()?.replace(Regex("\\s+"),"")==normalized
                    }
                }
                if(target==null && ++misses>2) {
                    scrollable(root)?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    swipeEditor(revealBelow=true)
                } else if(target!=null && !target!!.isVisibleToUser) {
                    if(target!!.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN.id)!=true && footer)
                        swipeEditor(revealBelow=true)
                }
                target!=null
            }} catch(error:Throwable) {
                val details=StringBuilder("missing=${labels.joinToString()}; edit=${vm.ui.value.creativeResult?.edit}; compare=${vm.ui.value.creativeResult?.compareOriginal}\n");var count=0
                fun dump(n:AccessibilityNodeInfo?,depth:Int) {
                    if(n==null || depth>16 || count++>400)return
                    val b=android.graphics.Rect();n.getBoundsInScreen(b)
                    details.append("  ".repeat(depth)).append("class=${n.className}; text=${n.text}; desc=${n.contentDescription}; range=${n.rangeInfo}; scrollable=${n.isScrollable}; visible=${n.isVisibleToUser}; actions=${n.actionList}; bounds=$b\n")
                    repeat(n.childCount){dump(n.getChild(it),depth+1)}
                }
                dump(instrumentation.uiAutomation.rootInActiveWindow,0)
                evidence("editor-missing-node.txt",details.toString(),overwrite=true)
                instrumentation.uiAutomation.takeScreenshot()?.let {bitmap ->try {File(activity.getExternalFilesDir(null),"acceptance-evidence/editor-missing-node.png").outputStream().use {bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}} finally {bitmap.recycle()}}
                throw error
            }
            return target!!
        }
        fun editorClick(vararg labels:String){
            val label=labels.first()
            val found=editorNode(*labels)
            found.refresh()
            val candidates=buildList {
                add(found)
                found.parent?.let(::add)
                repeat(found.childCount){found.getChild(it)?.let(::add)}
            }
            val clickable=candidates.firstOrNull {it.isClickable && it.isEnabled}
            when {
                clickable!=null -> clickNode(clickable,label)
                found.performAction(AccessibilityNodeInfo.ACTION_CLICK) -> Unit
                found.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)==true -> Unit
                else -> {
                    val bounds=android.graphics.Rect().also(found::getBoundsInScreen)
                    assertTrue("$label must have on-screen bounds $bounds",bounds.width()>8 && bounds.height()>8)
                    touch(bounds)
                }
            }
            SystemClock.sleep(150)
        }
        fun adjustableAncestor(current:AccessibilityNodeInfo):AccessibilityNodeInfo? {
            var candidate:AccessibilityNodeInfo?=current
            repeat(8) {
                val n=candidate ?: return null
                if(n.rangeInfo!=null && n.isVisibleToUser && n.isEnabled &&
                    n.actionList.any {it.id==AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.id})return n
                candidate=n.parent
            }
            return null
        }
        fun edit(values:List<Float>) {
            listOf("曝光","对比度","饱和度","色温","色调","褪色","强度").forEachIndexed {index,label ->
                val named=editorNode("${label}调节",label)
                fun siblingAdjustable(current:AccessibilityNodeInfo):AccessibilityNodeInfo? {
                    val parent=current.parent ?: return null
                    repeat(parent.childCount) {childIndex ->
                        val child=parent.getChild(childIndex) ?: return@repeat
                        if(child.rangeInfo!=null && child.actionList.any {it.id==AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.id})return child
                        repeat(child.childCount) {nestedIndex ->
                            val nested=child.getChild(nestedIndex) ?: return@repeat
                            if(nested.rangeInfo!=null && nested.actionList.any {it.id==AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.id})return nested
                        }
                    }
                    return null
                }
                val slider=adjustableAncestor(named) ?: siblingAdjustable(named) ?: run {
                    val details=StringBuilder("named=$label\n")
                    fun dump(n:AccessibilityNodeInfo?,depth:Int) {
                        if(n==null || depth>8)return
                        val b=android.graphics.Rect();n.getBoundsInScreen(b)
                        details.append("  ".repeat(depth)).append("text=${n.text}; desc=${n.contentDescription}; range=${n.rangeInfo}; actions=${n.actionList}; bounds=$b; visible=${n.isVisibleToUser}; children=${n.childCount}\n")
                        repeat(n.childCount){dump(n.getChild(it),depth+1)}
                    }
                    dump(named.parent,0);evidence("editor-slider-tree.txt",details.toString())
                    instrumentation.uiAutomation.takeScreenshot()?.let {bitmap ->
                        try {File(activity.getExternalFilesDir(null),"acceptance-evidence/editor-slider-tree.png").outputStream().use {bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}} finally {bitmap.recycle()}
                    }
                    error("named slider range missing: $label")
                }
                val range=slider.rangeInfo!!;assertTrue(values[index] in range.min..range.max)
                val beforeEdit=vm.ui.value.creativeResult!!.edit
                val beforeFields=listOf(beforeEdit.exposureStops,beforeEdit.contrast,beforeEdit.saturation,beforeEdit.temperature,beforeEdit.tint,beforeEdit.fade,beforeEdit.styleStrength)
                assertTrue(slider.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.id,
                    android.os.Bundle().apply {putFloat(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE,values[index])}))
                awaitCondition(3000) {
                    val e=vm.ui.value.creativeResult!!.edit
                    val actual=listOf(e.exposureStops,e.contrast,e.saturation,e.temperature,e.tint,e.fade,e.styleStrength)
                    kotlin.math.abs(actual[index]-values[index])<.001f
                }
                android.util.Log.i("EditorAcceptance","slider=$label accepted=${values[index]}")
                val afterEdit=vm.ui.value.creativeResult!!.edit
                listOf(afterEdit.exposureStops,afterEdit.contrast,afterEdit.saturation,afterEdit.temperature,afterEdit.tint,afterEdit.fade,afterEdit.styleStrength).forEachIndexed {other,value ->
                    if(other!=index)assertEquals("$label must not change field $other",beforeFields[other],value,.001f)
                }
            }
            val e=vm.ui.value.creativeResult!!.edit
            listOf(e.exposureStops,e.contrast,e.saturation,e.temperature,e.tint,e.fade,e.styleStrength).forEachIndexed {i,v->assertEquals(values[i],v,.001f)}
        }
        fun bytes(uri:android.net.Uri)=activity.contentResolver.openInputStream(uri)!!.use {it.readBytes()}
        val before=vm.ui.value.recentPhoto
        click("拍照快门")
        awaitCondition(30000){vm.ui.value.recentPhoto!=null && vm.ui.value.recentPhoto!=before && vm.ui.value.creativeResult!=null}
        val original=android.net.Uri.parse(vm.ui.value.recentPhoto!!);owned+=original
        val originalBytes=bytes(original)
        try {
            click("美颜·关闭");click("编辑刚拍照片")
            awaitCondition(5000){vm.ui.value.creativeResultVisible}
            val initialEdit=vm.ui.value.creativeResult!!.edit
            listOf(listOf(.5f,.2f,-.2f,.3f,-.3f,.2f,.7f),listOf(-.5f,-.2f,.2f,-.3f,.3f,.1f,.4f)).forEachIndexed {round,values ->
                android.util.Log.i("EditorAcceptance","round=$round begin")
                edit(values);val edited=vm.ui.value.creativeResult!!.edit
                assertTrue("slider edits must record undo history",vm.ui.value.creativeResult!!.canUndo)
                editorClick("撤销");assertNotEquals(edited,vm.ui.value.creativeResult!!.edit)
                editorClick("重做");assertEquals(edited,vm.ui.value.creativeResult!!.edit)
                editorClick("对比原图")
                awaitCondition(3000){vm.ui.value.creativeResult!!.compareOriginal}
                editorClick("对比原图")
                awaitCondition(3000){!vm.ui.value.creativeResult!!.compareOriginal}
                if(round==0){editorClick("重置");assertEquals(initialEdit,vm.ui.value.creativeResult!!.edit);edit(values)}
                val previous=vm.ui.value.recentPhoto
                editorClick("另存新副本")
                awaitCondition(30000){!vm.ui.value.creativeResult!!.exportInProgress && vm.ui.value.recentPhoto!=previous}
                val copy=android.net.Uri.parse(vm.ui.value.recentPhoto!!);owned+=copy
                val encoded=bytes(copy);val bitmap=android.graphics.BitmapFactory.decodeByteArray(encoded,0,encoded.size)
                assertNotNull(bitmap);assertTrue(bitmap!!.width>0 && bitmap.height>0);bitmap.recycle()
                assertArrayEquals(originalBytes,bytes(original));assertNull(vm.ui.value.creativeResult!!.failedExportId)
                android.util.Log.i("EditorAcceptance","round=$round exported")
            }
            assertEquals(3,owned.distinct().size)
            evidence("编辑两轮证据.txt","source=actualCameraXJPEG; actualActivityUi=true; sevenSlidersEachRound=true; rounds=2; undoRedoCompareReset=true; exports=2; originalBytesUnchanged=true")
        } finally {owned.distinct().forEach {activity.contentResolver.delete(it,null,null)}}
    }
    @Test(timeout=120000) fun actualLiveWithMeasuredPrebufferPublishesVerifiedMotionPhoto() {
        prepare();var owned:String?=null
        val priorPriority=vm.ui.value.capturePriority
        val requestedPriority=InstrumentationRegistry.getArguments().getString("liveCapturePriority")?.let(CapturePriority::valueOf) ?: priorPriority
        val priorThermal=vm.ui.value.thermalLevel
        val simulatedThermal=InstrumentationRegistry.getArguments().getString("liveSimulatedThermal")?.let(ThermalLevel::valueOf)
        val binder=MainActivity::class.java.getDeclaredField("camera").apply {isAccessible=true}.get(activity) as CameraBinder
        val recording=CameraBinder::class.java.getDeclaredField("liveRecording").apply {isAccessible=true}.get(binder) as LiveRecordingController
        try {
            simulatedThermal?.let {level ->instrumentation.runOnMainSync {binder.updateThermalLevel(level);vm.onThermalLevel(level)}}
            if(requestedPriority!=priorPriority) {
                val before=vm.deliveredAnalysisFrames
                instrumentation.runOnMainSync {vm.setCapturePriority(requestedPriority)}
                awaitCondition(30000){vm.deliveredAnalysisFrames>before+3 && vm.ui.value.cameraError==null}
            }
            val shutterNode=locate("拍照快门")
            instrumentation.runOnMainSync {vm.setLivePhotoEnabled(true)}
            awaitCondition(35000){recording.hasPrebuffer() || vm.ui.value.liveFallbackReason!=null}
            assertTrue("actual retained prebuffer is required for this positive case",recording.hasPrebuffer())
            android.util.Log.i("MotionStage","test prebuffer observed; actual shutter accessibility click t=${SystemClock.elapsedRealtime()}")
            val prior=vm.ui.value.recentPhoto
            clickNode(shutterNode,"拍照快门")
            android.util.Log.i("MotionStage","test shutter accessibility action delivered t=${SystemClock.elapsedRealtime()}")
            awaitCondition(30000){vm.ui.value.recentPhoto!=null && vm.ui.value.recentPhoto!=prior}
            owned=vm.ui.value.recentPhoto!!
            awaitCondition(15000){vm.ui.value.creativeResult?.photos?.any {it.originalUri==owned}==true}
            val photo=vm.ui.value.creativeResult!!.selectedPhoto
            evidence("实际Live正向.txt","actualCamera=true; capturePriority=$requestedPriority; simulatedThermal=$simulatedThermal; waitedOnlyInTestForMeasuredPrebuffer=true; isMotionPhoto=${photo.isMotionPhoto}; fallback=${vm.ui.value.liveFallbackReason}; warning=${photo.warning}")
            assertTrue("published asset must be verified Motion Photo, not fallback",photo.isMotionPhoto)
            val uri=android.net.Uri.parse(owned)
            val inspected=PublishedAssetVerifier.verifyPending(activity.contentResolver,uri,true)
            PublishedAssetVerifier.verifyPublished(activity.contentResolver,uri,inspected.displayName,
                requireNotNull(inspected.relativePath),true)
            evidence("实际Live正向资产.txt","publishedMotionTailVerified=true; bytes=${inspected.length}; width=${inspected.width}; height=${inspected.height}")
        } finally {
            evidence("实际Live正向收尾.txt","analysis=${vm.deliveredAnalysisFrames}; fallback=${vm.ui.value.liveFallbackReason}; cameraError=${vm.ui.value.cameraError}")
            instrumentation.runOnMainSync {vm.setLivePhotoEnabled(false);vm.setCapturePriority(priorPriority);if(simulatedThermal!=null){binder.updateThermalLevel(priorThermal);vm.onThermalLevel(priorThermal)}}
            owned?.let {activity.contentResolver.delete(android.net.Uri.parse(it),null,null)}
        }
    }
    @Test(timeout=120000) fun discardMarkerWriteFailureKeepsActualActivityRetryStateUntilDurable() {
        prepare()
        val binder=MainActivity::class.java.getDeclaredField("camera").apply {isAccessible=true}.get(activity) as CameraBinder
        val id=com.photocoach.app.creative.CaptureIdentity.create()
        val directory=File(activity.noBackupFilesDir,"pending-captures").apply {mkdirs()}
        val source=File.createTempFile("discard-${id.value}-",".jpg",directory).apply {writeBytes(byteArrayOf(1,2,3))}
        val spec=CaptureSpec(id,1,System.currentTimeMillis(),com.photocoach.app.creative.CreativeStyle.ORIGINAL,
            saveStrategy=SaveStrategy.ORIGINAL_WITH_RECIPE,derivativeQuality=DerivativeQuality.FULL,livePhotoRequested=false)
        val record=PendingCapture(source,spec,{}, {vm.onSaveFailed(it,true,null)}, {},0,0,
            jpegPreparation=JpegPreparation(source.path,20,16,0,0,20,16,0,95))
        record.coordinator.complete(SaveStage.SPACE_CHECK)
        val journalDirectory=File(activity.filesDir,"save-journal")
        val store=SaveJournalStore(journalDirectory)
        val blocker=File(journalDirectory,"${record.toJournal().key}.json.tmp")
        try {
            instrumentation.runOnMainSync {binder.retainJpegPreparationFailure(record,java.io.IOException("injected initial save failure"))}
            assertFalse(store.read(record.toJournal())!!.discarded)
            assertTrue(blocker.mkdir());File(blocker,"owned-blocker").writeText("force atomic journal temporary write failure")
            click("放弃")
            awaitCondition(5000){(vm.ui.value.guidance.stage as? com.photocoach.coach.GuidanceStage.SaveFailed)?.message?.contains("未能记录放弃")==true}
            assertEquals(id.value,binder.pendingCaptureId);assertTrue(source.exists())
            assertFalse(store.read(record.toJournal())!!.discarded)
            assertNotNull(node("重试"));assertNotNull(node("放弃"))
            assertTrue(File(blocker,"owned-blocker").delete());assertTrue(blocker.delete())
            click("放弃")
            awaitCondition(5000){vm.ui.value.guidance.stage !is com.photocoach.coach.GuidanceStage.SaveFailed}
            assertNull(binder.pendingCaptureId);assertFalse(source.exists());assertNull(store.read(record.toJournal()))
            evidence("放弃标记证据.txt","actualActivityDiscardButton=true; injectedFirstJournalWriteFailure=true; retainedPendingAndRetryState=true; secondDurableDiscardClears=true")
        } finally {
            File(blocker,"owned-blocker").delete();blocker.delete()
            instrumentation.runOnMainSync {binder.discardPending()}
            source.delete();store.delete(record.toJournal())
        }
    }
    @get:Rule val activityRule=ActivityScenarioRule(MainActivity::class.java)
    private lateinit var activity:MainActivity
    private lateinit var vm:AppViewModel
    private val positionedWindows=mutableSetOf<Int>()
    private val instrumentation get()=InstrumentationRegistry.getInstrumentation()
    private fun refresh() {activityRule.scenario.onActivity {activity=it;vm=ViewModelProvider(it)[AppViewModel::class.java]}}
    private fun awaitCondition(timeout:Long,condition:()->Boolean) {
        val deadline=SystemClock.elapsedRealtime()+timeout
        while(!condition()) {if(SystemClock.elapsedRealtime()>=deadline)fail("condition timed out after $timeout ms");SystemClock.sleep(50)}
    }
    private fun node(label:String):AccessibilityNodeInfo? {
        val root=instrumentation.uiAutomation.rootInActiveWindow ?: return null
        val normalizedLabel=label.replace(Regex("\\s+"),"")
        fun visible(current:AccessibilityNodeInfo?):AccessibilityNodeInfo? {
            if(current==null || !current.isVisibleToUser)return null
            if(current.text?.toString()?.lineSequence()?.firstOrNull()?.replace(Regex("\\s+"),"")==normalizedLabel ||
                current.contentDescription?.toString()?.replace(Regex("\\s+"),"")==normalizedLabel)return current
            for(index in current.childCount-1 downTo 0) {
                visible(current.getChild(index,AccessibilityNodeInfo.FLAG_PREFETCH_DESCENDANTS_HYBRID))?.let {return it}
            }
            return null
        }
        return visible(root)
    }
    private fun locate(label:String):AccessibilityNodeInfo {
        var target:AccessibilityNodeInfo?=null
        var scrolls=0
        val bottomAction=label in setOf("打开参数建议","编辑刚拍照片","打开","分享","收藏","回收站")
        val windowId=instrumentation.uiAutomation.rootInActiveWindow?.windowId
        var positioned=windowId in positionedWindows
        var scrollBounds:android.graphics.Rect?=null
        fun scroll(current:AccessibilityNodeInfo?):Boolean {
            if(current==null)return false
            if(scrollBounds==null && current.isScrollable && current.className?.toString()?.contains("Horizontal")!=true)
                scrollBounds=android.graphics.Rect().also(current::getBoundsInScreen).apply {
                    intersect(activity.windowManager.currentWindowMetrics.bounds)
                    inset(12,24)
                }.also {evidence("activity-scroll-bounds.txt","label=$label; scroll=$it")}
            scrollBounds?.let {bounds ->
                val down=SystemClock.uptimeMillis()
                for(step in 0..10) {
                    val action=when(step){0->android.view.MotionEvent.ACTION_DOWN;10->android.view.MotionEvent.ACTION_UP;else->android.view.MotionEvent.ACTION_MOVE}
                    val y=bounds.bottom-bounds.height()*(.1f+.8f*step/10)
                    val event=android.view.MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,bounds.exactCenterX(),y,0)
                    event.source=android.view.InputDevice.SOURCE_TOUCHSCREEN
                    try {assertTrue(instrumentation.uiAutomation.injectInputEvent(event,false))} finally {event.recycle()}
                    SystemClock.sleep(20)
                }
                return true
            }
            repeat(current.childCount){if(scroll(current.getChild(it)))return true}
            return false
        }
        try {awaitCondition(if(bottomAction)60000 else 15000){
            if(bottomAction && !positioned) {
                positioned=true
                // These actions are below the long style/editor content. Position first;
                // repeatedly walking each intermediate Compose virtual tree is expensive.
                for(attempt in 0 until 6) {
                    if(!scroll(instrumentation.uiAutomation.rootInActiveWindow))break
                    scrolls++;SystemClock.sleep(400)
                }
                windowId?.let(positionedWindows::add)
            }
            target=node(label)
            if(target==null && bottomAction && scrolls<12 && scroll(instrumentation.uiAutomation.rootInActiveWindow)) {
                scrolls++;SystemClock.sleep(350)
                target=node(label)
            }
            target!=null}}
        catch(error:Throwable) {
            instrumentation.uiAutomation.takeScreenshot()?.let {bitmap ->
                try {File(activity.getExternalFilesDir(null),"acceptance-evidence/activity-accessibility-missing.png").outputStream().use {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)
                }} finally {bitmap.recycle()}
            }
            val details=StringBuilder("missing=$label; scrolls=$scrolls\n")
            fun dump(current:AccessibilityNodeInfo?,depth:Int) {
                if(current==null || depth>20 || details.length>20000)return
                details.append("  ".repeat(depth)).append("${current.className};text=${current.text};desc=${current.contentDescription};visible=${current.isVisibleToUser};scroll=${current.isScrollable}\n")
                repeat(current.childCount){dump(current.getChild(it),depth+1)}
            }
            dump(instrumentation.uiAutomation.rootInActiveWindow,0);evidence("activity-accessibility-missing.txt",details.toString())
            throw error
        }
        return target!!
    }
    private fun clickNode(target:AccessibilityNodeInfo,label:String) {
        var clickable=target
        repeat(6){if(!clickable.isClickable)clickable=clickable.parent ?: clickable}
        assertTrue("$label must be enabled",clickable.isEnabled)
        assertTrue("$label must be clickable",clickable.isClickable)
        assertTrue("$label click rejected",clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK))
    }
    private fun click(label:String)=clickNode(locate(label),label)
    private fun touch(bounds:android.graphics.Rect,waitForFinish:Boolean=true) {
        val down=SystemClock.uptimeMillis()
        for(action in listOf(android.view.MotionEvent.ACTION_DOWN,android.view.MotionEvent.ACTION_UP)) {
            val event=android.view.MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,bounds.exactCenterX(),bounds.exactCenterY(),0)
            event.source=android.view.InputDevice.SOURCE_TOUCHSCREEN
            try {assertTrue(instrumentation.uiAutomation.injectInputEvent(event,waitForFinish))} finally {event.recycle()}
        }
    }
    private fun evidence(name:String,text:String,overwrite:Boolean=false) {
        val directory=File(activity.getExternalFilesDir(null),"acceptance-evidence").apply {mkdirs()}
        val file=File(directory,name)
        if(overwrite)file.writeText(text+"\n") else file.appendText(text+"\n")
    }
    private fun prepare() {
        refresh()
        if(activity.checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED) {
            instrumentation.uiAutomation.grantRuntimePermission(activity.packageName,Manifest.permission.CAMERA)
            activityRule.scenario.moveToState(Lifecycle.State.CREATED);activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
            refresh()
        }
        awaitCondition(20000){node("同意并继续")!=null || vm.ui.value.guidance.shutterEnabled}
        if(node("同意并继续")!=null)click("同意并继续")
        instrumentation.runOnMainSync {vm.setThreeShotBurstEnabled(false);vm.setLivePhotoEnabled(false);vm.setBeautyPreset(BeautyPreset.OFF)
            vm.setSaveStrategy(SaveStrategy.ORIGINAL_WITH_RECIPE);vm.setCaptureTimer(CaptureTimer.OFF)}
        try {awaitCondition(30000){vm.ui.value.cameraError==null && vm.ui.value.guidance.shutterEnabled && vm.deliveredAnalysisFrames>0}}
        finally {evidence("activity-prepare.txt","cameraError=${vm.ui.value.cameraError}; shutter=${vm.ui.value.guidance.shutterEnabled}; delivered=${vm.deliveredAnalysisFrames}; accepted=${vm.acceptedAnalysisFrames}")}
        evidence("activity-analysis.txt","realtime=${activity.cameraTimestampsRealtime}; delivered=${vm.deliveredAnalysisFrames}; accepted=${vm.acceptedAnalysisFrames}; ageMs=${vm.lastAnalysisAgeMs}")
        val metrics=activity.windowManager.currentWindowMetrics
        evidence("activity-window.txt","windowBounds=${metrics.bounds}; systemBars=${metrics.windowInsets.getInsetsIgnoringVisibility(android.view.WindowInsets.Type.systemBars())}")
    }
    private fun openPanel() {click("美颜·关闭");click("打开参数建议");awaitCondition(5000){vm.ui.value.parameterPanelOpen}}
    @Test(timeout=180000) fun actualPostPhotoActionsGrantUrisFavoriteAndConfirmOrCancelTrash() {
        prepare()
        val messages=java.util.concurrent.ConcurrentLinkedQueue<String>()
        val messageObserver=kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined).launch {
            vm.ui.collect {it.controlMessage?.let(messages::add)}
        }
        var owned:android.net.Uri?=null
        val intents=java.util.concurrent.CopyOnWriteArrayList<android.content.Intent>()
        var missingViewer=false
        val monitor=object:android.app.Instrumentation.ActivityMonitor() {
            override fun onStartActivity(intent:android.content.Intent):android.app.Instrumentation.ActivityResult? {
                if(intent.action !in setOf(android.content.Intent.ACTION_VIEW,android.content.Intent.ACTION_CHOOSER))return null
                intents+=android.content.Intent(intent)
                if(missingViewer)throw android.content.ActivityNotFoundException("injected missing handler")
                return android.app.Instrumentation.ActivityResult(android.app.Activity.RESULT_CANCELED,null)
            }
        }
        instrumentation.addMonitor(monitor)
        try {
            val previous=vm.ui.value.recentPhoto
            click("拍照快门")
            awaitCondition(25000){vm.ui.value.creativeResult!=null && vm.ui.value.recentPhoto!=previous}
            val uri=android.net.Uri.parse(vm.ui.value.recentPhoto!!);owned=uri
            instrumentation.runOnMainSync {vm.openCreativeResult()}
            click("打开")
            awaitCondition(2000){intents.any {it.action==android.content.Intent.ACTION_VIEW}}
            val view=intents.last()
            assertEquals(uri,view.data);assertTrue(view.flags and android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION!=0)
            assertEquals("image/jpeg",view.type)
            click("分享")
            awaitCondition(2000){intents.any {it.action==android.content.Intent.ACTION_CHOOSER}}
            val send=intents.last().getParcelableExtra(android.content.Intent.EXTRA_INTENT,android.content.Intent::class.java)!!
            assertEquals(android.content.Intent.ACTION_SEND,send.action)
            assertEquals(uri,send.getParcelableExtra(android.content.Intent.EXTRA_STREAM,android.net.Uri::class.java))
            assertTrue(send.flags and android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION!=0)
            missingViewer=true;click("打开")
            awaitCondition(2000){vm.ui.value.controlMessage=="没有可用的图片查看器"}
            missingViewer=false
            click("收藏")
            awaitCondition(2000){vm.ui.value.controlMessage=="已标记为收藏"}
            val copied=java.util.concurrent.CountDownLatch(1)
            val bounds=activity.windowManager.currentWindowMetrics.bounds
            val feedback=android.graphics.Bitmap.createBitmap(bounds.width(),bounds.height(),android.graphics.Bitmap.Config.ARGB_8888)
            val copyStatus=java.util.concurrent.atomic.AtomicInteger(-1)
            instrumentation.runOnMainSync {activity.window.decorView.postOnAnimation {
                android.view.PixelCopy.request(activity.window,feedback,{status ->copyStatus.set(status);copied.countDown()},android.os.Handler(android.os.Looper.getMainLooper()))
            }}
            assertTrue(copied.await(2,java.util.concurrent.TimeUnit.SECONDS));assertEquals(android.view.PixelCopy.SUCCESS,copyStatus.get())
            evidence("activity-post-feedback.txt","capture=Window PixelCopy; status=${copyStatus.get()}; messageAfterCopy=${vm.ui.value.controlMessage}")
            feedback.let {bitmap ->
                try {File(activity.getExternalFilesDir(null),"acceptance-evidence/activity-post-feedback.png").outputStream().use {
                    assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it))
                }} finally {bitmap.recycle()}
            }
            fun column(name:String):Int = activity.contentResolver.query(uri,arrayOf(name),null,null,null)!!.use {
                assertTrue(it.moveToFirst());it.getInt(0)
            }
            assertEquals(1,column(android.provider.MediaStore.MediaColumns.IS_FAVORITE))
            messages.clear()
            click("回收站")
            click("Deny")
            awaitCondition(5000){"未移动照片" in messages}
            assertEquals(0,column(android.provider.MediaStore.MediaColumns.IS_TRASHED))
            messages.clear()
            click("回收站")
            click("Allow")
            awaitCondition(5000){"已移到系统回收站" in messages}
            assertEquals(1,column(android.provider.MediaStore.MediaColumns.IS_TRASHED))
            // A real stale URI exercises the resolver's zero-row favorite failure path.
            assertEquals(1,activity.contentResolver.delete(uri,null,null));owned=null
            click("收藏")
            awaitCondition(2000){vm.ui.value.controlMessage=="系统相册未接受收藏标记"}
            evidence("activity-post-actions.txt","source=owned-emulator-camera-JPEG; actualUiButtons=4; viewAndShareReadGrant=true; missingViewer=injected; favoriteSuccessAndStaleUriFailure=true; trashCancelAndConfirm=true")
        } finally {
            messageObserver.cancel()
            instrumentation.removeMonitor(monitor)
            owned?.let {activity.contentResolver.delete(it,null,null)}
        }
    }
    @Test(timeout=180000) fun actualThreeAndTenSecondTimersConsumeVolumeKeysAndCancelWithoutCapture() {
        prepare()
        val audio=activity.getSystemService(android.media.AudioManager::class.java)
        val volume=audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val owned=mutableSetOf<String>()
        fun key(code:Int) {
            instrumentation.sendKeySync(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,code))
            instrumentation.sendKeySync(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP,code))
        }
        try {
            for(timer in listOf(CaptureTimer.THREE_SECONDS,CaptureTimer.TEN_SECONDS)) {
                instrumentation.runOnMainSync {vm.setCaptureTimer(timer)}
                val shutter=android.graphics.Rect().also(locate("拍照快门")::getBoundsInScreen)
                val pulse=vm.ui.value.shutterPulse
                val previous=vm.ui.value.recentPhoto
                // Exercise both directions through Activity.dispatchKeyEvent/onKeyDown.
                val heldAt=SystemClock.uptimeMillis()
                instrumentation.sendKeySync(android.view.KeyEvent(heldAt,heldAt,android.view.KeyEvent.ACTION_DOWN,
                    android.view.KeyEvent.KEYCODE_VOLUME_UP,0))
                awaitCondition(2000){vm.ui.value.countdownSeconds==timer.seconds}
                awaitCondition(1800){vm.ui.value.countdownSeconds==timer.seconds-1}
                repeat(2) {repeatIndex ->
                    instrumentation.sendKeySync(android.view.KeyEvent(heldAt,SystemClock.uptimeMillis(),android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_VOLUME_UP,repeatIndex+1))
                    SystemClock.sleep(100)
                    assertNotNull("held key must not cancel the timer",vm.ui.value.countdownSeconds)
                    assertTrue("held key must not restart the timer",vm.ui.value.countdownSeconds!!<timer.seconds)
                    assertEquals(pulse,vm.ui.value.shutterPulse)
                }
                instrumentation.sendKeySync(android.view.KeyEvent(heldAt,SystemClock.uptimeMillis(),android.view.KeyEvent.ACTION_UP,
                    android.view.KeyEvent.KEYCODE_VOLUME_UP,0))
                key(android.view.KeyEvent.KEYCODE_VOLUME_DOWN)
                awaitCondition(2000){vm.ui.value.countdownSeconds==null}
                SystemClock.sleep((timer.seconds+1)*1000L)
                assertEquals(pulse,vm.ui.value.shutterPulse);assertEquals(previous,vm.ui.value.recentPhoto)
                // Screen cancellation traverses the real shutter accessibility action too.
                touch(shutter)
                awaitCondition(2000){vm.ui.value.countdownSeconds==timer.seconds}
                SystemClock.sleep(150)
                touch(shutter)
                awaitCondition(2000){vm.ui.value.countdownSeconds==null}
                SystemClock.sleep((timer.seconds+1)*1000L)
                assertEquals(pulse,vm.ui.value.shutterPulse);assertEquals(previous,vm.ui.value.recentPhoto)
                if(timer==CaptureTimer.THREE_SECONDS)touch(shutter) else key(android.view.KeyEvent.KEYCODE_VOLUME_UP)
                val observed=mutableListOf<Pair<Int,Long>>()
                awaitCondition((timer.seconds+25)*1000L) {
                    vm.ui.value.countdownSeconds?.let {value ->
                        if(observed.lastOrNull()?.first!=value)observed+=value to SystemClock.elapsedRealtime()
                    }
                    vm.ui.value.recentPhoto!=null && vm.ui.value.recentPhoto!=previous
                }
                owned+=vm.ui.value.recentPhoto!!
                assertEquals((timer.seconds downTo 1).toList(),observed.map {it.first})
                assertTrue("countdown must use actual one-second delays",observed.zipWithNext().all {(a,b)->b.second-a.second>=750})
                assertEquals(pulse+1,vm.ui.value.shutterPulse)
                SystemClock.sleep(1200)
                assertEquals(pulse+1,vm.ui.value.shutterPulse)
                awaitCondition(20000){vm.ui.value.creativeResult?.selectedPhoto?.displayUri==vm.ui.value.recentPhoto}
                assertEquals(1,vm.ui.value.creativeResult!!.photos.size)
                assertEquals(volume,audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC))
                evidence("activity-timer.txt","seconds=${timer.seconds}; observed=$observed; screenAndKeyCancellation=true; captures=1; musicVolumeUnchanged=true")
                awaitCondition(10000){vm.ui.value.guidance.shutterEnabled}
            }
        } finally {
            instrumentation.runOnMainSync {vm.setCaptureTimer(CaptureTimer.OFF)}
            owned.forEach {activity.contentResolver.delete(android.net.Uri.parse(it),null,null)}
        }
    }
    @Test(timeout=120000) fun realActivityBackgroundAndRecreationResumeAnalysisAndCapture() {
        prepare();val owned=mutableSetOf<String>()
        try {
            repeat(2){iteration ->
                val before=vm.deliveredAnalysisFrames
                activityRule.scenario.moveToState(Lifecycle.State.CREATED);activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
                if(iteration==1)activityRule.scenario.recreate()
                refresh()
                if(iteration==1) {
                    instrumentation.runOnMainSync {activity.requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE}
                    awaitCondition(15000){activity.windowManager.currentWindowMetrics.bounds.let {it.width()>it.height()}}
                }
                awaitCondition(30000){vm.deliveredAnalysisFrames>before+2 && vm.ui.value.guidance.shutterEnabled}
                if(iteration==1) {
                    val shutter=android.graphics.Rect().also(locate("拍照快门")::getBoundsInScreen)
                    val skip=android.graphics.Rect().also(locate("跳过")::getBoundsInScreen)
                    assertFalse("landscape skip and shutter must not overlap",android.graphics.Rect.intersects(shutter,skip))
                    click("跳过")
                    assertTrue(vm.ui.value.guidance.shutterEnabled)
                    instrumentation.uiAutomation.takeScreenshot()!!.let {bitmap ->
                        try {assertTrue(bitmap.width>bitmap.height)
                            File(activity.getExternalFilesDir(null),"acceptance-evidence/activity-landscape.png").outputStream().use {
                                assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it))
                            }
                        } finally {bitmap.recycle()}
                    }
                    evidence("activity-landscape.txt","actualRequestedOrientation=LANDSCAPE; window=${activity.windowManager.currentWindowMetrics.bounds}; shutter=$shutter; skip=$skip; actualSkipClick=true")
                }
                val previous=vm.ui.value.recentPhoto
                click("拍照快门")
                awaitCondition(20000){vm.ui.value.recentPhoto!=null && vm.ui.value.recentPhoto!=previous}
                val uri=vm.ui.value.recentPhoto!!;owned+=uri
                activity.contentResolver.openInputStream(android.net.Uri.parse(uri)).use {input ->
                    val bitmap=android.graphics.BitmapFactory.decodeStream(input);assertNotNull(bitmap);bitmap!!.recycle()}
                evidence("activity-lifecycle.txt","iteration=$iteration; recreated=${iteration==1}; deliveredBefore=$before; deliveredAfter=${vm.deliveredAnalysisFrames}; originalDecode=true")
            }
        } finally {
            instrumentation.runOnMainSync {activity.requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
            owned.forEach {activity.contentResolver.delete(android.net.Uri.parse(it),null,null)}
        }
    }
    @Test(timeout=120000) fun actualLiveGraphKeepsAnalysisAndPublishesOnePrimaryOrExplicitJpegFallback() {
        prepare();var owned:String?=null
        try {
            val before=vm.deliveredAnalysisFrames
            instrumentation.runOnMainSync {vm.setLivePhotoEnabled(true)}
            awaitCondition(30000){vm.ui.value.livePhotoAvailable || vm.ui.value.liveFallbackReason!=null}
            awaitCondition(30000){vm.deliveredAnalysisFrames>before+5 && vm.ui.value.guidance.shutterEnabled}
            SystemClock.sleep(2200)
            val prior=vm.ui.value.recentPhoto
            click("拍照快门")
            awaitCondition(30000){vm.ui.value.recentPhoto!=null && vm.ui.value.recentPhoto!=prior}
            owned=vm.ui.value.recentPhoto!!
            val uri=android.net.Uri.parse(owned)
            val bytes=activity.contentResolver.openInputStream(uri)!!.use {it.readBytes()}
            val bitmap=android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.size)
            assertNotNull(bitmap);bitmap!!.recycle()
            evidence("activity-live-graph.txt","available=${vm.ui.value.livePhotoAvailable}; fallback=${vm.ui.value.liveFallbackReason}; " +
                "deliveredBefore=$before; deliveredAfter=${vm.deliveredAnalysisFrames}; uri=$uri; bytes=${bytes.size}; cameraError=${vm.ui.value.cameraError}; message=${vm.ui.value.controlMessage}")
            assertNull(vm.ui.value.cameraError);assertTrue(vm.ui.value.guidance.shutterEnabled)
        } finally {
            instrumentation.runOnMainSync {vm.setLivePhotoEnabled(false)}
            owned?.let {activity.contentResolver.delete(android.net.Uri.parse(it),null,null)}
        }
    }
    @Test(timeout=90000) fun actualParameterPanelKeepsAnalysisAndDoesNotStartSpeechOnClose() {
        prepare();val previous=vm.deliveredAnalysisFrames
        openPanel()
        awaitCondition(20000){vm.deliveredAnalysisFrames>previous+2 && !activity.speechActive}
        val speechCount=activity.submittedUtterances;val round=vm.ui.value.guidance.roundId
        assertTrue(locate("拍照").isEnabled)
        click("关闭")
        awaitCondition(20000){!vm.ui.value.parameterPanelOpen && vm.deliveredAnalysisFrames>previous+4}
        assertEquals(round,vm.ui.value.guidance.roundId);assertEquals(speechCount,activity.submittedUtterances)
        assertTrue(vm.ui.value.guidance.shutterEnabled);assertTrue(locate("拍照快门").isEnabled)
    }
    @Test(timeout=180000) fun controlledCueUsesActualActivitySpeechAndPanelCancellationOrUnavailableFallback() {
        prepare()
        val cueSignals=com.photocoach.coach.Signals(faceCount=1,faceRatio=.03f)
        val cueOverlay=com.photocoach.app.analysis.OverlayGeometry(emptyList(),emptyList(),showSilhouette=false)
        // Populate the portrait recommendations before measuring menu bounds. Their extra
        // rows otherwise shift the button when the controlled face frame first arrives.
        instrumentation.runOnMainSync {vm.onFrame(cueSignals,cueOverlay)}
        SystemClock.sleep(350)
        click("美颜·关闭")
        val panelButton=locate("打开参数建议")
        val panelBounds=android.graphics.Rect().also(panelButton::getBoundsInScreen)
        instrumentation.runOnMainSync {vm.setVoiceEnabled(true);vm.selectIntent(com.photocoach.coach.ShotIntent.CLOSE_UP)}
        repeat(3) {index ->
            instrumentation.runOnMainSync {vm.onFrame(cueSignals,cueOverlay)}
            if(index<2)SystemClock.sleep(320)
        }
        awaitCondition(15000){activity.speechActive || vm.ui.value.ttsFailed}
        val speakingBefore=activity.speechActive
        val stoppedBefore=activity.stoppedWhileSpeaking
        // Recomposition replaces accessibility node ids during speech. Touch the bounds that
        // were obtained from the actual visible menu before triggering the controlled cue.
        touch(panelBounds)
        try {awaitCondition(5000){vm.ui.value.parameterPanelOpen && !activity.speechActive}}
        catch(error:Throwable) {
            evidence("activity-tts-touch.txt","bounds=$panelBounds; speakingBefore=$speakingBefore; speakingAfter=${activity.speechActive}; panelOpen=${vm.ui.value.parameterPanelOpen}; stoppedBefore=$stoppedBefore; stoppedAfter=${activity.stoppedWhileSpeaking}")
            instrumentation.uiAutomation.takeScreenshot()?.let {bitmap ->
                try {File(activity.getExternalFilesDir(null),"acceptance-evidence/activity-tts-touch.png").outputStream().use {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)
                }} finally {bitmap.recycle()}
            }
            throw error
        }
        if(speakingBefore)assertTrue("panel must actually stop active speech",activity.stoppedWhileSpeaking>stoppedBefore)
        else assertTrue("unavailable voice must be explicit",vm.ui.value.ttsFailed)
        val submitted=activity.submittedUtterances
        click("关闭");awaitCondition(5000){!vm.ui.value.parameterPanelOpen}
        SystemClock.sleep(500)
        assertEquals(submitted,activity.submittedUtterances);assertTrue(vm.ui.value.guidance.shutterEnabled)
        evidence("activity-tts.txt","cueInput=synthetic; speakingBeforePanel=$speakingBefore; stoppedBefore=$stoppedBefore; stoppedAfter=${activity.stoppedWhileSpeaking}; submitted=$submitted; ttsFailed=${vm.ui.value.ttsFailed}; speakingAfterClose=${activity.speechActive}")
    }
}
