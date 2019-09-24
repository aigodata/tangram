package com.github.mengxianun.core.parser;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.parser.action.CRUDActionParser;
import com.github.mengxianun.core.parser.action.NativeActionParser;
import com.github.mengxianun.core.parser.action.SqlActionParser;
import com.github.mengxianun.core.parser.action.StructActionParser;
import com.github.mengxianun.core.parser.action.StructsActionParser;
import com.github.mengxianun.core.parser.action.TransactionActionParser;
import com.github.mengxianun.core.parser.info.SimpleInfo;

public class ParserFactory {

	private ParserFactory() {
		throw new AssertionError();
	}

	public static ActionParser getActionParser(SimpleInfo simpleInfo, DataContext dataContext) {
		switch (simpleInfo.operation()) {
		case STRUCT:
			return new StructActionParser(simpleInfo, dataContext);
		case STRUCTS:
			return new StructsActionParser(simpleInfo, dataContext);
		case TRANSACTION:
			return new TransactionActionParser(simpleInfo, dataContext);
		case SQL:
			return new SqlActionParser(simpleInfo, dataContext);
		case NATIVE:
			return new NativeActionParser(simpleInfo, dataContext);
		case DETAIL:
		case QUERY:
		case SELECT:
		case SELECT_DISTINCT:
		case INSERT:
		case UPDATE:
		case DELETE:
			return new CRUDActionParser(simpleInfo, dataContext);

		default:
			break;
		}
		throw new UnsupportedOperationException();
	}

}
