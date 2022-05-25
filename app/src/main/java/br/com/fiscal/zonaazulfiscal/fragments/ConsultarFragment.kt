package br.com.fiscal.zonaazulfiscal.fragments

import android.accounts.NetworkErrorException
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.HandlerCompat.postDelayed
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import br.com.fiscal.zonaazulfiscal.*
import br.com.fiscal.zonaazulfiscal.databinding.ActivityIrregularidadeBinding
import br.com.fiscal.zonaazulfiscal.databinding.FragmentConsultarBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.lang.Exception
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.reflect.KClass

class ConsultarFragment : Fragment() {


    private lateinit var etPlaca : AppCompatEditText
    private lateinit var btnConsultar : AppCompatButton
    private lateinit var btnNovaPlaca : AppCompatButton
    private lateinit var informacaoPlaca :ConstraintLayout
    private lateinit var informacaoPlaca2 :ConstraintLayout
    private lateinit var layoutNoConnection : LinearLayout
    private lateinit var btnTentarNovamente : AppCompatButton
    private lateinit var tvRegular2 : AppCompatTextView
    private lateinit var tvIrregular : AppCompatTextView
    private lateinit var tvIrregularMotivo : AppCompatTextView
    private lateinit var tvDataPagamento : TextView
    private lateinit var btnNo : ImageButton
    private lateinit var tvPlacaIrregular : TextView
    private lateinit var tvPlacaRegular : TextView
    private lateinit var binding: FragmentConsultarBinding
    private lateinit var functions : FirebaseFunctions
    private lateinit var btnYes : ImageButton
    private lateinit var imagemZonaAzul : ImageView
    private lateinit var progressbarConsulta : ProgressBar
    private lateinit var db : FirebaseFirestore
    private val logEntry = "CHECAR-PAGAMENTO"
    private val gson = GsonBuilder().enableComplexMapKeySerialization().create()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentConsultarBinding.inflate(layoutInflater)

    }
    override fun onStart() {
        super.onStart()
        (activity as MainActivity).supportActionBar!!.hide()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        var rootView : View = inflater.inflate(R.layout.fragment_consultar, container, false)

        functions = Firebase.functions("southamerica-east1")

        etPlaca = rootView.findViewById(R.id.etPlaca)
        etPlaca.setOnClickListener{
            limparViews()
        }
        btnConsultar = rootView.findViewById(R.id.btnConsultar)
        btnConsultar.setOnClickListener{
            hideKeyboard()
            consultarPlaca()




        }
        btnNovaPlaca = rootView.findViewById(R.id.btnNovaPlaca)
        btnNovaPlaca.setOnClickListener{
            btnConsultar.visibility = View.VISIBLE
            consultarNovaPlaca()
        }

        informacaoPlaca = rootView.findViewById(R.id.informacaoPlaca)
        informacaoPlaca2 = rootView.findViewById(R.id.informacaoPlaca2)
        layoutNoConnection= rootView.findViewById(R.id.layoutNoConnection)
        tvIrregular = rootView.findViewById(R.id.tvIrregular)
        tvIrregularMotivo = rootView.findViewById(R.id.tvIrregularMotivo)
        tvPlacaIrregular = rootView.findViewById(R.id.tvPlacaIrregular)
        tvPlacaRegular = rootView.findViewById(R.id.tvPlacaRegular)
        progressbarConsulta = rootView.findViewById(R.id.progressConsultar)
        imagemZonaAzul = rootView.findViewById(R.id.imgZonaAzul)
        tvRegular2 = rootView.findViewById(R.id.tvRegular2)
        btnNo = rootView.findViewById(R.id.btnNo)
        btnNo.setOnClickListener{
            btnConsultar.visibility = View.VISIBLE
            botaoNo()
        }
        btnYes = rootView.findViewById(R.id.btnYes)
        btnYes.setOnClickListener{
            val intent = Intent(activity, IrregularidadeActivity::class.java)
            intent.putExtra("placa", etPlaca.text.toString().uppercase())
            startActivity(intent)
        }
        btnTentarNovamente = rootView.findViewById(R.id.btnTentarNovamente)
        btnTentarNovamente.setOnClickListener{

            layoutNoConnection.visibility = View.GONE
            val p = Pagamento(etPlaca.text.toString().uppercase())
            progressbarConsulta.visibility = View.VISIBLE
            checarPagamento(p)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {

                        val e = task.exception
                        if (e is FirebaseFunctionsException) {
                            val code = e.code
                            val details = e.details
                        }

                    }else{
                        val genericResp = gson.fromJson(task.result, FunctionsGenericResponse::class.java)

                        val payload = genericResp.payload.toString()
                        if(payload == "Ticket não pago"){
                            progressbarConsulta.visibility = View.GONE
                            imagemZonaAzul.visibility = View.GONE
                            val messageIrregularPlate = "Placa do veículo  :  ${etPlaca.text}"
                            val messageIrregularPlate2 = "${payload.toString()}"
                            tvPlacaIrregular.text = messageIrregularPlate
                            tvIrregularMotivo.text = messageIrregularPlate2
                            informacaoPlaca.visibility = View.VISIBLE
                        }else{
                            progressbarConsulta.visibility = View.GONE
                            imagemZonaAzul.visibility = View.GONE
                            val messageRegularPlate = "Placa do veículo  :  ${etPlaca.text}"
                            tvPlacaRegular.text = messageRegularPlate
                            informacaoPlaca2.visibility = View.VISIBLE
                        }

                    }
                }).addOnFailureListener {
                    if (it is FirebaseNetworkException){
                        progressbarConsulta.visibility = View.GONE
                        btnConsultar.visibility = View.GONE
                        layoutNoConnection.visibility = View.VISIBLE
                    }else{
                        progressbarConsulta.visibility = View.GONE
                        btnConsultar.visibility = View.GONE
                        layoutNoConnection.visibility = View.VISIBLE
                    }
                }
        }

        return rootView
    }

    private fun consultarPlaca(){

        val placa = etPlaca.text.toString().uppercase()

        val pattern : Pattern = Pattern.compile("[A-Z]{3}[0-9]{4}")
        val pattern2 : Pattern = Pattern.compile("[A-Z]{3}[0-9][0-9A-Z][0-9]{2}")
        val mat: Matcher =  pattern.matcher(placa)
        val mat2 : Matcher = pattern2.matcher(placa)
        if(!mat.matches() && !mat2.matches()){

            Toast.makeText(binding.root.context,"Por favor, digite uma placa válida", Toast.LENGTH_LONG).show()

        }else if(placa.isNullOrEmpty()){

            Toast.makeText(binding.root.context,"O campo placa não pode ser vazio", Toast.LENGTH_LONG).show()

        }else if(placa.length != 7){

            Toast.makeText(binding.root.context,"Por favor, digite uma placa válida", Toast.LENGTH_LONG).show()

        }else{
            btnConsultar.visibility = View.GONE
            progressbarConsulta.visibility = View.VISIBLE
            val p = Pagamento(etPlaca.text.toString().uppercase())
            checarPagamento(p)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {

                        val e = task.exception
                        if (e is FirebaseFunctionsException) {
                            val code = e.code
                            val details = e.details
                        }

                    }else{
                        val genericResp = gson.fromJson(task.result, FunctionsGenericResponse::class.java)

                        val payload = genericResp.payload.toString()
                        if(payload == "Ticket não pago"){
                            progressbarConsulta.visibility = View.GONE
                            imagemZonaAzul.visibility = View.GONE
                            val messageIrregularPlate = "Placa do veículo  :  ${etPlaca.text}"
                            val messageIrregularPlate2 = "${payload.toString()}"
                            tvPlacaIrregular.text = messageIrregularPlate
                            tvIrregularMotivo.text = messageIrregularPlate2
                            informacaoPlaca.visibility = View.VISIBLE
                        }else{
                            progressbarConsulta.visibility = View.GONE
                            imagemZonaAzul.visibility = View.GONE
                            val messageRegularPlate = "Placa do veículo  :  ${etPlaca.text}"
                            tvPlacaRegular.text = messageRegularPlate
                            informacaoPlaca2.visibility = View.VISIBLE
                        }

                    }
                }).addOnFailureListener {
                    if (it is FirebaseNetworkException){
                        progressbarConsulta.visibility = View.GONE
                        btnConsultar.visibility = View.GONE
                        layoutNoConnection.visibility = View.VISIBLE
                    }else{
                        progressbarConsulta.visibility = View.GONE
                        btnConsultar.visibility = View.GONE
                        layoutNoConnection.visibility = View.VISIBLE
                    }
                }

        }

        }

    private fun consultarNovaPlaca(){
        etPlaca.text!!.clear()
        informacaoPlaca2.visibility = View.GONE
        imagemZonaAzul.visibility = View.VISIBLE
    }

    private fun limparViews(){
        imagemZonaAzul.visibility = View.VISIBLE
        informacaoPlaca.visibility = View.GONE
        informacaoPlaca2.visibility = View.GONE
    }
    private fun botaoNo(){
        imagemZonaAzul.visibility = View.VISIBLE
        informacaoPlaca.visibility = View.GONE
        etPlaca.text!!.clear()
    }


    fun hideKeyboard() {
        val activity = this.activity as MainActivity
        view?.let { activity.hideKeyboard(it) }

    }



    private fun checarPagamento(p: Pagamento): Task<String> {
        val data = hashMapOf(
            "placa" to p.plate
        )
        return functions
            .getHttpsCallable("checarPagamentoVeiculos")
            .call(data)
            .continueWith { task ->
                val res = gson.toJson(task.result?.data)
                res
            }
    }
}