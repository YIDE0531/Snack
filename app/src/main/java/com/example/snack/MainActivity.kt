package com.example.snack

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.ads.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private val RC_SIGNIN: Int = 100
    private val auth = FirebaseAuth.getInstance()
    private lateinit var viewModel: SnackViewModel
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var mAdView: AdView
    private val appConfig = AppConfig.instance
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var dialog: AlertDialog

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        checkLogin()
    }

    @ExperimentalStdlibApi
    private fun checkLogin(){
        auth.addAuthStateListener {
            if(auth.currentUser == null) {
                val auth = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(
                        listOf(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build()
//                            AuthUI.IdpConfig.FacebookBuilder().build()
                        )
                    )
                    .setIsSmartLockEnabled(false)
                    .setLogo(R.mipmap.ic_launcher)
                    .build()
                startActivityForResult(auth, RC_SIGNIN)
            }else{
                if(auth.currentUser!!.isEmailVerified) {
                    tv_email.text = "HI! ${auth.currentUser?.email}"
                    interstitialAdInit()    //  插頁式廣告
                    bannerAdInit()  //  橫幅廣告
                    startGame()
                }else{
                    verifyEmailListener()
                }
            }
        }
    }

    private fun bannerAdInit(){
        mAdView = AdView(this)
        mAdView.adSize = AdSize.BANNER
        mAdView.adUnitId = appConfig.getBannerAdUnitId()
        ll_adView.addView(mAdView)
        mAdView.loadAd(AdRequest.Builder().build())
    }

    private fun interstitialAdInit(){
        MobileAds.initialize(this) {}
        mInterstitialAd = InterstitialAd(this)
        // test:    ca-app-pub-3940256099942544/1033173712
        // prod:    ca-app-pub-9136945281854153/6318477670

        mInterstitialAd.adUnitId = appConfig.getInterstitialAdUnitId()
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

    @ExperimentalStdlibApi
    private fun startGame(){
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
        viewModel.start()
        imb_top.setOnClickListener { viewModel.move(Direction.TOP) }
        imb_left.setOnClickListener { viewModel.move(Direction.LEFT) }
        imb_right.setOnClickListener { viewModel.move(Direction.RIGHT) }
        imb_bottom.setOnClickListener { viewModel.move(Direction.DOWN) }
    }

    @ExperimentalStdlibApi
    private fun verifyEmailListener(){
        dialog = setProgressDialog(this, "請先到信箱確認信件後才可進行遊玩")
        dialog.show()
        CoroutineScope(Dispatchers.IO).launch{
            while(!auth.currentUser!!.isEmailVerified){
                auth.currentUser!!.reload()
                Thread.sleep(1000)
            }
            withContext(Dispatchers.Main) {
                dialog.cancel()
                tv_email.text = "HI! ${auth.currentUser?.email}"
                interstitialAdInit()    //  插頁式廣告
                bannerAdInit()  //  橫幅廣告
                startGame()
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

    @ExperimentalStdlibApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGNIN){
            if(resultCode == RESULT_OK){
                tv_email.text = "HI! ${auth.currentUser?.email}"
                if(!auth.currentUser!!.isEmailVerified) {
                    AlertDialog.Builder(this)
                        .setTitle("系統訊息")
                        .setMessage("須先驗證電子信箱才可以進行遊玩")
                        .setPositiveButton("確認") { _, _ ->
                            auth.currentUser?.sendEmailVerification()
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@MainActivity, "Verify Email Sent", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@MainActivity, "Verify Email Error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }
}