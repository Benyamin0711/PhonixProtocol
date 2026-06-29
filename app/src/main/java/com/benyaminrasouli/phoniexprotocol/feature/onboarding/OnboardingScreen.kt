package com.benyaminrasouli.phoniexprotocol.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.benyaminrasouli.phoniexprotocol.core.navigation.Screen
import com.benyaminrasouli.phoniexprotocol.core.ui.components.PhoenixButton
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark

@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    0 -> LanguageStep(
                        selectedLanguage = state.language,
                        onLanguageSelected = viewModel::setLanguage
                    )
                    1 -> ProfileStep(
                        fullName = state.fullName,
                        username = state.username,
                        birthYear = state.birthYear,
                        onFullNameChange = viewModel::setFullName,
                        onUsernameChange = viewModel::setUsername,
                        onBirthYearChange = viewModel::setBirthYear
                    )
                    2 -> IdentityStep(
                        selectedPath = state.identityPath,
                        onPathSelected = viewModel::setIdentityPath
                    )
                    3 -> ReviewStep(
                        fullName = state.fullName,
                        username = state.username,
                        birthYear = state.birthYear,
                        identityPath = state.identityPath,
                        language = state.language
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Step indicator
            StepIndicator(
                currentStep = state.currentStep,
                totalSteps = 4,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Navigation buttons
            if (state.currentStep > 0) {
                PhoenixButton(
                    text = "Back",
                    onClick = viewModel::previousStep,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            PhoenixButton(
                text = if (state.currentStep == 3) "Complete Setup" else "Next",
                onClick = {
                    if (state.currentStep == 3) {
                        viewModel.saveProfile {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    } else {
                        viewModel.nextStep()
                    }
                },
                enabled = when (state.currentStep) {
                    0 -> true
                    1 -> state.fullName.isNotBlank() && state.username.isNotBlank()
                    2 -> state.identityPath.isNotBlank()
                    3 -> !state.isSaving
                    else -> false
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = if (index <= currentStep)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.extraSmall
                    )
            )
        }
    }
}
