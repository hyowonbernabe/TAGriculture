package com.example.tagriculture.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tagriculture.OnboardingSlideFragment
import com.example.tagriculture.R

class OnboardingAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val slides = listOf(
        OnboardingSlideFragment.newInstance(
            "Welcome to TAGriculture",
            "Farmers today still rely on paper to track livestock records. This is time-consuming, error-prone, and difficult to manage. TAGriculture modernizes this process by moving record-keeping into a simple, digital system.",
            R.drawable.onboarding_slide_1
        ),
        OnboardingSlideFragment.newInstance(
            "Smart Tracking with NFC",
            "With TAGriculture, each animal’s RFID/NFC ear tag becomes its digital identity. Simply scan the tag with your phone to instantly view, register, or update livestock information.",
            R.drawable.onboarding_slide_2
        ),
        OnboardingSlideFragment.newInstance(
            "Easy Records & Insights",
            "Easily maintain vaccination records, certificates, and growth data. TAGriculture turns updates into clear visual insights, so farmers can monitor livestock progress and health over time.",
            R.drawable.onboarding_slide_3
        ),
        OnboardingSlideFragment.newInstance(
            "Your Farm in Your Pocket",
            "All your livestock, organized in one place. TAGriculture gives farmers a simple, structured way to view, edit, and manage their herd from a single app.",
            R.drawable.onboarding_slide_4
        )
    )

    override fun getItemCount(): Int = slides.size

    override fun createFragment(position: Int): Fragment = slides[position]
}