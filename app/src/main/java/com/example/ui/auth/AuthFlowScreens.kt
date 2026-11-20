package com.example.ui.auth

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.credentials.CredentialManager
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val AuthCreamBackground = Color(0xFFF7F3EE)
private val AuthOrange = Color(0xFFE95726)
private val AuthDarkInk = Color(0xFF1A1D21)
private val AuthMutedInk = Color(0xFF5D646E)
private val AuthFieldBorder = Color(0xFFCBC6BF)
private val AuthFieldContainer = Color(0xFFFAF8F5)
private val AuthCardWhite = Color(0xFFFFFFFF)

enum class AuthStep {
    WELCOME,
    CREATE_ACCOUNT,
    SIGN_IN,
    VERIFY_EMAIL
}

data class PendingUnityIdDraft(
    val email: String = "",
    val username: String = "",
    val fullName: String = "",
    val emailVerified: Boolean = false
)

@Composable
fun AuthFlowScreen(
    onGoogleAuthComplete: (PendingUnityIdDraft) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var currentStep by rememberSaveable { mutableStateOf(AuthStep.WELCOME) }
    var previousStep by rememberSaveable { mutableStateOf(AuthStep.WELCOME) }

    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var fullName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var agreedToTerms by rememberSaveable { mutableStateOf(false) }

    var expectedCode by rememberSaveable { mutableStateOf("") }
    var enteredCode by rememberSaveable { mutableStateOf("") }
    var resendSecondsRemaining by rememberSaveable { mutableIntStateOf(42) }
    var isEmailCodeVerified by rememberSaveable { mutableStateOf(false) }

    var isGoogleLoading by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var showTermsDialog by remember { mutableStateOf(false) }

    if (currentStep != AuthStep.WELCOME) {
        BackHandler {
            authErrorMessage = null
            currentStep = when (currentStep) {
                AuthStep.VERIFY_EMAIL -> previousStep
                AuthStep.CREATE_ACCOUNT, AuthStep.SIGN_IN -> AuthStep.WELCOME
                AuthStep.WELCOME -> AuthStep.WELCOME
            }
        }
    }

    LaunchedEffect(currentStep, resendSecondsRemaining) {
        if (currentStep == AuthStep.VERIFY_EMAIL && resendSecondsRemaining > 0) {
            delay(1_000L)
            resendSecondsRemaining -= 1
        }
    }

    fun triggerGoogleSignIn(draft: PendingUnityIdDraft) {
        isGoogleLoading = true
        authErrorMessage = null
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isGoogleLoading = false
                onGoogleAuthComplete(draft)
            },
            onAuthError = { msg ->
                isGoogleLoading = false
                authErrorMessage = msg
            },
            scope = scope,
            onAuthCancelled = {
                isGoogleLoading = false
                authErrorMessage = "Google Sign-In was cancelled. Tap Google to try again."
            }
        )
    }

    fun showAppleComingSoon() {
        scope.launch {
            snackbarHostState.showSnackbar("Sign in with Apple is coming soon. Please continue with Google.")
        }
    }

    fun generateAndSendCode(targetEmail: String, returnStep: AuthStep) {
        expectedCode = Random.nextInt(100_000, 999_999).toString()
        enteredCode = ""
        isEmailCodeVerified = false
        resendSecondsRemaining = 42
        authErrorMessage = null
        previousStep = returnStep
        email = targetEmail.trim()
        currentStep = AuthStep.VERIFY_EMAIL
    }

    Scaffold(
        containerColor = AuthCreamBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AuthCreamBackground)
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedContent(
                targetState = currentStep,
                label = "auth_step_transition",
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxSize()
            ) { step ->
                when (step) {
                    AuthStep.WELCOME -> WelcomeAuthStep(
                        isGoogleLoading = isGoogleLoading,
                        errorMessage = authErrorMessage,
                        onCreateUnityIdClick = {
                            authErrorMessage = null
                            currentStep = AuthStep.CREATE_ACCOUNT
                        },
                        onSignInClick = {
                            authErrorMessage = null
                            currentStep = AuthStep.SIGN_IN
                        },
                        onGoogleClick = {
                            triggerGoogleSignIn(PendingUnityIdDraft())
                        },
                        onAppleClick = ::showAppleComingSoon
                    )

                    AuthStep.CREATE_ACCOUNT -> CreateAccountStep(
                        email = email,
                        username = username,
                        fullName = fullName,
                        password = password,
                        agreedToTerms = agreedToTerms,
                        isGoogleLoading = isGoogleLoading,
                        errorMessage = authErrorMessage,
                        onBack = {
                            authErrorMessage = null
                            currentStep = AuthStep.WELCOME
                        },
                        onEmailChange = { email = it },
                        onUsernameChange = { username = it },
                        onFullNameChange = { fullName = it },
                        onPasswordChange = { password = it },
                        onAgreedChange = { agreedToTerms = it },
                        onOpenTerms = { showTermsDialog = true },
                        onCreateAccountSubmit = {
                            val trimmedEmail = email.trim()
                            val trimmedUser = username.trim()
                            val trimmedName = fullName.trim()
                            when {
                                !trimmedEmail.contains("@") || !trimmedEmail.contains(".") -> {
                                    authErrorMessage = "Enter a valid email address."
                                }
                                trimmedUser.length < 2 -> {
                                    authErrorMessage = "Username must be at least 2 characters."
                                }
                                trimmedName.isBlank() -> {
                                    authErrorMessage = "Enter your full name."
                                }
                                password.length < 6 -> {
                                    authErrorMessage = "Password must be at least 6 characters."
                                }
                                !agreedToTerms -> {
                                    authErrorMessage = "Please agree to the Terms of Service and Privacy Policy."
                                }
                                else -> {
                                    generateAndSendCode(trimmedEmail, AuthStep.CREATE_ACCOUNT)
                                }
                            }
                        },
                        onGoogleClick = {
                            triggerGoogleSignIn(
                                PendingUnityIdDraft(
                                    email = email.trim(),
                                    username = username.trim(),
                                    fullName = fullName.trim(),
                                    emailVerified = false
                                )
                            )
                        },
                        onAppleClick = ::showAppleComingSoon
                    )

                    AuthStep.SIGN_IN -> SignInStep(
                        email = email,
                        isGoogleLoading = isGoogleLoading,
                        errorMessage = authErrorMessage,
                        onBack = {
                            authErrorMessage = null
                            currentStep = AuthStep.WELCOME
                        },
                        onEmailChange = { email = it },
                        onSendEmailCodeClick = {
                            val trimmedEmail = email.trim()
                            if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                                authErrorMessage = "Enter a valid email address to receive a code."
                            } else {
                                generateAndSendCode(trimmedEmail, AuthStep.SIGN_IN)
                            }
                        },
                        onGoogleClick = {
                            triggerGoogleSignIn(PendingUnityIdDraft(email = email.trim()))
                        },
                        onAppleClick = ::showAppleComingSoon,
                        onSwitchToCreate = {
                            authErrorMessage = null
                            currentStep = AuthStep.CREATE_ACCOUNT
                        }
                    )

                    AuthStep.VERIFY_EMAIL -> VerifyEmailStep(
                        email = email,
                        expectedCode = expectedCode,
                        enteredCode = enteredCode,
                        resendSecondsRemaining = resendSecondsRemaining,
                        isEmailCodeVerified = isEmailCodeVerified,
                        isGoogleLoading = isGoogleLoading,
                        errorMessage = authErrorMessage,
                        onBack = {
                            authErrorMessage = null
                            currentStep = previousStep
                        },
                        onCodeChange = { next ->
                            val digits = next.filter { it.isDigit() }.take(6)
                            enteredCode = digits
                            if (authErrorMessage != null) authErrorMessage = null
                        },
                        onAutoFillCode = {
                            enteredCode = expectedCode
                            authErrorMessage = null
                        },
                        onOpenEmailApp = {
                            val mailIntent = Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri()).apply {
                                putExtra(Intent.EXTRA_SUBJECT, "Unity ID Verification Code: $expectedCode")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Your 6-digit Unity Hub verification code is: $expectedCode"
                                )
                            }
                            runCatching { context.startActivity(mailIntent) }
                        },
                        onVerifyClick = {
                            if (enteredCode.length < 6) {
                                authErrorMessage = "Enter all 6 digits from your verification code."
                            } else if (enteredCode != expectedCode) {
                                authErrorMessage = "Incorrect code. Check the 6-digit code and try again."
                            } else {
                                authErrorMessage = null
                                isEmailCodeVerified = true
                                triggerGoogleSignIn(
                                    PendingUnityIdDraft(
                                        email = email.trim(),
                                        username = username.trim(),
                                        fullName = fullName.trim(),
                                        emailVerified = true
                                    )
                                )
                            }
                        },
                        onResendCodeClick = {
                            expectedCode = Random.nextInt(100_000, 999_999).toString()
                            enteredCode = ""
                            resendSecondsRemaining = 42
                            authErrorMessage = null
                        },
                        onContinueWithGoogle = {
                            triggerGoogleSignIn(
                                PendingUnityIdDraft(
                                    email = email.trim(),
                                    username = username.trim(),
                                    fullName = fullName.trim(),
                                    emailVerified = true
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service & Privacy") },
            text = {
                Text(
                    "Your Unity ID lets you manage local projects, tutorials, and account profile details across Unity Hub. Google Sign-In secures your session via Firebase Authentication.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        agreedToTerms = true
                        showTermsDialog = false
                    }
                ) {
                    Text("Accept", color = AuthOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun WelcomeAuthStep(
    isGoogleLoading: Boolean,
    errorMessage: String?,
    onCreateUnityIdClick: () -> Unit,
    onSignInClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp)
            .testTag("auth_welcome_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.unity_logo),
            contentDescription = stringResource(R.string.cd_unity_hub_logo),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(64.dp)
                .testTag("auth_welcome_unity_logo")
        )

        Spacer(modifier = Modifier.height(34.dp))

        Text(
            text = stringResource(R.string.auth_welcome_headline),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 27.sp,
                lineHeight = 33.sp
            ),
            color = AuthDarkInk,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.auth_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = AuthMutedInk,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(38.dp))

        Button(
            onClick = onCreateUnityIdClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthOrange,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("welcome_create_unity_id_button")
        ) {
            Text(
                text = stringResource(R.string.auth_create_unity_id),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onSignInClick,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.25.dp, AuthDarkInk),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = AuthCardWhite,
                contentColor = AuthDarkInk
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("welcome_sign_in_button")
        ) {
            Text(
                text = stringResource(R.string.auth_sign_in),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        OrContinueDivider()

        Spacer(modifier = Modifier.height(16.dp))

        SocialAuthButtons(
            isGoogleLoading = isGoogleLoading,
            onGoogleClick = onGoogleClick,
            onAppleClick = onAppleClick
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(14.dp))
            AuthErrorBanner(message = errorMessage)
        }
    }
}

@Composable
private fun CreateAccountStep(
    email: String,
    username: String,
    fullName: String,
    password: String,
    agreedToTerms: Boolean,
    isGoogleLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAgreedChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit,
    onCreateAccountSubmit: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .testTag("auth_create_account_screen")
    ) {
        AuthTopBar(
            title = stringResource(R.string.auth_create_account_title),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(10.dp))

        Image(
            painter = painterResource(id = R.drawable.unity_logo),
            contentDescription = stringResource(R.string.cd_unity_hub_logo),
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(34.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.auth_create_unity_id_heading),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
            ),
            color = AuthDarkInk
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.auth_create_unity_id_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = AuthMutedInk
        )

        Spacer(modifier = Modifier.height(18.dp))

        UnityOutlinedField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "alex.chen@unity.com",
            keyboardType = KeyboardType.Email,
            testTag = "create_email_input"
        )

        Spacer(modifier = Modifier.height(12.dp))

        UnityOutlinedField(
            value = username,
            onValueChange = onUsernameChange,
            label = "Username",
            placeholder = "alexchen",
            testTag = "create_username_input"
        )

        Spacer(modifier = Modifier.height(12.dp))

        UnityOutlinedField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = "Full name",
            placeholder = "Alex Chen",
            testTag = "create_fullname_input"
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            placeholder = { Text("At least 6 characters") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = AuthMutedInk
                    )
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = unityFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_password_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onAgreedChange(!agreedToTerms) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = agreedToTerms,
                onCheckedChange = onAgreedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = AuthOrange,
                    uncheckedColor = AuthMutedInk,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.testTag("create_terms_checkbox")
            )
            Spacer(modifier = Modifier.width(6.dp))
            val termsAnnotated = buildAnnotatedString {
                append("I agree to the ")
                withStyle(
                    SpanStyle(
                        color = AuthOrange,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("Terms of Service")
                }
                append(" and ")
                withStyle(
                    SpanStyle(
                        color = AuthOrange,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("Privacy Policy")
                }
            }
            Text(
                text = termsAnnotated,
                style = MaterialTheme.typography.bodyMedium,
                color = AuthDarkInk,
                modifier = Modifier.clickable(onClick = onOpenTerms)
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            AuthErrorBanner(message = errorMessage)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCreateAccountSubmit,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthOrange,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("create_unity_id_submit_button")
        ) {
            Text(
                text = stringResource(R.string.auth_create_unity_id),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        OrContinueDivider()

        Spacer(modifier = Modifier.height(14.dp))

        SocialAuthButtons(
            isGoogleLoading = isGoogleLoading,
            onGoogleClick = onGoogleClick,
            onAppleClick = onAppleClick
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SignInStep(
    email: String,
    isGoogleLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onSendEmailCodeClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onSwitchToCreate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .testTag("auth_sign_in_screen")
    ) {
        AuthTopBar(
            title = stringResource(R.string.auth_sign_in),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.unity_logo),
            contentDescription = stringResource(R.string.cd_unity_hub_logo),
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(38.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sign in to Unity ID",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = AuthDarkInk
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Sign in quickly with Google, or receive a 6-digit verification code by email.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = AuthMutedInk
        )

        Spacer(modifier = Modifier.height(22.dp))

        SocialAuthButtons(
            isGoogleLoading = isGoogleLoading,
            googleLabel = "Sign in with Google",
            onGoogleClick = onGoogleClick,
            onAppleClick = onAppleClick
        )

        Spacer(modifier = Modifier.height(22.dp))

        OrContinueDivider(label = "or sign in with email code")

        Spacer(modifier = Modifier.height(18.dp))

        UnityOutlinedField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "alex@studio.dev",
            keyboardType = KeyboardType.Email,
            testTag = "sign_in_email_input"
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            AuthErrorBanner(message = errorMessage)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSendEmailCodeClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthOrange,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("send_email_code_button")
        ) {
            Text(
                text = "Send 6-digit code",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New to Unity?",
                style = MaterialTheme.typography.bodyMedium,
                color = AuthMutedInk
            )
            TextButton(onClick = onSwitchToCreate) {
                Text(
                    text = "Create Unity ID",
                    color = AuthOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun VerifyEmailStep(
    email: String,
    expectedCode: String,
    enteredCode: String,
    resendSecondsRemaining: Int,
    isEmailCodeVerified: Boolean,
    isGoogleLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onCodeChange: (String) -> Unit,
    onAutoFillCode: () -> Unit,
    onOpenEmailApp: () -> Unit,
    onVerifyClick: () -> Unit,
    onResendCodeClick: () -> Unit,
    onContinueWithGoogle: () -> Unit
) {
    val formattedTimer = remember(resendSecondsRemaining) {
        val mins = resendSecondsRemaining / 60
        val secs = resendSecondsRemaining % 60
        "$mins:${secs.toString().padStart(2, '0')}"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("auth_verify_email_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(
            title = stringResource(R.string.auth_verify_email_title),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(26.dp))

        Surface(
            color = AuthCardWhite,
            shape = RoundedCornerShape(22.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.MailOutline,
                    contentDescription = null,
                    tint = AuthDarkInk,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.auth_check_inbox),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = AuthDarkInk,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "We sent a 6-digit code to\n$email",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.5.sp),
            color = AuthMutedInk,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(26.dp))

        OtpSixDigitField(
            code = enteredCode,
            onCodeChange = onCodeChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            color = AuthCardWhite,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AuthFieldBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Verification code: $expectedCode",
                        style = MaterialTheme.typography.labelMedium,
                        color = AuthDarkInk
                    )
                    Text(
                        text = "Tap Fill or open your mail app",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuthMutedInk
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = onAutoFillCode,
                        modifier = Modifier.testTag("autofill_code_button")
                    ) {
                        Text("Fill", color = AuthOrange, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = onOpenEmailApp) {
                        Text("Email", color = AuthDarkInk)
                    }
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            AuthErrorBanner(message = errorMessage)
        }

        AnimatedVisibility(visible = isEmailCodeVerified) {
            Surface(
                color = Color(0xFFD1FAE5),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF059669)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Email verified! Complete sign-in with Google to enter Unity Hub.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF065F46)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isEmailCodeVerified) {
            Button(
                onClick = onVerifyClick,
                enabled = !isGoogleLoading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuthOrange,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("verify_email_submit_button")
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.auth_verify),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        } else {
            Button(
                onClick = onContinueWithGoogle,
                enabled = !isGoogleLoading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuthOrange,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("verify_continue_google_button")
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    GoogleGLogo(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (resendSecondsRemaining > 0) {
            Text(
                text = "Resend code in $formattedTimer",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AuthOrange
            )
        } else {
            TextButton(
                onClick = onResendCodeClick,
                modifier = Modifier.testTag("resend_code_button")
            ) {
                Text(
                    text = "Resend code",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AuthOrange
                )
            }
        }
    }
}

@Composable
private fun OtpSixDigitField(
    code: String,
    onCodeChange: (String) -> Unit
) {
    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("verify_otp_input"),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                repeat(6) { index ->
                    val char = code.getOrNull(index)?.toString() ?: ""
                    val isFocused = index == code.length.coerceAtMost(5)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AuthFieldContainer)
                            .border(
                                width = if (isFocused) 1.75.dp else 1.dp,
                                color = if (isFocused) AuthOrange else AuthDarkInk.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = AuthDarkInk
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun AuthTopBar(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("auth_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = AuthDarkInk
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = AuthDarkInk
        )
    }
}

@Composable
private fun UnityOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = unityFieldColors(),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

@Composable
private fun unityFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = AuthFieldContainer,
    unfocusedContainerColor = AuthFieldContainer,
    focusedBorderColor = AuthOrange,
    unfocusedBorderColor = AuthFieldBorder,
    focusedLabelColor = AuthOrange,
    unfocusedLabelColor = AuthMutedInk,
    focusedTextColor = AuthDarkInk,
    unfocusedTextColor = AuthDarkInk
)

@Composable
private fun OrContinueDivider(label: String = stringResource(R.string.auth_or_continue_with)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = AuthFieldBorder
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AuthMutedInk,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = AuthFieldBorder
        )
    }
}

@Composable
private fun SocialAuthButtons(
    isGoogleLoading: Boolean,
    googleLabel: String = stringResource(R.string.auth_google),
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onGoogleClick,
            enabled = !isGoogleLoading,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.25.dp, AuthDarkInk.copy(alpha = 0.75f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = AuthCardWhite,
                contentColor = AuthDarkInk
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("google_sign_in_button")
        ) {
            if (isGoogleLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AuthOrange,
                    strokeWidth = 2.dp
                )
            } else {
                GoogleGLogo(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = googleLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        OutlinedButton(
            onClick = onAppleClick,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.25.dp, AuthFieldBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = AuthFieldContainer,
                contentColor = AuthDarkInk
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("apple_coming_soon_button")
        ) {
            AppleMarkLogo(
                tint = AuthDarkInk,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.auth_apple),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                color = Color(0xFFE7E2DC),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = stringResource(R.string.auth_coming_soon),
                    style = MaterialTheme.typography.labelSmall,
                    color = AuthMutedInk,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun AuthErrorBanner(message: String) {
    Surface(
        color = Color(0xFFFEE2E2),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auth_error_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Color(0xFFB91C1C),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF991B1B)
            )
        }
    }
}

@Composable
private fun GoogleGLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.21f
        val inset = strokeWidth / 2f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)

        drawArc(
            color = Color(0xFFEA4335),
            startAngle = -145f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -5f,
            sweepAngle = 55f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(size.width * 0.52f, size.height * 0.5f),
            end = Offset(size.width * 0.94f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Butt
        )
    }
}

@Composable
private fun AppleMarkLogo(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val appleBody = Path().apply {
            moveTo(w * 0.78f, h * 0.54f)
            cubicTo(w * 0.78f, h * 0.41f, w * 0.89f, h * 0.35f, w * 0.90f, h * 0.34f)
            cubicTo(w * 0.83f, h * 0.25f, w * 0.73f, h * 0.23f, w * 0.69f, h * 0.23f)
            cubicTo(w * 0.60f, h * 0.22f, w * 0.52f, h * 0.28f, w * 0.48f, h * 0.28f)
            cubicTo(w * 0.44f, h * 0.28f, w * 0.37f, h * 0.23f, w * 0.30f, h * 0.23f)
            cubicTo(w * 0.21f, h * 0.23f, w * 0.12f, h * 0.29f, w * 0.08f, h * 0.37f)
            cubicTo(w * -0.02f, h * 0.53f, w * 0.05f, h * 0.77f, w * 0.14f, h * 0.90f)
            cubicTo(w * 0.19f, h * 0.96f, w * 0.25f, h * 1.00f, w * 0.32f, h * 0.99f)
            cubicTo(w * 0.38f, h * 0.99f, w * 0.41f, h * 0.95f, w * 0.49f, h * 0.95f)
            cubicTo(w * 0.57f, h * 0.95f, w * 0.59f, h * 0.99f, w * 0.66f, h * 0.99f)
            cubicTo(w * 0.73f, h * 0.99f, w * 0.78f, h * 0.93f, w * 0.83f, h * 0.86f)
            cubicTo(w * 0.88f, h * 0.79f, w * 0.91f, h * 0.71f, w * 0.91f, h * 0.71f)
            cubicTo(w * 0.91f, h * 0.70f, w * 0.78f, h * 0.66f, w * 0.78f, h * 0.54f)
            close()
        }
        val leaf = Path().apply {
            moveTo(w * 0.63f, h * 0.17f)
            cubicTo(w * 0.67f, h * 0.12f, w * 0.70f, h * 0.06f, w * 0.69f, h * 0.00f)
            cubicTo(w * 0.63f, h * 0.00f, w * 0.56f, h * 0.04f, w * 0.52f, h * 0.09f)
            cubicTo(w * 0.49f, h * 0.13f, w * 0.46f, h * 0.20f, w * 0.47f, h * 0.25f)
            cubicTo(w * 0.53f, h * 0.26f, w * 0.59f, h * 0.22f, w * 0.63f, h * 0.17f)
            close()
        }
        drawPath(appleBody, color = tint)
        drawPath(leaf, color = tint)
    }
}
