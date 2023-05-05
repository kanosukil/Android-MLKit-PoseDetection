package edu.blazepose.fallencheck.notice

import android.util.Log
import androidx.lifecycle.ViewModel
import edu.blazepose.fallencheck.util.listToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.Date
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class MailViewModel : ViewModel() {
    companion object {
        private const val TAG = "Mail ViewModel"

        // 发送方邮箱
        private const val Default_UserName =

        // 邮箱 POP 协议的授权码
        private const val Default_UserPassword =
    }

    fun sendMail(toAddress: String, device: String, times: List<String>) {
        runBlocking(Dispatchers.IO) {
            try {
                val pro = Properties()
                pro["mail.smtp.host"] = "smtp.126.com"
                pro["mail.smtp.port"] = 25
                pro["mail.smtp.auth"] = true
                pro["mail.smtp.starttls.enable"] = true
                val message = MimeMessage(
                    Session.getInstance(pro, object : Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication =
                            PasswordAuthentication(Default_UserName, Default_UserPassword)
                    })/*.also { it.debug = true }*/
                )
                message.setFrom(InternetAddress(Default_UserName))
                message.setRecipient(Message.RecipientType.TO, InternetAddress(toAddress))
                message.setSubject("检测到跌倒事件", "UTF-8")
                message.sentDate = Date()
                message.setText(
                    "设备[$device]于:\n" +
                            "${listToString(times)}\n" +
                            "检测到跌倒事件(前面的都为疑似跌倒事件, 最后一个确定为跌倒事件)",
                    "UTF-8"
                )
                Transport.send(message) // 发送
            } catch (ex: Exception) {
                Log.e(TAG, "发送邮件异常: ${ex.localizedMessage}", ex)
            }
        }
    }
}