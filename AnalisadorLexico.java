import java.util.ArrayList; // Importa a classe ArrayList para criar listas dinâmicas de palavras e linhas

// Classe responsável pelo motor de análise léxica (scanner), transformando o texto bruto em tokens
public class AnalisadorLexico {

    private StringBuilder texto; // Armazena todo o código-fonte carregado do arquivo
    private int i; // Ponteiro global (cabeçote de leitura) que guarda a posição do caractere atual no texto
    private ArrayList<Linha> linhasErradas = new ArrayList<>(); // Lista que armazena apenas as linhas que contêm erros léxicos
    // Dicionário estático contendo todas as palavras reservadas da linguagem (baseada em Pascal)
    private static String[] palavrasReservadas = { "ABSOLUTE", "ARRAY", "BEGIN", "CASE",
        "CHAR", "CONST", "DIV", "DO", "DOWTO", "ELSE", "END", "EXTERNAL",
        "FILE", "FOR", "FORWARD", "FUNC", "FUNCTION", "GOTO", "IF", "IMPLEMENTATION",
        "INTEGER", "INTERFACE", "INTERRUPT", "LABEL", "MAIN", "NIL", "NIT",
        "OF", "PACKED", "PROC", "PROGRAM", "REAL", "RECORD", "REPEAT", "SET",
        "SHL", "SHR", "STRING", "THEN", "TO", "TYPE", "UNIT", "UNTIL", "USES",
        "VAR", "WHILE", "WITH", "XOR", "READ", "WRITE", "WRITELN"};

    // Construtor que recebe o código-fonte e prepara o analisador para iniciar a leitura do zero
    public AnalisadorLexico(StringBuilder texto) {
        this.texto = texto; // Atribui o texto recebido ao atributo global da classe
        //System.out.println("TEXTO:\n"+texto.toString());
        i=0; // Posiciona o cabeçote de leitura no primeiro caractere do arquivo (índice 0)
    }

    // Método principal que percorre o código-fonte caractere por caractere e roteia para as funções de reconhecimento
    public ArrayList<Linha> classificarTolkens() {
        ArrayList<Palavra> palavras = new ArrayList<>(); // Lista que guardará todos os tokens extraídos em sequência

        int tamanho = texto.length(); // Obtém a quantidade total de caracteres do arquivo
        i=0; // Reinicia o ponteiro de leitura no começo do texto
        // Laço principal que avança pelo texto enquanto houver caracteres para ler
        while(i<tamanho-1){
            
            Palavra novaPalavra = null; // Variável temporária para guardar o token que será descoberto nesta iteração
            char c = texto.charAt(i); // Extrai o caractere exato na posição atual do ponteiro 'i'

            // Se começar com letra (minúscula ou maiúscula), desvia para o reconhecedor de identificadores/palavras reservadas
            if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
                novaPalavra=achouIdentificador();
            // Se começar com dígito numérico (0 a 9), desvia para o autômato reconhecedor de números
            else if(c>='0' && c<='9')
                novaPalavra = achouNumeral();
            // Se for uma quebra de linha, cria uma Palavra vazia (token "ESPACO") para marcar o fim da linha visual
            else if(c=='\n'){
                novaPalavra = new Palavra();
                i++; // Avança o ponteiro após consumir a quebra de linha
            // Se for um espaço em branco simples, apenas avança o ponteiro e pula para o próximo ciclo (ignora espaços)
            }else if(c=='\t'){
                novaPalavra = new Palavra("\t","tab");
                i++; // Avança o ponteiro após consumir a quebra de linha
            }else if(c==' '){
                novaPalavra = new Palavra(" ", "espaco");
                i++;
            // Se for uma barra '/', pode ser início de comentário '/*' ou operador de divisão
            }else if(c=='/'){
                novaPalavra = achouComentario(palavras);
            // Se for aspas duplas '"', inicia a leitura de uma literal de texto (String)
            }else if(c=='"'){
                novaPalavra = achouString(palavras);
            // Se for aspas simples '\'', inicia a leitura de um caractere único (Char)
            }else if(c=='\''){
                novaPalavra = achouChar();
            // Caso não seja nenhum dos anteriores, trata como operador, pontuação ou caractere inválido
            }else { 
                novaPalavra = achouSimbolo(); 
            }
            // Se um token válido ou inválido foi gerado na rodada, adiciona-o na lista geral de palavras
            if(novaPalavra!=null)
                palavras.add(novaPalavra);
        }

        //imprimindo:
        // Bloco de depuração (debug) que imprime no console do Java todos os lexemas e seus respectivos tokens
        System.out.println("------------------------------------------");
        for(Palavra p: palavras){
            System.out.println(p.lexema+": "+p.token);
        }
        System.out.println("------------------------------------------");

        // Envia a lista linear de palavras para ser organizada em objetos Linha e retorna para a tela principal
        return geradorLinhas(palavras);
    }

