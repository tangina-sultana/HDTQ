/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/compact/bitmap/Bitmap375.java $
 * Revision: $Rev: 129 $
 * Last modified: $Date: 2013-01-21 00:08:27 +0000 (lun, 21 ene 2013) $
 * Last modified by: $Author: mario.arias $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contacting the authors:
 *   Mario Arias:               mario.arias@deri.org
 *   Javier D. Fernandez:       jfergar@infor.uva.es
 *   Miguel A. Martinez-Prieto: migumar2@infor.uva.es
 */

package org.rdfhdt.hdt.compact.bitmap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.roaringbitmap.RoaringBitmap;

/**
 * Implements an index on top of the Bitmap64 to solve select and rank queries more efficiently.
 * 
 * index -> O(n) rank1 -> O(1) select1 -> O(log log n)
 * 
 * @author mario.arias
 *
 */
public class BitmapRoaring64 implements ModifiableBitmap {
	// Constants

	// for long, it would be Roaring64NavigableMap, but they are a bit buggy
	// Roaring64NavigableMap r;
	RoaringBitmap r;

	public BitmapRoaring64() {
		// r = new Roaring64NavigableMap();
		r = new RoaringBitmap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hdt.compact.bitmap.Bitmap#access(long)
	 */
	@Override
	public boolean access(long bitIndex) {
		return r.contains((int) bitIndex);
	}

	public void set(long bitIndex, boolean value) {
		if (value == true)
			r.add((int) bitIndex);
		else
			// r.removeLong(bitIndex);
			r.remove((int) bitIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hdt.compact.bitmap.Bitmap#rank1(long)
	 */
	@Override
	public long rank1(long pos) {
		// for long
		// return r.rankLong(pos);
		if (pos >= 0)
			return r.rank((int) pos);
		else
			return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hdt.compact.bitmap.Bitmap#rank0(long)
	 */
	@Override
	public long rank0(long pos) {
		throw new NotImplementedException("rank0 method is not implemented in Roaring Bitmaps");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hdt.compact.bitmap.Bitmap#select0(long)
	 */
	@Override
	public long select0(long x) {
		throw new NotImplementedException("rank0 method is not implemented in Roaring Bitmaps");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hdt.compact.bitmap.Bitmap#select1(long)
	 */
	@Override
	public long select1(long x) {
		long position = x - 1;
		if (position == -1) {
			return -1;
		}
		if (position < r.getLongCardinality()) {
			return r.select((int) position);
		} else {
			return r.select((int) r.getLongCardinality() - 1) + 1;
		}

	}

	@Override
	public long selectPrev1(long start) {
		return select1(rank1(start));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hdt.compact.bitmap.Bitmap#countOnes()
	 */
	@Override
	public long countOnes() {
		return r.getLongCardinality();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hdt.compact.bitmap.Bitmap#countZeros()
	 */
	@Override
	public long countZeros() {
		throw new NotImplementedException("rank0 method is not implemented in Roaring Bitmaps");
	}

	@Override
	public long getSizeBytes() {
		return r.serializedSizeInBytes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hdt.compact.bitmap.Bitmap#getType()
	 */
	@Override
	public String getType() {
		return HDTVocabulary.BITMAP_TYPE_ROARING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hdt.compact.bitmap.Bitmap#save(java.io.OutputStream, hdt.listener.ProgressListener)
	 */
	@Override
	public void save(OutputStream output, ProgressListener listener) throws IOException {
		// Write Type and Numbits
		output.write(BitmapFactory.TYPE_BITMAP_ROAR);
		DataOutputStream oos = new DataOutputStream(output);
		// r.writeExternal(oos); //seems it is not working properly
		r.runOptimize(); // optimize space with runlength compression (possible at the cost of speed)
		r.serialize(oos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hdt.compact.bitmap.Bitmap#load(java.io.InputStream, hdt.listener.ProgressListener)
	 */
	@Override
	public void load(InputStream input, ProgressListener listener) throws IOException {
		// Read type and numbits
		int type = input.read();
		if (type != BitmapFactory.TYPE_BITMAP_ROAR) {
			throw new IllegalArgumentException("Trying to read BitmapRoaring64 on a section that is not BitmapRoaring64");
		}
		DataInputStream iin = new DataInputStream(input);
		// r.readExternal(iin); ////seems it is not working properly
		r.deserialize(iin);
	}

	@Override
	public long selectNext1(long start) {
		
		long pos = rank1(start - 1);
		if (pos < r.getLongCardinality()) {
			return select1(pos + 1);
		} else
			return -1;

	}

	@Override
	public long getNumBits() {
		return 0;
	}

	@Override
	public void append(boolean value) {
		// TODO Auto-generated method stub

	}
}