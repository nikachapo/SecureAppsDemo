package com.example.secureappsdemo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
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
        val map = CryptoManager().encrypt(
            textToEncrypt.toByteArray(), "1111".toCharArray()
        )

        findViewById<TextView>(R.id.hellotext).text = map["encrypted"]?.joinToString(", ")
        ObjectOutputStream(FileOutputStream(file)).use {
            it.writeObject(map)
        }
    }

    fun getEncryptedData() {

        var decrypted: ByteArray? = null
        ObjectInputStream(FileInputStream(file)).use { it ->
            val data = it.readObject()

            when (data) {
                is Map<*, *> -> {

                    if (data.containsKey("iv") && data.containsKey("salt") && data.containsKey("encrypted")) {
                        val iv = data["iv"]
                        val salt = data["salt"]
                        val encrypted = data["encrypted"]
                        if (iv is ByteArray && salt is ByteArray && encrypted is ByteArray) {
                            decrypted = CryptoManager().decrypt(
                                hashMapOf("iv" to iv, "salt" to salt, "encrypted" to encrypted),

                                password = "1111".toCharArray()
                            )
                        }
                    }
                }
            }
        }

        findViewById<TextView>(R.id.hellotext).text = String(decrypted!!, Charsets.UTF_8)

    }
}

