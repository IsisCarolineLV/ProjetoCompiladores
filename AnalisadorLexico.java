import java.util.ArrayList; // Importa a classe ArrayList para criar listas dinamicas de palavras e linhas

// Classe responsavel pelo motor de analise lexica (scanner), transformando o texto bruto em tokens
public class AnalisadorLexico {

    private StringBuilder texto; // Armazena todo o codigo-fonte carregado do arquivo
    private int i; // Ponteiro global (cabecote de leitura) que guarda a posicao do caractere atual no texto
    private ArrayList<Linha> linhasErradas = new ArrayList<>(); // Lista que armazena apenas as linhas que contem erros lexicos
    // Dicionario estatico contendo todas as palavras reservadas da linguagem (baseada em Pascal)
    private static String[] palavrasReservadas = { "ABSOLUTE", "ARRAY", "BEGIN", "CASE",
        "CHAR", "CONST", "DIV", "DO", "DOWTO", "ELSE", "END", "EXTERNAL",
        "FILE", "FOR", "FORWARD", "FUNC", "FUNCTION", "GOTO", "IF", "IMPLEMENTATION",
        "INTEGER", "INTERFACE", "INTERRUPT", "LABEL", "MAIN", "NIL", "NIT",
        "OF", "PACKED", "PROC", "PROGRAM", "REAL", "RECORD", "REPEAT", "SET",
        "SHL", "SHR", "STRING", "THEN", "TO", "TYPE", "UNIT", "UNTIL", "USES",
        "VAR", "WHILE", "WITH", "XOR", "READ", "WRITE", "WRITELN"};

    // Construtor que recebe o codigo-fonte e prepara o analisador para iniciar a leitura do zero
    public AnalisadorLexico(StringBuilder texto) {
        this.texto = texto; // Atribui o texto recebido ao atributo global da classe
        //System.out.println("TEXTO:\n"+texto.toString());
        i=0; // Posiciona o cabecote de leitura no primeiro caractere do arquivo (indice 0)
    }

    // Metodo principal que percorre o codigo-fonte caractere por caractere e roteia para as funcoes de reconhecimento
    public ArrayList<Linha> classificarTolkens() {
        ArrayList<Palavra> palavras = new ArrayList<>(); // Lista que guardara todos os tokens extraidos em sequencia

        int tamanho = texto.length(); // Obtem a quantidade total de caracteres do arquivo
        i=0; // Reinicia o ponteiro de leitura no comeco do texto
        // Laco principal que avanca pelo texto enquanto houver caracteres para ler
        while(i<tamanho-1){
            
            Palavra novaPalavra = null; // Variavel temporaria para guardar o token que sera descoberto nesta iteracao
            char c = texto.charAt(i); // Extrai o caractere exato na posicao atual do ponteiro 'i'

            // Se comecar com letra (minuscula ou maiuscula), desvia para o reconhecedor de identificadores/palavras reservadas
            if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
                novaPalavra=achouIdentificador();
            // Se comecar com digito numerico (0 a 9), desvia para o automato reconhecedor de numeros
            else if(c>='0' && c<='9')
                novaPalavra = achouNumeral();
            // Se for uma quebra de linha, cria uma Palavra vazia (token "ESPACO") para marcar o fim da linha visual
            else if(c=='\n'){
                novaPalavra = new Palavra();
                i++; // Avanca o ponteiro apos consumir a quebra de linha
            // Se for um espaco em branco simples, apenas avanca o ponteiro e pula para o proximo ciclo (ignora espacos)
            }else if(c=='\t'){
                novaPalavra = new Palavra("\t","tab");
                i++; // Avanca o ponteiro apos consumir a quebra de linha
            }else if(c==' '){
                novaPalavra = new Palavra(" ", "espaco");
                i++;
            // Se for uma barra '/', pode ser inicio de comentario '/*' ou operador de divisao
            }else if(c=='/'){
                novaPalavra = achouComentario(palavras);
            // Se for aspas duplas '"', inicia a leitura de uma literal de texto (String)
            }else if(c=='"'){
                novaPalavra = achouString(palavras);
            // Se for aspas simples '\'', inicia a leitura de um caractere unico (Char)
            }else if(c=='\''){
                novaPalavra = achouChar();
            // Caso nao seja nenhum dos anteriores, trata como operador, pontuacao ou caractere invalido
            }else { 
                novaPalavra = achouSimbolo(); 
            }
            // Se um token valido ou invalido foi gerado na rodada, adiciona-o na lista geral de palavras
            if(novaPalavra!=null)
                palavras.add(novaPalavra);
        }

        //imprimindo:
        // Bloco de depuracao (debug) que imprime no console do Java todos os lexemas e seus respectivos tokens
        System.out.println("------------------------------------------");
        for(Palavra p: palavras){
            System.out.println(p.lexema+": "+p.token);
        }
        System.out.println("------------------------------------------");

        // Envia a lista linear de palavras para ser organizada em objetos Linha e retorna para a tela principal
        return geradorLinhas(palavras);
    }

