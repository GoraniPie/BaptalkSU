package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.RecruitBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : RecruitBinding
    private lateinit var postRecruitmentLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecruitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 프래그먼트(모집글조회) 작성 인텐트 끝나면 리사이클뷰 갱신
        postRecruitmentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val fragment = supportFragmentManager.findFragmentById(R.id.frame_layout) as Recruit
                fragment.refreshRecyclerView()
            }
        }

        replaceFragment(Recruit())

        binding.bottomNavigationView.setOnItemSelectedListener {
            Log.i("nav bar", "item changed")
            when (it.itemId) {
                R.id.recruit -> replaceFragment(Recruit())
                R.id.map -> replaceFragment(Map())
                R.id.profile -> replaceFragment(Profile())
                R.id.chat -> replaceFragment(Chat())
            }
            true
        }



    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
    fun launchPostRecruitment() {
        val intent = Intent(this, PostRecruitment::class.java)
        postRecruitmentLauncher.launch(intent)
    }
}