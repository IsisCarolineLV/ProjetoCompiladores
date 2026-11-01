import java.awt.*; // Importa todos os componentes da biblioteca gráfica Swing (JFrame, JPanel, JButton, etc.)
import java.io.BufferedReader; // Importa classes de layout, cores, fontes e dimensões (AWT)
import java.io.File; // Importa o leitor de fluxo de texto em buffer para leitura eficiente do arquivo
import java.io.FileReader; // Importa a representação de arquivos e diretórios do sistema operacional
import java.io.IOException; // Importa a classe para ler arquivos de caracteres do disco
import java.util.ArrayList; // Importa a classe de tratamento de exceções de Entrada/Saída (erro de leitura de arquivo)
import javax.swing.*; // Importa a estrutura de lista dinâmica

// Classe principal que gerencia a interface gráfica (GUI), leitura de arquivos e orquestração da análise
public class Main{
    private static String caminhoAtual=""; // Guarda o endereço (caminho absoluto) do arquivo selecionado no computador
    private static AnalisadorLexico analisador; // Referência para a instância do motor do analisador léxico
    private static JLabel areaErro; // Rótulo visual no rodapé que exibe a mensagem descritiva do erro léxico
    private static JPanel areaConteudo; // Painel central onde as linhas do código-fonte são desenhadas
    private static JLabel lblTitulo; // Rótulo que exibe o nome do arquivo selecionado na tela

    private static StringBuilder texto; // Armazena todo o conteúdo textual lido do arquivo antes da análise
    private static ArrayList<Linha> linhasComErro; // Lista que guarda apenas as linhas que apresentaram erro para navegação
    private static int contErro=0; // Índice que controla qual erro está sendo focado no momento pelos botões

    //botoes:
    private static JButton btnPesquisar; // Botão para abrir o explorador de arquivos
    private static JButton btnAnalisar; // Botão para disparar a análise léxica sobre o arquivo carregado
    private static JButton btnErroAnterior; // Botão para subir até o erro léxico anterior
    private static JButton btnProximoErro; // Botão para descer até o próximo erro léxico
    
    // Método de entrada padrão da aplicação Java
    public static void main(String[] args){
        //criarTela();
        // Garante que a interface gráfica seja construída e executada na Thread de despacho de eventos do Swing (EDT)
        SwingUtilities.invokeLater(Main::criarTela);
    } 