    // Metodo responsavel por reconhecer literais de caractere unico entre aspas simples (ex: 'a' ou '\n')
    private Palavra achouChar() {
        Palavra nova = null; // Inicializa o token de retorno como nulo
        i++; // Avanca o ponteiro 'i' para passar da primeira aspa simples '\'' de abertura
        // Verifica se o caractere seguinte ao conteudo ja e a aspa simples de fechamento (caso padrao: 'a')
        if(texto.charAt(i+1)=='\''){
            nova = new Palavra("\'"+texto.charAt(i)+"\'", "char"); // Monta o lexema com as aspas e classifica como "char"
            i+=2; // Avanca 2 posicoes (o caractere lido + a aspa de fechamento)
        // Verifica se o caractere interno e uma contrabarra para caracteres de escape (ex: '\n', '\t')
        }else if(texto.charAt(i)=='\\'){
            nova = new Palavra("\'"+texto.charAt(i)+texto.charAt(i+1)+"\'", "char"); // Junta a contrabarra e o caractere escapado
            i+=3; // Avanca 3 posicoes (a contrabarra + o caractere escapado + a aspa de fechamento)
        // Se nao fechou corretamente, classifica a aspa solta como um erro lexico
        }else{
            nova = new Palavra("\'", "identificador invalido");
        }
        return nova; // Retorna o token de caractere ou de erro
    }

    // Metodo responsavel por reconhecer cadeias de texto entre aspas duplas (Strings), suportando multiplas linhas
    private Palavra achouString(ArrayList<Palavra> palavras) {
        ArrayList<Palavra> stringGrande = new ArrayList<>(); // Lista auxiliar caso a string ocupe varias linhas
        String lexema = "\""; // Inicia o lexema com a aspa dupla de abertura
        boolean saiuNormal=false; // Flag que controla se a string foi devidamente fechada com outra aspa dupla
        int iInicial =i; // Salva a posicao da aspa inicial para fazer rollback (voltar atras) se a string nao for fechada
        i++; // Avanca o ponteiro para o primeiro caractere dentro da string
            
        // Percorre o texto ate encontrar o fechamento da string ou acabar o arquivo
        while (i < texto.length()) {
            char c = texto.charAt(i); // Le o caractere atual da string
            
            // Verifica se achou o fechamento */
            // Se encontrar a aspa dupla de fechamento, encerra a leitura da string com sucesso
            if (c=='"') {
                lexema += "\""; // Anexa a aspa final ao lexema
                i ++; // Avanca o ponteiro para depois da string
                saiuNormal=true; // Marca que a string foi fechada corretamente
                break; // Sai do laco while
            }

            // Se encontrar uma quebra de linha dentro da string, divide o token para nao quebrar o layout visual
            if (c == '\n') {
                if (!lexema.isEmpty()) {
                    stringGrande.add(new Palavra(lexema, "string")); // Salva o trecho da string lido nesta linha
                }

                stringGrande.add(new Palavra()); // Adiciona um token de quebra de linha ("ESPACO") na lista auxiliar
                
                lexema = ""; // Reseta o lexema para a proxima linha do comentario
                i++; // Avanca o ponteiro para a linha seguinte
            } else {
                lexema += c; // Concatena o caractere normal ao texto da string
                i++; // Avanca para o proximo caractere
            }
        }
        // Se a string foi fechada com aspas normalmente, valida e retorna os tokens gerados
        if (!lexema.isEmpty() && saiuNormal) {
            if(stringGrande.size()>0) palavras.addAll(stringGrande); // Se tinha varias linhas, despeja as partes anteriores na lista principal
            return new Palavra(lexema, "string"); // Retorna a parte final (ou unica) da string
        }
            // Caso o arquivo tenha acabado sem fechar as aspas, restaura o ponteiro para logo apos a aspa inicial
            i = iInicial +1;
            // Se nao for string, entao ah um identificador invalido
        return new Palavra("\"", "identificador invalido"); // Pune apenas a aspa aberta como erro lexico e analisa o resto normalmente
    }

