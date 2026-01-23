package pt.ipt.dam2025.iptfit.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import pt.ipt.dam2025.iptfit.IPTFitApplication
import pt.ipt.dam2025.iptfit.R
import pt.ipt.dam2025.iptfit.data.local.entity.User
import pt.ipt.dam2025.iptfit.databinding.ActivityLoginBinding
import pt.ipt.dam2025.iptfit.ui.main.MainActivity
import java.security.MessageDigest

/**
 * Activity de Login e Registo
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((application as IPTFitApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar se já está autenticado
        val app = application as IPTFitApplication
        if (app.sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (validateLogin(username, password)) {
                val hashedPassword = hashPassword(password)
                viewModel.login(username, hashedPassword)
            }
        }

        binding.tvRegister.setOnClickListener {
            showRegisterDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE

            result.onSuccess { user ->
                // Guardar sessão
                val app = application as IPTFitApplication
                app.sessionManager.saveUserSession(user.id, user.username, user.name)

                Toast.makeText(this, "Bem-vindo, ${user.name}!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }

            result.onFailure { error ->
                binding.tilPassword.error = error.message
            }
        }

        viewModel.registerResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE

            result.onSuccess {
                Toast.makeText(this, "Registo efetuado com sucesso! Faça login.", Toast.LENGTH_LONG).show()
            }

            result.onFailure { error ->
                Toast.makeText(this, "Erro no registo: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }
    }

    private fun validateLogin(username: String, password: String): Boolean {
        var isValid = true

        // Limpar erros anteriores
        binding.tilUsername.error = null
        binding.tilPassword.error = null

        // Validar username
        if (username.isEmpty()) {
            binding.tilUsername.error = "Introduza o nome de utilizador"
            isValid = false
        } else if (username.length < 3) {
            binding.tilUsername.error = "Nome de utilizador deve ter pelo menos 3 caracteres"
            isValid = false
        }

        // Validar password
        if (password.isEmpty()) {
            binding.tilPassword.error = "Introduza a password"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password deve ter pelo menos 6 caracteres"
            isValid = false
        }

        return isValid
    }

    private fun showRegisterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_register, null)
        val etRegName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRegName)
        val etRegEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRegEmail)
        val etRegUsername = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRegUsername)
        val etRegPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRegPassword)
        val etRegPasswordConfirm = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRegPasswordConfirm)

        val tilRegName = dialogView.findViewById<TextInputLayout>(R.id.tilRegName)
        val tilRegEmail = dialogView.findViewById<TextInputLayout>(R.id.tilRegEmail)
        val tilRegUsername = dialogView.findViewById<TextInputLayout>(R.id.tilRegUsername)
        val tilRegPassword = dialogView.findViewById<TextInputLayout>(R.id.tilRegPassword)
        val tilRegPasswordConfirm = dialogView.findViewById<TextInputLayout>(R.id.tilRegPasswordConfirm)

        AlertDialog.Builder(this)
            .setTitle("Criar Nova Conta")
            .setView(dialogView)
            .setPositiveButton("Registar") { _, _ ->
                // Validação será feita antes
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .apply {
                show()

                // Substituir o botão positivo para fazer validação
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val name = etRegName.text.toString().trim()
                    val email = etRegEmail.text.toString().trim()
                    val username = etRegUsername.text.toString().trim()
                    val password = etRegPassword.text.toString()
                    val passwordConfirm = etRegPasswordConfirm.text.toString()

                    // Limpar erros
                    tilRegName.error = null
                    tilRegEmail.error = null
                    tilRegUsername.error = null
                    tilRegPassword.error = null
                    tilRegPasswordConfirm.error = null

                    var isValid = true

                    // Validar nome
                    if (name.isEmpty()) {
                        tilRegName.error = "Introduza o seu nome"
                        isValid = false
                    } else if (name.length < 3) {
                        tilRegName.error = "Nome deve ter pelo menos 3 caracteres"
                        isValid = false
                    }

                    // Validar email
                    if (email.isEmpty()) {
                        tilRegEmail.error = "Introduza o email"
                        isValid = false
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        tilRegEmail.error = "Email inválido"
                        isValid = false
                    }

                    // Validar username
                    if (username.isEmpty()) {
                        tilRegUsername.error = "Introduza o nome de utilizador"
                        isValid = false
                    } else if (username.length < 3) {
                        tilRegUsername.error = "Username deve ter pelo menos 3 caracteres"
                        isValid = false
                    } else if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                        tilRegUsername.error = "Apenas letras, números e _"
                        isValid = false
                    }

                    // Validar password
                    if (password.isEmpty()) {
                        tilRegPassword.error = "Introduza a password"
                        isValid = false
                    } else if (password.length < 6) {
                        tilRegPassword.error = "Password deve ter pelo menos 6 caracteres"
                        isValid = false
                    }

                    // Validar confirmação de password
                    if (passwordConfirm.isEmpty()) {
                        tilRegPasswordConfirm.error = "Confirme a password"
                        isValid = false
                    } else if (password != passwordConfirm) {
                        tilRegPasswordConfirm.error = "Passwords não coincidem"
                        isValid = false
                    }

                    if (isValid) {
                        val hashedPassword = hashPassword(password)
                        val newUser = User(
                            username = username,
                            password = hashedPassword,
                            name = name,
                            email = email
                        )

                        viewModel.register(newUser)
                        dismiss()
                    }
                }
            }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Função simples de hash (SHA-256)
     * Nota: Em produção, usar BCrypt ou Argon2
     */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}