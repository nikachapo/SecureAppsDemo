package com.example.secureappsdemo

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.*

class MainActivity : AppCompatActivity() {


    val file by lazy {
        File(
            filesDir.absolutePath + File.separator +
                    "encryptedfile.dat"
        )
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println(BuildConfig.API_KEY)
        BuildConfig.SOME_KEY

        getSharedPreferences("Test", MODE_PRIVATE).apply {
            edit {
                putString("hello_key", "Hello world!")
            }
        }

        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
//        val encryptedFile = EncryptedFile.Builder(
//            file, this, masterKey, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
//        ).build()
//
//        encryptedFile.openFileInput().use {
//        }
//        encryptedFile.openFileOutput().use {
//        }

        val encryptedSharedPreferences = EncryptedSharedPreferences.create(
            "TestEncrypted",
            masterKey,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        encryptedSharedPreferences.edit {
            putString("hello_key", "Hello World!")
        }

        println("......................" + TestClass::class.java.simpleName)


        ///////////

        findViewById<Button>(R.id.btEncrypt).setOnClickListener {
            saveEncryptedData()
        }
        findViewById<Button>(R.id.btDecrypt).setOnClickListener {
            getEncryptedData()
        }
    }

    private fun saveEncryptedData() {
        val textToEncrypt = findViewById<EditText>(R.id.textToEncrypt).text.toString()
        KeyStoreCryptoManager().encrypt(
            textToEncrypt.toByteArray(),
            ObjectOutputStream(
                FileOutputStream(file)
            )
        )
    }

    fun getEncryptedData() {
        val t = KeyStoreCryptoManager().decrypt(
            ObjectInputStream(FileInputStream(file))
        )
        findViewById<TextView>(R.id.hellotext).text = String(t, Charsets.UTF_8)

    }
}

