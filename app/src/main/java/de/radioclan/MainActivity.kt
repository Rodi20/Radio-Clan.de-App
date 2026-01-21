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

        // UI Elemente finden
        webChat = findViewById(R.id.webChat)
        webMobil = findViewById(R.id.webMobil)
        playerContainer = findViewById(R.id.playerContainer)
        streamListLayout = findViewById(R.id.streamList)

        // Webseiten im Hintergrund laden (Paralleler Betrieb)
        setupWebView(webChat, "https://Radio-Clan.de/RDC")
        setupWebView(webMobil, "https://radio-clan.de/mobil2.php")

        // Streams live von der Webseite laden
        loadStreamsOnline()
    }

    private fun setupWebView(wv: WebView, url: String) {
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true // Wichtig für Login-Erhalt
        wv.webViewClient = WebViewClient()
        wv.loadUrl(url)
    }

    private fun loadStreamsOnline() {
        thread {
            try {
                // JSON von deiner URL laden
                val jsonText = URL("https://radio-clan.de/stream.json").readText()
                val jsonObject = JSONObject(jsonText)
                val jsonArray = jsonObject.getJSONArray("streams")

                runOnUiThread {
                    streamListLayout.removeAllViews()
                    for (i in 0 until jsonArray.length()) {
                        val stream = jsonArray.getJSONObject(i)
                        val rawName = stream.getString("name")
                        val url = stream.getString("url")

                        // Schönheit: "laut.fm/" aus dem Namen entfernen
                        val cleanName = rawName.replace("laut.fm/", "").replace("-", " ").capitalize()

                        val btn = Button(this).apply {
                            text = cleanName
                            setBackgroundColor(Color.parseColor("#B30000")) // Clan Rot
                            setTextColor(Color.WHITE)
                            setOnClickListener { startRadio(url) }
                            
                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.setMargins(0, 15, 0, 15)
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

    // --- Funktionen für die Buttons unten (Navigation) ---

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