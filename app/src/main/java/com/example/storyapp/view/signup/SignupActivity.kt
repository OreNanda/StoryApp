package com.example.storyapp.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.storyapp.databinding.ActivitySignupBinding
import com.example.storyapp.view.ViewModelFactory

class SignupActivity : AppCompatActivity() {
    private val viewModel by viewModels<SignupViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAction()
        playAnimation()
    }

    private fun setupAction() {
        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Password minimal 8 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            viewModel.registerUser(name, email, password)
        }

        viewModel.register.observe(this) { response ->
            binding.progressBar.visibility = View.GONE
            if (response != null) {
                Toast.makeText(this@SignupActivity, "Berhasil: ${response.message}", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@SignupActivity, "Gagal mendaftar. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.ivIllustration, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.tvRegisterTitle, View.ALPHA, 1f).setDuration(200)
        val nameTextView = ObjectAnimator.ofFloat(binding.tvRegisterName, View.ALPHA, 1f).setDuration(200)
        val nameEditTextLayout = ObjectAnimator.ofFloat(binding.tlRegisterName, View.ALPHA, 1f).setDuration(200)
        val emailTextView = ObjectAnimator.ofFloat(binding.tvRegisterEmail, View.ALPHA, 1f).setDuration(200)
        val emailEditTextLayout = ObjectAnimator.ofFloat(binding.tlRegisterEmail, View.ALPHA, 1f).setDuration(200)
        val passwordTextView = ObjectAnimator.ofFloat(binding.tvRegisterPassword, View.ALPHA, 1f).setDuration(200)
        val passwordEditTextLayout = ObjectAnimator.ofFloat(binding.tlRegisterPassword, View.ALPHA, 1f).setDuration(200)
        val loginButton = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(200)

        AnimatorSet().apply {
            playSequentially(
                title,
                nameTextView,
                nameEditTextLayout,
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