    // Método responsável por instanciar, configurar e posicionar todos os elementos visuais da janela
    public static void criarTela() {

        //configuracoes do rotulo
        // Obtém o gerenciador global de ToolTips (aqueles balões que mostram o tipo do token ao passar o mouse)
        ToolTipManager manager = ToolTipManager.sharedInstance();
        manager.setInitialDelay(100);  // Define que o balão aparecerá super rápido (100 milissegundos após parar o mouse)
        manager.setDismissDelay(10000); // Define que o balão ficará visível por até 10 segundos (10000 ms)
        manager.setReshowDelay(50); // Define o tempo de resposta ao mover o mouse rapidamente de um token para outro (50 ms)

        // Tela principal
        JFrame frame = new JFrame("Analisador Lexico"); // Cria a janela principal do programa com o título na barra superior
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Encerra o processo do Java ao clicar no 'X' da janela
        frame.setSize(1024, 720); // Define a largura (800px) e altura (600px) inicial da janela
        frame.setLocationRelativeTo(null); // Centraliza a janela exatamente no meio da tela do monitor

        // Organizador Vertical
        JPanel organizadorV = new JPanel(); // Cria o painel mestre que vai empilhar os elementos de cima para baixo
        organizadorV.setLayout(new BoxLayout(organizadorV, BoxLayout.Y_AXIS)); // Configura o empilhamento no eixo Y (vertical)
        organizadorV.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Adiciona margem interna de 10px nas bordas da janela

        //Organizador Horizontal (Painel para os botoes)
        // Cria uma barra superior alinhada à esquerda (sem espaçamento padrão) para enfileirar os botões
        JPanel organizadorH = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
        organizadorH.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinha a barra de botões à esquerda do painel vertical
        
        btnPesquisar = new JButton("Pesquisar"); // Instancia o botão de buscar arquivo
        btnAnalisar = new JButton("Analisar"); // Instancia o botão de analisar tokens
        btnAnalisar.setEnabled(false); // Começa desabilitado (só ativa após o usuário escolher um arquivo)

        btnErroAnterior = new JButton("⬆ Erro Anterior"); // Instancia o botão de voltar erro
        btnErroAnterior.setEnabled(false); // Começa desabilitado (só ativa se houver erros após a análise)
            
        btnProximoErro = new JButton("⬇ Proximo Erro"); // Instancia o botão de avançar erro
        btnProximoErro.setEnabled(false); // Começa desabilitado (só ativa se houver erros após a análise)

        organizadorH.add(btnPesquisar); // Adiciona o botão Pesquisar na barra horizontal
        organizadorH.add(Box.createRigidArea(new Dimension(5, 0))); // Cria um espaçamento invisível de 5px horizontais entre os botões
        organizadorH.add(btnAnalisar); // Adiciona o botão Analisar
        organizadorH.add(Box.createRigidArea(new Dimension(5, 0))); // Espaçamento de 5px
        organizadorH.add(btnErroAnterior); // Adiciona o botão Erro Anterior
        organizadorH.add(Box.createRigidArea(new Dimension(5, 0))); // Espaçamento de 5px
        organizadorH.add(btnProximoErro); // Adiciona o botão Próximo Erro
        
        // Trava a altura máxima da barra de botões em 40px para ela não esticar verticalmente
        organizadorH.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // titulo do arquivo
        lblTitulo = new JLabel("Arquivo: nenhum arquivo selecionado"); // Texto padrão antes de carregar qualquer código
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14)); // Configura a fonte sem serifa, em negrito e tamanho 14
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinha o título à esquerda

        // ScrollPane
        areaConteudo = new JPanel(); // Cria o painel onde as linhas do código serão inseridas
        areaConteudo.setLayout(new BoxLayout(areaConteudo, BoxLayout.Y_AXIS)); // Configura para empilhar as linhas verticalmente
        areaConteudo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Margem interna de 5px
        
        // Envolve a areaConteudo em um JScrollPane para criar barras de rolagem automática (horizontal e vertical)
        JScrollPane scrollPane = new JScrollPane(areaConteudo);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinha a caixa de código à esquerda
        scrollPane.setMinimumSize(new Dimension (0,100));
        scrollPane.setPreferredSize(new Dimension(1024,500));

        // Pane normal para aparecer o erro
        
        JPanel paneErro = new JPanel(new BorderLayout()); // Cria o painel inferior para exibir os detalhes dos erros
        paneErro.setBorder(BorderFactory.createTitledBorder("Painel de Erro")); // Cria uma borda desenhada com o título "Painel de Erro"
        areaErro = new JLabel("Nenhum erro encontrado."); // Texto inicial quando não há erros
        areaErro.setForeground(Color.RED); // Pinta a fonte da mensagem de erro de vermelho
        paneErro.add(new JScrollPane(areaErro), BorderLayout.CENTER); // Coloca o texto de erro dentro de uma área com rolagem no centro do painel
        
        paneErro.setPreferredSize(new Dimension(800, 180)); // Define o tamanho ideal do painel de erros (altura de 120px)
        paneErro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180)); // Trava a altura máxima em 120px
        paneErro.setMinimumSize(new Dimension(0,180));
        paneErro.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinha à esquerda

        // Montando as peças no Organizador Vertical
        organizadorV.add(organizadorH); // 1º item no topo: Barra de botões
        organizadorV.add(Box.createRigidArea(new Dimension(0, 10))); // Espaço vertical vazio de 10px
        organizadorV.add(lblTitulo); // 2º item: Nome do arquivo selecionado
        organizadorV.add(Box.createRigidArea(new Dimension(0, 10))); // Espaço vertical vazio de 10px
        organizadorV.add(scrollPane); // 3º item: Área central com o código-fonte e barras de rolagem
        organizadorV.add(Box.createRigidArea(new Dimension(0, 10))); // Espaço vertical vazio de 10px
        organizadorV.add(paneErro); // 4º item no rodapé: Painel de exibição de erros

        frame.add(organizadorV); // Insere o organizador vertical completo dentro da janela principal
        frame.setVisible(true); // Torna a janela visível na tela para o usuário

        //listeners
        // Define a ação executada ao clicar no botão "Pesquisar"
        btnPesquisar.addActionListener(e ->{
            String t = chamarTelaPesquisa(); // Abre a janela de seleção de arquivos e recebe o nome do arquivo escolhido
            if(!t.equals("nenhum arquivo selecionado")){
                areaConteudo.removeAll(); // Limpa o código anterior que estava na tela
                lblTitulo.setText("Arquivo: "+t); // Atualiza o rótulo com o nome do novo arquivo
                //System.out.println("Caminho lido:" +caminhoAtual);
                texto = getTexto(); // Lê o arquivo do disco, exibe o texto bruto na tela e guarda no StringBuilder
            }
            btnAnalisar.setEnabled(true); // Libera o botão "Analisar" para uso
        });

        // Define a ação executada ao clicar no botão "Analisar"
        btnAnalisar.addActionListener(e->{
            if(caminhoAtual.equals("")){
                areaErro.setText("Nenhum arquivo selecionado!"); // Prevenção caso tente analisar sem ter escolhido um arquivo
            }else{
                analisador = new AnalisadorLexico(texto); // Instancia o motor léxico passando o texto bruto do arquivo
                ArrayList<Linha> linhas = analisador.classificarTolkens(); // Executa toda a análise léxica e recebe as linhas prontas e coloridas
                linhasComErro = analisador.getLinhasErradas(); // Recupera a lista contendo apenas as linhas que deram erro léxico
                imprimirArquivo(linhas); // Substitui o texto bruto na tela pelas linhas analisadas com destaque de sintaxe e tooltips
                contErro=0; // Posiciona o índice de navegação no primeiro erro (posição 0)
                
                // Se pelo menos 1 linha apresentou erro léxico, ativa a navegação de erros e foca no primeiro erro
                if(linhasComErro.size()>=1){
                    btnErroAnterior.setEnabled(true);
                    btnProximoErro.setEnabled(true);
                    mostraErro(linhasComErro, 0); // Destaca a primeira linha com erro na tela
                }
            }
            
        });

        // Define a ação do botão "Próximo Erro": avança o contador e foca na próxima linha problemática
        btnProximoErro.addActionListener(e->{
            contErro++;
            mostraErro(linhasComErro, contErro);
        });

        // Define a ação do botão "Erro Anterior": retrocede o contador e foca na linha problemática anterior
        btnErroAnterior.addActionListener(e->{
            contErro--;
            mostraErro(linhasComErro, contErro);
        });
    }
    

    // Método auxiliar que gerencia o foco visual, rolagem de tela e ativação dos botões de navegação de erros
    private static void mostraErro(ArrayList<Linha> linhas, int i) {
        // Garante que o índice 'i' está dentro dos limites válidos da lista de erros
        if (i >= 0 && i < linhas.size() && linhas.size()>0) {
            areaErro.setText(linhas.get(i).getErro()); // Exibe a descrição textual do erro daquela linha específica no rodapé
            linhas.get(i).destacarLinha();  //destaca linha atual (pinta de vermelho escuro e rola a tela até ela)
            if(i>0) linhas.get(i-1).normalizaLinha();   //retorna a anteriormente destacada para a cor normal (vermelho claro)
            if(i<linhas.size()-1) linhas.get(i+1).normalizaLinha();   //retorna a anteriormente destacada para a cor normal (vermelho claro)
            btnErroAnterior.setEnabled(i > 0); // Desativa o botão "Anterior" se já estiver no primeiro erro (índice 0)
            btnProximoErro.setEnabled(i < linhas.size() - 1); // Desativa o botão "Próximo" se já estiver no último erro da lista
        }
    }

    // Método que abre a janela nativa do sistema operacional para o usuário escolher o arquivo de código-fonte
    public static String chamarTelaPesquisa() {
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir"))); // Instancia o seletor de arquivos do Swing

        int resultado = fileChooser.showOpenDialog(null); // Exibe a caixa de diálogo "Abrir Arquivo" e aguarda a ação do usuário

        // Se o usuário escolheu um arquivo e clicou em "Abrir/OK" (APPROVE_OPTION)
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile(); // Captura o objeto File correspondente ao arquivo escolhido

            caminhoAtual = arquivo.getAbsolutePath(); // Extrai e armazena o caminho completo do arquivo no disco (ex: C:\pasta\codigo.pas)

            return fileChooser.getSelectedFile().getName(); // Retorna apenas o nome do arquivo (ex: codigo.pas) para exibir no título
        }

        return "nenhum arquivo selecionado"; // Retorno padrão caso o usuário feche a janela ou clique em "Cancelar"
        
    }

    // Método que lê o arquivo físico do disco linha por linha, monta a pré-visualização sem cores e preenche o StringBuilder
    public static StringBuilder getTexto(){
        StringBuilder texto= new StringBuilder(""); // Inicia o acumulador mutável de texto vazio
        // Usa try-with-resources para abrir o arquivo e garantir que ele será fechado automaticamente ao terminar a leitura
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoAtual))) {

            String linha;
            int cont=1; // Contador para numerar as linhas na pré-visualização
            // Lê o arquivo linha por linha até chegar ao fim (null)
            while ((linha = br.readLine()) != null) {
                areaConteudo.add(gerarLinha(cont++, linha)); // Cria um painel simples (sem análise léxica) e adiciona na tela
                texto.append( linha +"\n"); // Anexa a linha lida e a quebra de linha '\n' no StringBuilder que irá para o analisador
            }

        } catch (IOException e) {
            e.printStackTrace(); // Imprime o erro no console caso o arquivo não seja encontrado ou não possa ser lido
        }
        areaConteudo.revalidate(); // Solicita ao Swing que recalcule o layout do painel após adicionar as novas linhas
        areaConteudo.repaint(); // Força o redesenho visual dos componentes na tela

        return texto; // Retorna o conteúdo completo do arquivo pronto para ser enviado ao AnalisadorLexico
    }

    // Método que substitui a visualização bruta pela visualização processada (com cores por token, tooltips e fundos de erro)
    public static void imprimirArquivo(ArrayList<Linha> linhas){
        areaConteudo.removeAll(); // Remove todas as linhas brutas que estavam desenhadas na tela

        // Percorre a lista de objetos Linha gerados pelo AnalisadorLexico
        for(Linha l : linhas){
            areaConteudo.add(l.gerarLinha()); // Pede para cada Linha gerar seu JPanel colorido com os tokens e o adiciona na tela
        }

        areaConteudo.revalidate(); // Recalcula a estrutura visual da área de conteúdo com os novos componentes
        areaConteudo.repaint(); // Atualiza a tela imediatamente para exibir o código colorido
    }

    // Método auxiliar usado apenas na pré-visualização (antes de clicar em Analisar) para mostrar o texto puro recém-aberto
    public static JPanel gerarLinha(int num, String conteudo){
        // Cria um painel horizontal simples alinhado à esquerda (espaçamento de 10px horizontal e 5px vertical)
        JPanel novaLinha = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JLabel numeroLinha = new JLabel(String.valueOf(num)); // Rótulo com o número da linha
        JLabel texto = new JLabel(conteudo); // Rótulo único contendo a linha inteira sem separação de tokens e sem cor

        novaLinha.add(numeroLinha); // Adiciona o número ao painel da linha
        novaLinha.add(texto); // Adiciona o texto bruto ao painel da linha

        novaLinha.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinha o painel à esquerda

        // Calcula a altura ideal da linha e trava a altura máxima para evitar distorções no BoxLayout vertical
        Dimension tamanho = novaLinha.getPreferredSize();
        novaLinha.setMaximumSize(new Dimension(Integer.MAX_VALUE, tamanho.height));
        
        
        return novaLinha; // Retorna o painel simples para ser exibido na pré-visualização

    }
}