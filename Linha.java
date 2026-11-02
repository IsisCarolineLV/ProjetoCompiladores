import java.awt.Color; // Importado para definir o alinhamento dos componentes na tela
import java.awt.Component; // Importado para controlar as dimensoes (largura e altura) do painel
import java.awt.Dimension; // Importado para o gerenciador de layout que organiza os elementos lado a lado
import java.awt.FlowLayout; // Importado para utilizar a estrutura de lista dinamica
import java.util.ArrayList; // Importado para criar margens e bordas nos componentes visuais
import javax.swing.BorderFactory; // Importado para criar rotulos de texto (como o numero da linha)
import javax.swing.JLabel; // Importado para criar o painel (container) que vai agrupar as palavras
import javax.swing.JPanel; // Importado para manipular as cores de fundo (usado para destacar os erros)

// Classe responsavel por agrupar os tokens (Palavra) gerados e criar a representacao visual de uma linha de codigo
public class Linha{

    private int num; // Armazena o numero da linha no codigo-fonte original
    private ArrayList<Palavra> palavras; // Lista contendo todos os tokens/palavras extraidos nesta linha
    private boolean certo; // Sinalizador (flag) que indica se a linha passou pela analise sem erros lexicos
    private JPanel refLinha; // Guarda uma referencia na memoria para o painel visual criado, permitindo alterar sua cor depois

    private String erro; // Armazena a mensagem descritiva caso algum erro lexico seja encontrado nesta linha

    // Construtor: Inicializa uma nova linha quando o analisador encontra uma quebra de linha ('\n')
    public Linha (int num, ArrayList<Palavra> palavras){
        this.num=num;
        this.palavras = palavras;
        certo=true; // Toda linha nasce assumindo que esta correta (sem erros)
        erro=""; // Inicializa a mensagem de erro vazia
    }
    
    // Metodo chamado pelo geradorLinhas (no analisador) quando uma palavra lanca uma excecao de token invalido
    public void setErro(String erro){
        certo=false; // Marca a flag indicando que a linha agora contem um erro
        this.erro += erro; // Concatena a mensagem do erro (pode acumular mais de um erro na mesma linha)
    }

    // Metodo principal da interface grafica: transforma os dados da classe em um container visual para a tela
    public JPanel gerarLinha(){
        // Cria um novo container com layout em fluxo, alinhado a esquerda, com 3px de espaco horizontal e 0px vertical
        JPanel novaLinha = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0)); 
        // Garante que o container inteiro ficara encostado no lado esquerdo do painel principal da tela
        novaLinha.setAlignmentX(Component.LEFT_ALIGNMENT); 
        // Cria o texto contendo o numero da linha (ex: "1- ")
        JLabel numeroLinha = new JLabel(num+"- ");
        novaLinha.add(numeroLinha); // Adiciona o numero da linha como o primeiro elemento do painel
        // Adiciona um padding (margem interna) de 10 pixels em todos os lados para o texto nao colar nas bordas
        novaLinha.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Laco que percorre cada token capturado nesta linha
        for(Palavra p: palavras){
            JLabel labelGerado = p.criaLabel();
            
            if (labelGerado != null) {
                novaLinha.add(labelGerado);
            }
        }
        
        // Se o metodo setErro foi chamado anteriormente, esta condicao sera verdadeira
        if(!certo){
            // Muda a cor de fundo do painel inteiro para um tom de vermelho suave, sinalizando o erro visualmente
            novaLinha.setBackground(Color.decode("#eb7c7c"));
        }

        // Obtem o tamanho ideal calculado pelo Java para acomodar todos os itens adicionados
        Dimension tamanho = novaLinha.getPreferredSize();
        // Trava a altura maxima do painel para o seu tamanho ideal, evitando que a linha estique verticalmente e quebre o layout
        novaLinha.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, tamanho.height)
        );

        refLinha = novaLinha; // Salva o painel visual pronto na variavel global para poder manipula-lo nos botoes de navegacao
        return novaLinha; // Devolve o painel para a classe Main adiciona-lo a tela
    }

    // Metodo de acesso (Getter) que retorna se a linha esta livre de erros
    public boolean taCerto() {
        return certo;
    }

    // Metodo de acesso (Getter) que devolve a mensagem de erro acumulada
    public String getErro() {
        return erro;
    }

    // Metodo de acesso (Getter) que devolve o numero da linha
    public int getNum(){
        return num;
    }

    // Acionado pelos botoes "Proximo Erro" ou "Erro Anterior" para focar a visao do usuario nesta linha
    public void destacarLinha(){
        // Muda o fundo para um vermelho mais forte e chamativo, indicando que e o erro selecionado no momento
        refLinha.setBackground(Color.decode("#ee4141"));
        // Forca a barra de rolagem (scroll) do programa a descer/subir automaticamente ate que este painel fique visivel
        refLinha.scrollRectToVisible(new java.awt.Rectangle(0, 0, 10, refLinha.getHeight()));
    }

    // Acionado quando o usuario clica para navegar para OUTRO erro, tirando o foco atual desta linha
    public void normalizaLinha(){
        // Devolve a cor de fundo da linha para o vermelho suave, indicando que ela tem erro, mas nao e o foco atual
        refLinha.setBackground(Color.decode("#eb7c7c"));
    }

}