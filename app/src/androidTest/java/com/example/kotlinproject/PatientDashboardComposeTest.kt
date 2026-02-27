package com.example.kotlinproject

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PatientDashboardComposeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<PatientDashboard>()

    @Test
    fun test_patient_dashboard_wrong_title_failure() {
        // This test is designed to FAIL.
        // The actual title is "MedSathi", but we are asserting "Admin Panel".
        composeRule.onNodeWithText("Admin Panel")
            .assertIsDisplayed()
    }

    @Test
    fun test_patient_dashboard_missing_element_failure() {
        // This test is also designed to FAIL.
        // We are looking for a "Delete Account" button that doesn't exist on the dashboard.
        composeRule.onNodeWithText("Delete Account")
            .assertIsDisplayed()
    }
}
