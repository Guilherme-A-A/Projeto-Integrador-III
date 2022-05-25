package br.com.fiscal.zonaazulfiscal

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import br.com.fiscal.zonaazulfiscal.databinding.FragmentIrregularidadeBinding
import br.com.fiscal.zonaazulfiscal.fragments.ConsultarFragment
import com.google.android.gms.tasks.Task
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.GsonBuilder
import java.io.ByteArrayOutputStream
import java.util.regex.Matcher
import java.util.regex.Pattern

class irregularidadeFragment : Fragment() {

    private lateinit var binding: FragmentIrregularidadeBinding
    private lateinit var etPlaca : TextInputEditText
    private lateinit var etDescricao : TextInputEditText
    private lateinit var imagem1 : ShapeableImageView
    private lateinit var imagem2 : ShapeableImageView
    private lateinit var imagem3 : ShapeableImageView
    private lateinit var imagem4 : ShapeableImageView
    private lateinit var btnRegistrarIrreg : AppCompatButton
    private lateinit var functions : FirebaseFunctions
    private lateinit var Storage : FirebaseStorage
    private val gson = GsonBuilder().enableComplexMapKeySerialization().create()

    private lateinit var actualImage: ShapeableImageView

    private var imageNames: Array<ShapeableImageView?> = arrayOf(null,null,null,null)


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = FragmentIrregularidadeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

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

        functions = Firebase.functions("southamerica-east1")

        var rootView : View = inflater.inflate(R.layout.fragment_irregularidade, container, false)



        etPlaca = rootView.findViewById(R.id.etPlacaIrreg)
        etDescricao = rootView.findViewById(R.id.etDescricaoIrregularidade)
        imagem1 = rootView.findViewById(R.id.imageView1)

        imagem1.setOnClickListener{
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
            actualImage = imagem1
            imageNames.set(0,actualImage)
        }

        imagem2 = rootView.findViewById(R.id.imageView2)
        imagem2.setOnClickListener{
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
            actualImage = imagem2
            imageNames.set(1,actualImage)
        }

        imagem3 = rootView.findViewById(R.id.imageView3)
        imagem3.setOnClickListener{
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
            actualImage = imagem3
            imageNames.set(2,actualImage)
        }

        imagem4 = rootView.findViewById(R.id.imageView4)
        imagem4.setOnClickListener{
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
            actualImage = imagem4
            imageNames.set(3,actualImage)
        }

        btnRegistrarIrreg = rootView.findViewById(R.id.btnRegistrarIrreg)
        btnRegistrarIrreg.setOnClickListener{
            registrarIrregularidade()
        }

        return rootView
    }
    private fun registrarIrregularidade(){
        val placa = etPlaca.text.toString()
        val descricao = etDescricao.text.toString()

        val pattern : Pattern = Pattern.compile("[A-Z]{3}[0-9]{4}")
        val mat: Matcher =  pattern.matcher(placa)
        if(!mat.matches()){

            Toast.makeText(binding.root.context,"Por favor, digite uma placa válida (Ex : FBT6150)", Toast.LENGTH_LONG).show()

        }else if(placa.isNullOrEmpty() || descricao.isNullOrEmpty()){
            Toast.makeText(binding.root.context,
                "Os canpos de placa e decsrição não podem estar vazios",
                Toast.LENGTH_LONG).show()
        }else if(placa.length != 7){
            Toast.makeText(binding.root.context,
                "Por favor digite uma placa válida",
                Toast.LENGTH_LONG).show()
        }else if(descricao.length > 20){
            Toast.makeText(binding.root.context,
                "A descrição deve conter no máximo 20 caracteres",
                Toast.LENGTH_LONG).show()
            }else{
                val p = Irregularidade(etDescricao.text.toString(), etPlaca.text.toString())

                registrarIrregularidade(p)
                    .addOnCompleteListener{ task ->
                       if (!task.isSuccessful){
                           val e = task.exception
                           if(e is FirebaseFunctionsException){
                               val code = e.code
                               val details = e.details
                           }
                       }else{
                           Snackbar.make(btnRegistrarIrreg, "Irregularidade registrada com sucesso", Snackbar.LENGTH_LONG).show()
                       }

                    }
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
    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it){
                startForResult.launch(Intent(activity, CameraPreviewActivity::class.java))
            }else{
                val snack = Snackbar.make(binding.root, "Você não concedeu a permissão de camera", Snackbar.LENGTH_LONG)
                snack.setBackgroundTint(Color.RED)
                snack.show()
            }
        }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK){
            val uriImage = result.data?.data
            actualImage.setImageURI(uriImage)
        }
    }

    private fun uploadImage()
    {
        var count = 0;
        for (item in imageNames ) {
            if (item != null) {
                val storageRef = Storage.reference

                val newRef = storageRef.child("image$count.jpg")

                val newImagesRef = storageRef.child("images/$newRef")

                newRef.name == newImagesRef.name
                newRef.path == newImagesRef.path

                val bitmap = (item.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                var uploadTask = newImagesRef.putBytes(data)
                uploadTask.addOnFailureListener {
                    Snackbar.make(
                        binding.root,
                        "Erro ao inserir imagem",
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }.addOnSuccessListener { taskSnapshot ->
                    println("reference" + taskSnapshot.metadata?.reference)
                }
            }
            count++
        }
    }

}