    // Método responsável por reconhecer literais de caractere único entre aspas simples (ex: 'a' ou '\n')
    private Palavra achouChar() {
        Palavra nova = null; // Inicializa o token de retorno como nulo
        i++; // Avança o ponteiro 'i' para passar da primeira aspa simples '\'' de abertura
        // Verifica se o caractere seguinte ao conteúdo já é a aspa simples de fechamento (caso padrão: 'a')
        if(texto.charAt(i+1)=='\''){
            nova = new Palavra("\'"+texto.charAt(i)+"\'", "char"); // Monta o lexema com as aspas e classifica como "char"
            i+=2; // Avança 2 posições (o caractere lido + a aspa de fechamento)
        // Verifica se o caractere interno é uma contrabarra para caracteres de escape (ex: '\n', '\t')
        }else if(texto.charAt(i)=='\\'){
            nova = new Palavra("\'"+texto.charAt(i)+texto.charAt(i+1)+"\'", "char"); // Junta a contrabarra e o caractere escapado
            i+=3; // Avança 3 posições (a contrabarra + o caractere escapado + a aspa de fechamento)
        // Se não fechou corretamente, classifica a aspa solta como um erro léxico
        }else{
            nova = new Palavra("\'", "identificador invalido");
        }
        return nova; // Retorna o token de caractere ou de erro
    }

    // Método responsável por reconhecer cadeias de texto entre aspas duplas (Strings), suportando múltiplas linhas
    private Palavra achouString(ArrayList<Palavra> palavras) {
        ArrayList<Palavra> stringGrande = new ArrayList<>(); // Lista auxiliar caso a string ocupe várias linhas
        String lexema = "\""; // Inicia o lexema com a aspa dupla de abertura
        boolean saiuNormal=false; // Flag que controla se a string foi devidamente fechada com outra aspa dupla
        int iInicial =i; // Salva a posição da aspa inicial para fazer rollback (voltar atrás) se a string não for fechada
        i++; // Avança o ponteiro para o primeiro caractere dentro da string
            
        // Percorre o texto até encontrar o fechamento da string ou acabar o arquivo
        while (i < texto.length()) {
            char c = texto.charAt(i); // Lê o caractere atual da string
            
            // Verifica se achou o fechamento */
            // Se encontrar a aspa dupla de fechamento, encerra a leitura da string com sucesso
            if (c=='"') {
                lexema += "\""; // Anexa a aspa final ao lexema
                i ++; // Avança o ponteiro para depois da string
                saiuNormal=true; // Marca que a string foi fechada corretamente
                break; // Sai do laço while
            }

            // Se encontrar uma quebra de linha dentro da string, divide o token para não quebrar o layout visual
            if (c == '\n') {
                if (!lexema.isEmpty()) {
                    stringGrande.add(new Palavra(lexema, "string")); // Salva o trecho da string lido nesta linha
                }

                stringGrande.add(new Palavra()); // Adiciona um token de quebra de linha ("ESPACO") na lista auxiliar
                
                lexema = ""; // Reseta o lexema para a próxima linha do comentário
                i++; // Avança o ponteiro para a linha seguinte
            } else {
                lexema += c; // Concatena o caractere normal ao texto da string
                i++; // Avança para o próximo caractere
            }
        }
        // Se a string foi fechada com aspas normalmente, valida e retorna os tokens gerados
        if (!lexema.isEmpty() && saiuNormal) {
            if(stringGrande.size()>0) palavras.addAll(stringGrande); // Se tinha várias linhas, despeja as partes anteriores na lista principal
            return new Palavra(lexema, "string"); // Retorna a parte final (ou única) da string
        }
            // Caso o arquivo tenha acabado sem fechar as aspas, restaura o ponteiro para logo após a aspa inicial
            i = iInicial +1;
            // Se nao for string, entao ah um identificador invalido
        return new Palavra("\"", "identificador invalido"); // Pune apenas a aspa aberta como erro léxico e analisa o resto normalmente
    }

