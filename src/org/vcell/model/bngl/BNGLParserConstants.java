/* Generated By:JJTree&JavaCC: Do not edit this line. BNGLParserConstants.java */
package org.vcell.model.bngl;

public interface BNGLParserConstants {

  int EOF = 0;
  int PLUS = 5;
  int QUESTION_MARK = 6;
  int COMMA = 7;
  int NAMED_ATTRIBUTE = 8;
  int VARIABLE_ATTRIBUTE = 9;
  int MINUS = 10;
  int BOND = 11;
  int DOT = 12;
  int REACTION_ARROW_RIGHT = 13;
  int REACTION_ARROW_LEFT = 14;
  int REACTION_ARROW_BOTH = 15;
  int BEGIN = 16;
  int MODEL = 17;
  int END = 18;
  int MOLECULES = 19;
  int MOLECULE = 20;
  int SPECIES = 21;
  int SEED = 22;
  int ACTION = 23;
  int PARAMETERS = 24;
  int REACTION = 25;
  int RULES = 26;
  int TYPES = 27;
  int OBSERVABLES = 28;
  int INTEGER = 29;
  int IDENTIFIER = 30;
  int LETTER = 31;
  int DIGIT = 32;
  int ESCAPED_EXPRESSION = 33;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"+\"",
    "\"?\"",
    "\",\"",
    "\"~\"",
    "\"%\"",
    "\"-\"",
    "\"!\"",
    "\".\"",
    "\"->\"",
    "\"<-\"",
    "\"<->\"",
    "\"begin\"",
    "\"model\"",
    "\"end\"",
    "<MOLECULES>",
    "\"molecule\"",
    "<SPECIES>",
    "\"seed\"",
    "\"action\"",
    "\"parameters\"",
    "\"reaction\"",
    "\"rules\"",
    "\"types\"",
    "\"observables\"",
    "<INTEGER>",
    "<IDENTIFIER>",
    "<LETTER>",
    "<DIGIT>",
    "<ESCAPED_EXPRESSION>",
    "\"(\"",
    "\")\"",
  };

}
