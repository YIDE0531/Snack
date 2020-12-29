package com.example.snack

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.google.android.gms.ads.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: SnackViewModel
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var mAdView: AdView
    private val appConfig = AppConfig().instance

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        interstitialAdInit()    //  插頁式廣告
        bannerAdInit()  //  橫幅廣告

        viewModel = ViewModelProvider(this).get(SnackViewModel::class.java)
        viewModel.body.observe(this) {
            game_view.snackBody = it
            game_view.invalidate()
        }
        viewModel.apple.observe(this) {
            game_view.apple = it
            game_view.invalidate()
        }
        viewModel.score.observe(this) {
            tv_score.text = it.toString()
        }
        viewModel.gameState.observe(this) {
            if(it == GameState.GAME_OVER){
                AlertDialog.Builder(this@MainActivity)
                        .setTitle("Game")
                        .setMessage("Game Over")
                        .setPositiveButton("OK"){_, _ ->
                            mInterstitialAd.show()
                        }
                        .show()
            }
        }

//        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
////            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                    .setAction("Action", null).show()
//            viewModel.reset()
//        }
        viewModel.start()
        imb_top.setOnClickListener { viewModel.move(Direction.TOP) }
        imb_left.setOnClickListener { viewModel.move(Direction.LEFT) }
        imb_right.setOnClickListener { viewModel.move(Direction.RIGHT) }
        imb_bottom.setOnClickListener { viewModel.move(Direction.DOWN) }

    }

    private fun bannerAdInit(){
        mAdView = AdView(this)
        mAdView.adSize = AdSize.BANNER
        mAdView.adUnitId = if(appConfig.target == Target.PROD){
            "ca-app-pub-9136945281854153/6833482996"
        }else {
            "ca-app-pub-3940256099942544/6300978111"
        }
        ll_adView.addView(mAdView)
        mAdView.loadAd(AdRequest.Builder().build())
    }

    private fun interstitialAdInit(){
        MobileAds.initialize(this) {}
        mInterstitialAd = InterstitialAd(this)
        // test:    ca-app-pub-3940256099942544/1033173712
        // prod:    ca-app-pub-9136945281854153/6318477670

        mInterstitialAd.adUnitId = if(appConfig.target == Target.PROD){
            "ca-app-pub-9136945281854153/6318477670"
        }else {
            "ca-app-pub-3940256099942544/1033173712"
        }
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                mAdView.loadAd(AdRequest.Builder().build())
                Toast.makeText(this@MainActivity, "按下右上角圖示後進行重玩", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @ExperimentalStdlibApi
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_restart -> {
                viewModel.reset()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}