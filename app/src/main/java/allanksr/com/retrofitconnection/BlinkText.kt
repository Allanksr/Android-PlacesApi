package allanksr.com.retrofitconnection

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.core.graphics.ColorUtils

class BlinkText(context: Context) {
    var appContext = context
    fun blinkTextInView(textView: TextView){
        val valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        valueAnimator.duration = 500
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.repeatMode = ValueAnimator.REVERSE
        valueAnimator.addUpdateListener { it ->
            val fractionAnim = it.animatedValue as Float
            textView.visibility = View.VISIBLE
            textView.setTextColor(
                    ColorUtils.blendARGB(Color.parseColor("#00cc00"),
                            appContext.resources.getColor(R.color.transparent), fractionAnim))
        }
        valueAnimator.start()



        textView.setOnClickListener{
            valueAnimator.cancel()
            textView.setTextColor(Color.parseColor("#00cc00"))
        }
    }
}