package net.appitiza.sunmisample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.appitiza.sunmisample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var  printerInstance : PrintHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        printerInstance = PrintHelper(MainActivity@this)
        setClick()
    }
   private fun setClick()
    {
        mBinding.btnPrint.setOnClickListener {
            if (printerInstance.getPrinterStatus())
            {
                printerInstance.printTest()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        printerInstance.deInitialize(MainActivity@this)
    }
}