    // Metodo responsavel por diferenciar comentarios de bloco '/* ... */' do operador de divisao '/'
    private Palavra achouComentario(ArrayList<Palavra> palavras) {
        String lexema = ""; // Inicia o acumulador de caracteres do comentario vazio
        
        // Faz o lookahead (espia a proxima posicao): se houver um '*' apos a '/', confirma que e um comentario de bloco
        if (i + 1 < texto.length() && texto.charAt(i + 1) == '*') {
            lexema += "/*"; // Inicia o lexema com os delimitadores de abertura
            i += 2; // Pula o '/*'
            
            // Consome todos os caracteres ate achar o fechamento '*/' ou o fim do arquivo
            while (i < texto.length()) {
                char c = texto.charAt(i); // Le o caractere atual dentro do comentario
                
                // Verifica se achou o fechamento */
                if (i + 1 < texto.length() && c == '*' && texto.charAt(i + 1) == '/') {
                    lexema += "*/"; // Anexa os delimitadores de fechamento ao lexema
                    i += 2; // Pula o '*/'
                    break; // Encerra o laco do comentario
                }

                // Se o comentario ocupar varias linhas, divide em varios tokens para manter a formatacao visual por linha
                if (c == '\n') {
                    if (!lexema.isEmpty()) {
                        palavras.add(new Palavra(lexema, "comentario")); // Guarda o trecho do comentario da linha atual
                    }

                    palavras.add(new Palavra()); // Injeta o marcador de quebra de linha ("ESPACO")
                    
                    lexema = ""; // Reseta o lexema para a proxima linha do comentario
                    i++; // Avanca o ponteiro apos o '\n'
                } else {
                    lexema += c; // Acumula o caractere do comentario
                    i++; // Avanca o ponteiro
                }
            }
            // Retorna o ultimo pedaco (ou linha unica) do comentario
            if (!lexema.isEmpty()) {
                return new Palavra(lexema, "comentario");
            }
            return null; // Caso tenha terminado exatamente apos uma quebra de linha, retorna nulo
        }
        
        // Se nao for '/*', entao ah apenas um operador de divisao '/'
        i++; // Avanca o ponteiro consumindo apenas a barra '/'
        return new Palavra("/", "operador aritmetico"); // Classifica a barra isolada como divisao
    }

