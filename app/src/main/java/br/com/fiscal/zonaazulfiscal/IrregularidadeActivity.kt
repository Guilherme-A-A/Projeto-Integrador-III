package br.com.fiscal.zonaazulfiscal

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.PackageManagerCompat.LOG_TAG
import br.com.fiscal.zonaazulfiscal.databinding.ActivityIrregularidadeBinding
import br.com.fiscal.zonaazulfiscal.fragments.ConsultarFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import java.io.ByteArrayOutputStream
import java.io.File

class
IrregularidadeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIrregularidadeBinding
    private lateinit var imagem1 : ShapeableImageView
    private lateinit var imagem2 : ShapeableImageView
    private lateinit var imagem3 : ShapeableImageView
    private lateinit var imagem4 : ShapeableImageView
    private lateinit var etDescricao : TextInputEditText
    private lateinit var progressIrregularidade : ProgressBar
    private lateinit var constraintTotalIrregularidadeActivity : ConstraintLayout


    private  lateinit var btnRegistrarIrregularidade : AppCompatButton

    private lateinit var actualImage: ShapeableImageView

    private var imageNames: Array<ShapeableImageView?> = arrayOf(null,null,null,null)

    private lateinit var functions: FirebaseFunctions

    private val gson = GsonBuilder().enableComplexMapKeySerialization().create()

    private val logEntry = "REGISTRAR_IRREGULARIDADE";

    private lateinit var storage : FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIrregularidadeBinding.inflate(layoutInflater)
        setContentView(binding.root)



        functions = Firebase.functions("southamerica-east1")
        storage = Firebase.storage("gs://zonaazulfiscal-7640c.appspot.com")
        progressIrregularidade = findViewById(R.id.progressIrregularidadeActivity)

        imagem1 = findViewById(R.id.imageView)
        imagem1.setOnClickListener{
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
            actualImage = imagem1
            imageNames.set(0,actualImage)

        }
        imagem2 = findViewById(R.id.imageView5)
        imagem2.setOnClickListener{
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
            actualImage = imagem2
            imageNames.set(1,actualImage)

        }
        imagem3 = findViewById(R.id.imageView3)
        imagem3.setOnClickListener{
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
            actualImage = imagem3
            imageNames.set(2,actualImage)


        }
        imagem4 = findViewById(R.id.imageView4)
        imagem4.setOnClickListener{
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
            actualImage = imagem4
            imageNames.set(3,actualImage)

        }

        etDescricao = findViewById(R.id.etDescricao)

        btnRegistrarIrregularidade = findViewById(R.id.btnRegistrarIrregularidade)

        etDescricao = findViewById(R.id.etDescricao)

        constraintTotalIrregularidadeActivity = findViewById(R.id.constraintTotalIrregularidadeActivity)
        constraintTotalIrregularidadeActivity.setOnClickListener {
            hideKeyBoard()
        }

        binding.btnRegistrarIrregularidade.setOnClickListener{
            hideKeyBoard()
            val placa  = intent.getStringExtra("placa")

            btnRegistrarIrregularidade.visibility = View.GONE
            progressIrregularidade.visibility = View.VISIBLE

            val p = Irregularidade(etDescricao.text.toString(), placa )
            registrarIrregularidade(p)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        val e = task.exception
                        if (e is FirebaseFunctionsException) {
                            val code = e.code
                            val details = e.details
                        }
                    }else{

                        val genericResp = gson.fromJson(task.result, FunctionsGenericResponse::class.java)

                        val insertInfo = gson.fromJson(genericResp.payload.toString(), GenericInsertResponse::class.java)

                        progressIrregularidade.visibility = View.GONE
                        btnRegistrarIrregularidade.visibility = View.VISIBLE


                        Snackbar.make(btnRegistrarIrregularidade, "Irregularidade registrada com sucesso" ,
                            Snackbar.LENGTH_LONG).show();

                        uploadImage()

                        //retonarConsulta()
                    }
                }).addOnFailureListener{
                    val errorMessage = when(it){
                        is FirebaseNetworkException -> "Erro ao registrar irregularidade, por favor conecte-se a internet"
                        else -> "Erro ao registrar irregularidade, por favor cenecte-se a internet"
                    }
                    val snack = Snackbar.make(btnRegistrarIrregularidade, errorMessage,
                        Snackbar.LENGTH_LONG)
                    snack.setBackgroundTint(Color.RED)
                    snack.show()
                }

        }


    }

    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it){
                startForResult.launch(Intent(this, CameraPreviewActivity::class.java))
            }else{
                val snack = Snackbar.make(binding.root, "Você não concedeu a permissão de camera", Snackbar.LENGTH_LONG)
                snack.setBackgroundTint(Color.RED)
                snack.show()
            }
        }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK){
            val actualUri = result.data?.data
            actualImage.setImageURI(actualUri)
        }
    }



    private fun registrarIrregularidade(p: Irregularidade): Task<String> {
        val data = hashMapOf(
            "descricao" to p.descricao,
            "placa" to p.placa
        )
        return functions.getHttpsCallable("addNewIrregularity")
            .call(data)
            .continueWith { task ->
                // se faz necessario transformar a string de retorno como uma string Json valida.
                val res = gson.toJson(task.result?.data)
                res
            }
    }


    fun hideKeyBoard(){
        val inn = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var currentFocus = this.currentFocus
        if(currentFocus == null){
            currentFocus = View(this)
        }
        inn.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }

    private fun retonarConsulta(){
       val handler = Handler()
        handler.postDelayed({
            retornaConsultaIntent()
        }, 500)
    }

    private fun retornaConsultaIntent(){
        val intent = Intent(this, ConsultarFragment::class.java)
        startActivity(intent)
    }

    private fun uploadImage()
    {
        var count = 0;
        val storageRef = storage.reference
        for (item in imageNames ) {
            if (item != null) {

                val newRef = storageRef.child("image$count.jpg")
                val mountainImagesRef = storageRef.child("image/$newRef")

                newRef.name == mountainImagesRef.name
                newRef.path == mountainImagesRef.path

                val bitmap = (item.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos)
                val data = baos.toByteArray()

                var uploadTask = newRef.putBytes(data)
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    println("reference" + taskSnapshot.metadata?.reference)
                }.addOnFailureListener {
                    Log.d("Upload", it.toString())
                    Snackbar.make(
                        binding.root,
                        "Erro ao inserir imagem" + it.toString(),
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
            }
            count++
        }
    }

}