    // Método responsável por diferenciar comentários de bloco '/* ... */' do operador de divisão '/'
    private Palavra achouComentario(ArrayList<Palavra> palavras) {
        String lexema = ""; // Inicia o acumulador de caracteres do comentário vazio
        
        // Faz o lookahead (espia a próxima posição): se houver um '*' após a '/', confirma que é um comentário de bloco
        if (i + 1 < texto.length() && texto.charAt(i + 1) == '*') {
            lexema += "/*"; // Inicia o lexema com os delimitadores de abertura
            i += 2; // Pula o '/*'
            
            // Consome todos os caracteres até achar o fechamento '*/' ou o fim do arquivo
            while (i < texto.length()) {
                char c = texto.charAt(i); // Lê o caractere atual dentro do comentário
                
                // Verifica se achou o fechamento */
                if (i + 1 < texto.length() && c == '*' && texto.charAt(i + 1) == '/') {
                    lexema += "*/"; // Anexa os delimitadores de fechamento ao lexema
                    i += 2; // Pula o '*/'
                    break; // Encerra o laço do comentário
                }

                // Se o comentário ocupar várias linhas, divide em vários tokens para manter a formatação visual por linha
                if (c == '\n') {
                    if (!lexema.isEmpty()) {
                        palavras.add(new Palavra(lexema, "comentario")); // Guarda o trecho do comentário da linha atual
                    }

                    palavras.add(new Palavra()); // Injeta o marcador de quebra de linha ("ESPACO")
                    
                    lexema = ""; // Reseta o lexema para a próxima linha do comentário
                    i++; // Avança o ponteiro após o '\n'
                } else {
                    lexema += c; // Acumula o caractere do comentário
                    i++; // Avança o ponteiro
                }
            }
            // Retorna o último pedaço (ou linha única) do comentário
            if (!lexema.isEmpty()) {
                return new Palavra(lexema, "comentario");
            }
            return null; // Caso tenha terminado exatamente após uma quebra de linha, retorna nulo
        }
        
        // Se nao for '/*', entao ah apenas um operador de divisao '/'
        i++; // Avança o ponteiro consumindo apenas a barra '/'
        return new Palavra("/", "operador aritmetico"); // Classifica a barra isolada como divisão
    }

    // Método responsável por classificar operadores (atribuição, relacionais, aritméticos, lógicos) e pontuação
    private Palavra achouSimbolo() {
        char c = texto.charAt(i); // Captura o símbolo atual apontado por 'i'
        //atribuicao
        // Verifica se é ':' seguido imediatamente por '=', formando o operador de atribuição do Pascal ':='
        if (c == ':' && i + 1 < texto.length() && texto.charAt(i + 1) == '=') { 
            i += 2; // Avança 2 casas para consumir ':='
            return new Palavra(":=", "operador de atribuição"); 
        }

        //relacionais
        // Verifica operadores que começam com '>' (pode ser '>=' ou apenas '>')
        if (c == '>') { 
            if (i + 1 < texto.length() && texto.charAt(i + 1) == '=') { //confere se eh >=
                i += 2; return new Palavra(">=", "operador relacional"); 
            } else { 
                i++; return new Palavra(">", "operador relacional"); 
            }
        } 
        // Verifica operadores que começam com '<' (pode ser '<=', '<>' de diferente, ou apenas '<')
        if (c == '<') {
            if (i + 1 < texto.length()) { 
                char proximo = texto.charAt(i + 1); // Espia o caractere seguinte
                // Ignora espaços ou quebras de linha entre o '<' e o complemento do operador
                while(proximo==' ' || proximo =='\n') proximo = texto.charAt((++i) + 1); 
                if (proximo == '=') { //confere se eh >=
                    i += 2; 
                    return new Palavra("<=", "operador relacional"); 
                } else if (proximo == '>') { //confere se eh !=
                    i += 2; 
                    return new Palavra("<>", "operador relacional"); 
                }
            } 
            i++; // Se não formou '<=' nem '<>', consome apenas o '<'
            return new Palavra("<", "operador relacional"); 
        } 
        // Verifica o operador relacional de igualdade '='
        if (c == '=') {
            i++; 
            return new Palavra("=", "operador relacional"); 
        }

        //aritmetico
        // Verifica se é um dos operadores matemáticos básicos
        if (c == '+' || c == '-' || c == '*' || c == '/' || c=='%') { 
            i++; 
            return new Palavra(c+"", "operador aritmetico"); 
        } 

        //especiais
        // Verifica delimitadores e pontuações da gramática (parênteses, vírgula, ponto e vírgula, dois pontos isolado)
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

        // Verifica símbolos matemáticos para operadores lógicos (E, OU, NÃO)
        if (c=='^' || c=='∨' || c=='¬') { 
            i++; 
            return new Palavra(c+"", "operador lógico"); 
        } 

        //se chegou ate aqui nao eh nenhum caractere resconhecido
        // Qualquer caractere estranho à linguagem (ex: '@', '_', '#') cai aqui e gera erro léxico
        i++; 
        return new Palavra(c + "", "identificador invalido");
    }

