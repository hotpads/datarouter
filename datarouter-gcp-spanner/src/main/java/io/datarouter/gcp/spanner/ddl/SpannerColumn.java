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
package io.datarouter.gcp.spanner.ddl;

import java.util.Objects;

public class SpannerColumn{

	private final String name;
	private final SpannerColumnType type;
	private final Boolean nullable;

	public SpannerColumn(String name, SpannerColumnType type, Boolean nullable){
		this.name = name;
		this.type = type;
		this.nullable = nullable;
	}

	public String generateColumnDef(){
		StringBuilder sb = new StringBuilder();
		sb.append(" ");
		sb.append(name);
		sb.append(" ");
		String columnDef = type.getSpannerType().getCode().name();
		if(type.requiresLength()){
			columnDef += "(MAX)";
		}
		if(type.isArray()){
			sb.append("ARRAY<");
			sb.append(columnDef);
			sb.append(">");
		}else{
			sb.append(columnDef);
		}
		if(!nullable){
			sb.append(" ");
			sb.append("NOT NULL");
		}
		return sb.toString();
	}

	public SpannerColumnType getType(){
		return type;
	}

	public String getName(){
		return name;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null || getClass() != obj.getClass()){
			return false;
		}
		SpannerColumn that = (SpannerColumn)obj;
		return Objects.equals(name, that.name) && type == that.type && Objects.equals(nullable, that.nullable);
	}

	@Override
	public int hashCode(){
		return Objects.hash(name, type, nullable);
	}

}
