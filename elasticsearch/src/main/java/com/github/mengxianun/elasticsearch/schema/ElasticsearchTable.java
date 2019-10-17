package com.github.mengxianun.elasticsearch.schema;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.mengxianun.core.schema.AbstractTable;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.TableType;

public class ElasticsearchTable extends AbstractTable {

	private final Set<String> aliases;

	public ElasticsearchTable(String name, TableType type, Schema schema) {
		super(name, type, schema);
		this.aliases = new HashSet<>();
	}

	@Override
	public Map<String, Object> getInfo() {
		Map<String, Object> info = super.getInfo();
		info.put("aliases", aliases);
		return info;
	}

	public void addAlias(String alias) {
		aliases.add(alias);
	}

	public void addAliases(Set<String> aliases) {
		this.aliases.addAll(aliases);
	}

	public Set<String> getAliases() {
		return aliases;
	}

}