    // Máquina de Estados (Autômato Finito Determinístico) para reconhecer números inteiros, reais e em notação científica
    private Palavra achouNumeral() {
        String lexema = ""; // Acumulador para os caracteres do número
        char estado = 'i'; // i: inteiro, p: ponto lido, f: flutuante, c: char_cientifico, s: sinal_expoente, n: notacao_concluida

        // Percorre os caracteres enquanto fizerem parte da estrutura numérica
        while (i < texto.length()) {
            char c = texto.charAt(i); // Lê o caractere atual

            // Transições ao ler um dígito de '0' a '9'
            if (c >= '0' && c <= '9') {
                lexema += c; // Adiciona o dígito ao lexema
                if (estado == 'c' || estado == 's') { 
                    estado = 'n'; // Se estava aguardando o expoente após 'e' ou sinal, conclui a notação científica
                } else if (estado == 'p') {
                    estado = 'f'; // Se havia acabado de ler um ponto decimal, confirma que é um número real (flutuante)
                }
                i++; // Avança o ponteiro
            } 
            // Transição ao encontrar um ponto decimal '.' (só é válido se o número ainda for inteiro 'i')
            else if (c == '.' && estado == 'i') {
                // Se houver outro ponto em seguida ('..'), interrompe o número (ex: operador de intervalo em Pascal)
                if (i + 1 < texto.length() && texto.charAt(i + 1) == '.') {
                    break;
                }
                lexema += c; // Anexa o ponto decimal ao número
                estado = 'p'; // Muda para o estado intermediário 'p' (aguardando casas decimais)
                i++;
            } 
            // Transição para notação científica padrão ('e' ou 'E') a partir de um inteiro ou real
            else if ((c == 'e' || c == 'E') && (estado == 'i' || estado == 'f')) {
                lexema += c;
                estado = 'c'; // Muda para o estado intermediário 'c' (aguardando sinal ou dígitos do expoente)
                i++;
            } 
            // Transição para notação científica customizada 'x10e' a partir de um inteiro ou real
            else if (c == 'x' && (estado == 'i' || estado == 'f')) {
                // A sua notação customizada: x10e
                // Faz um lookahead de 3 posições para confirmar se os próximos caracteres formam exatamente "x10e"
                if (i + 3 < texto.length() && 
                    texto.charAt(i + 1) == '1' && 
                    texto.charAt(i + 2) == '0' && 
                    texto.charAt(i + 3) == 'e') {
                    lexema += "x10e"; // Anexa a sequência completa ao lexema
                    estado = 'c'; // Vai para o estado de notação científica aguardando o expoente
                    i += 4; // Avança os 4 caracteres de uma vez
                } else {
                    break; // Era só um 'x' de outra coisa, sai do número
                }
            } 
            // Transição para o sinal opcional ('+' ou '-') exclusivo logo após o indicador de notação científica
            else if ((c == '+' || c == '-') && estado == 'c') {
                // Sinal opcional logo após o 'e' ou 'x10e'
                lexema += c;
                estado = 's'; // 's' = sinal lido, aguardando expoente numérico
                i++;
            } 
            else {
                break; // Qualquer outro caractere encerra o número
            }
        }

        // Validação de número mal-formado (ex: terminou em "123." ou "1.5e" sem completar)
        // Se o autômato parou em um estado não-final ('p', 'c' ou 's'), retorna erro léxico
        if (estado == 'p' || estado == 'c' || estado == 's') {
            return new Palavra(lexema, "identificador invalido"); 
        }

        // Classifica o token de acordo com o estado final
        String tipo = "numero inteiro"; // Se permaneceu no estado 'i', é inteiro
        if (estado == 'f') tipo = "numero real"; // Se terminou no estado 'f', é real
        if (estado == 'n') tipo = "numero em notacao cientifica"; // Se terminou no estado 'n', é científico

        return new Palavra(lexema, tipo); // Cria e retorna o objeto Palavra com o número validado
    }

