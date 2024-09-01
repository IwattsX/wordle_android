package com.example.wordle_

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var counter = 0

        var wordToGuess = FourLetterWordList.getRandomFourLetterWord()

        val userGuess = findViewById<EditText>(R.id.UserGuess)

        val submit = findViewById<Button>(R.id.button)

        val revealView = findViewById<TextView>(R.id.reveal_ans)


        val linearLayoutRight = findViewById<LinearLayout>(R.id.rightoutputs)
        val linearLayoutLeft = findViewById<LinearLayout>(R.id.leftoutputs)


        submit.setOnClickListener {
            val guess = userGuess.text.toString().uppercase()
            if (counter < 3) {
                counter = handleGuessSubmission(
                    guess, wordToGuess, userGuess, linearLayoutLeft, linearLayoutRight, revealView, submit, counter
                )
            } else {
                counter = 0
                resetGame(userGuess, linearLayoutLeft, linearLayoutRight, submit)
                wordToGuess = FourLetterWordList.getRandomFourLetterWord()
                revealView.visibility = View.INVISIBLE
            }
        }

        userGuess.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val guess = userGuess.text.toString().uppercase()
                if (counter < 3) {
                    counter = handleGuessSubmission(
                        guess, wordToGuess, userGuess, linearLayoutLeft, linearLayoutRight, revealView, submit, counter
                    )
                } else {
                    counter = 0
                    resetGame(userGuess, linearLayoutLeft, linearLayoutRight, submit)
                    wordToGuess = FourLetterWordList.getRandomFourLetterWord()
                    revealView.visibility = View.INVISIBLE
                }
                return@OnKeyListener true
            }
            false
        })


    }

    private fun handleGuessSubmission(
        guess: String,
        wordToGuess: String,
        userGuess: EditText,
        linearLayoutLeft: LinearLayout,
        linearLayoutRight: LinearLayout,
        revealView: TextView,
        submit: Button,
        counter: Int
    ): Int {
        val checkResult = checkGuess(guess, wordToGuess)
        val coloredGuess = colorizeGuess(guess, checkResult)

        if (guess.length == 4) {
            val leftTextView = TextView(this).apply {
                text = "Guess #${counter + 1}"
                textSize = 18f
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            linearLayoutLeft.addView(leftTextView)

            val rightTextView = TextView(this).apply {
                text = coloredGuess
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            linearLayoutRight.addView(rightTextView)

            if (counter == 2 || checkResult.joinToString("") == "OOOO") {
                if (checkResult.joinToString("") == "OOOO") {
                    revealView.text = "You are correct! Word = $wordToGuess"
                } else {
                    revealView.text = "Ran out of tries, the word was $wordToGuess"
                }
                revealView.visibility = View.VISIBLE
                submit.text = "RESTART"
            }
            userGuess.text.clear()
            return counter + 1
        } else {
            Toast.makeText(userGuess.context, "Word needs to be 4 letters", Toast.LENGTH_SHORT).show()
            return counter
        }
    }


    private fun resetGame(userGuess : EditText, linearLayoutLeft : LinearLayout, linearLayoutRight : LinearLayout, submit_btn : Button) {
        // Clear the user input
        userGuess.text.clear()

        // Clear the counters and text views
        linearLayoutLeft.removeAllViews()
        linearLayoutRight.removeAllViews()

        // Reset button text back to "Submit"
        submit_btn.text = "Submit"
    }

    /**
     * MODIFIED version of checkGuess to handle
     * word to guess = goat and you guess good would appear as OO+X but should be 00XX bc o only appears once
     *
     * */
    private fun checkGuess(guess: String, wordToGuess: String): List<Char> {
        val result = MutableList(guess.length) { 'X' } // Initialize result with all (4) 'X'


        // Hashmap
        val charFrequency = mutableMapOf<Char, Int>()
        for (char in wordToGuess) {
            if (charFrequency.containsKey(char)) {
                charFrequency[char] = charFrequency[char]!! + 1
            } else {
                charFrequency[char] = 1
            }
        }

        // Update freq and also say what indexes are correct
        for (i in guess.indices) {
            if (guess[i] == wordToGuess[i]) {
                result[i] = 'O'
                charFrequency[guess[i]] = charFrequency[guess[i]]!! - 1 // Decrease the frequency count
            }
        }

        // Step 3: Check for correct letters in incorrect positions (+)
        // The rest will be X's from how it was initialized
        for (i in guess.indices) {
            if (result[i] == 'O') continue // Skip already matched correct positions

            if (guess[i] in charFrequency && charFrequency[guess[i]]!! > 0) {
                result[i] = '+'
                charFrequency[guess[i]] = charFrequency[guess[i]]!! - 1 // Decrease the frequency count
            }
        }

        return result
    }


    private fun colorizeGuess(guess: String, result: List<Char>): SpannableString {
        val spannable = SpannableString(guess)

        // Loop through the guess string and colorize characters based on result
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