package dev.cemil.authwithdevicebiometricpinorpassword

import android.app.KeyguardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.Executor
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var homeContext: Context
    private lateinit var homeActivity: AppCompatActivity
    var fingerAttempts = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        homeContext = this
        homeActivity = this
        setProps()
        stat_biometric.text = availableBiometric().toString().toUpperCase(Locale.getDefault())
        stat_finger.text = availableBiometric().toString().toUpperCase(Locale.getDefault())
        stat_pin.text = availableDevicePin().toString().toUpperCase(Locale.getDefault())

        if(availableBiometric()) {
            //user can use phone PIN instead finger?
            val allowWithPhoneCredential = Random().nextBoolean()
            promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
                setTitle("Biometric Authentication")
                setSubtitle("Use your fingerprint to authenticate")
                setDescription("You can authenticate with your biometrics ${if (allowWithPhoneCredential) "also with Device PIN/Password" else ""}")
                setDeviceCredentialAllowed(allowWithPhoneCredential)
                setNegativeButtonText(if(allowWithPhoneCredential) "" else "CANCEL")
            }.build()
            btn_authenticate.isEnabled = availableBiometric()
            btn_authenticate.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }
        }

    }

    private fun availableBiometric() : Boolean {
        return BiometricManager.from(homeContext).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun availableDevicePin() : Boolean {
        return (homeContext.getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardSecure
    }

    private fun setProps() {
        executor = ContextCompat.getMainExecutor(homeContext)
        biometricPrompt = BiometricPrompt(homeActivity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        //if result is not 0 operation is failed
                        if (errorCode > 0) Toast.makeText(homeContext, "$errString $errorCode", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(homeContext, "Success", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        fingerAttempts--
                        Toast.makeText(homeContext, "Try again \n $fingerAttempts attempts left", Toast.LENGTH_SHORT).show()
                    }
                })
    }
}