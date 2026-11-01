import java.awt.Color; // Importa a classe Color para manipular as cores do texto na interface gráfica
import javax.swing.JLabel; // Importa o componente JLabel para criar os rótulos de texto visuais

// Classe que representa a menor unidade com significado no código-fonte lido (Token)
public class Palavra {
    String lexema; // Armazena a cadeia de caracteres exata lida do arquivo (ex: "123", "PROGRAM", ">=")
    String token; // Armazena a classificação gramatical associada ao lexema (ex: "numero inteiro", "palavra reservada")
    
    // Construtor principal para inicializar uma nova palavra quando o analisador a reconhece
    public Palavra(String lexema, String token) {
        this.lexema = lexema;
        this.token = token;
    }

    // Construtor sem parâmetros usado especificamente para representar quebras de linha/espaços vazios
    public Palavra(){
        lexema=null; // Define como nulo pois não há texto a ser exibido na tela
        token="ESPACO"; // Atribui o token especial "ESPACO" usado para quebrar linhas na interface
    }

    // Método responsável por gerar a representação visual deste token na interface gráfica
    public JLabel criaLabel(){

        if(lexema == null) {
            return null; 
        } else if(lexema.equals("\t")) {
            // Desenha 4 espaços invisíveis largos se o arquivo tiver Tabs reais
            return new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;</html>"); 
        } else if(lexema.equals(" ")) {
            // Desenha 1 espaço invisível largo se o arquivo usar espaços na indentação
            return new JLabel("<html>&nbsp;</html>"); 
        }else { // Verifica se a palavra tem um conteúdo real para ser exibido
            JLabel novo = new JLabel(lexema); // Instancia o componente visual do texto
            novo.setForeground(Color.decode(corToken())); // Altera a cor da fonte chamando o método corToken()
            novo.setToolTipText(token); // Configura o balão de ajuda para exibir a categoria gramatical ao passar o mouse

            // Adiciona um ouvinte de eventos de mouse para criar interatividade
            novo.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    // Quando o ponteiro do mouse entra na área da palavra, muda o ícone para uma mão (link)
                    novo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    // Quando o ponteiro do mouse sai, devolve o cursor ao formato normal de seta
                    novo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                }
            });

            return novo; // Devolve o componente visual já pronto, com cor, tooltip e eventos configurados
        }
    }

    // Método para obter a classificação do token, que funciona como gatilho de validação de erro
    public String getTolken() throws Exception{
        if(token.equals("identificador invalido")) 
            // Se o motor de análise carimbou erro, lança uma exceção para o método geradorLinhas capturar
            throw new Exception(token+": "+lexema);
        return token; // Se o token for válido, simplesmente devolve a classificação em String
    } 

    // Método auxiliar (dicionário de cores) que converte o tipo do token num código Hexadecimal
    public String corToken(){
        if(token.equals("identificador")) return "#13a2cd"; // Azul para variáveis e nomes
        if(token.equals("palavra reservada")) return "#e3c502"; // Amarelo para chaves da linguagem
        if(token.equals("comentario")) return "#0e8b19"; // Verde para blocos ignorados
        if(token.equals("string")) return "#fc8211"; // Laranja escuro para texto
        if(token.equals("char")) return "#f69b47"; // Laranja claro para caracteres únicos
        // Agrupamento de todas as notações numéricas e caracteres especiais para a cor Roxa
        if(token.equals("numero inteiro") || 
        token.equals("numero real") ||
        token.equals("numero em notacao cientifica") ||
        token.equals("caractere especial")) return "#8400a8";
        // Cor de base escura (quase preta) para o que não foi coberto acima, como operadores e pontuações
        return "#1d0505";
    }
}
