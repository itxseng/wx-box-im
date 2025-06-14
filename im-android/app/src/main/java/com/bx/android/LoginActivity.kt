package com.bx.android

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import com.bx.android.model.MessageStore
import com.bx.android.model.Conversation
import android.content.Intent
import android.content.SharedPreferences
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUser = findViewById<EditText>(R.id.etUserName)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val user = etUser.text.toString()
            val pass = etPass.text.toString()
            login(user, pass)
        }
    }

    private fun login(user: String, pass: String) {
        val json = JSONObject().apply {
            put("userName", user)
            put("password", pass)
            put("terminal", 1)
        }
        val reqBody = RequestBody.create(MediaType.parse("application/json"), json.toString())
        val req = Request.Builder()
            .url("https://www.boxim.online/api/login")
            .post(reqBody)
            .build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "网络错误", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                val obj = body?.let { JSONObject(it) }
                if (obj != null && obj.getInt("code") == 200) {
                    val data = obj.getJSONObject("data")
                    val accessToken = data.getString("accessToken")
                    val refreshToken = data.getString("refreshToken")
                    val prefs: SharedPreferences = getSharedPreferences("auth", MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("accessToken", accessToken)
                        putString("refreshToken", refreshToken)
                        apply()
                    }
                    WebSocketManager.connect("wss://www.boxim.online/im", accessToken)
                    // 初始化示例会话数据
                    MessageStore.addConversation(Conversation(1, "示例会话"))
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, obj?.getString("message") ?: "登录失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
