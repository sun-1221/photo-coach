package com.photocoach.app.camera

import java.io.IOException
import java.util.concurrent.Executor

internal fun dispatchMotionCompletion(executor:Executor,budget:MotionSessionBudget,now:()->Long,
    current:()->Boolean,result:Result<FinalizedMotionVideo>,consume:(Result<FinalizedMotionVideo>)->Unit) {
    executor.execute {
        if(!current()){result.getOrNull()?.file?.delete();return@execute}
        val accepted=if(result.isSuccess && budget.expired(now())) {
            result.getOrNull()?.file?.delete()
            Result.failure(IOException("Live 编码完成超过本代期限"))
        } else result
        consume(accepted)
    }
}
