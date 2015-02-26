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

package com.hotpads.datarouter.util.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class HashMethods{

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


	public static class Tests{
		@Test
		public void testLongDJB(){
			long hash1 = longDJBHash("public-school_HOLMES ELEMENTARY_4902 MT. ARARAT DR_SAN DIEGO_CA_92111");
			long hash2 = longDJBHash("private-school_Burleson Adventist School_1635 Fox Lane_Burleson_TX_76028");
			Assert.assertFalse(hash1 == hash2);
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