    // Metodo responsavel por classificar operadores (atribuicao, relacionais, aritmeticos, logicos) e pontuacao
    private Palavra achouSimbolo() {
        char c = texto.charAt(i); // Captura o simbolo atual apontado por 'i'
        //atribuicao
        // Verifica se e ':' seguido imediatamente por '=', formando o operador de atribuicao do Pascal ':='
        if (c == ':' && i + 1 < texto.length() && texto.charAt(i + 1) == '=') { 
            i += 2; // Avanca 2 casas para consumir ':='
            return new Palavra(":=", "operador de atribuicao"); 
        }

        //relacionais
        // Verifica operadores que comecam com '>' (pode ser '>=' ou apenas '>')
        if (c == '>') { 
            if (i + 1 < texto.length() && texto.charAt(i + 1) == '=') { //confere se eh >=
                i += 2; return new Palavra(">=", "operador relacional"); 
            } else { 
                i++; return new Palavra(">", "operador relacional"); 
            }
        } 
        // Verifica operadores que comecam com '<' (pode ser '<=', '<>' de diferente, ou apenas '<')
        if (c == '<') {
            if (i + 1 < texto.length()) { 
                char proximo = texto.charAt(i + 1); // Espia o caractere seguinte
                // Ignora espacos ou quebras de linha entre o '<' e o complemento do operador
                while(proximo==' ' || proximo =='\n') proximo = texto.charAt((++i) + 1); 
                if (proximo == '=') { //confere se eh >=
                    i += 2; 
                    return new Palavra("<=", "operador relacional"); 
                } else if (proximo == '>') { //confere se eh !=
                    i += 2; 
                    return new Palavra("<>", "operador relacional"); 
                }
            } 
            i++; // Se nao formou '<=' nem '<>', consome apenas o '<'
            return new Palavra("<", "operador relacional"); 
        } 
        // Verifica o operador relacional de igualdade '='
        if (c == '=') {
            i++; 
            return new Palavra("=", "operador relacional"); 
        }

        //aritmetico
        // Verifica se e um dos operadores matematicos basicos
        if (c == '+' || c == '-' || c == '*' || c == '/' || c=='%') { 
            i++; 
            return new Palavra(c+"", "operador aritmetico"); 
        } 

        //especiais
        // Verifica delimitadores e pontuacoes da gramatica (parenteses, virgula, ponto e virgula, dois pontos isolado)
        if (c == '(' || c == ')' || c == ',' || c == ';' || c == ':') { 
            i++; 
            return new Palavra(c+"", "simbolo especial"); 
        } 

        //fim
        // Verifica o ponto final '.' que encerra o programa em Pascal
        if (c == '.') {
            i++; 
            return new Palavra(".", "fim"); 
        }

        // Verifica simbolos matematicos para operadores logicos (E, OU, NaO)
        if (c=='^' || c=='∨' || c=='¬') { 
            i++; 
            return new Palavra(c+"", "operador logico"); 
        } 

        //se chegou ate aqui nao eh nenhum caractere resconhecido
        // Qualquer caractere estranho a linguagem (ex: '@', '_', '#') cai aqui e gera erro lexico
        i++; 
        return new Palavra(c + "", "identificador invalido");
    }

