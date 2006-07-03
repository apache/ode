package com.fs.pxe.ql.jcc;

import java.io.StringReader;

public class Parser extends QLParser {
    public Parser(String query) {
        super(new StringReader(query));
    }
}
