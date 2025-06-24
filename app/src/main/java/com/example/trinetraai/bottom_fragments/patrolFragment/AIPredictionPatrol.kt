package com.example.trinetraai.bottom_fragments.patrolFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.trinetraai.R

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [AIPredictionPatrol.newInstance] factory method to
 * create an instance of this fragment.
 */
class AIPredictionPatrol : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_a_i_prediction_patrol, container, false)
    }


}