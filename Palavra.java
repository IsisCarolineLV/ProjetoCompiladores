import java.awt.Color; // Importa a classe Color para manipular as cores do texto na interface grafica
import javax.swing.JLabel; // Importa o componente JLabel para criar os rotulos de texto visuais

// Classe que representa a menor unidade com significado no codigo-fonte lido (Token)
public class Palavra {
    String lexema; // Armazena a cadeia de caracteres exata lida do arquivo (ex: "123", "PROGRAM", ">=")
    String token; // Armazena a classificacao gramatical associada ao lexema (ex: "numero inteiro", "palavra reservada")
    
    // Construtor principal para inicializar uma nova palavra quando o analisador a reconhece
    public Palavra(String lexema, String token) {
        this.lexema = lexema;
        this.token = token;
    }

    // Construtor sem parametros usado especificamente para representar quebras de linha/espacos vazios
    public Palavra(){
        lexema=null; // Define como nulo pois nao ha texto a ser exibido na tela
        token="ESPACO"; // Atribui o token especial "ESPACO" usado para quebrar linhas na interface
    }

    // Metodo responsavel por gerar a representacao visual deste token na interface grafica
    public JLabel criaLabel(){

        if(lexema == null) {
            return null; 
        } else if(lexema.equals("\t")) {
            // Desenha 4 espacos invisiveis largos se o arquivo tiver Tabs reais
            return new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;</html>"); 
        } else if(lexema.equals(" ")) {
            // Desenha 1 espaco invisivel largo se o arquivo usar espacos na indentacao
            return new JLabel("<html>&nbsp;</html>"); 
        }else { // Verifica se a palavra tem um conteudo real para ser exibido
            JLabel novo = new JLabel(lexema); // Instancia o componente visual do texto
            novo.setForeground(Color.decode(corToken())); // Altera a cor da fonte chamando o metodo corToken()
            novo.setToolTipText(token); // Configura o balao de ajuda para exibir a categoria gramatical ao passar o mouse

            // Adiciona um ouvinte de eventos de mouse para criar interatividade
            novo.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    // Quando o ponteiro do mouse entra na area da palavra, muda o icone para uma mao (link)
                    novo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    // Quando o ponteiro do mouse sai, devolve o cursor ao formato normal de seta
                    novo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                }
            });

            return novo; // Devolve o componente visual ja pronto, com cor, tooltip e eventos configurados
        }
    }

    // Metodo para obter a classificacao do token, que funciona como gatilho de validacao de erro
    public String getTolken() throws Exception{
        if(token.equals("identificador invalido")) 
            // Se o motor de analise carimbou erro, lanca uma excecao para o metodo geradorLinhas capturar
            throw new Exception(token+": "+lexema);
        return token; // Se o token for valido, simplesmente devolve a classificacao em String
    } 

    // Metodo auxiliar (dicionario de cores) que converte o tipo do token num codigo Hexadecimal
    public String corToken(){
        if(token.equals("identificador")) return "#13a2cd"; // Azul para variaveis e nomes
        if(token.equals("palavra reservada")) return "#e3c502"; // Amarelo para chaves da linguagem
        if(token.equals("comentario")) return "#0e8b19"; // Verde para blocos ignorados
        if(token.equals("string")) return "#fc8211"; // Laranja escuro para texto
        if(token.equals("char")) return "#f69b47"; // Laranja claro para caracteres unicos
        // Agrupamento de todas as notacoes numericas e caracteres especiais para a cor Roxa
        if(token.equals("numero inteiro") || 
        token.equals("numero real") ||
        token.equals("numero em notacao cientifica") ||
        token.equals("caractere especial")) return "#8400a8";
        // Cor de base escura (quase preta) para o que nao foi coberto acima, como operadores e pontuacoes
        return "#1d0505";
    }
}
