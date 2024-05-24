package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.RecruitBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : RecruitBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecruitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Recruit())

        binding.bottomNavigationView.setOnItemSelectedListener {

            when(it.itemId){

                R.id.recruit -> replaceFragment(Recruit())
                R.id.map -> replaceFragment(Map())
                R.id.profile -> replaceFragment(Profile())
                R.id.chat -> replaceFragment(Chat())

                else ->{

                }

            }

            true

        }



        setContentView(R.layout.recruit)
        val bt_PostNew = findViewById<Button>(R.id.bt_PostNew)
        bt_PostNew.setOnClickListener {
            val intent = Intent(this, PostRecruitment::class.java)
            startActivity(intent)
        }
    }

    private fun replaceFragment(fragment : Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
}