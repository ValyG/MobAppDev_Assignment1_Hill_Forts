package org.wit.placemark.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.jetbrains.anko.intentFor
import org.wit.placemark.R
import java.util.*
import kotlin.concurrent.schedule

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Timer().schedule(3000){
            this@SplashScreenActivity.finish()
            startActivity(intentFor<PlacemarkListActivity>())
        }
    }
}
