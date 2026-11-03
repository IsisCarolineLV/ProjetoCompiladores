import java.awt.*; // Importa todos os componentes da biblioteca grafica Swing (JFrame, JPanel, JButton, etc.)
import java.io.BufferedReader; // Importa classes de layout, cores, fontes e dimensoes (AWT)
import java.io.File; // Importa o leitor de fluxo de texto em buffer para leitura eficiente do arquivo
import java.io.FileReader; // Importa a representacao de arquivos e diretorios do sistema operacional
import java.io.IOException; // Importa a classe para ler arquivos de caracteres do disco
import java.util.ArrayList; // Importa a classe de tratamento de excecoes de Entrada/Saida (erro de leitura de arquivo)
import javax.swing.*; // Importa a estrutura de lista dinamica

// Classe principal que gerencia a interface grafica (GUI), leitura de arquivos e orquestracao da analise
public class Main{
    private static String caminhoAtual=""; // Guarda o endereco (caminho absoluto) do arquivo selecionado no computador
    //private static AnalisadorLexico analisadorLexico; // Referencia para a instancia do motor do analisador lexico
    //private static AnalisadorSintatico analisadorSintatico; // Referencia para a instancia do motor do analisador lexico
    private static JLabel areaErro; // Rotulo visual no rodape que exibe a mensagem descritiva do erro lexico
    private static JPanel areaConteudo; // Painel central onde as linhas do codigo-fonte sao desenhadas
    private static JLabel lblTitulo; // Rotulo que exibe o nome do arquivo selecionado na tela
    private static boolean mostrando = false;
    private static boolean arquivoFoiAtualizado = false;

    private static StringBuilder texto; // Armazena todo o conteudo textual lido do arquivo antes da analise
    private static ArrayList<Linha> linhasComErro; // Lista que guarda apenas as linhas que apresentaram erro para navegacao
    private static ArrayList<Linha> linhasErradasLexico, linhasErradasSintatico;
    private static int contErro=0; // indice que controla qual erro esta sendo focado no momento pelos botoes

    //botoes:
    private static JButton btnPesquisar; // Botao para abrir o explorador de arquivos
    private static JButton btnAnalisar; // Botao para disparar a analise lexica sobre o arquivo carregado
    private static JButton btnErroAnterior; // Botao para subir ate o erro lexico anterior
    private static JButton btnProximoErro; // Botao para descer ate o proximo erro lexico
    private static JButton btnAnalisarLexico; // Botao para analisar apenas lexicalmente
    private static JButton btnAnalisarSintatico; // Botao para analisar sintaticamente
    private static JButton btnMostrar; // Botao para mostrar os botoes de analise lexica e sintatica

    //listas de linhas:
    private static ArrayList<Linha> resultadoAnalise;
    private static ArrayList<Palavra> listaPalavras;
    
    // Metodo de entrada padrao da aplicacao Java
    public static void main(String[] args){
        //criarTela();
        // Garante que a interface grafica seja construida e executada na Thread de despacho de eventos do Swing (EDT)
        SwingUtilities.invokeLater(Main::criarTela);
    } 

