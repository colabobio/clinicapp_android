package org.broadinstitute.clinicapp.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec

/**
 * Manage cryptographic key in keystore
 */
//class Cryptography @Throws(
//    CertificateException::class,
//    NoSuchAlgorithmException::class,
//    KeyStoreException::class,
//    IOException::class,
//    NoSuchProviderException::class,
//    InvalidAlgorithmParameterException::class
//)
class Cryptography
constructor(private val keyName: String) {
    private var keyStore: KeyStore? = null
    private var secretKey: SecretKey? = null

    init {
        try {
            initKeystore()
            loadOrGenerateKey()
        }catch (e :Exception){
            e.printStackTrace()
        }

    }

    @Throws(NoSuchProviderException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class)
    private fun loadOrGenerateKey() {
        getKey()
        if (secretKey == null) generateKey()
    }

    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class)
    private fun initKeystore() {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore!!.load(null)
    }

    private fun getKey() {
        try {
            val secretKeyEntry = keyStore!!.getEntry(keyName, null) as KeyStore.SecretKeyEntry
            // if no key was found -> generate new
            secretKey = secretKeyEntry.secretKey
        } catch (e: KeyStoreException) {
            // failed to retrieve -> will generate new
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Throws(NoSuchProviderException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class)
    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        secretKey = keyGenerator.generateKey()
    }

    fun encrypt(toEncrypt: String): String {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT)
            val encrypted =
                Base64.encodeToString(cipher.doFinal(toEncrypt.toByteArray(StandardCharsets.UTF_8)), Base64.DEFAULT)
            return encrypted + SEPARATOR + iv
        }catch (e: NoSuchPaddingException) {
           e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }catch (e:IllegalBlockSizeException){
            e.printStackTrace()
        }catch (e : Exception){
            e.printStackTrace()
        }

        return toEncrypt
    }



    fun decrypt(toDecrypt: String): String {

      try {
          val parts = toDecrypt.split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
          if (parts.size != 2)
              return toDecrypt
              //throw AssertionError("String to decrypt must be of the form: 'BASE64_DATA" + SEPARATOR + "BASE64_IV'")
          val encrypted = Base64.decode(parts[0], Base64.DEFAULT)
          val iv = Base64.decode(parts[1], Base64.DEFAULT)
          val cipher = Cipher.getInstance(TRANSFORMATION)
          val spec = IvParameterSpec(iv)
          cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
          return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)

      }catch (e:IllegalBlockSizeException){
          e.printStackTrace()
          return  toDecrypt
      }
      catch (e : Exception){
          e.printStackTrace()
          return  toDecrypt
      }
    }


    companion object {
        private var INSTANCE: Cryptography? = null
        private const val KEY_ALIAS = "CLINIC-Crypto_app"
        private const val TRANSFORMATION =
            KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val SEPARATOR = ","

        @JvmStatic
        fun getInstance() =
            INSTANCE ?: synchronized(Cryptography::class.java) {
                    INSTANCE ?: Cryptography(KEY_ALIAS)
                        .also { INSTANCE = it }
            }
    }
}