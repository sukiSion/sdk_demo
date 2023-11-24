package cn.dabby.demo.sdk

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cn.dabby.demo.sdk.databinding.ActivityMainBinding
import cn.weijing.sdk.wiiauth.IIdAuthAidlListener
import cn.weijing.sdk.wiiauth.IWiiAuthAidlInterface
import cn.weijing.sdk.wiiauth.entities.AuthRequestContent
import cn.weijing.sdk.wiiauth.entities.AuthResultContent
import cn.weijing.sdk.wiiauth.entities.WiiAuthConfigBean
import java.lang.ref.WeakReference
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var wiiAuthInterface: IWiiAuthAidlInterface
    private  val activityResultLauncher: ActivityResultLauncher<String> by lazy {
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){
            if(!it){
                Toast.makeText(this , "请给予相机权限" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    class IdAuthAidlListener(
        val activityRef: WeakReference<AppCompatActivity>
    ): IIdAuthAidlListener.Stub() {

        override fun onAuthResult(p0: AuthResultContent?) {
            p0?.let {
                val logStr = String.format(
                    "调试：\n\n返回码: %d\n\n返回信息: %s\n\nToken: %s",
                    it.retCode,
                    it.retMessage,
                    it.certToken
                )
                activityRef.get()?.let {
                        Toast.makeText(it , logStr, Toast.LENGTH_SHORT).show()

                }

            }
        }
    }



    private val serviceConnection by lazy {
        object : ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d("Sion", "onServiceConnected: "
                        + "\npid: " + Process.myPid()
                        + "\nThread: " + Thread.currentThread())
                try {
                    wiiAuthInterface = IWiiAuthAidlInterface.Stub.asInterface(service)
                    wiiAuthInterface.setWiiAuthConfig(
                        WiiAuthConfigBean().also {
                            it.setIsDebugMode(false)
                            it.setH("test")
                        }
                    )
                    wiiAuthInterface.setIdAuthResultCallback(IdAuthAidlListener(WeakReference(this@MainActivity)))
                }catch (e: RemoteException){
                    e.printStackTrace()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        bindService(
            Intent("android.intent.action.wiiauth.service").also {
                it.setPackage(packageName)
            },
            serviceConnection,
            BIND_AUTO_CREATE
        )
        activityResultLauncher.launch(Manifest.permission.CAMERA)
        activityMainBinding.btAuth.setOnClickListener {
            if(this.packageManager.checkPermission(Manifest.permission.CAMERA , packageName) == PackageManager.PERMISSION_DENIED){
                activityResultLauncher.launch(Manifest.permission.CAMERA)
                return@setOnClickListener
            }
            NetApi.requestAccessToken(
                onFail = {

                }
            ){
                accessToken ->
                val fullName = activityMainBinding.etName.text.toString()
                val idNum = activityMainBinding.etIdCard.text.toString()
                if(fullName.isNotBlank() && idNum.isNotBlank()){
                    NetApi.requestCertToken(
                        accessToken,
                        fullName = fullName,
                        idNum = idNum,
                        onFail = {

                        }
                    ){
                            certToken: String, timeStamp: String, authMode: Int ->
                        NetApi.requestCertTokenSignature(
                            accessToken,
                            certToken,
                            timeStamp,
                            {

                            }
                        ){
                            certTokenSignature ->
                            if(this::wiiAuthInterface.isInitialized){
                                Log.d("Sion", "人脸启动")
                                wiiAuthInterface.launchAuth(AuthRequestContent().also {
                                    it.fullName = fullName
                                    it.mode = 66
                                    it.idNum = idNum
                                    it.certToken = certToken
                                    it.certTokenSignature = certTokenSignature
                                    it.operationType = AuthRequestContent.OPERATION_INPUT_LIVENESS
                                    it.idStartDate = ""
                                    it.idEndDate = ""
                                })
                            }else{
                                Log.e("Sion" , "服务启动失败")
                            }
                        }
                    }
                }
            }
        }
    }
}