package com.example.kotlinproject

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminDashboardComposeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<AdminDashboard>()

    @Test
    fun admin_dashboard_components_load() {
        // Wait for the Dashboard title to appear (gives time for initial composition and potential data fetch)
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithText("MedSathi - Admin").fetchSemanticsNodes().isNotEmpty()
        }

        // 1. Check Top Bar Title
        composeRule.onNodeWithText("MedSathi - Admin").assertIsDisplayed()

        // 2. Check Menu Icon
        composeRule.onNodeWithContentDescription("Menu").assertIsDisplayed()

        // 3. Check Welcome Card
        composeRule.onNodeWithText("Welcome back,").assertIsDisplayed()

        // 4. Check Statistics Headers
        composeRule.onNodeWithText("Total Patients").assertIsDisplayed()
        composeRule.onNodeWithText("Total Staffs").assertIsDisplayed()
    }

    @Test
    fun test_drawer_opens() {
        // Wait for screen to be ready and click Menu
        composeRule.onNodeWithContentDescription("Menu").performClick()
        
        // Wait for drawer animation
        composeRule.waitForIdle()

        // Check if drawer items appear
        composeRule.onNodeWithText("Administrator").assertIsDisplayed()
        
        // "Dashboard" exists in the drawer and potentially as part of "Admin Dashboard" title, 
        // so we use onFirst() for robustness.
        composeRule.onAllNodesWithText("Dashboard").onFirst().assertIsDisplayed()
        composeRule.onNodeWithText("Manage Patients").assertIsDisplayed()
        composeRule.onNodeWithText("Manage Staffs").assertIsDisplayed()
    }

    @Test
    fun test_profile_icon_click() {
        // Ambiguity fix: Both the TopAppBar and WelcomeCard have a "Profile" content description icon.
        // We target the first one (TopAppBar) to ensure the click is valid.
        composeRule.onAllNodesWithContentDescription("Profile")
            .onFirst()
            .assertIsDisplayed()
            .performClick()
    }
}
