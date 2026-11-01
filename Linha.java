import java.awt.Color; // Importado para definir o alinhamento dos componentes na tela
import java.awt.Component; // Importado para controlar as dimensões (largura e altura) do painel
import java.awt.Dimension; // Importado para o gerenciador de layout que organiza os elementos lado a lado
import java.awt.FlowLayout; // Importado para utilizar a estrutura de lista dinâmica
import java.util.ArrayList; // Importado para criar margens e bordas nos componentes visuais
import javax.swing.BorderFactory; // Importado para criar rótulos de texto (como o número da linha)
import javax.swing.JLabel; // Importado para criar o painel (container) que vai agrupar as palavras
import javax.swing.JPanel; // Importado para manipular as cores de fundo (usado para destacar os erros)

// Classe responsável por agrupar os tokens (Palavra) gerados e criar a representação visual de uma linha de código
public class Linha{

    private int num; // Armazena o número da linha no código-fonte original
    private ArrayList<Palavra> palavras; // Lista contendo todos os tokens/palavras extraídos nesta linha
    private boolean certo; // Sinalizador (flag) que indica se a linha passou pela análise sem erros léxicos
    private JPanel refLinha; // Guarda uma referência na memória para o painel visual criado, permitindo alterar sua cor depois

    private String erro; // Armazena a mensagem descritiva caso algum erro léxico seja encontrado nesta linha

    // Construtor: Inicializa uma nova linha quando o analisador encontra uma quebra de linha ('\n')
    public Linha (int num, ArrayList<Palavra> palavras){
        this.num=num;
        this.palavras = palavras;
        certo=true; // Toda linha nasce assumindo que está correta (sem erros)
        erro=""; // Inicializa a mensagem de erro vazia
    }
    
    // Método chamado pelo geradorLinhas (no analisador) quando uma palavra lança uma exceção de token inválido
    public void setErro(String erro){
        certo=false; // Marca a flag indicando que a linha agora contém um erro
        this.erro += erro; // Concatena a mensagem do erro (pode acumular mais de um erro na mesma linha)
    }

    // Método principal da interface gráfica: transforma os dados da classe em um container visual para a tela
    public JPanel gerarLinha(){
        // Cria um novo container com layout em fluxo, alinhado à esquerda, com 3px de espaço horizontal e 0px vertical
        JPanel novaLinha = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0)); 
        // Garante que o container inteiro ficará encostado no lado esquerdo do painel principal da tela
        novaLinha.setAlignmentX(Component.LEFT_ALIGNMENT); 
        // Cria o texto contendo o número da linha (ex: "1- ")
        JLabel numeroLinha = new JLabel(num+"- ");
        novaLinha.add(numeroLinha); // Adiciona o número da linha como o primeiro elemento do painel
        // Adiciona um padding (margem interna) de 10 pixels em todos os lados para o texto não colar nas bordas
        novaLinha.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Laço que percorre cada token capturado nesta linha
        for(Palavra p: palavras){
            JLabel labelGerado = p.criaLabel();
            
            if (labelGerado != null) {
                novaLinha.add(labelGerado);
            }
        }
        
        // Se o método setErro foi chamado anteriormente, esta condição será verdadeira
        if(!certo){
            // Muda a cor de fundo do painel inteiro para um tom de vermelho suave, sinalizando o erro visualmente
            novaLinha.setBackground(Color.decode("#eb7c7c"));
        }

        // Obtém o tamanho ideal calculado pelo Java para acomodar todos os itens adicionados
        Dimension tamanho = novaLinha.getPreferredSize();
        // Trava a altura máxima do painel para o seu tamanho ideal, evitando que a linha estique verticalmente e quebre o layout
        novaLinha.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, tamanho.height)
        );

        refLinha = novaLinha; // Salva o painel visual pronto na variável global para poder manipulá-lo nos botões de navegação
        return novaLinha; // Devolve o painel para a classe Main adicioná-lo à tela
    }

    // Método de acesso (Getter) que retorna se a linha está livre de erros
    public boolean taCerto() {
        return certo;
    }

    // Método de acesso (Getter) que devolve a mensagem de erro acumulada
    public String getErro() {
        return erro;
    }

    // Método de acesso (Getter) que devolve o número da linha
    public int getNum(){
        return num;
    }

    // Acionado pelos botões "Próximo Erro" ou "Erro Anterior" para focar a visão do usuário nesta linha
    public void destacarLinha(){
        // Muda o fundo para um vermelho mais forte e chamativo, indicando que é o erro selecionado no momento
        refLinha.setBackground(Color.decode("#ee4141"));
        // Força a barra de rolagem (scroll) do programa a descer/subir automaticamente até que este painel fique visível
        refLinha.scrollRectToVisible(new java.awt.Rectangle(0, 0, 10, refLinha.getHeight()));
    }

    // Acionado quando o usuário clica para navegar para OUTRO erro, tirando o foco atual desta linha
    public void normalizaLinha(){
        // Devolve a cor de fundo da linha para o vermelho suave, indicando que ela tem erro, mas não é o foco atual
        refLinha.setBackground(Color.decode("#eb7c7c"));
    }

}