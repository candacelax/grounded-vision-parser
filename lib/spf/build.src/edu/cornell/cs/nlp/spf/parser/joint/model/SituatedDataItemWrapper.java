/*******************************************************************************
 * Copyright (C) 2011 - 2015 Yoav Artzi, All rights reserved.
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package edu.cornell.cs.nlp.spf.parser.joint.model;

import java.util.Map;

import edu.cornell.cs.nlp.spf.data.IDataItem;
import edu.cornell.cs.nlp.spf.data.sentence.Sentence;
import edu.cornell.cs.nlp.spf.data.situated.ISituatedDataItem;

/**
 * Wraps a situated data item to only expose the language component.
 * 
 * @author Yoav Artzi
 * @param <DI>
 * @param <STATE>
 */
public class SituatedDataItemWrapper<DI extends ISituatedDataItem<Sentence, STATE>, STATE>
		implements IDataItem<Sentence> {
	
	private static final long			serialVersionUID	= 9125402561551010485L;
	private final IDataItem<Sentence>	sample;
	private final DI					situatedDataItem;
	
	public SituatedDataItemWrapper(DI situatedDataItem) {
		this.sample = situatedDataItem.getSample();
		this.situatedDataItem = situatedDataItem;
	}
	
	@Override
	public Sentence getSample() {
		return sample.getSample();
	}
	
	public DI getSituatedDataItem() {
		return situatedDataItem;
	}

	@Override
	public Map<String, String> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean existsInCache(String parse) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validInCache(String parse) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addToCache(String parse, Boolean isValid) {
		// TODO Auto-generated method stub
		
	}
	
}
