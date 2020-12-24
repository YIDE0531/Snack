package com.example.snack

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        var viewModel = ViewModelProvider(this).get(SnackViewModel::class.java)
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
                        .setPositiveButton("OK", null)
                        .show()
            }

        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
            viewModel.reset()
        }
        viewModel.start()
        imb_top.setOnClickListener { viewModel.move(Direction.TOP) }
        imb_left.setOnClickListener { viewModel.move(Direction.LEFT) }
        imb_right.setOnClickListener { viewModel.move(Direction.RIGHT) }
        imb_bottom.setOnClickListener { viewModel.move(Direction.DOWN) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}