    // Maquina de Estados (Automato Finito Deterministico) para reconhecer numeros inteiros, reais e em notacao cientifica
    private Palavra achouNumeral() {
        String lexema = ""; // Acumulador para os caracteres do numero
        char estado = 'i'; // i: inteiro, p: ponto lido, f: flutuante, c: char_cientifico, s: sinal_expoente, n: notacao_concluida

        // Percorre os caracteres enquanto fizerem parte da estrutura numerica
        while (i < texto.length()) {
            char c = texto.charAt(i); // Le o caractere atual

            // Transicoes ao ler um digito de '0' a '9'
            if (c >= '0' && c <= '9') {
                lexema += c; // Adiciona o digito ao lexema
                if (estado == 'c' || estado == 's') { 
                    estado = 'n'; // Se estava aguardando o expoente apos 'e' ou sinal, conclui a notacao cientifica
                } else if (estado == 'p') {
                    estado = 'f'; // Se havia acabado de ler um ponto decimal, confirma que e um numero real (flutuante)
                }
                i++; // Avanca o ponteiro
            } 
            // Transicao ao encontrar um ponto decimal '.' (so e valido se o numero ainda for inteiro 'i')
            else if (c == '.' && estado == 'i') {
                // Se houver outro ponto em seguida ('..'), interrompe o numero (ex: operador de intervalo em Pascal)
                if (i + 1 < texto.length() && texto.charAt(i + 1) == '.') {
                    break;
                }
                lexema += c; // Anexa o ponto decimal ao numero
                estado = 'p'; // Muda para o estado intermediario 'p' (aguardando casas decimais)
                i++;
            } 
            // Transicao para notacao cientifica padrao ('e' ou 'E') a partir de um inteiro ou real
            else if ((c == 'e' || c == 'E') && (estado == 'i' || estado == 'f')) {
                lexema += c;
                estado = 'c'; // Muda para o estado intermediario 'c' (aguardando sinal ou digitos do expoente)
                i++;
            } 
            // Transicao para notacao cientifica customizada 'x10e' a partir de um inteiro ou real
            else if (c == 'x' && (estado == 'i' || estado == 'f')) {
                // A sua notacao customizada: x10e
                // Faz um lookahead de 3 posicoes para confirmar se os proximos caracteres formam exatamente "x10e"
                if (i + 3 < texto.length() && 
                    texto.charAt(i + 1) == '1' && 
                    texto.charAt(i + 2) == '0' && 
                    texto.charAt(i + 3) == 'e') {
                    lexema += "x10e"; // Anexa a sequencia completa ao lexema
                    estado = 'c'; // Vai para o estado de notacao cientifica aguardando o expoente
                    i += 4; // Avanca os 4 caracteres de uma vez
                } else {
                    break; // Era so um 'x' de outra coisa, sai do numero
                }
            } 
            // Transicao para o sinal opcional ('+' ou '-') exclusivo logo apos o indicador de notacao cientifica
            else if ((c == '+' || c == '-') && estado == 'c') {
                // Sinal opcional logo apos o 'e' ou 'x10e'
                lexema += c;
                estado = 's'; // 's' = sinal lido, aguardando expoente numerico
                i++;
            } 
            else {
                break; // Qualquer outro caractere encerra o numero
            }
        }

        // Validacao de numero mal-formado (ex: terminou em "123." ou "1.5e" sem completar)
        // Se o automato parou em um estado nao-final ('p', 'c' ou 's'), retorna erro lexico
        if (estado == 'p' || estado == 'c' || estado == 's') {
            return new Palavra(lexema, "identificador invalido"); 
        }

        // Classifica o token de acordo com o estado final
        String tipo = "numero inteiro"; // Se permaneceu no estado 'i', e inteiro
        if (estado == 'f') tipo = "numero real"; // Se terminou no estado 'f', e real
        if (estado == 'n') tipo = "numero em notacao cientifica"; // Se terminou no estado 'n', e cientifico

        return new Palavra(lexema, tipo); // Cria e retorna o objeto Palavra com o numero validado
    }

    // Metodo responsavel por ler palavras (identificadores de variaveis ou palavras reservadas da linguagem)
    private Palavra achouIdentificador() {
        Palavra novoId = null;
        String lexema = texto.charAt(i)+""; // Inicia o lexema com a primeira letra ja lida
        int aux=i+1; // Cria um ponteiro auxiliar para avancar enquanto houver letras ou numeros
        // Continua lendo enquanto os caracteres seguintes forem alfanumericos (letras ou digitos)
        while(aux<texto.length()){
            char c = texto.charAt(aux);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')|| (c>='0' && c<='9')){
                lexema+=c; // Concatena o caractere a palavra
            }else{
                break; // Se encontrar espaco, operador ou pontuacao, encerra a leitura da palavra
            }
            aux++;
        }
        i=aux; // Atualiza o ponteiro global 'i' para a posicao onde a palavra terminou

