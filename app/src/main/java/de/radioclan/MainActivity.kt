package de.radioclan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var webChat: WebView
    private lateinit var webMobil: WebView
    private lateinit var playerContainer: View
    private lateinit var streamListLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webChat = findViewById(R.id.webChat)
        webMobil = findViewById(R.id.webMobil)
        playerContainer = findViewById(R.id.playerContainer)
        streamListLayout = findViewById(R.id.streamList)

        // Webseiten laden
        setupWebView(webChat, "https://Radio-Clan.de/RDC")
        setupWebView(webMobil, "https://radio-clan.de/mobil2.php")

        // WICHTIG: Streams laden
        loadStreamsOnline()
    }

    private fun setupWebView(wv: WebView, url: String) {
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true
        wv.webViewClient = WebViewClient()
        wv.loadUrl(url)
    }

    private fun loadStreamsOnline() {
        thread {
            try {
                val jsonText = URL("https://radio-clan.de/stream.json").readText()
                val jsonObject = JSONObject(jsonText)
                val jsonArray = jsonObject.getJSONArray("streams")

                runOnUiThread {
                    streamListLayout.removeAllViews()
                    for (i in 0 until jsonArray.length()) {
                        val stream = jsonArray.getJSONObject(i)
                        val name = stream.getString("name")
                        // Wir nehmen die URL direkt aus der JSON
                        val streamUrl = stream.getString("url") 

                        val btn = Button(this).apply {
                            text = name.replace("laut.fm/", "").uppercase()
                            setBackgroundColor(Color.parseColor("#B30000"))
                            setTextColor(Color.WHITE)
                            
                            setOnClickListener { 
                                startRadio(streamUrl) 
                            }

                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.setMargins(0, 10, 0, 10)
                            layoutParams = params
                        }
                        streamListLayout.addView(btn)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startRadio(url: String) {
        val intent = Intent(this, AudioService::class.java)
        intent.putExtra("STREAM_URL", url)
        startForegroundService(intent)
    }

    fun showPlayer(v: View) {
        playerContainer.visibility = View.VISIBLE
        webChat.visibility = View.GONE
        webMobil.visibility = View.GONE
    }

    fun showChat(v: View) {
        playerContainer.visibility = View.GONE
        webChat.visibility = View.VISIBLE
        webMobil.visibility = View.GONE
    }

    fun showMobil(v: View) {
        playerContainer.visibility = View.GONE
        webChat.visibility = View.GONE
        webMobil.visibility = View.VISIBLE
    }
}