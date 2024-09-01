package com.example.wordle_

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    /**
     * Parameters / Fields:
     *   wordToGuess : String - the target word the user is trying to guess
     *   guess : String - what the user entered as their guess
     *
     * Returns a String of 'O', '+', and 'X', where:
     *   'O' represents the right letter in the right place
     *   '+' represents the right letter in the wrong place
     *   'X' represents a letter not in the target word
     */

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var guesses = 0

        val wordToGuess = FourLetterWordList.getRandomFourLetterWord()

        val user_guess = findViewById<EditText>(R.id.UserGuess)

        val submit_btn = findViewById<Button>(R.id.button)

        val textView = findViewById<TextView>(R.id.textView)

        submit_btn.setOnClickListener {
            val guess: String = user_guess.text.toString().uppercase()


            if (guess.length < 4) {
                Toast.makeText(
                    user_guess.context,
                    "You need to enter a 4 letter word",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val res = checkGuess(guess, wordToGuess)
                textView.text = colorizeResult(res)
            }
        }

        user_guess.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                Toast.makeText(user_guess.context, wordToGuess, Toast.LENGTH_SHORT).show()
                val guess: String = user_guess.text.toString().uppercase()

                if (guess.length < 4) {
                    Toast.makeText(
                        user_guess.context,
                        "You need to enter a 4 letter word",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val res = checkGuess(guess, wordToGuess)
                    textView.text = colorizeResult(res)
                }

                return@OnKeyListener true
            }
            false
        })
    }



    //MODIFIED version of checkGuess to handle
    /**
     * guess = goat and you guess good would appear as XO+X
     *
     * */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkGuess(guess: String, wordToGuess: String): String {
        val result = CharArray(guess.length) { 'X' } // Initialize result with 'X'

        // Step 1: Count frequency of each character in wordToGuess
        val charFrequency = mutableMapOf<Char, Int>()
        for (char in wordToGuess) {
            charFrequency[char] = charFrequency.getOrDefault(char, 0) + 1
        }

        // Step 2: Check for correct positions (O) and update frequencies
        for (i in guess.indices) {
            if (guess[i] == wordToGuess[i]) {
                result[i] = 'O'
                charFrequency[guess[i]] = charFrequency[guess[i]]!! - 1 // Decrease the frequency count
            }
        }

        // Step 3: Check for correct letters in incorrect positions (+)
        for (i in guess.indices) {
            if (result[i] == 'O') continue // Skip already matched correct positions

            if (guess[i] in charFrequency && charFrequency[guess[i]]!! > 0) {
                result[i] = '+'
                charFrequency[guess[i]] = charFrequency[guess[i]]!! - 1 // Decrease the frequency count
            }
        }

        return String(result) // Convert CharArray back to String
    }





    private fun colorizeResult(result: String): SpannableString {
        val spannable = SpannableString(result)

        // Loop through the result string and colorize 'X' and 'O'
        for (i in result.indices) {
            when (result[i]) {
                'X' -> spannable.setSpan(
                    ForegroundColorSpan(Color.RED),
                    i,
                    i + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                'O' -> spannable.setSpan(
                    ForegroundColorSpan(Color.GREEN),
                    i,
                    i + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                '+' -> spannable.setSpan(
                    ForegroundColorSpan(Color.YELLOW),
                    i,
                    i + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return spannable
    }
}