        //procura lexema na lista de palavras reservadas
        // Compara a palavra lida com o dicionario da linguagem (ignorando diferencas entre maiusculas e minusculas)
        for(String s: palavrasReservadas){
            if(lexema.equalsIgnoreCase(s)){
                novoId = new Palavra(lexema, "palavra reservada"); // Se encontrar no vetor, classifica como palavra reservada
                return novoId;
            }
        }

        /*for(String o: operacoesLogicas){
            if(lexema.equalsIgnoreCase(o)){
                novoId = new Palavra(lexema, "operador logico");
                return novoId;
            }
        }

        if(lexema.equalsIgnoreCase("mod")){
            novoId = new Palavra(lexema, "operador aritmetico");
            return novoId;
        }*/
        
        // Se nao consta na lista de palavras reservadas, classifica como identificador (nome de variavel/funcao)
        novoId = new Palavra(lexema, "identificador");
        return novoId;
    }

    // Metodo que agrupa a lista continua de tokens em objetos Linha e captura erros lexicos via excecoes
    public ArrayList<Linha> geradorLinhas(ArrayList<Palavra> palavras){
        ArrayList<Linha> linhas = new ArrayList<>(); // Lista final de linhas que sera enviada para a interface grafica
        ArrayList<Palavra> palavrasLinha = new ArrayList<>(); // Acumulador temporario de palavras da linha atual
        int cont=1; // Contador da numeracao das linhas (comeca na linha 1)
        boolean erroNaLinha=false; // Flag para indicar se a linha atual possui algum token invalido
        Linha l =null;
        String mensagemErro=""; // Acumula a descricao dos erros encontrados naquela linha

        // Percorre todas as palavras geradas pela analise lexica
        for(Palavra p: palavras){
            try {
                // O metodo getTolken() lanca uma Exception caso o token seja "identificador invalido"
                // Se o token for "ESPACO" (quebra de linha '\n'), fecha a linha atual e cria o objeto Linha
                if(p.getTolken().equals("ESPACO")){
                    l = new Linha(cont++, palavrasLinha); // Instancia a linha com seu numero e lista de palavras
                    linhas.add(l); // Adiciona na lista de linhas do arquivo
                    // Se alguma palavra desta linha lancou excecao anteriormente, registra o erro na linha
                    if(erroNaLinha){
                        l.setErro(mensagemErro); // Marca a linha como errada (fundo vermelho) e anexa a mensagem
                        mensagemErro = ""; // Limpa a mensagem para a proxima linha
                        linhasErradas.add(l); // Guarda a referencia na lista de navegacao de erros
                        erroNaLinha=false; // Reseta a flag de erro para a proxima linha
                    }
                    palavrasLinha = new ArrayList<>(); // Cria uma nova lista vazia para comecar a proxima linha

                }else{
                    palavrasLinha.add(p); // Se for uma palavra normal e sem erro, adiciona na linha atual
                }
            // Captura a excecao lancada por getTolken() quando encontra um "identificador invalido"
            } catch (Exception e) {
                erroNaLinha = true; // Sinaliza que a linha atual possui erro lexico
                palavrasLinha.add(p); // Adiciona a palavra invalida na linha mesmo assim para ela ser exibida na tela
                mensagemErro+="  ------ "+e.getMessage(); // Concatena o detalhe do erro (ex: "identificador invalido: @")
            }
        }

        // Caso a ultima linha do arquivo nao termine com '\n', empacota as palavras restantes na ultima Linha
        if(!palavrasLinha.isEmpty()){

            l = new Linha(cont++, palavrasLinha);
            linhas.add(l);

            if(erroNaLinha){
                l.setErro(mensagemErro);
                linhasErradas.add(l);
            }
        }

        return linhas; // Devolve todas as linhas prontas para serem desenhadas na tela
    }

    // Metodo de acesso (Getter) para a classe Main obter a lista de linhas com erro e habilitar os botoes de navegacao
    public ArrayList<Linha> getLinhasErradas() {
        return linhasErradas;
    }
    
}