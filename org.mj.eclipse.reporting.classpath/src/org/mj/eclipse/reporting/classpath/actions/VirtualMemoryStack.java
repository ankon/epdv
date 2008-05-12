/**
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.mj.eclipse.reporting.classpath.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Stack;

import org.mj.eclipse.reporting.classpath.Activator;

/**
 * @author Mounir Jarraï
 *
 * @param <T>
 */
public class VirtualMemoryStack<T> implements IStack<T> {

	private RandomAccessFile virtualStack;

	private Stack<StackIndex> internalReferences;

	/**
	 * @throws IOException
	 */
	public VirtualMemoryStack() throws IOException {
		File workDirectory = Activator.getDefault().getStateLocation().toFile();
		File tempFile = File.createTempFile(getFileName(), ".stack", workDirectory);
		virtualStack = new RandomAccessFile(tempFile, "rw");
		internalReferences = new Stack<StackIndex>();
	}

	/**
	 * @see org.mj.eclipse.reporting.classpath.actions.IStack#pop()
	 */
	public T pop() {
		try {
			StackIndex index = internalReferences.pop();
			virtualStack.seek(index.position);
			// Attention au cast en int ! essayer d'utiliser un ByteBuffer dans une version java.nio
			byte[] itemArray = new byte[(int) index.size];
			int read = virtualStack.read(itemArray);
			if (index.size != read) {
				throw new IllegalStateException("Readed size differs form object size !");
			}
//			virtualStack.setLength(virtualStack.length() - index.size);
			T item = arrayToObject(itemArray);
			return item;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @see org.mj.eclipse.reporting.classpath.actions.IStack#push(java.lang.Object)
	 */
	public T push(T item) {
		try {
			byte[] itemArray = objectToArray(item);
			StackIndex index = new StackIndex(virtualStack.getFilePointer(), itemArray.length);
			virtualStack.seek(virtualStack.length());
			virtualStack.write(itemArray);
			internalReferences.push(index);
			return item;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @see org.mj.eclipse.reporting.classpath.actions.IStack#isEmpty()
	 */
	public boolean isEmpty() {
		return internalReferences.isEmpty();
	}

	/**
	 * @see org.mj.eclipse.reporting.classpath.actions.IStack#clear()
	 */
	public void clear() {
		internalReferences.clear();
		// TODO remove files
	}

	/**
	 * @return
	 */
	private String getFileName() {
		Calendar.getInstance().getTimeInMillis();
		return String.valueOf(getUniqueIdWithinJVM()) + "_" + String.valueOf(Calendar.getInstance().getTimeInMillis());
	}

	/**
	 * @return
	 */
	private long getUniqueIdWithinJVM() {
		return Thread.currentThread().getId() ^ Thread.currentThread().getStackTrace().hashCode();
	}

	ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
	ObjectOutputStream oos = new ObjectOutputStream(baos);

	/**
	 * @param t
	 * @return
	 * @throws IOException
	 */
	private byte[] objectToArray(T t) throws IOException {
		try {
			oos.writeUnshared(t);
			return baos.toByteArray();
		} finally {
			if (baos != null) {
				baos.reset();
			}
			if (oos != null) {
//				oos.close();
			}
		}
	}
	

	/**
	 * @param array
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private T arrayToObject(byte array[]) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		try {
			bais = new ByteArrayInputStream(array);
			ois = new ObjectInputStream(bais);
			T readObject = (T) ois.readUnshared();
			return readObject;
		} finally {
			if (bais != null) {
				bais.close();
			}
			if (ois != null) {
				ois.close();
			}
		}
	}

	/**
	 * @author mjarra�
	 * 
	 */
	private static class StackIndex {
		long position = 0;
		long size = 0;

		public StackIndex(long position, long size) {
			this.position = position;
			this.size = size;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[position=" + position + ", size=" + size + "]";
		}

	}
}
