package br.com.fiscal.zonaazulfiscal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class PrimeiraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_primeira)
    }

    fun irParaAreaFiscal(view: View) {
        val intentAreaFiscal = Intent(this, MainActivity::class.java)
        startActivity(intentAreaFiscal)
    }
}