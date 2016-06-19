package com.github.jferard.pgloaderutils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LineParser {
	private static final int CR = 0x0D;
	private static final int LF = 0x0A;

	List<Line> lines;
	private int size;
	private int lastChar;
	private Line curLine;

	LineParser(int defaultSize) {
		this.lastChar = 0;
		this.size = defaultSize;
		this.lines = new LinkedList<Line>();
		this.curLine = new Line(this.size);
	}

	void put(int c) {
		if (this.lastChar == CR) {
			if (c == LF) {
				this.lastChar = LF;
			} else {
				this.curLine.append(c);
				this.lastChar = 0;
			}
		} else {
			if (c == CR || c == LF) {
				this.lines.add(this.curLine);
				this.curLine = new Line(this.size);
				this.lastChar = c;
			} else
				this.curLine.append(c);
		}
	}

	public List<Line> getLines() {
		return this.lines;
	}
}