    // Metodo responsavel por instanciar, configurar e posicionar todos os elementos visuais da janela
    public static void criarTela() {

        Tabela.preencher();

        //configuracoes do rotulo
        // Obtem o gerenciador global de ToolTips (aqueles baloes que mostram o tipo do token ao passar o mouse)
        ToolTipManager manager = ToolTipManager.sharedInstance();
        manager.setInitialDelay(100);  // Define que o balao aparecera super rapido (100 milissegundos apos parar o mouse)
        manager.setDismissDelay(10000); // Define que o balao ficara visivel por ate 10 segundos (10000 ms)
        manager.setReshowDelay(50); // Define o tempo de resposta ao mover o mouse rapidamente de um token para outro (50 ms)

        // Tela principal
        JFrame frame = new JFrame("Analisador Lexico"); // Cria a janela principal do programa com o titulo na barra superior
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Encerra o processo do Java ao clicar no 'X' da janela
        frame.setSize(1024, 720); // Define a largura (800px) e altura (600px) inicial da janela
        frame.setLocationRelativeTo(null); // Centraliza a janela exatamente no meio da tela do monitor

        // Organizador Vertical
        JPanel organizadorV = new JPanel(); // Cria o painel mestre que vai empilhar os elementos de cima para baixo
        organizadorV.setLayout(new BoxLayout(organizadorV, BoxLayout.Y_AXIS)); // Configura o empilhamento no eixo Y (vertical)
        organizadorV.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Adiciona margem interna de 10px nas bordas da janela

        //Organizador Horizontal (Painel para os botoes)
        // Cria uma barra superior alinhada a esquerda (sem espacamento padrao) para enfileirar os botoes
        JPanel organizadorH = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
        organizadorH.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinha a barra de botoes a esquerda do painel vertical
        
        btnPesquisar = new JButton("Pesquisar"); // Instancia o botao de buscar arquivo
        btnAnalisar = new JButton("Analisar"); // Instancia o botao de analisar tokens
        btnAnalisar.setEnabled(false); // Comeca desabilitado (so ativa apos o usuario escolher um arquivo)

        btnErroAnterior = new JButton("⬆ Erro Anterior"); // Instancia o botao de voltar erro
        btnErroAnterior.setEnabled(false); // Comeca desabilitado (so ativa se houver erros apos a analise)
            
        btnProximoErro = new JButton("⬇ Proximo Erro"); // Instancia o botao de avancar erro
        btnProximoErro.setEnabled(false); // Comeca desabilitado (so ativa se houver erros apos a analise)

        btnMostrar = new JButton("+");

        btnAnalisarLexico = new JButton("Analise Lexica"); // Instancia o botao de analisar tokens
        btnAnalisarLexico.setEnabled(false); // Comeca desabilitado (so ativa apos o usuario escolher um arquivo)
        btnAnalisarLexico.setVisible(false);

        btnAnalisarSintatico = new JButton("Analise Sintatica"); // Instancia o botao de analisar tokens
        btnAnalisarSintatico.setEnabled(false); // Comeca desabilitado (so ativa apos o usuario escolher um arquivo)
        btnAnalisarSintatico.setVisible(false);

        organizadorH.add(btnPesquisar); // Adiciona o botao Pesquisar na barra horizontal
        organizadorH.add(Box.createRigidArea(new Dimension(5, 0))); // Cria um espacamento invisivel de 5px horizontais entre os botoes
        organizadorH.add(btnAnalisar); // Adiciona o botao Analisar
        organizadorH.add(Box.createRigidArea(new Dimension(5, 0))); // Espacamento de 5px
        organizadorH.add(btnMostrar); // Adiciona o botao Mostrar
        organizadorH.add(Box.createRigidArea(new Dimension(5, 0))); // Espacamento de 5px
        organizadorH.add(btnAnalisarLexico); // Adiciona o botao Analisar
        organizadorH.add(Box.createRigidArea(new Dimension(5, 0))); // Espacamento de 5px
        organizadorH.add(btnAnalisarSintatico); // Adiciona o botao Analisar
        organizadorH.add(Box.createRigidArea(new Dimension(50, 0))); // Espacamento de 5px
        organizadorH.add(btnErroAnterior); // Adiciona o botao Erro Anterior
        organizadorH.add(Box.createRigidArea(new Dimension(5, 0))); // Espacamento de 5px
        organizadorH.add(btnProximoErro); // Adiciona o botao Proximo Erro
        
        // Trava a altura maxima da barra de botoes em 40px para ela nao esticar verticalmente
        organizadorH.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // titulo do arquivo
        lblTitulo = new JLabel("Arquivo: nenhum arquivo selecionado"); // Texto padrao antes de carregar qualquer codigo
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14)); // Configura a fonte sem serifa, em negrito e tamanho 14
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinha o titulo a esquerda

        // ScrollPane
        areaConteudo = new JPanel(); // Cria o painel onde as linhas do codigo serao inseridas
        areaConteudo.setLayout(new BoxLayout(areaConteudo, BoxLayout.Y_AXIS)); // Configura para empilhar as linhas verticalmente
        areaConteudo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Margem interna de 5px
        
        // Envolve a areaConteudo em um JScrollPane para criar barras de rolagem automatica (horizontal e vertical)
        JScrollPane scrollPane = new JScrollPane(areaConteudo);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinha a caixa de codigo a esquerda
        scrollPane.setMinimumSize(new Dimension (0,100));
        scrollPane.setPreferredSize(new Dimension(1024,500));

        // Pane normal para aparecer o erro
        
        JPanel paneErro = new JPanel(new BorderLayout()); // Cria o painel inferior para exibir os detalhes dos erros
        paneErro.setBorder(BorderFactory.createTitledBorder("Painel de Erro")); // Cria uma borda desenhada com o titulo "Painel de Erro"
        areaErro = new JLabel("         Nenhum erro encontrado."); // Texto inicial quando nao ha erros
        areaErro.setForeground(Color.RED); // Pinta a fonte da mensagem de erro de vermelho
        paneErro.add(new JScrollPane(areaErro), BorderLayout.CENTER); // Coloca o texto de erro dentro de uma area com rolagem no centro do painel
        
        paneErro.setPreferredSize(new Dimension(800, 180)); // Define o tamanho ideal do painel de erros (altura de 120px)
        paneErro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180)); // Trava a altura maxima em 120px
        paneErro.setMinimumSize(new Dimension(0,180));
        paneErro.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinha a esquerda

        // Montando as pecas no Organizador Vertical
        organizadorV.add(organizadorH); // 1º item no topo: Barra de botoes
        organizadorV.add(Box.createRigidArea(new Dimension(0, 10))); // Espaco vertical vazio de 10px
        organizadorV.add(lblTitulo); // 2º item: Nome do arquivo selecionado
        organizadorV.add(Box.createRigidArea(new Dimension(0, 10))); // Espaco vertical vazio de 10px
        organizadorV.add(scrollPane); // 3º item: area central com o codigo-fonte e barras de rolagem
        organizadorV.add(Box.createRigidArea(new Dimension(0, 10))); // Espaco vertical vazio de 10px
        organizadorV.add(paneErro); // 4º item no rodape: Painel de exibicao de erros

        frame.add(organizadorV); // Insere o organizador vertical completo dentro da janela principal
        frame.setVisible(true); // Torna a janela visivel na tela para o usuario

        //LISTENERS ------------------------------------------------------------------------------------------------------
        // Define a acao executada ao clicar no botao "Pesquisar"
        btnPesquisar.addActionListener(e ->{
            String t = chamarTelaPesquisa(); // Abre a janela de selecao de arquivos e recebe o nome do arquivo escolhido
            if(!t.equals("nenhum arquivo selecionado")){
                areaConteudo.removeAll(); // Limpa o codigo anterior que estava na tela
                resultadoAnalise = null; //limpa as linhas salvas
                resultadoAnalise = null;  //limpa as linhas salvas
                lblTitulo.setText("Arquivo: "+t); // Atualiza o rotulo com o nome do novo arquivo
                //System.out.println("Caminho lido:" +caminhoAtual);
                texto = getTexto(); // Le o arquivo do disco, exibe o texto bruto na tela e guarda no StringBuilder
                btnAnalisar.setEnabled(true); // Libera o botao "Analisar" para uso
                btnAnalisarLexico.setEnabled(true);
                btnAnalisarSintatico.setEnabled(false);
            }
        });

        // Define a acao executada ao clicar no botao "Analisar"
        btnAnalisar.addActionListener(e->{
            if(caminhoAtual.equals("")){
                areaErro.setText("Nenhum arquivo selecionado!"); // Prevencao caso tente analisar sem ter escolhido um arquivo
            }else{
                texto = getMudancas(); // Le o arquivo do disco para captar mudancas
                AnalisadorLexico analisadorLexico = new AnalisadorLexico(texto); // Instancia o motor lexico passando o texto bruto do arquivo
                resultadoAnalise = analisadorLexico.classificarTolkens(); // Executa toda a analise lexica e recebe as linhas prontas e coloridas
                linhasErradasLexico = analisadorLexico.getLinhasErradas(); // Recupera a lista contendo apenas as linhas que deram erro lexico
                listaPalavras = analisadorLexico.listaPalavras;
                AnalisadorSintatico analisadorSintatico = new AnalisadorSintatico();
                resultadoAnalise = analisadorSintatico.analisar(listaPalavras);
                linhasErradasSintatico  = analisadorSintatico.getLinhasErradas();
                linhasComErro = linhasErradasSintatico;
                imprimir(resultadoAnalise, linhasComErro);
            }
        });

        // Define a acao do botao "Mostrar": mostra ou esconde os botoes de passo a passo
        btnMostrar.addActionListener(e->{
            mostrando = !mostrando;
            btnAnalisarLexico.setVisible(mostrando);
            btnAnalisarSintatico.setVisible(mostrando);

            if(mostrando)   btnMostrar.setText("-");
            else    btnMostrar.setText("+");
        });

        btnAnalisarLexico.addActionListener(e ->{
            if(caminhoAtual.equals("")){
                areaErro.setText("Nenhum arquivo selecionado!"); // Prevencao caso tente analisar sem ter escolhido um arquivo
            }else{
                arquivoFoiAtualizado = false;
                StringBuilder textoAux = getMudancas(); // Le o arquivo do disco para captar mudancas
                if(!texto.equals(textoAux)){
                    texto = textoAux;
                    AnalisadorLexico analisadorLexico = new AnalisadorLexico(texto); // Instancia o motor lexico passando o texto bruto do arquivo
                    resultadoAnalise = analisadorLexico.classificarTolkens(); // Executa toda a analise lexica e recebe as linhas prontas e coloridas
                    linhasErradasLexico = analisadorLexico.getLinhasErradas(); // Recupera a lista contendo apenas as linhas que deram erro lexico
                    arquivoFoiAtualizado = true;
                    listaPalavras = analisadorLexico.listaPalavras;
                }
                linhasComErro = linhasErradasLexico;
                imprimir(resultadoAnalise, linhasComErro);
                
                btnAnalisarSintatico.setEnabled(true);
            }
        });

        btnAnalisarSintatico.addActionListener(e->{
            if(arquivoFoiAtualizado){
                AnalisadorSintatico analisadorSintatico = new AnalisadorSintatico(); // Instancia o motor lexico passando o texto bruto do arquivo
                resultadoAnalise = analisadorSintatico.analisar(listaPalavras);
                linhasErradasSintatico = analisadorSintatico.getLinhasErradas();
            }
            linhasComErro = linhasErradasSintatico;
            System.out.println("analisou sintaticamente :)");
            imprimir(resultadoAnalise, linhasComErro);
        });

        // Define a acao do botao "Proximo Erro": avanca o contador e foca na proxima linha problematica
        btnProximoErro.addActionListener(e->{
            contErro++;
            mostraErro(linhasComErro, contErro);
        });

        // Define a acao do botao "Erro Anterior": retrocede o contador e foca na linha problematica anterior
        btnErroAnterior.addActionListener(e->{
            contErro--;
            mostraErro(linhasComErro, contErro);
        });
    }

    private static void imprimir(ArrayList<Linha> resultado, ArrayList<Linha> linhasErradas){
        imprimirArquivo(resultado); // Substitui o texto bruto na tela pelas linhas analisadas com destaque de sintaxe e tooltips
        contErro=0; // Posiciona o indice de navegacao no primeiro erro (posicao 0)
        
        // Se pelo menos 1 linha apresentou erro lexico, ativa a navegacao de erros e foca no primeiro erro
        if(linhasErradas.size()>=1){
            btnErroAnterior.setEnabled(true);
            btnProximoErro.setEnabled(true);
            mostraErro(linhasErradas, 0); // Destaca a primeira linha com erro na tela
        }else{
            areaErro.setText("        Nenhum erro encontrado :)");
        }
    }
    

    // Metodo auxiliar que gerencia o foco visual, rolagem de tela e ativacao dos botoes de navegacao de erros
    private static void mostraErro(ArrayList<Linha> linhas, int i) {
        // Garante que o indice 'i' esta dentro dos limites validos da lista de erros
        if (i >= 0 && i < linhas.size() && linhas.size()>0) {
            areaErro.setText(linhas.get(i).getErro()); // Exibe a descricao textual do erro daquela linha especifica no rodape
            linhas.get(i).destacarLinha();  //destaca linha atual (pinta de vermelho escuro e rola a tela ate ela)
            if(i>0) linhas.get(i-1).normalizaLinha();   //retorna a anteriormente destacada para a cor normal (vermelho claro)
            if(i<linhas.size()-1) linhas.get(i+1).normalizaLinha();   //retorna a anteriormente destacada para a cor normal (vermelho claro)
            btnErroAnterior.setEnabled(i > 0); // Desativa o botao "Anterior" se ja estiver no primeiro erro (indice 0)
            btnProximoErro.setEnabled(i < linhas.size() - 1); // Desativa o botao "Proximo" se ja estiver no ultimo erro da lista
        }
    }

    // Metodo que abre a janela nativa do sistema operacional para o usuario escolher o arquivo de codigo-fonte
    public static String chamarTelaPesquisa() {
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir"))); // Instancia o seletor de arquivos do Swing

        int resultado = fileChooser.showOpenDialog(null); // Exibe a caixa de dialogo "Abrir Arquivo" e aguarda a acao do usuario

        // Se o usuario escolheu um arquivo e clicou em "Abrir/OK" (APPROVE_OPTION)
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile(); // Captura o objeto File correspondente ao arquivo escolhido

            caminhoAtual = arquivo.getAbsolutePath(); // Extrai e armazena o caminho completo do arquivo no disco (ex: C:\pasta\codigo.pas)

            return fileChooser.getSelectedFile().getName(); // Retorna apenas o nome do arquivo (ex: codigo.pas) para exibir no titulo
        }

        return "nenhum arquivo selecionado"; // Retorno padrao caso o usuario feche a janela ou clique em "Cancelar"
        
    }

    // Metodo que le o arquivo fisico do disco linha por linha, monta a pre-visualizacao sem cores e preenche o StringBuilder
    public static StringBuilder getTexto(){
        StringBuilder texto= new StringBuilder(""); // Inicia o acumulador mutavel de texto vazio
        // Usa try-with-resources para abrir o arquivo e garantir que ele sera fechado automaticamente ao terminar a leitura
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoAtual))) {

            String linha;
            int cont=1; // Contador para numerar as linhas na pre-visualizacao
            // Le o arquivo linha por linha ate chegar ao fim (null)
            while ((linha = br.readLine()) != null) {
                areaConteudo.add(gerarLinha(cont++, linha)); // Cria um painel simples (sem analise lexica) e adiciona na tela
                texto.append( linha +"\n"); // Anexa a linha lida e a quebra de linha '\n' no StringBuilder que ira para o analisador
            }

        } catch (IOException e) {
            e.printStackTrace(); // Imprime o erro no console caso o arquivo nao seja encontrado ou nao possa ser lido
        }
        areaConteudo.revalidate(); // Solicita ao Swing que recalcule o layout do painel apos adicionar as novas linhas
        areaConteudo.repaint(); // Forca o redesenho visual dos componentes na tela

        return texto; // Retorna o conteudo completo do arquivo pronto para ser enviado ao AnalisadorLexico
    }

    public static StringBuilder getMudancas(){
        StringBuilder texto= new StringBuilder(""); // Inicia o acumulador mutavel de texto vazio
        // Usa try-with-resources para abrir o arquivo e garantir que ele sera fechado automaticamente ao terminar a leitura
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoAtual))) {
            String linha;
            // Le o arquivo linha por linha ate chegar ao fim (null)
            while ((linha = br.readLine()) != null) {
                texto.append( linha +"\n"); // Anexa a linha lida e a quebra de linha '\n' no StringBuilder que ira para o analisador
            }
        } catch (IOException e) {
            e.printStackTrace(); // Imprime o erro no console caso o arquivo nao seja encontrado ou nao possa ser lido
        }
        return texto;
    }

    // Metodo que substitui a visualizacao bruta pela visualizacao processada (com cores por token, tooltips e fundos de erro)
    public static void imprimirArquivo(ArrayList<Linha> linhas){
        areaConteudo.removeAll(); // Remove todas as linhas brutas que estavam desenhadas na tela

        // Percorre a lista de objetos Linha gerados pelo AnalisadorLexico
        for(Linha l : linhas){
            areaConteudo.add(l.gerarLinha()); // Pede para cada Linha gerar seu JPanel colorido com os tokens e o adiciona na tela
        }

        areaConteudo.revalidate(); // Recalcula a estrutura visual da area de conteudo com os novos componentes
        areaConteudo.repaint(); // Atualiza a tela imediatamente para exibir o codigo colorido
    }

    // Metodo auxiliar usado apenas na pre-visualizacao (antes de clicar em Analisar) para mostrar o texto puro recem-aberto
    public static JPanel gerarLinha(int num, String conteudo){
        // Cria um painel horizontal simples alinhado a esquerda (espacamento de 10px horizontal e 5px vertical)
        JPanel novaLinha = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JLabel numeroLinha = new JLabel(String.valueOf(num)); // Rotulo com o numero da linha
        JLabel texto = new JLabel(conteudo); // Rotulo unico contendo a linha inteira sem separacao de tokens e sem cor

        novaLinha.add(numeroLinha); // Adiciona o numero ao painel da linha
        novaLinha.add(texto); // Adiciona o texto bruto ao painel da linha

        novaLinha.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinha o painel a esquerda

        // Calcula a altura ideal da linha e trava a altura maxima para evitar distorcoes no BoxLayout vertical
        Dimension tamanho = novaLinha.getPreferredSize();
        novaLinha.setMaximumSize(new Dimension(Integer.MAX_VALUE, tamanho.height));
        
        
        return novaLinha; // Retorna o painel simples para ser exibido na pre-visualizacao

    }
}