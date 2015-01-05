package com.hotpads.datarouter.util.ssl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import sun.security.provider.X509Factory;

import com.hotpads.util.core.io.ReaderTool;

public class KeystoreManager{

	private static final char[] PASSWORD = "changeit".toCharArray();
	
	private final KeyStore keystore;

	public KeystoreManager() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, NoSuchProviderException{
		this.keystore = KeyStore.getInstance(KeyStore.getDefaultType());
//		this.keystore = KeyStore.getInstance("JKS", "SUN");
		keystore.load(null, null);
	}

	public void addCertificates(String clientName, String dir, String certFilename, String caFilename,
			String keyFilename) throws KeyManagementException, CertificateException, KeyStoreException,
			NoSuchAlgorithmException, IOException, InvalidKeySpecException{
		X509Certificate clientCert = getCertFromFilesystem(dir, certFilename);
		addCertificate(clientName + "-cert", clientCert);
		X509Certificate caCert = getCertFromFilesystem(dir, caFilename);
		addCertificate(clientName + "-ca", caCert);

		X509Certificate[] chain = new X509Certificate[2];
		chain[0] = clientCert;
		chain[1] = caCert;

//		PrivateKey keyCert = getRsaPkcs8Base64KeyFromFilesystem(dir, keyFilename);
		PrivateKey keyCert = getRsaPemKeyFromFilesystem(dir, keyFilename);
//		PrivateKey keyCert = getRsaPkcs8KeyFromFilesystem(dir, keyFilename);
		addKey(clientName + "-key", keyCert, chain);

		reInitSslContext();
	}

	private void reInitSslContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException{
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keystore);
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, tmf.getTrustManagers(), null);
	}

	private void addCertificate(String alias, X509Certificate cert) throws CertificateException, KeyStoreException,
			NoSuchAlgorithmException, IOException, KeyManagementException{
		keystore.setCertificateEntry(alias, cert);
	}

	private void addKey(String alias, PrivateKey cert, X509Certificate[] certChain) throws CertificateException,
			KeyStoreException, NoSuchAlgorithmException, IOException, KeyManagementException{
		keystore.setKeyEntry(alias, cert, PASSWORD, certChain);
//		keystore.setKeyEntry(alias, cert.getEncoded(), certChain);

	}

	private X509Certificate getCertFromFilesystem(String dir, String filename) throws FileNotFoundException,
			CertificateException{
		FileInputStream is = new FileInputStream(dir + filename);
	    String rawFile = ReaderTool.accumulateStringAndClose(is).toString();
	    String certBase64 = rawFile.replaceAll(X509Factory.BEGIN_CERT, "").replaceAll(X509Factory.END_CERT, "");
	    byte[] certBytes = new Base64().decode(certBase64);
		X509Certificate cert = (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(
				new ByteArrayInputStream(certBytes));
		return cert;
	}

//	private PrivateKey getRsaPkcs8Base64KeyFromFilesystem(String dir, String filename) throws FileNotFoundException,
//			CertificateException, NoSuchAlgorithmException, InvalidKeySpecException{
//		FileInputStream is = new FileInputStream(dir + filename);
//	    String rawFile = ReaderTool.accumulateStringAndClose(is).toString();
//	    String certBase64 = rawFile.replaceAll("-----BEGIN PRIVATE KEY-----", "").replaceAll("-----END PRIVATE KEY-----", "");
//	    byte[] certBytes = new Base64().decode(certBase64);
//		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(certBytes);
//		KeyFactory kf = KeyFactory.getInstance("RSA");
//		PrivateKey privKey = kf.generatePrivate(keySpec);
//		return privKey;
//	}
//
//	private PrivateKey getRsaPkcs8KeyFromFilesystem(String dir, String filename) throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, IOException{
//		FileInputStream is = new FileInputStream(dir + filename);
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		IOUtils.copy(is, baos);
//		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(baos.toByteArray());
//		KeyFactory kf = KeyFactory.getInstance("RSA");
//		PrivateKey privKey = kf.generatePrivate(keySpec);
//		return privKey;
//	}

	private PrivateKey getRsaPemKeyFromFilesystem(String dir, String filename) throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, IOException{
		PrivateKeyReader keyReader = new PrivateKeyReader(dir + filename);
		return keyReader.getPrivateKey();
	}
	
	
	/********************** tests ******************************/
	
	public static class Tests{
		String dir = "/home/mcorgan/gcloud/dstest/src/main/resources/private/cloudsql/db-a/";
		String cert = "client-cert.pem";
		String ca = "server-ca.pem";
		String key = "client-key.pem";
		
		@Test
		public void testGetCertFromFilesystem() throws Exception{
			new KeystoreManager().getCertFromFilesystem(dir, cert);
		}
		
		@Test
		public void testAddCertificates() throws Exception{
			new KeystoreManager().addCertificates("myclient", dir, cert, ca, key);
		}
	}
	
}
