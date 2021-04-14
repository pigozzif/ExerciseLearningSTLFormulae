/* JFlex example: part of Java language lexer specification */
package simhya.modelchecking.mtl.parser;

import java_cup.runtime.*;

/**
* This is the JFLEX lexer for MTL formulae.
*/

%%

%class MTLlexer
%unicode
%cup
%line
%column
%public


%{
    StringBuffer string = new StringBuffer();

    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }

    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}


LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}
TraditionalComment = "/*" ~"*/" 
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}

Identifier = [:letter:] ( [:letter:] | [:digit:] | "_" )*
IntegerNumber = 0 | [1-9][0-9]*
FloatNumber = {IntegerNumber}? "." [0-9]+
ExpNumber = {FloatNumber} [eE] [-]? {IntegerNumber}
Number = {IntegerNumber} | {FloatNumber} | {ExpNumber}

%state MATH

%%




<YYINITIAL> {
    /* keywords */
    "F" { return symbol(sym.EVENTUALLY,"F"); }
    "G" { return symbol(sym.GLOBALLY,"G"); }
    "P" { return symbol(sym.PROB,"P"); }
    "U" { return symbol(sym.UNTIL,"U"); }
    "W" { return symbol(sym.WUNTIL,"W"); }
    "X" { return symbol(sym.NEXT,"X"); }
    "TRUE" | "true" | "True"    { return symbol(sym.TRUE); }
    "FALSE" | "false" | "False" { return symbol(sym.FALSE); }
    "AND" | "&&" { return symbol(sym.AND); }
    "OR" | "||" { return symbol(sym.OR); }
    "NOT" | "!" { return symbol(sym.NOT); }
    "IMPLY" | "-->" { return symbol(sym.IMPLY); }

    /* operators */
    
    <MATH> "<=" { return symbol(sym.LESSEQUAL,"<="); }
    <MATH> ">=" { return symbol(sym.GREATEREQUAL,">="); }
    <MATH> "<" { return symbol(sym.LESS,"<"); }
    <MATH> ">" { return symbol(sym.GREATER,">"); }

    

    <MATH> "(" { return symbol(sym.LROUND,"("); }
    <MATH> ")" { return symbol(sym.RROUND,")"); }
    "[" { return symbol(sym.LSQUARE,"["); }
    "]" { return symbol(sym.RSQUARE,"]"); }
    "{" { yybegin(MATH); return symbol(sym.LBRACE,"{");}
    <MATH> "}" { yybegin(YYINITIAL); return symbol(sym.RBRACE,"}"); }

    ";" { return symbol(sym.SEMICOLON); }
    <MATH> "," { return symbol(sym.COMMA); }
    "=" { return symbol(sym.EQ); }
    "?" { return symbol(sym.QMARK); }

    /* identifiers */
    <MATH> {Identifier} { return symbol(sym.IDENTIFIER,yytext()); }

    /* literals */
    <MATH> {Number} { return symbol(sym.NUMBER,yytext()); }

    /* comments */
    <MATH> {Comment} { /* ignore */ }
    
    /* whitespace */
    <MATH> {WhiteSpace} { /* ignore */ }
} 

<MATH> {
    "==" { return symbol(sym.EQUAL,"=="); }
    "!=" { return symbol(sym.NOTEQUAL,"!="); }
    "+" { return symbol(sym.PLUS,"+"); }
    "-" { return symbol(sym.MINUS,"-"); }
    "*" { return symbol(sym.TIMES,"*"); }
    "/" { return symbol(sym.DIVIDE,"/"); }
    "%" { return symbol(sym.MOD,"%"); }
    "^" { return symbol(sym.POW,"^"); }
}
