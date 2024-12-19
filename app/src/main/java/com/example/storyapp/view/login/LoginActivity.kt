package com.example.storyapp.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.databinding.ActivityLoginBinding
import com.example.storyapp.view.ViewModelFactory
import com.example.storyapp.view.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAction()
        playAnimation()
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            viewModel.loginUser(email, password)

            viewModel.login.observe(this) { response ->
                binding.progressBar.visibility = View.GONE
                if (response.error != true) {
                    val token = response.loginResult?.token
                    val name = response.loginResult?.name
                    val user = token?.let { name?.let { name -> UserModel(name, email, it, true) } }
                    user?.let { it1 -> viewModel.saveSession(it1) }
                    Toast.makeText(this@LoginActivity, response.message, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.ivIllustration, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.tvLoginTitle, View.ALPHA, 1f).setDuration(200)
        val message = ObjectAnimator.ofFloat(binding.tvMessage, View.ALPHA, 1f).setDuration(200)
        val emailTextView = ObjectAnimator.ofFloat(binding.tvTitleEmail, View.ALPHA, 1f).setDuration(200)
        val emailEditTextLayout = ObjectAnimator.ofFloat(binding.tlLoginEmail, View.ALPHA, 1f).setDuration(200)
        val passwordTextView = ObjectAnimator.ofFloat(binding.tvTitlePassword, View.ALPHA, 1f).setDuration(200)
        val passwordEditTextLayout = ObjectAnimator.ofFloat(binding.tlLoginPassword, View.ALPHA, 1f).setDuration(200)
        val loginButton = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(200)

        AnimatorSet().apply {
            playSequentially(
                title,
                message,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                loginButton
            )
            start()
        }
    }
}