/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.web.http;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DigRunnerTests{

	private static final String RESULT = """
			; <<>> DiG 9.16.1-Ubuntu <<>> datarouter.io
			;; global options: +cmd
			;; Got answer:
			;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 15301
			;; flags: qr rd ra; QUERY: 1, ANSWER: 2, AUTHORITY: 0, ADDITIONAL: 1

			;; OPT PSEUDOSECTION:
			; EDNS: version: 0, flags:; udp: 65494
			;; QUESTION SECTION:
			;datarouter.io.			IN	A

			;; ANSWER SECTION:
			datarouter.io.		599	IN	A	3.33.152.147
			datarouter.io.		599	IN	A	15.197.142.173

			;; Query time: 0 msec
			;; SERVER: 127.0.0.53#53(127.0.0.53)
			;; WHEN: Wed Apr 06 13:56:52 PDT 2022
			;; MSG SIZE  rcvd: 74
			""";

	private static final String NO_RESULT = """
			; <<>> DiG 9.16.1-Ubuntu <<>> nothing.datarouter.io
			;; global options: +cmd
			;; Got answer:
			;; ->>HEADER<<- opcode: QUERY, status: NXDOMAIN, id: 32432
			;; flags: qr rd ra; QUERY: 1, ANSWER: 0, AUTHORITY: 0, ADDITIONAL: 1

			;; OPT PSEUDOSECTION:
			; EDNS: version: 0, flags:; udp: 65494
			;; QUESTION SECTION:
			;nothing.datarouter.io.		IN	A

			;; Query time: 88 msec
			;; SERVER: 127.0.0.53#53(127.0.0.53)
			;; WHEN: Wed Apr 06 13:57:48 PDT 2022
			;; MSG SIZE  rcvd: 50
			""";

	@Test
	public void testParse(){
		Assert.assertEquals(DigRunner.parse(RESULT).size(), 2);
		Assert.assertEquals(DigRunner.parse(NO_RESULT).size(), 0);
	}

	@Test
	public void parseAnswer(){
		assertTheAnswer("datarouter.io.		599	IN	A	3.33.152.147");
		assertTheAnswer("datarouter.io. 599	IN	A	3.33.152.147");
		assertTheAnswer("datarouter.io. 599 IN	A	3.33.152.147");
		assertTheAnswer("datarouter.io. 599 IN A	3.33.152.147");
	}

	private void assertTheAnswer(String string){
		DnsAnswer dnsAnswer = DigRunner.parseAnswer(string);
		Assert.assertEquals(dnsAnswer.name, "datarouter.io.");
		Assert.assertEquals(dnsAnswer.ttl, "599");
		Assert.assertEquals(dnsAnswer.clazz, "IN");
		Assert.assertEquals(dnsAnswer.type, "A");
		Assert.assertEquals(dnsAnswer.value, "3.33.152.147");
	}
}
