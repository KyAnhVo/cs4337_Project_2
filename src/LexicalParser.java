import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LexicalParser {
    public static final int MAX_LEXEME_LENGTH = 60;

    public static Token nextToken;
    public static CharacterClass charClass;
    public static char nextChar;
    public static String lexeme;
    public static FileReader fileReader;

    private static StringBuilder lexemeBuilder;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java LexicalParser <file>");
            System.exit(1);
        }

        try {
            fileReader = new FileReader(args[0]);
            lexemeBuilder = new StringBuilder();
            getChar();  // Initialize first character

            // Initialize first token
            lex();

            // Continue until EOF
            while (nextToken != Token.EOF) {
                System.out.println(nextToken + ": " + lexeme);
                lex();
            }
            System.out.println("EOF reached");

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + args[0]);
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }
    }

    /**
     * Put nextChar into lexeme, unless lexeme is too long
     */
    public static void addChar() throws RuntimeException {
        if (lexemeBuilder.length() > MAX_LEXEME_LENGTH) {
            throw new RuntimeException("Lexeme exceeds maximum length");
        }
        lexemeBuilder.append(nextChar);
    }

    /**
     * Get next input from file and determine its character class
     */
    public static void getChar() throws IOException {
        try {
            nextChar = (char) fileReader.read();
        }
        catch (IOException e) {
            System.out.println("Error reading file");
            throw e;
        }

        if (Character.isLetter(nextChar))           charClass = CharacterClass.LETTER;
        else if (Character.isDigit(nextChar))       charClass = CharacterClass.DIGIT;
        else if (Character.isWhitespace(nextChar))  charClass = CharacterClass.WHITESPACE;
        else if (nextChar == '\'')                  charClass = CharacterClass.QUOTE;
        else if (nextChar == '\"')                  charClass = CharacterClass.STR_QUOTE;
        else if (nextChar == '(')                   charClass = CharacterClass.LPAREN;
        else if (nextChar == ')')                   charClass = CharacterClass.RPAREN;
        else if (nextChar == '#')                   charClass = CharacterClass.HASH;
        else if (nextChar == (char) -1)             charClass = CharacterClass.EOF;
        else                                        charClass = CharacterClass.OTHER_ASCII;
    }

    public static void getNonBlank() throws IOException {
        while (charClass == CharacterClass.WHITESPACE) {
            getChar();
        }
    }

    public static void lex() throws Exception {
        lexemeBuilder = new StringBuilder();
        getNonBlank();

        switch (charClass) {
            case DIGIT:
                do {
                    addChar();
                    getChar();
                } while (charClass == CharacterClass.DIGIT);
                lexeme = lexemeBuilder.toString();
                lexemeBuilder = new StringBuilder();
                nextToken = Token.NUM_LITERAL;
                break;

            case LPAREN:
                addChar();
                lexeme = lexemeBuilder.toString();
                lexemeBuilder = new StringBuilder();
                nextToken = Token.LPAREN;
                break;

            case RPAREN:
                addChar();
                lexeme = lexemeBuilder.toString();
                lexemeBuilder = new StringBuilder();
                nextToken = Token.RPAREN;
                break;

            case HASH:
                addChar();
                getChar();
                if (nextChar != 't' && nextChar != 'f')
                    throw new RuntimeException("Invalid boolean value");
                addChar();
                lexeme = lexemeBuilder.toString();
                lexemeBuilder = new StringBuilder();
                nextToken = Token.BOOL_LITERAL;
                break;

            case EOF:
                addChar();
                lexeme = lexemeBuilder.toString();
                lexemeBuilder = new StringBuilder();
                nextToken = Token.EOF;
                break;

            case QUOTE:
                addChar();
                lexeme = lexemeBuilder.toString();
                lexemeBuilder = new StringBuilder();
                nextToken = Token.QUOTE;
                break;

            case STR_QUOTE:
                do {
                    addChar();
                    getChar();
                } while (charClass != CharacterClass.STR_QUOTE && charClass != CharacterClass.EOF);
                if (charClass == CharacterClass.EOF)
                    throw new RuntimeException("Unterminated string literal");

                addChar(); // add closing quote
                lexeme = lexemeBuilder.toString();
                lexemeBuilder = new StringBuilder();
                nextToken = Token.STR_LITERAL;
                break;

            // can be identifier or keyword
            case LETTER:
            case OTHER_ASCII:
                do {
                    addChar();
                    getChar();
                } while (charClass == CharacterClass.LETTER
                        || charClass == CharacterClass.OTHER_ASCII
                        || charClass == CharacterClass.DIGIT);
                lexeme = lexemeBuilder.toString();
                lexemeBuilder = new StringBuilder();

                switch(lexeme) {
                    case "lambda":
                        nextToken = Token.LAMBDA_KW;
                        break;
                    case "define":
                        nextToken = Token.DEFINE_KW;
                        break;
                    case "quote":
                        nextToken = Token.QUOTE_KW;
                        break;
                    case "car":
                        nextToken = Token.CAR_KW;
                        break;
                    case "cdr":
                        nextToken = Token.CDR_KW;
                        break;
                    case "cons":
                        nextToken = Token.CONS_KW;
                        break;
                    case "add1":
                        nextToken = Token.ADD1_KW;
                        break;
                    case "sub1":
                        nextToken = Token.SUB1_KW;
                        break;
                    default:
                        nextToken = Token.NAME;
                        break;
                }
        }

        getChar();
    }


}
