package com.example.androidapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.androidapp.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    val networkManager: NetworkManager = NetworkManager.initialize(this) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        binding = ActivitySignInBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.registerHint.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.signIn.setOnClickListener {
            networkManager.signIn(
                binding.username.editText?.text.toString(),
                binding.password.editText?.text.toString(),
                {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    binding.signInError.visibility = View.GONE
                },
                {
                    runOnUiThread {
                        binding.signInError.visibility = View.VISIBLE
                        binding.signInError.text = it.recoverySuggestion
                    }
                }
            )
        }
    }
}