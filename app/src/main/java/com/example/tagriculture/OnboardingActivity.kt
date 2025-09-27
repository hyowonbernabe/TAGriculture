package com.example.tagriculture

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.tagriculture.adapters.OnboardingAdapter

class OnboardingActivity : AppCompatActivity() {

    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var indicatorContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        val nextButton: Button = findViewById(R.id.btn_next)
        val skipButton: Button = findViewById(R.id.btn_skip)
        indicatorContainer = findViewById(R.id.indicator_container)

        onboardingAdapter = OnboardingAdapter(this)
        viewPager.adapter = onboardingAdapter

        setupIndicatorDots()
        updateIndicatorDots(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicatorDots(position)
                if (position == onboardingAdapter.itemCount - 1) {
                    nextButton.text = "Get Started"
                } else {
                    nextButton.text = "Next"
                }
            }
        })

        nextButton.setOnClickListener {
            if (viewPager.currentItem < onboardingAdapter.itemCount - 1) {
                viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }
        skipButton.setOnClickListener { finishOnboarding() }
    }

    private fun setupIndicatorDots() {
        val dots = arrayOfNulls<ImageView>(onboardingAdapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val margin = (4 * resources.displayMetrics.density).toInt()
        layoutParams.setMargins(margin, 0, margin, 0)

        for (i in dots.indices) {
            dots[i] = ImageView(applicationContext)
            dots[i]?.let {
                it.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.dot_unselected))
                it.layoutParams = layoutParams
                indicatorContainer.addView(it)
            }
        }
    }

    private fun updateIndicatorDots(position: Int) {
        val childCount = indicatorContainer.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorContainer.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.dot_selected))
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.dot_unselected))
            }
        }
    }

    private fun finishOnboarding() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_complete", true).apply()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}