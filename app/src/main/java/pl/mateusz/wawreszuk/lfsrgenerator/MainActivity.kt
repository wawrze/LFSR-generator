package pl.mateusz.wawreszuk.lfsrgenerator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private var generatorLFSR: GeneratorLFSR? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        setupButton(false)
    }

    private fun setupButton(started: Boolean) {
        if (started) {
            activity_main_button.text = getString(R.string.end_generating)
            activity_main_button.setOnClickListener { stopGenerator() }
        } else {
            activity_main_button.text = getString(R.string.start_generating)
            activity_main_button.setOnClickListener { createGenerator() }
        }
    }

    private fun createGenerator() {
        val polynomial = activity_main_polynomial_input.text.toString()
        val interval = activity_main_interval_input.text.toString().toLongOrNull() ?: 1000L
        try {
            generatorLFSR = GeneratorLFSR(polynomial, interval)
        } catch (e: WrongKeyFormatException) {
            Toast.makeText(this, e.messageRes, Toast.LENGTH_LONG).show()
        }
        generatorLFSR?.let { startGenerator() }
    }

    private fun startGenerator() {
        activity_main_output.text.clear()
        setupButton(true)
        generatorLFSR?.generateObservable()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(
                        { bit ->
                            val oldText = activity_main_output.text.toString()
                            val newValue = if (bit) "1" else "0"
                            val newText = "$newValue$oldText"
                            activity_main_output.setText(newText)
                        },
                        { _ -> Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show() }
                )
                ?.addToDisposables()
    }

    private fun stopGenerator() {
        compositeDisposable.clear()
        generatorLFSR = null
        setupButton(false)
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun Disposable.addToDisposables() {
        compositeDisposable.add(this)
    }

}