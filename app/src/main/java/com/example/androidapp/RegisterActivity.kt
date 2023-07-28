package com.example.androidapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.androidapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val networkManager: NetworkManager = NetworkManager.initialize(this) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.signInHint.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.register.setOnClickListener {
            networkManager.signUp(
                binding.email.editText?.text.toString(),
                binding.username.editText?.text.toString(),
                binding.password.editText?.text.toString(),
                {
                    networkManager.register {
                        runOnUiThread {
                            binding.registerError.visibility = View.GONE

                            binding.register.text = "..."
                            val editText = EditText(this)
                            val dialog = AlertDialog.Builder(this)
                                .setTitle("Verification Code")
                                .setMessage("You will receive an email shortly. Please enter the verification code we sent you.")
                                .setView(editText)
                                .setPositiveButton("Confirm") { _, _ ->
                                    val verificationCode = editText.text.toString()
                                    networkManager.confirmSignUp(
                                        binding.username.editText?.text.toString(),
                                        verificationCode
                                    ) {
                                        networkManager.signIn(
                                            binding.username.editText?.text.toString(),
                                            binding.password.editText?.text.toString(),
                                            {
                                                val intent = Intent(this, MainActivity::class.java)
                                                startActivity(intent)
                                            },
                                            {}
                                        )
                                    }
                                }
                                .setNegativeButton("Cancel", null)
                                .create()
                            dialog.show()
                        }
                    }
                },
                {
                    runOnUiThread {
                        binding.registerError.visibility = View.VISIBLE
                        binding.registerError.text = it.recoverySuggestion
                    }
                }
            )
        }
    }
}