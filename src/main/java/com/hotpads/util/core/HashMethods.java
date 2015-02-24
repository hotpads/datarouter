/*
 ********************************************************************
 *                                                                  *
 *                  General Hash Functions Library                  *
 * Author: Arash Partow - 2002                                      *
 * URL: http://www.partow.net                                       *
 *                                                                  *
 * Copyright Notice:                                                *
 * Free use of this library is permitted under the guidelines and   *
 * in accordance with the most current version of the Common Public *
 * License.                                                         *
 * http://www.opensource.org/licenses/cpl.php                       *
 *                                                                  *
 ********************************************************************
 */

package com.hotpads.util.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class HashMethods{

	public static long RSHash(String str){
		int b = 378551;
		int a = 63689;
		long hash = 0;
		for(int i = 0; i < str.length(); i++){
			hash = hash * a + str.charAt(i);
			a = a * b;
		}
		return (hash & 0x7FFFFFFFFFFFFFFFl);
	}

	public static long JSHash(String str){
		long hash = 1315423911;
		for(int i = 0; i < str.length(); i++){
			hash ^= ((hash << 5) + str.charAt(i) + (hash >> 2));
		}
		return (hash & 0x7FFFFFFFFFFFFFFFl);
	}

	public static long PJWHash(String str){
		long BitsInUnignedInt = (long)(4 * 8);
		long ThreeQuarters = (long)((BitsInUnignedInt * 3) / 4);
		long OneEighth = (long)(BitsInUnignedInt / 8);
		long HighBits = (long)(0xFFFFFFFF) << (BitsInUnignedInt - OneEighth);
		long hash = 0;
		long test = 0;
		for(int i = 0; i < str.length(); i++){
			hash = (hash << OneEighth) + str.charAt(i);
			if((test = hash & HighBits) != 0){
				hash = ((hash ^ (test >> ThreeQuarters)) & (~HighBits));
			}
		}
		return (hash & 0x7FFFFFFFFFFFFFFFl);
	}

	public static long ELFHash(String str){
		long hash = 0;
		long x = 0;
		for(int i = 0; i < str.length(); i++){
			hash = (hash << 4) + str.charAt(i);
			if((x = hash & 0xF0000000L) != 0){
				hash ^= (x >> 24);
				hash &= ~x;
			}
		}
		return (hash & 0x7FFFFFFFFFFFFFFFl);
	}

	public static long BKDRHash(String str){
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;
		for(int i = 0; i < str.length(); i++){
			hash = (hash * seed) + str.charAt(i);
		}
		return (hash & 0x7FFFFFFFFFFFFFFFl);
	}

	public static long SDBMHash(String str){
		long hash = 0;
		for(int i = 0; i < str.length(); i++){
			hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
		}
		return (hash & 0x7FFFFFFFFFFFFFFFl);
	}

	@Deprecated
	// Returns an int in a long. Do not change - used for some ids
	public static long intDJBHash(String str){
		int hash = 5381;
		for(int i = 0; i < str.length(); i++){
			hash = ((hash << 5) + hash) + str.charAt(i);
		}
		return (hash & 0x7FFFFFFF);
	}

	public static long longDJBHash(String str){
		long hash = 5381l;
		for(int i = 0; i < str.length(); i++){
			hash = ((hash << 5) + hash) + str.charAt(i);
		}
		return (hash & 0x7FFFFFFFFFFFFFFFl);
	}

	public static long longDJBHash(byte[] in){
		long hash = 5381l;
		for(int i = 0; i < in.length; i++){
			hash = ((hash << 5) + hash) + in[i];
		}
		return (hash & 0x7FFFFFFFFFFFFFFFl);
	}

	public static long APHash(String str){
		long hash = 0;
		for(int i = 0; i < str.length(); i++){
			if((i & 1) == 0){
				hash ^= ((hash << 7) ^ str.charAt(i) ^ (hash >> 3));
			}else{
				hash ^= (~((hash << 11) ^ str.charAt(i) ^ (hash >> 5)));
			}
		}
		return (hash & 0x7FFFFFFFFFFFFFFFl);
	}

	public static Long longMD5DJBHash(String in){
		return longMD5DJBHash(StringByteTool.getUtf8Bytes(in));
	}

	public static Long longMD5DJBHash(byte[] in){
		try{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(in);
			return longDJBHash(md5.digest());

		}catch(NoSuchAlgorithmException e){
			return null;
		}
	}

	public static Long bytesMD5Hash(byte[] in){
		try{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(in);
			return LongByteTool.fromRawBytes(md5.digest(), 0);

		}catch(NoSuchAlgorithmException e){
			return null;
		}
	}

	public static String getMD5Hash(String stringToHash){
		return getMD5Hash(stringToHash.getBytes());
	}

	public static String getMD5Hash(byte[] originalBytes){
		try{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			StringBuilder sb = new StringBuilder();
			md5.update(originalBytes);
			String appendMe;
			for(byte b : md5.digest()){
				appendMe = Integer.toHexString(b & 0xff);
				if(appendMe.length() == 1){
					sb.append("0" + appendMe);
				}else{
					sb.append(appendMe);
				}
			}
			return sb.toString();
		}catch(Exception e){
			return null;
		}
	}

	public static class Tests{
		@Test
		public void testDJBCollision(){
			long hash1 = intDJBHash("public-school_HOLMES ELEMENTARY_4902 MT. ARARAT DR_SAN DIEGO_CA_92111");
			long hash2 = intDJBHash("private-school_Burleson Adventist School_1635 Fox Lane_Burleson_TX_76028");
			Assert.assertEquals(hash1, hash2);
		}
		@Test
		public void testLongDJB(){
			long hash1 = longDJBHash("public-school_HOLMES ELEMENTARY_4902 MT. ARARAT DR_SAN DIEGO_CA_92111");
			long hash2 = longDJBHash("private-school_Burleson Adventist School_1635 Fox Lane_Burleson_TX_76028");
			Assert.assertFalse(hash1 == hash2);
		}
		@Test
		public void testHashAreaId(){
			// ensure area ids continue to hash the same way
			Assert.assertEquals(7660878l, intDJBHash("CTY-24-02025"));
			Assert.assertEquals(16211954l, intDJBHash("ZIP-12-32008"));
		}
		@Test public void testMd5() throws NoSuchAlgorithmException{
			Set<Long> buckets = SetTool.createTreeSet();
			for(int serverNum = 98; serverNum <= 101; ++serverNum){
				String serverName = "HadoopNode98:10012:" + serverNum;
				for(int i = 0; i < 1000; ++i){
					Long bucket = longMD5DJBHash(StringByteTool.getUtf8Bytes(serverName+i));
					buckets.add(bucket);
				}
			}
			int counter = 0;
			double avg = 0;
			for(Long b : buckets){
				avg = (avg * counter + b) / (counter + 1);
				++counter;
			}
			double halfLong = Long.MAX_VALUE / 2;
			System.out.println(avg / halfLong * 100 + "%");
		}
	}
}