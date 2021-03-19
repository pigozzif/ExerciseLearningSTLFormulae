package simhya.modelchecking.parser;
import java_cup.runtime.*;

/**
* This is the JFLEX lexer for SMC.
*/

%%

%class SMClexer
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

Operator = "+" | "-" | "*" | "/" | "%" | "^" | "==" | "!=" | "<=" | "<" | ">" | ">="
Boolean = "&&" | "||" | "!"
Other = "." | "," | "=" | "?" | "_"
Parenthesis =  "{" | "}" | "[" | "]" | "(" | ")"
Formula = ( [:letter:] | [:digit:]  | {Operator} | {Parenthesis} | {Other} | {Boolean} | {WhiteSpace} )+

%state MATH, FORMULA, FORMULA_ID

%%




<YYINITIAL> {
    /* keywords */
    "F" | "G" | "P" | "U" | "W" | "X"  { return symbol(sym.RESERVED); }
    "TRUE" |  "true" | "True" | "FALSE" | "false" | "False" { return symbol(sym.RESERVED); }
    "AND" | "OR" | "NOT" { return symbol(sym.RESERVED); }
    "param" { return symbol(sym.PARAM); }
    "#MTL" { yybegin(FORMULA); return symbol(sym.MTL); }


    <MATH> ";" { yybegin(YYINITIAL); return symbol(sym.SEMICOLON); }
    "=" { yybegin(MATH); return symbol(sym.ASSIGN); }
    ":=" { yybegin(MATH); return symbol(sym.DEFINE); }
    

    /* identifiers */
    <MATH, FORMULA_ID> {Identifier} { return symbol(sym.IDENTIFIER,yytext()); }

    /* literals */
    <MATH> {Number} { return symbol(sym.NUMBER,yytext()); }

    /* comments */
    <MATH> {Comment} { /* ignore */ }
    
    /* whitespace */
    <MATH> {WhiteSpace} { /* ignore */ }
} 

<FORMULA> { 
    {WhiteSpace} { /* ignore */ }
    ":" { yybegin(FORMULA_ID); return symbol(sym.COLON); }
    ";" { return symbol(sym.SEMICOLON); }
    {Formula} { return symbol(sym.FORMULA,yytext()); }
}

<FORMULA_ID> ":" { yybegin(FORMULA); return symbol(sym.COLON); }

<MATH> {
    /* operators */
    "==" { return symbol(sym.EQUAL,"=="); }
    "!=" { return symbol(sym.NOTEQUAL,"!="); }
    "<=" { return symbol(sym.LESSEQUAL,"<="); }
    ">=" { return symbol(sym.GREATEREQUAL,">="); }
    "<" { return symbol(sym.LESS,"<"); }
    ">" { return symbol(sym.GREATER,">"); }
    "+" { return symbol(sym.PLUS,"+"); }
    "-" { return symbol(sym.MINUS,"-"); }
    "*" { return symbol(sym.TIMES,"*"); }
    "/" { return symbol(sym.DIVIDE,"/"); }
    "%" { return symbol(sym.MOD,"%"); }
    "^" { return symbol(sym.POW,"^"); }
    "," { return symbol(sym.COMMA); }
    "(" { return symbol(sym.LROUND,"("); }
    ")" { return symbol(sym.RROUND,")"); }
}
