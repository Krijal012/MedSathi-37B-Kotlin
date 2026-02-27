package com.example.kotlinproject

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminDashboardComposeTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private fun launchActivity() {
        val intent = Intent(
            androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation().targetContext,
            AdminDashboard::class.java
        )
        ActivityScenario.launch<AdminDashboard>(intent)
    }

    @Test
    fun admin_dashboard_title_is_displayed() {
        launchActivity()
        composeRule.onNodeWithText("MedSathi - Admin")
            .assertIsDisplayed()
    }

    @Test
    fun menu_icon_is_clickable() {
        launchActivity()
        composeRule.onNodeWithContentDescription("Menu")
            .performClick()
    }

    @Test
    fun welcome_card_is_displayed() {
        launchActivity()
        composeRule.onNodeWithText("Welcome back,")
            .assertIsDisplayed()
    }

    @Test
    fun stats_cards_are_displayed() {
        launchActivity()
        composeRule.onNodeWithText("Total Patients").assertIsDisplayed()
        composeRule.onNodeWithText("Total Staffs").assertIsDisplayed()
    }

    @Test
    fun profile_icon_is_displayed() {
        launchActivity()
        composeRule.onNodeWithContentDescription("Profile")
            .assertIsDisplayed()
    }
}
