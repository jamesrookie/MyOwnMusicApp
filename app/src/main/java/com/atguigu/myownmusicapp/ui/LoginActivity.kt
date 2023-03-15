package com.atguigu.myownmusicapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment:NavHostFragment=supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController=navHostFragment.navController
    }
}