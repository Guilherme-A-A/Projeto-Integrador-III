package br.com.fiscal.zonaazulfiscal.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import br.com.fiscal.zonaazulfiscal.MainActivity
import br.com.fiscal.zonaazulfiscal.MapsActivity
import br.com.fiscal.zonaazulfiscal.R
import br.com.fiscal.zonaazulfiscal.databinding.FragmentIrregularidadeBinding
import br.com.fiscal.zonaazulfiscal.databinding.FragmentItnerarioBinding
import com.google.android.gms.maps.SupportMapFragment

class ItnerarioFragment : Fragment() {
    private lateinit var binding: FragmentItnerarioBinding
    private lateinit var maps : ConstraintLayout

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).supportActionBar!!.hide()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        val rootview = inflater.inflate(R.layout.fragment_itnerario, container, false)

        maps = rootview.findViewById(R.id.acessoMaps)

        maps.setOnClickListener{
            val intent = Intent(this.activity, MapsActivity::class.java)
            startActivity(intent)
        }



        return rootview

    }


}