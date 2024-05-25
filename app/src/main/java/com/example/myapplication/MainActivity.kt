package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.RecruitBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : RecruitBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecruitBinding.inflate(layoutInflater)
        replaceFragment(Recruit())
        setContentView(binding.root)

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
}