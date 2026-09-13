package com.photocoach.app
import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import com.photocoach.app.beauty.BeautyPreset
import org.junit.Assert.*
import org.junit.Test
class BuildVariantAcceptanceTest {
    @Test fun installedBuildMatchesRequestedResearchVariantAndIsolatesCreativeSettings() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val args=InstrumentationRegistry.getArguments()
        val condition=BuildConfig::class.java.getField("RESEARCH_CONDITION").get(null) as String
        val scene=BuildConfig::class.java.getField("RESEARCH_SCENE").get(null) as String
        args.getString("expectedCondition")?.let {assertEquals(it,condition)}
        args.getString("expectedScene")?.let {assertEquals(it,scene)}
        val store=ViewModelStore()
        try {instrumentation.runOnMainSync {
            val vm=AppViewModel(instrumentation.targetContext.applicationContext as Application);store.put("vm",vm)
            assertEquals(condition!="NONE",vm.ui.value.researchMode)
            if(condition!="NONE") {
                vm.setThreeShotBurstEnabled(true);vm.setLivePhotoEnabled(true);vm.setBeautyPreset(BeautyPreset.NATURAL)
                assertFalse(vm.ui.value.threeShotBurstEnabled);assertFalse(vm.ui.value.livePhotoEnabled)
                assertEquals(BeautyPreset.OFF,vm.ui.value.beautyPreset)
            }
        }} finally {instrumentation.runOnMainSync {store.clear()}}
    }
}
