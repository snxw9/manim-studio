package com.manimstudio.app

import android.app.Application
import com.manimstudio.app.engine.ManimRenderer
import com.manimstudio.app.engine.ProotEngine

class ManimStudioApp : Application() {
    lateinit var engine: ProotEngine
    lateinit var renderer: ManimRenderer

    override fun onCreate() {
        super.onCreate()
        engine = ProotEngine(this)
        renderer = ManimRenderer(engine)
    }
}
