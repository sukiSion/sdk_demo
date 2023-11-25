package cn.dabby.demo.sdk

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import cn.dabby.demo.sdk.bean.CertTokenRequestEntity
import cn.dabby.demo.sdk.bean.IdInfoEntity
import cn.dabby.demo.sdk.bean.SigBean
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * @author Sion
 * @date 2023/11/24 15:46
 * @version 1.0.0
 * @description
 **/
object NetApi {
    const val HOST = "https://auth.weijing.gov.cn/v2/api/"
    const val TEST = "https://sit.weijing.gov.cn/v2/api/"

    private val handle by lazy {
        Handler(
            Looper.getMainLooper()
        )
    }

    fun requestAccessToken(
        onStart: () -> Unit,
        onFail: (message: String) -> Unit,
        onSuccess: (mAccessToken: String) -> Unit
    ){
        onStart()
        OkHttpClient.getOkHttpClient().newCall(
           Request.Builder()
               .url(Uri.parse("${TEST}getaccesstoken").buildUpon()
                   .appendQueryParameter(
                       "clientId" , "f683b3886bb4b0b8"
                   )
                   .appendQueryParameter(
                       "clientSecret" , "a7930628-b126-45a1-bb75-1a006093bb0b"
                   )
                   .build().toString().toHttpUrl())
               .get()
               .build()
        ).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("reponseAccessToken" , "onFailure")
                onFail(e.stackTraceToString())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string()
                Log.d("reponseAccessToken" , "${responseString}")
                try {
                    val jsonObject = JSONObject(responseString)
                    val retCode = jsonObject.getInt("retCode")
                    if (retCode == 0) {
                        val mAccessToken = jsonObject.getString("accessToken")
                        onSuccess(mAccessToken)
                    }else{
                        val retMessage = jsonObject.getString("retMessage")
                        onFail(retMessage)
                    }
                }catch (e: JSONException){
                    e.printStackTrace()
                }
            }
        })
    }

    fun requestCertToken(
        accessToken: String,
        fullName: String,
        idNum: String,
        onFail: (message: String) -> Unit,
        onSuccess: (certToken: String, timeStamp: String, authMode: Int) -> Unit
    ){
        OkHttpClient.getOkHttpClient().newCall(
            Request.Builder()
                .url("${TEST}authreq")
                .post(Gson().toJson(
                    CertTokenRequestEntity(
                        accessToken = accessToken,
                        authType = "SdkRegular",
                        mode = 66,
                        idInfo = IdInfoEntity(
                            fullName = fullName,
                            idNum = idNum
                        )
                    )
                ) .toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()
        ).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                onFail(e.stackTraceToString())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string()
                Log.d("certToken" , "${responseString}")
                try {
                    val jsonObject = JSONObject(responseString)
                    val retCode = jsonObject["retCode"]
                    if (retCode == 0) {
                        val ctidData = jsonObject.getJSONObject("ctidData")
                        val authMode = ctidData.getInt("authMode")
                        val tokenInfo = jsonObject.getJSONObject("tokenInfo")
                        val certToken = tokenInfo.getString("certToken")
                        val timeStamp = tokenInfo.getString("timestamp")
                        onSuccess(certToken , timeStamp , authMode)
                    }else{
                        val retMessage = jsonObject.getString("retMessage")
                        onFail(retMessage)
                    }
                }catch (e: JSONException){
                    e.printStackTrace()
                }
            }
        })
    }

    fun requestCertTokenSignature(
        accessToken: String,
        certToken: String,
        timeStamp: String,
        onFail: (message: String) -> Unit,
        onSuccess: (certTokenSignature: String) -> Unit
    ){
        OkHttpClient.getOkHttpClient().newCall(
            Request.Builder()
                .url("${TEST}tpsig")
                .post(Gson().toJson(
                    SigBean(
                        accessToken,
                        certToken,
                        timeStamp
                    )
                ).toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()
        ).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                onFail(e.stackTraceToString())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string()
                Log.d("requestSignature", "${responseString}")
                try{
                    val jsonObject = JSONObject(responseString)
                    val certTokenSignature = jsonObject.getString("sig")
                    onSuccess(certTokenSignature)
                }catch (e: JSONException){
                    e.printStackTrace()
                }
            }
        })
    }
}