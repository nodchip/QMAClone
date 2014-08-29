//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.server.handwriting;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Parser extends DefaultHandler {

  private static final Logger logger = Logger.getLogger(Parser.class.toString());
  private static final double SCALING = 1.0 / 1000;
  private final Map<Character, double[][][][]> data = new HashMap<Character, double[][][][]>();
  private char character = 0;
  private List<double[][][]> characters = null;
  private List<double[][]> strokes = null;
  private List<double[]> stroke = null;
  private final File file;

  public Parser(File file) {
    this.file = file;
  }

  public void run() {
    try {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      SAXParser parser = parserFactory.newSAXParser();
      parser.parse(file, this);
    } catch (Exception e) {
      logger.log(Level.WARNING, "ストロークファイルの入力に失敗しました", e);
    }
  }

  public void startElement(String uri, String localName, String name, Attributes attributes)
      throws SAXException {
    if (name.equals("character")) {
      characters = new ArrayList<double[][][]>();
    } else if (name.equals("utf8")) {
    } else if (name.equals("strokes")) {
      strokes = new ArrayList<double[][]>();
    } else if (name.equals("stroke")) {
      stroke = new ArrayList<double[]>();
    } else if (name.equals("point")) {
      final double x = Double.parseDouble(attributes.getValue("x")) * SCALING;
      final double y = Double.parseDouble(attributes.getValue("y")) * SCALING;
      stroke.add(new double[] { x, y });
    }
  }

  public void endElement(String uri, String localName, String name) throws SAXException {
    if (name.equals("character")) {
      data.put(character, characters.toArray(new double[0][][][]));
      characters = null;
    } else if (name.equals("utf8")) {
    } else if (name.equals("strokes")) {
      characters.add(strokes.toArray(new double[0][][]));
      strokes = null;
    } else if (name.equals("stroke")) {
      strokes.add(stroke.toArray(new double[0][]));
      stroke = null;
    } else if (name.equals("point")) {
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    character = ch[start];
  }

  public Map<Character, double[][][][]> getData() {
    return data;
  }
}
