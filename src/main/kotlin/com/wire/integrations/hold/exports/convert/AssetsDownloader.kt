package com.wire.integrations.hold.exports.convert
//
//import java.io.File
//import java.lang.Exception
//import java.security.MessageDigest
//import java.util.Arrays
//
//class AssetsDownloader {
//
//    @Throws(Exception::class)
//    fun downloadAsset(api: API, message: MessageAssetBase): File? {
//        val file: File = com.wire.bots.hold.utils.Helper.assetFile(message.getAssetKey(), message.getMimeType())
//        val cipher: ByteArray = api.downloadAsset(message.getAssetKey(), message.getAssetToken())
//        val sha256 = MessageDigest.getInstance("SHA-256").digest(cipher)
//        if (!Arrays.equals(sha256, message.getSha256())) throw Exception("Failed sha256 check")
//        val image: ByteArray = Util.decrypt(message.getOtrKey(), cipher)
//        return com.wire.bots.hold.utils.Helper.save(image, file)
//    }
//
//
//}
