package pt.ipt.dam2025.iptfit.ui.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam2025.iptfit.R
import pt.ipt.dam2025.iptfit.databinding.ActivityAboutBinding

/**
 * Activity que mostra informações sobre o projeto
 */
class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sobre"
        binding.tvAuthor1Name.text = "Diogo Vicente Jorge"
        binding.tvAuthor1Number.text = "Nº 25947"
        binding.ivAuthor1Photo.setImageResource(R.drawable.diogo)


        binding.tvAuthor2Name.text = "Gonçalo Marques Serrano"
        binding.tvAuthor2Number.text = "Nº 25948"
        binding.ivAuthor2Photo.setImageResource(R.drawable.goncalo)

        binding.fabBack.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}