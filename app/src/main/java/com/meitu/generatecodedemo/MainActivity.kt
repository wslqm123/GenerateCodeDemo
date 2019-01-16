package com.meitu.generatecodedemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.meitu.annotationprocessor.BindView

import com.meitu.annotationprocessor.PrintName

class MainActivity : AppCompatActivity() {

    @PrintName
    private val mNull: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
