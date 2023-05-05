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

        // 发送方邮箱(意义等效于管理员邮箱)
        private const val Default_UserName =

        // 邮箱 POP/SMTP 协议的授权码(非邮箱密码)
        private const val Default_UserPassword =
    }

    /**
     * 发送邮件
     *
     * @param toAddress 收件人地址/用户地址
     * @param device 设备名(添加到邮件内容当中)
     * @param times 跌倒检测触发事件列表(添加到邮件内容当中)
     */
    fun sendMail(toAddress: String, device: String, times: List<String>) {
        runBlocking(Dispatchers.IO) {
            send(
                toAddress = toAddress,
                msg = "设备[$device]于:\n${listToString(times)}\n" +
                        "检测到跌倒事件(前面的都为疑似跌倒事件, 最后一个确定为跌倒事件)"
            )
        }
    }

    /**
     * 时间列表长度达到一定值但为触发跌倒事件时发送
     */
    fun sendMailSc(toAddress: String, device: String, times: List<String>) {
        runBlocking(Dispatchers.IO) {
            send(
                toAddress = toAddress,
                msg = "设备[$device]于:\n${listToString(times)}\n" +
                        "检测到疑似跌倒事件,未确定发生跌倒事件."
            )
        }
    }

    private fun send(toAddress: String, msg: String) {
        try {
            // 配置与邮件服务器建立连接的属性(发件人相关)
            val pro = Properties()
            pro["mail.smtp.host"] = "smtp.126.com"
            pro["mail.smtp.port"] = 25
            pro["mail.smtp.auth"] = true
            pro["mail.smtp.starttls.enable"] = true
            // 构建 Message 主体
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
                msg,
                "UTF-8"
            )
            // 发送邮件
            Transport.send(message)
        } catch (ex: Exception) {
            Log.e(TAG, "发送邮件异常: ${ex.localizedMessage}", ex)
        }
    }
}