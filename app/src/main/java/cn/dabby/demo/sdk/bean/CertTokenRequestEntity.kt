package cn.dabby.demo.sdk.bean

/**
 * @author Sion
 * @date 2023/11/24 16:40
 * @version 1.0.0
 * @description
 **/
data class CertTokenRequestEntity(
    val accessToken: String,
    val authType: String,
    val mode: Int,
    val idInfo: IdInfoEntity
)

data class IdInfoEntity(
    val fullName: String,
    val idNum: String,
    val idStartDate: String = "",
    val idEndDate: String = ""
)