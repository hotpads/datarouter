package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.google.common.base.Objects;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClientFactory;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.java.ReflectionTool;

/*
 * TODO This class needs work to remove intermediate objects to reduce GC pressure.
 */
public class HBaseSubEntityResultParser<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final EntityFieldInfo<EK,E> entityFieldInfo;
	private final EntityPartitioner<EK> partitioner;
	private final DatabeanFieldInfo<PK,D,F> fieldInfo;


	public HBaseSubEntityResultParser(EntityFieldInfo<EK,E> entityFieldInfo, DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.entityFieldInfo = entityFieldInfo;
		this.partitioner = entityFieldInfo.getEntityPartitioner();
		this.fieldInfo = fieldInfo;
	}


	/***************** parse EK, PK, and column name ************************/

	public EK getEkFromRowBytes(byte[] rowBytes){
		EK ek = ReflectionTool.create(entityFieldInfo.getEntityKeyClass());
		int byteOffset = partitioner.getNumPrefixBytes();
		for(Field<?> field : fieldInfo.getEkFields()){
			if(byteOffset == rowBytes.length) {// ran out of bytes. leave remaining fields blank
				break;
			}
			Object value = field.fromBytesWithSeparatorButDoNotSet(rowBytes, byteOffset);
			field.setUsingReflection(ek, value);
			byteOffset += field.numBytesWithSeparator(rowBytes, byteOffset);
		}
		return ek;
	}

	public Pair<PK,String> parsePrimaryKeyAndFieldName(Cell cell){
		PK pk = ReflectionTool.create(fieldInfo.getPrimaryKeyClass());
		//EK
		//be sure to get the entity key fields from DatabeanFieldInfo in case the PK overrode the EK field names
		parseEkFieldsFromBytesToPk(cell, pk);
		//post-EK
		int fieldNameOffset = parsePostEkFieldsFromBytesToPk(cell, pk);
		//fieldName
		String fieldName = StringByteTool.fromUtf8BytesOffset(CellUtil.cloneQualifier(cell), fieldNameOffset);
		return new Pair<>(pk, fieldName);
	}


	/****************** parse multiple hbase rows ********************/

	public List<PK> getPrimaryKeysWithMatchingQualifierPrefixMulti(Result[] rows){
		List<PK> results = new ArrayList<>();
		for(Result row : rows){
			if(row.isEmpty()) {
				continue;
			}
			ArrayList<PK> pksFromSingleGet = getPrimaryKeysWithMatchingQualifierPrefix(row);
			results.addAll(DrCollectionTool.nullSafe(pksFromSingleGet));
		}
		return results;
	}

	public List<D> getDatabeansWithMatchingQualifierPrefixMulti(Result[] rows){
		List<D> results = new ArrayList<>();
		for(Result row : rows){
			if(row.isEmpty()) {
				continue;
			}
			List<D> databeansFromSingleGet = getDatabeansWithMatchingQualifierPrefix(row, null);
			results.addAll(DrCollectionTool.nullSafe(databeansFromSingleGet));
		}
		return results;
	}


	/****************** parse single hbase row ********************/

	public ArrayList<PK> getPrimaryKeysWithMatchingQualifierPrefix(Result row){
		return getPrimaryKeysWithMatchingQualifierPrefix(row, null);
	}

	public ArrayList<PK> getPrimaryKeysWithMatchingQualifierPrefix(Result row, Integer limit){
		if(row == null) {
			return new ArrayList<>(0);
		}
		//unfortunately, we expect a bunch of duplicate PK's
		ArrayList<PK> pks = new ArrayList<>();
		PK previousPk = null;
		for(Cell cell : DrIterableTool.nullSafe(row.listCells())){//row.list() can return null
			if(!matchesNodePrefix(cell)) {
				continue;
			}
			Pair<PK,String> pkAndFieldName = parsePrimaryKeyAndFieldName(cell);
			PK pk = pkAndFieldName.getLeft();
			if(Objects.equal(previousPk, pk)){
				continue;
			}
			pks.add(pk);
			if(limit != null && pks.size() >= limit){
				break;
			}
			previousPk = pk;
		}
		return pks;
	}

	public List<D> getDatabeansWithMatchingQualifierPrefix(Result row, Integer limit){
		if(row == null) {
			return Collections.emptyList();
		}
		return getDatabeansForKvsWithMatchingQualifierPrefix(row.listCells(), limit);
	}

	public List<D> getDatabeansForKvsWithMatchingQualifierPrefix(List<Cell> cells, Integer limit){
		if(DrCollectionTool.isEmpty(cells)) {
			return Collections.emptyList();
		}
		List<D> databeans = new ArrayList<>();
		D databean = null;
		for(Cell cell : cells){
			if(!matchesNodePrefix(cell)) {
				continue;
			}
			Pair<PK,String> pkAndFieldName = parsePrimaryKeyAndFieldName(cell);
			if(databean == null || DrObjectTool.notEquals(databean.getKey(), pkAndFieldName.getLeft())){
				//we're about to start a new databean
				if(limit != null && databeans.size() == limit){
					break;
				}
				databean = fieldInfo.getDatabeanSupplier().get();
				ReflectionTool.set(fieldInfo.getKeyJavaField(), databean, pkAndFieldName.getLeft());
				databeans.add(databean);
			}
			setDatabeanField(databean, pkAndFieldName.getRight(), CellUtil.cloneValue(cell));
		}
		return databeans;
	}

	public D makeDatabeanWithOneField(Cell cell){
		Pair<PK,String> pkAndFieldName = parsePrimaryKeyAndFieldName(cell);
		D databean = fieldInfo.getDatabeanSupplier().get();
		ReflectionTool.set(fieldInfo.getKeyJavaField(), databean, pkAndFieldName.getLeft());
		setDatabeanField(databean, pkAndFieldName.getRight(), cell.getValue());
		return databean;
	}

	public void setDatabeanField(D databean, String fieldName, byte[] bytesValue){
		Field<?> field = null;
		if(HBaseClientFactory.DUMMY_COL_NAME.equals(fieldName)){
			return;
		}
		field = fieldInfo.getNonKeyFieldByColumnName().get(fieldName);
		if(field == null){//field doesn't exist in the databean anymore.  skip it
			return;
		}
		//set the databean field value for this hbase cell
		Object value = field.fromBytesButDoNotSet(bytesValue, 0);
		field.setUsingReflection(databean, value);
	}


	/****************** private ********************/

	private boolean matchesNodePrefix(Cell cell){
		byte[] prefix = fieldInfo.getEntityColumnPrefixBytes();
		if(cell.getQualifierLength() < prefix.length){
			return false;
		}
		return Bytes.equals(cell.getValueArray(), cell.getQualifierOffset(), prefix.length, prefix, 0, prefix.length);
	}

	//parse the hbase row bytes after the partition offset
	private int parseEkFieldsFromBytesToPk(Cell cell, PK targetPk){
		int offset = partitioner.getNumPrefixBytes();
		byte[] fromBytes = CellUtil.cloneRow(cell);
		return parseFieldsFromBytesToPk(fieldInfo.getEkPkFields(), fromBytes, offset, targetPk);
	}

	//parse the hbase qualifier bytes
	private int parsePostEkFieldsFromBytesToPk(Cell cell, PK targetPk){
		byte[] entityColumnPrefixBytes = fieldInfo.getEntityColumnPrefixBytes();
		int offset = entityColumnPrefixBytes.length;
		byte[] fromBytes = CellUtil.cloneQualifier(cell);
		return parseFieldsFromBytesToPk(fieldInfo.getPostEkPkKeyFields(), fromBytes, offset, targetPk);
	}

	private int parseFieldsFromBytesToPk(List<Field<?>> fields, byte[] fromBytes, int offset, PK targetPk){
		int byteOffset = offset;
		for(Field<?> field : fields){
			if(byteOffset == fromBytes.length){// ran out of bytes. leave remaining fields blank
				break;
			}
			Object value = field.fromBytesWithSeparatorButDoNotSet(fromBytes, byteOffset);
			field.setUsingReflection(targetPk, value);
			byteOffset += field.numBytesWithSeparator(fromBytes, byteOffset);
		}
		return byteOffset;
	}
}
