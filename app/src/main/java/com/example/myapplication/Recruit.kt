package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
    private lateinit var postRecruitmentLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        postRecruitmentLauncher = registerForActivityResult(
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
        return inflater.inflate(R.layout.fragment_recruit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // recyclerView 설정
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recruitAdapter = RecruitAdapter(emptyList())
        recyclerView.adapter = recruitAdapter

        // "작성하기" 버튼 클릭 리스너
        val btPostNew = view.findViewById<ImageButton>(R.id.ibt_PostNew)
        btPostNew.setOnClickListener {
            //val intent = Intent(activity, PostRecruitment::class.java)
            val intent = Intent(activity, PostRecruitment::class.java)
            postRecruitmentLauncher.launch(intent)
        }

        // Firestore에서 데이터 가져오기
        loadRecruitData()

    }

    fun refreshRecyclerView() {
        loadRecruitData()
    }

    private fun loadRecruitData() {
        firestore.collection("recruitment")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(8)
            .get()
            .addOnSuccessListener { documents ->
                val recruitList = documents.toObjects(RecruitDataModel::class.java)
                recruitAdapter.updateList(recruitList)
            }
            .addOnFailureListener { exception ->
                Log.w("Recruit", "Error getting documents: ", exception)
            }
    }

}