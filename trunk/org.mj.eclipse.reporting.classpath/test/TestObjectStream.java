/**
 * Copyright (c) 2008, Mounir Jarraï
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. All advertising materials mentioning features or use of this software
 *       must display the following acknowledgement:
 *			This product includes software developed by Mounir Jarraï
 *      	and its contributors.
 *    4. Neither the name Mounir Jarraï nor the names of its contributors may 
 *       be used to endorse or promote products derived from this software 
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY MOUNIR JARRAÏ ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL MOUNIR JARRAÏ BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;

import junit.framework.TestCase;

/**
 * @author Mounir Jarraï
 *
 */
public class TestObjectStream extends TestCase {

	public TestObjectStream(String name) {
		super(name);
	}

	/**
	 * @throws java.lang.Exception
	 */
	protected void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	protected void tearDown() throws Exception {
	}

	public void testIS() throws IOException, ClassNotFoundException {
		//Write object 
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		DataToSerialize writeObject = new DataToSerialize(23, "Bat shit !", new Long(20));
		oos.writeObject(writeObject);
		System.out.println(baos.toByteArray());
		System.out.println(baos.toString());

		// Read object back
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		DataToSerialize readObject = (DataToSerialize) ois.readObject();

		assertTrue(writeObject.equals(readObject));

	}

	public void testKeyUnicity() {
		long key11 = Thread.currentThread().getId() ^ Thread.currentThread().getStackTrace().hashCode();
		long key12 = Thread.currentThread().getId() ^ Thread.currentThread().getStackTrace().hashCode();
		assertFalse(key11 == key12);

		long key21 = Thread.currentThread().getId() ^ Thread.currentThread().getStackTrace().hashCode() ^ System.currentTimeMillis();
		long key22 = Thread.currentThread().getId() ^ Thread.currentThread().getStackTrace().hashCode() ^ System.currentTimeMillis();
		assertFalse(key21 == key22);

		long key31 = Thread.currentThread().getId() ^ Thread.currentThread().getStackTrace().hashCode()
				^ Calendar.getInstance().getTimeInMillis();
		long key32 = Thread.currentThread().getId() ^ Thread.currentThread().getStackTrace().hashCode()
				^ Calendar.getInstance().getTimeInMillis();
		assertFalse(key31 == key32);

	}

}

/**
 * @author mjarra�
 * 
 */
class DataToSerialize implements Serializable {

	int value;
	String desc;
	Serializable any;

	DataToSerialize(int value, String desc, Serializable any) {
		this.value = value;
		this.desc = desc;
		this.any = any;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((any == null)
				? 0
				: any.hashCode());
		result = prime * result + ((desc == null)
				? 0
				: desc.hashCode());
		result = prime * result + value;
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DataToSerialize other = (DataToSerialize) obj;
		if (any == null) {
			if (other.any != null)
				return false;
		} else if (!any.equals(other.any))
			return false;
		if (desc == null) {
			if (other.desc != null)
				return false;
		} else if (!desc.equals(other.desc))
			return false;
		if (value != other.value)
			return false;
		return true;
	}
}