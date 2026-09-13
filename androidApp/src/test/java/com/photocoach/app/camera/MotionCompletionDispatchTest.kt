package com.photocoach.app.camera
import java.nio.file.Files
import java.util.concurrent.Executor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MotionCompletionDispatchTest {
    private fun video()=FinalizedMotionVideo(Files.createTempFile("motion-dispatch-",".mp4").toFile(),
        VerifiedMotionTimeline(1,MotionSensorSource("camera",1),emptyList()))
    @Test fun completionQueuedBeforeDeadlineIsRejectedWhenConsumedAfterIt() {
        val queue=mutableListOf<Runnable>();val executor=Executor(queue::add)
        val budget=MotionSessionBudget(1000);budget.ready(2000);budget.finishing(3000)
        var now=14999L;val video=video();var consumed:Result<FinalizedMotionVideo>?=null
        dispatchMotionCompletion(executor,budget,{now},{true},Result.success(video)){consumed=it}
        assertNull(consumed);assertTrue(video.file.exists())
        now=15000;queue.single().run()
        assertTrue(consumed!!.isFailure);assertFalse(video.file.exists())
    }
    @Test fun queuedOldGenerationCleansOnlyItsCompletedFileWithoutDelivery() {
        val queue=mutableListOf<Runnable>();val budget=MotionSessionBudget(1000);budget.ready(2000)
        val video=video();var current=true;var delivered=false
        dispatchMotionCompletion(Executor(queue::add),budget,{2500},{current},Result.success(video)){delivered=true}
        current=false;queue.single().run()
        assertFalse(delivered);assertFalse(video.file.exists())
    }
}
