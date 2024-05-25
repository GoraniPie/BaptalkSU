package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/*
TODO: 모집글 페이지 조회 구현, 모집글 상세조회 구현, 모집글 검색 구현
 */
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Recruit.newInstance] factory method to
 * create an instance of this fragment.
 */
class Recruit : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recruitAdapter: RecruitAdapter
    private val firestore = FirebaseFirestore.getInstance()
    // startactivity 대체
    private lateinit var postNewLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        postNewLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.i("intent 닫힘", "intent 닫힘.")
                firestore.collection("recruitment")
                    .orderBy("created_at", Query.Direction.DESCENDING) // 최신순으로 가져오기
                    .limit(8)
                    .get()
                    .addOnSuccessListener { documents ->
                        val recruitList = documents.toObjects(RecruitDataModel::class.java)
                        recruitAdapter.updateList(recruitList)
                    }
                    .addOnFailureListener { exception ->
                        Log.w("MainActivity", "Error getting documents: ", exception)
                    }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recruit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bt_PostNew 버튼 클릭 리스너
        val btPostNew = view.findViewById<Button>(R.id.bt_PostNew)
        btPostNew.setOnClickListener {
            val intent = Intent(activity, PostRecruitment::class.java)
            startActivity(intent)
        }

        // RecyclerView 설정
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recruitAdapter = RecruitAdapter(emptyList())
        recyclerView.adapter = recruitAdapter

        // Firestore에서 데이터 가져오기
        firestore.collection("recruitment")
            .orderBy("created_at", Query.Direction.DESCENDING) // 최신순으로 가져오기
            .limit(8)
            .get()
            .addOnSuccessListener { documents ->
                val recruitList = documents.toObjects(RecruitDataModel::class.java)
                recruitAdapter.updateList(recruitList)
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting documents: ", exception)
            }
        

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Recruit.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Recruit().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}