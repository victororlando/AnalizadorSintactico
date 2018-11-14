
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalizadorSintactico {

    String token;
    List<String> tokensList = new ArrayList<>();
    int cursor = 0;
    String[] tokensValidos;
    List<String> tokensPermitidos = new ArrayList<String>();
    boolean errorEncontrado;

    public void run() throws IOException {
        AnalizadorLexico al = new AnalizadorLexico();
        tokensList = al.analizarFuente();
        getToken();
        json(new String[]{"EOF"});

        if (!errorEncontrado) {
            System.out.println("El código fuente es sintacticamente correcto");
        }
        System.exit(0);

    }

    public static void main(String[] args) throws IOException {
        AnalizadorSintactico as = new AnalizadorSintactico();
        as.run();
    }

    public void check_input(String[] firsts, String[] follows) {
        if (!existeEnArray(token, firsts)) {
            tokensValidos = firsts;
            for (int i = 0; i < firsts.length; i++) {
                if (!tokensPermitidos.contains(i)) {
                    tokensPermitidos.add(firsts[i]);
                }
            }
            error();
            scanto(union(firsts, follows));
        }
    }

    public boolean existeEnArray(String token, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (token == componenteLexico(array[i])) {
                return true;
            }
        }
        return false;
    }

    public void error() {
        System.out.println("Se produjo un error al leer el token: " + token + ", Se esperaba uno de los sgtes: ( " + tokensEsperados() + ")");
        errorEncontrado = true;
    }

    public void scanto(String[] array) {
        while (!existeEnArray(token, array)) {
            getToken();
        }
    }

    public String getToken() {
        if (cursor < tokensList.size()) {
            token = tokensList.get(cursor++);
            System.out.println(token);
        }
        return token;
    }

    public void match(String t) {
        if (token == componenteLexico(t)) {
            getToken();
        } else {
            error();
        }
    }

    public String[] union(String[] first, String[] follow) {

        String[] unionArray = new String[first.length + follow.length + 1];

        for (int i = 0; i < first.length; i++) {
            unionArray[i] = first[i];
        }

        int pos = first.length;
        for (int i = 0; i < follow.length; i++) {
            unionArray[pos] = follow[i];
            pos++;
        }
        unionArray[pos] = "EOF";
        return unionArray;
    }

    public void json(String[] synchset) {
        check_input(new String[]{"{", "["}, synchset);
        if (!existeEnArray(token, synchset)) {
            element(new String[]{"EOF", ",", "]", "}"});
            match("EOF");
            check_input(synchset, new String[]{"{", "["});
        }
    }

    public void element(String[] synchset) {
        check_input(new String[]{"{", "["}, synchset);
        if (!existeEnArray(token, synchset)) {
            switch (token) {
                case "L_LLAVE":
                    objeto(new String[]{"EOF", ",", "]", "}"});
                    break;
                case "L_CORCHETE":
                    array(new String[]{"EOF", ",", "]", "}"});
                    break;
                default:
                    error();
            }
            check_input(synchset, new String[]{"{", "["});
        }
    }

    public void objeto(String[] synchset) {
        check_input(new String[]{"{"}, synchset);
        if (!existeEnArray(token, synchset)) {
            switch (token) {
                case "L_LLAVE":
                    match("{");
                    attribute_list(new String[]{"}"});
                    match("}");
                    break;
                case "R_LLAVE":
                    match("}");
                    break;
                default:
                    error();
            }
            check_input(synchset, new String[]{"{"});
        }
    }

    public void array(String[] synchset) {
        check_input(new String[]{"["}, synchset);
        if (!existeEnArray(token, synchset)) {
            switch (token) {
                case "L_CORCHETE":
                    match("[");
                    element_list(new String[]{"]"});
                    match("]");
                    break;
                case "R_CORCHETE":
                    match("]");
                    break;
                default:
                    error();
            }
            check_input(synchset, new String[]{"["});
        }
    }

    public void element_list(String[] synchset) {
        check_input(new String[]{"{", "["}, synchset);
        if (!existeEnArray(token, synchset)) {
            element(new String[]{"EOF", ",", "]", "}"});
            while (token == componenteLexico(",")) {
                match(",");
                element(new String[]{"EOF", ",", "]", "}"});
            }
            check_input(synchset, new String[]{"{", "["});
        }
    }

    public void attribute_list(String[] synchset) {
        check_input(new String[]{"String"}, synchset);
        if (!existeEnArray(token, synchset)) {
            attibute(new String[]{",", "}"});
            while (token == componenteLexico(",")) {
                match(",");
                attibute(new String[]{",", "}"});
            }
            check_input(synchset, new String[]{"String"});
        }
    }

    public void attibute(String[] synchset) {
        check_input(new String[]{"String"}, synchset);
        if (!existeEnArray(token, synchset)) {
            att_name(new String[]{":"});
            match(":");
            att_valor(new String[]{",", "}"});
            check_input(synchset, new String[]{"String"});
        }
    }

    public void att_name(String[] synchset) {
        check_input(new String[]{"String"}, synchset);
        if (!existeEnArray(token, synchset)) {
            match("String");
            check_input(synchset, new String[]{"String"});
        }

    }

    public void att_valor(String[] synchset) {
        check_input(new String[]{"{", "[", "String", "Num", "true", "false", "null"}, synchset);
        if (!existeEnArray(token, synchset)) {
            switch (token) {
                case "L_CORCHETE":
                    element(new String[]{"EOF", ",", "]", "}"});
                    break;
                case "L_LLAVE":
                    element(new String[]{"EOF", ",", "]", "}"});
                    break;
                case "LITERAL_CADENA":
                    match("String");
                    break;
                case "LITERAL_NUM":
                    match("Num");
                    break;
                case "PR_TRUE":
                    match("true");
                    break;
                case "PR_FALSE":
                    match("false");
                    break;
                case "PR_NULL":
                    match("null");
                    break;
                default:
                    error();
            }
            check_input(synchset, new String[]{"[", "{", "String", "Num", "true", "false", "null"});
        }
    }

    public String componenteLexico(String s) {
        switch (s) {
            case "{":
                return "L_LLAVE";
            case "}":
                return "R_LLAVE";
            case "[":
                return "L_CORCHETE";
            case "]":
                return "R_CORCHETE";
            case ",":
                return "COMA";
            case ":":
                return "DOS_PUNTOS";
            case "Num":
                return "LITERAL_NUM";
            case "true":
                return "PR_TRUE";
            case "false":
                return "PR_FALSE";
            case "null":
                return "PR_NULL";
            case "String":
                return "LITERAL_CADENA";
            case "EOF":
                return "EOF";
        }
        return null;
    }

    private String tokensEsperados() {
        String s = "";
        for (int i = 0; i < tokensValidos.length; i++) {
            s = s + "\"" + tokensValidos[i] + "\" ";
        }
        return s;
    }

}
