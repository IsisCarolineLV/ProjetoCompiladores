import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class AnalisadorSintatico {

    private ArrayList<Linha> linhasErradas;
    private static final Set<String> SINCRONIZACAO = new HashSet<>();

    public void setSinc(){
        SINCRONIZACAO.add(";");
        SINCRONIZACAO.add("end");
        SINCRONIZACAO.add("begin");
        SINCRONIZACAO.add(".");
        SINCRONIZACAO.add("$");
        SINCRONIZACAO.add("if");
        SINCRONIZACAO.add("while");
        SINCRONIZACAO.add("var");
        SINCRONIZACAO.add("procedure");
    }
    
    public ArrayList<Linha> getLinhasErradas(){return linhasErradas;}

    public ArrayList<Linha> analisar(ArrayList<Palavra> listaTolkens) {
        Queue<String> fila = new LinkedList<>();
        Stack<Integer> pilha = new Stack<>();
        ArrayList<Integer> idxOrig = new ArrayList<>();   // índice original de cada token na fila

        pilha.push(0);
        mapearPalavras(listaTolkens, fila, idxOrig);
        fila.add("$");
        idxOrig.add(-1);

        int cont=0;

        while(fila.size()>0){
            String s = buscarAcao(pilha.peek(), fila.peek());

            if (s == null) {
                do{
                    if(fila.size()<=0){
                        break;
                    }
                    marcarErro(listaTolkens, idxOrig.get(cont));
                    fila.poll();
                    s = fila.peek();
                    cont++;
                }while(s == null && !SINCRONIZACAO.contains(s));
                continue;
            }
            // aceite
            if (s.equals("OK")) {
                break;
            }

            String[] separado = s.split(" ");
            if(separado[0].equals("E")){
                fila.poll();
                cont++;
                pilha.push(Integer.parseInt(separado[1]));
            }else if(separado[0].equals("R")){
                int pop = separado.length - 3;
                for (int i = 0; i < pop; i++) pilha.pop();

                String naoTerminal = separado[1];
                String destino = buscarAcao(pilha.peek(), naoTerminal);
                if (destino == null) {
                    do{
                        marcarErro(listaTolkens, idxOrig.get(cont));
                        if(fila.size()<=0){
                            break;
                        }
                        fila.poll();
                        s = fila.peek();
                        cont++;
                    }while(s == null && !SINCRONIZACAO.contains(s));
                    continue;
                }
                pilha.push(Integer.parseInt(destino));
                // nao avança token
            }else {
                // se "OK" ja foi tratado, nao deveria chegar aqui
                marcarErro(listaTolkens, idxOrig.get(cont));
                break;
            }
        }

        return geradorLinhas(listaTolkens);
    }

    private void marcarErro(ArrayList<Palavra> lista, int idx) {
        if (idx >= 0 && idx < lista.size()) lista.get(idx).setErrada();
        else if (!lista.isEmpty())           lista.get(lista.size() - 1).setErrada();
    }

    private void mapearPalavras( 
        ArrayList<Palavra> listaTolkens, 
        Queue<String> fila,
        ArrayList<Integer> idxOrig
    ){
        int i=0;
        for (Palavra p : listaTolkens) {
            String tok = p.token == null ? "" : p.token;
            String lex = p.lexema == null ? "" : p.lexema;

            if (tok.equalsIgnoreCase("ESPACO") ||
                tok.equalsIgnoreCase("tab") ||
                tok.equalsIgnoreCase("comentario")){ i++; continue;}

            if (tok.equals("palavra reservada")) {
                String lower = lex.toLowerCase();
                if (lower.equals("integer") || lower.equals("real") || lower.equals("string")
                    || lower.equals("char") || lower.equals("boolean")) {
                    fila.add("tipo");
                } else {
                    fila.add(lower);                // "program", "var", "begin", ...
                }
            } else if (tok.equals("identificador")) {
                fila.add("identificador");          // ver item 2 abaixo
            } else if (tok.startsWith("numero")) {
                fila.add("numero");
            } else {
                fila.add(lex);                      // ":=", ";", "+", "(", ".", ...
            }
            idxOrig.add(i);
            i++;
        }
    }

    private String buscarPar(int estado, String s) {
        Integer col = Tabela.simbolo.get(s);
        if (col == null) return null;          
        return Tabela.tabela[estado][col];
    }

    private String buscarAcao(int estado, String s) {
        String a = buscarPar(estado, s);
        if (a != null) return a;
        if ("identificador".equals(s)) {
            a = buscarPar(estado, "variavel");
            if (a != null) return a;
            a = buscarPar(estado, "ident");
        }
        return a;
    }

    // Metodo que agrupa a lista continua de tokens em objetos Linha e captura erros lexicos via excecoes
    public ArrayList<Linha> geradorLinhas(ArrayList<Palavra> palavras) {
        ArrayList<Linha> linhas = new ArrayList<>();
        ArrayList<Palavra> palavrasLinha = new ArrayList<>();
        linhasErradas = new ArrayList<>();
        int cont = 1;
        boolean erroNaLinha = false;
        String mensagemErro = "";
        Linha l = null;

        for (Palavra p : palavras) {
            if (p.isErrada()) {
                erroNaLinha = true;
                mensagemErro += "           \"" + p.lexema + "\" fere a gramatica";
            }
            try {
                if (p.getTolken().equals("ESPACO")) {
                    l = new Linha(cont++, palavrasLinha);
                    linhas.add(l);
                    if (erroNaLinha) {
                        l.setErro(mensagemErro);
                        linhasErradas.add(l);
                        mensagemErro = "";
                        erroNaLinha = false;
                    }
                    palavrasLinha = new ArrayList<>();
                } else {
                    palavrasLinha.add(p);
                }
            } catch (Exception e) {
                erroNaLinha = true;
                palavrasLinha.add(p);
                mensagemErro += "          " + e.getMessage();
            }
        }
        if (!palavrasLinha.isEmpty()) {
            l = new Linha(cont++, palavrasLinha);
            linhas.add(l);
            if (erroNaLinha) {
                l.setErro(mensagemErro);
                linhasErradas.add(l);
            }
        }
        return linhas;
    }
}