    // Método responsável por ler palavras (identificadores de variáveis ou palavras reservadas da linguagem)
    private Palavra achouIdentificador() {
        Palavra novoId = null;
        String lexema = texto.charAt(i)+""; // Inicia o lexema com a primeira letra já lida
        int aux=i+1; // Cria um ponteiro auxiliar para avançar enquanto houver letras ou números
        // Continua lendo enquanto os caracteres seguintes forem alfanuméricos (letras ou dígitos)
        while(aux<texto.length()){
            char c = texto.charAt(aux);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')|| (c>='0' && c<='9')){
                lexema+=c; // Concatena o caractere à palavra
            }else{
                break; // Se encontrar espaço, operador ou pontuação, encerra a leitura da palavra
            }
            aux++;
        }
        i=aux; // Atualiza o ponteiro global 'i' para a posição onde a palavra terminou

        //procura lexema na lista de palavras reservadas
        // Compara a palavra lida com o dicionário da linguagem (ignorando diferenças entre maiúsculas e minúsculas)
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
        
        // Se não consta na lista de palavras reservadas, classifica como identificador (nome de variável/função)
        novoId = new Palavra(lexema, "identificador");
        return novoId;
    }

    // Método que agrupa a lista contínua de tokens em objetos Linha e captura erros léxicos via exceções
    public ArrayList<Linha> geradorLinhas(ArrayList<Palavra> palavras){
        ArrayList<Linha> linhas = new ArrayList<>(); // Lista final de linhas que será enviada para a interface gráfica
        ArrayList<Palavra> palavrasLinha = new ArrayList<>(); // Acumulador temporário de palavras da linha atual
        int cont=1; // Contador da numeração das linhas (começa na linha 1)
        boolean erroNaLinha=false; // Flag para indicar se a linha atual possui algum token inválido
        Linha l =null;
        String mensagemErro=""; // Acumula a descrição dos erros encontrados naquela linha

        // Percorre todas as palavras geradas pela análise léxica
        for(Palavra p: palavras){
            try {
                // O método getTolken() lança uma Exception caso o token seja "identificador invalido"
                // Se o token for "ESPACO" (quebra de linha '\n'), fecha a linha atual e cria o objeto Linha
                if(p.getTolken().equals("ESPACO")){
                    l = new Linha(cont++, palavrasLinha); // Instancia a linha com seu número e lista de palavras
                    linhas.add(l); // Adiciona na lista de linhas do arquivo
                    // Se alguma palavra desta linha lançou exceção anteriormente, registra o erro na linha
                    if(erroNaLinha){
                        l.setErro(mensagemErro); // Marca a linha como errada (fundo vermelho) e anexa a mensagem
                        mensagemErro = ""; // Limpa a mensagem para a próxima linha
                        linhasErradas.add(l); // Guarda a referência na lista de navegação de erros
                        erroNaLinha=false; // Reseta a flag de erro para a próxima linha
                    }
                    palavrasLinha = new ArrayList<>(); // Cria uma nova lista vazia para começar a próxima linha

                }else{
                    palavrasLinha.add(p); // Se for uma palavra normal e sem erro, adiciona na linha atual
                }
            // Captura a exceção lançada por getTolken() quando encontra um "identificador invalido"
            } catch (Exception e) {
                erroNaLinha = true; // Sinaliza que a linha atual possui erro léxico
                palavrasLinha.add(p); // Adiciona a palavra inválida na linha mesmo assim para ela ser exibida na tela
                mensagemErro+="  ------ "+e.getMessage(); // Concatena o detalhe do erro (ex: "identificador invalido: @")
            }
        }

        // Caso a última linha do arquivo não termine com '\n', empacota as palavras restantes na última Linha
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

    // Método de acesso (Getter) para a classe Main obter a lista de linhas com erro e habilitar os botões de navegação
    public ArrayList<Linha> getLinhasErradas() {
        return linhasErradas;
    }
    
}