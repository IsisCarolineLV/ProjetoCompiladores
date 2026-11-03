// ============================================================
// CODIGOS NUMERICOS DA TABELA DE ANALISE
// Terminais: 0 .. 32
// Nao-terminais: 33 em diante
// ============================================================

// ----------------------------
// TERMINAIS / LOOKAHEAD
// ----------------------------
// 0  = procedure
// 1  = var
// 2  = program
// 3  = begin
// 4  = end
// 5  = if
// 6  = else
// 7  = then
// 8  = while
// 9  = do
// 10 = ;
// 11 = .
// 12 = ,
// 13 = :
// 14 = :=
// 15 = (
// 16 = )
// 17 = +
// 18 = *
// 19 = div
// 20 = mod
// 21 = =
// 22 = <
// 23 = >
// 24 = <>
// 25 = <=
// 26 = >=
// 27 = ident
// 28 = tipo
// 29 = proc
// 30 = numero
// 31 = variavel
// 32 = $   (fim da entrada)

// ----------------------------
// NAO-TERMINAIS / COLUNAS DE GOTO
// ----------------------------
// 33 = S
// 34 = B
// 35 = C
// 36 = C'
// 37 = I
// 38 = D
// 39 = E
// 40 = E'
// 41 = P
// 42 = P'
// 43 = P''
// 44 = R
// 45 = R'
// 46 = M
// 47 = A
// 48 = A'
// 49 = EL
// 50 = F
// 51 = EX
// 52 = EX2
// 53 = OP
// 54 = SI
// 55 = SI'
// 56 = Q
// 57 = Q'
// 58 = T
// 59 = T'
// 60 = FAT

import java.util.HashMap;
import java.util.Map;

public class Tabela {
    // estados 0..116, colunas 0..60
    public static String[][] tabela = new String[117][61];
    public static Map<String, Integer> simbolo = new HashMap<>();

    private static void setCols(int estado, int[] cols, String valor) {
        for (int c : cols) {
            tabela[estado][c] = valor;
        }
    }

    public static void preencher() {
        for (int i = 0; i < 117; i++) {
            for (int j = 0; j < 61; j++) {
                tabela[i][j] = null;
            }
        }

        // conjuntos auxiliares
        int[] F_EX  = {10, 6, 7, 9, 12, 16};
        int[] F_SI  = {21, 22, 23, 24, 25, 26, 10, 6, 7, 9, 12, 16};
        int[] F_T   = {17, 30, 31, 15, 21, 22, 23, 24, 25, 26, 10, 6, 7, 9, 12, 16};
        int[] F_FAT = {18, 19, 20, 17, 30, 31, 15, 21, 22, 23, 24, 25, 26, 10, 6, 7, 9, 12, 16};
        int[] F_OP  = {30, 31, 15, 17};
        // F_T sem os FIRST(FAT): usado nos estados 75,76,77
        int[] F_T_SEM_FAT = {17, 21, 22, 23, 24, 25, 26, 10, 6, 7, 9, 12, 16};

        // 0
        tabela[0][2]  = "E 2";
        tabela[0][33] = "1";

        // 1
        tabela[1][32] = "OK";

        // 2
        tabela[2][27] = "E 3";

        // 3
        tabela[3][10] = "E 4";

        // 4
        tabela[4][1]  = "E 7";
        tabela[4][0]  = "R C -> ";
        tabela[4][3]  = "R C -> ";
        tabela[4][34] = "5";
        tabela[4][35] = "6";

        // 5
        tabela[5][11] = "E 8";

        // 6
        tabela[6][0]  = "E 10";
        tabela[6][3]  = "R D -> ";
        tabela[6][38] = "9";

        // 7
        tabela[7][27] = "E 11";
        tabela[7][36] = "12";

        // 8
        tabela[8][32] = "R S -> program ident ; B .";

        // 9
        tabela[9][3]  = "E 14";
        tabela[9][39] = "13";

        // 10
        tabela[10][27] = "E 15";

        // 11
        tabela[11][12] = "E 17";
        tabela[11][13] = "R I -> ";
        tabela[11][37] = "16";

        // 12
        tabela[12][1]  = "E 7";
        tabela[12][0]  = "R C -> ";
        tabela[12][3]  = "R C -> ";
        tabela[12][35] = "18";

        // 13
        tabela[13][11] = "R B -> C D E";
        tabela[13][10] = "R B -> C D E";

        // 14
        tabela[14][3]  = "E 19";
        tabela[14][31] = "E 22";
        tabela[14][29] = "E 23";
        tabela[14][5]  = "E 24";
        tabela[14][8]  = "E 25";
        tabela[14][40] = "20";
        tabela[14][46] = "21";

        // 15
        tabela[15][15] = "E 27";
        tabela[15][10] = "R P -> ";
        tabela[15][41] = "26";

        // 16
        tabela[16][13] = "E 28";

        // 17
        tabela[17][27] = "E 29";

        // 18
        tabela[18][0] = "R C -> var C' C";
        tabela[18][3] = "R C -> var C' C";

        // 19
        tabela[19][3]  = "E 19";
        tabela[19][31] = "E 22";
        tabela[19][29] = "E 23";
        tabela[19][5]  = "E 24";
        tabela[19][8]  = "E 25";
        tabela[19][46] = "30";
        tabela[19][50] = "31";

        // 20
        tabela[20][4] = "E 32";

        // 21
        tabela[21][10] = "E 33";

        // 22
        tabela[22][14] = "E 34";

        // 23
        tabela[23][15] = "E 35";
        tabela[23][10] = "R A -> ";
        tabela[23][6]  = "R A -> ";
        tabela[23][47] = "36";

        // 24
        tabela[24][15] = "E 37";
        tabela[24][31] = "E 38";
        tabela[24][17] = "E 42";
        tabela[24][30] = "E 45";
        tabela[24][51] = "39";
        tabela[24][54] = "40";
        tabela[24][56] = "41";
        tabela[24][58] = "43";
        tabela[24][60] = "44";

        // 25
        tabela[25][15] = "E 37";
        tabela[25][31] = "E 38";
        tabela[25][17] = "E 42";
        tabela[25][30] = "E 45";
        tabela[25][51] = "46";
        tabela[25][54] = "40";
        tabela[25][56] = "41";
        tabela[25][58] = "43";
        tabela[25][60] = "44";

        // 26
        tabela[26][10] = "E 47";

        // 27
        tabela[27][27] = "E 48";
        tabela[27][1]  = "E 49";
        tabela[27][42] = "50";
        tabela[27][44] = "51";
        tabela[27][45] = "52";

        // 28
        tabela[28][28] = "E 53";

        // 29
        tabela[29][12] = "E 17";
        tabela[29][13] = "R I -> ";
        tabela[29][37] = "54";

        // 30
        tabela[30][10] = "E 55";

        // 31
        tabela[31][4] = "E 56";

        // 32
        tabela[32][11] = "R E -> begin E' end";
        tabela[32][10] = "R E -> begin E' end";

        // 33
        tabela[33][3]  = "E 19";
        tabela[33][31] = "E 22";
        tabela[33][29] = "E 23";
        tabela[33][5]  = "E 24";
        tabela[33][8]  = "E 25";
        tabela[33][4]  = "R E' -> M ;";
        tabela[33][40] = "57";
        tabela[33][46] = "21";

        // 34
        tabela[34][15] = "E 37";
        tabela[34][31] = "E 38";
        tabela[34][17] = "E 42";
        tabela[34][30] = "E 45";
        tabela[34][51] = "58";
        tabela[34][54] = "40";
        tabela[34][56] = "41";
        tabela[34][58] = "43";
        tabela[34][60] = "44";

        // 35
        tabela[35][15] = "E 37";
        tabela[35][31] = "E 38";
        tabela[35][17] = "E 42";
        tabela[35][30] = "E 45";
        tabela[35][51] = "59";
        tabela[35][54] = "40";
        tabela[35][56] = "41";
        tabela[35][58] = "43";
        tabela[35][60] = "44";

        // 36
        tabela[36][10] = "R M -> proc A";
        tabela[36][6]  = "R M -> proc A";

        // 37
        tabela[37][15] = "E 37";
        tabela[37][31] = "E 38";
        tabela[37][17] = "E 42";
        tabela[37][30] = "E 45";
        tabela[37][51] = "60";
        tabela[37][54] = "40";
        tabela[37][56] = "41";
        tabela[37][58] = "43";
        tabela[37][60] = "44";

        // 38
        setCols(38, F_FAT, "R FAT -> variavel");

        // 39
        tabela[39][7] = "E 61";

        // 40
        tabela[40][21] = "E 64";
        tabela[40][22] = "E 65";
        tabela[40][23] = "E 66";
        tabela[40][24] = "E 67";
        tabela[40][25] = "E 68";
        tabela[40][26] = "E 69";
        setCols(40, F_EX, "R EX2 ->  ");
        tabela[40][52] = "62";
        tabela[40][53] = "63";

        // 41
        setCols(41, F_SI, "R SI -> Q");

        // 42
        tabela[42][15] = "E 37";
        tabela[42][31] = "E 38";
        tabela[42][30] = "E 45";
        tabela[42][56] = "70";
        tabela[42][58] = "43";
        tabela[42][60] = "44";

        // 43
        tabela[43][15] = "E 37";
        tabela[43][31] = "E 38";
        tabela[43][17] = "E 71";
        tabela[43][30] = "E 45";
        setCols(43, F_SI, "R Q' ->  ");
        tabela[43][58] = "72";
        tabela[43][57] = "73";
        tabela[43][60] = "44";

        // 44
        tabela[44][18] = "E 75";
        tabela[44][19] = "E 76";
        tabela[44][20] = "E 77";
        setCols(44, F_T, "R T' ->  ");
        tabela[44][59] = "74";

        // 45
        setCols(45, F_FAT, "R FAT -> numero");

        // 46
        tabela[46][9] = "E 78";

        // 47
        tabela[47][1]  = "E 7";
        tabela[47][0]  = "R C ->  ";
        tabela[47][3]  = "R C ->  ";
        tabela[47][34] = "79";
        tabela[47][35] = "6";

        // 48
        tabela[48][12] = "E 17";
        tabela[48][13] = "R I ->  ";
        tabela[48][37] = "80";

        // 49
        tabela[49][27] = "E 48";
        tabela[49][45] = "81";

        // 50
        tabela[50][16] = "E 82";

        // 51
        tabela[51][13] = "E 83";

        // 52
        tabela[52][13] = "R R -> R'";

        // 53
        tabela[53][10] = "E 84";

        // 54
        tabela[54][13] = "R I -> , ident I";

        // 55
        tabela[55][3]  = "E 19";
        tabela[55][31] = "E 22";
        tabela[55][29] = "E 23";
        tabela[55][5]  = "E 24";
        tabela[55][8]  = "E 25";
        tabela[55][4]  = "R F -> M ;";
        tabela[55][46] = "30";
        tabela[55][50] = "85";

        // 56
        tabela[56][10] = "R M -> begin F end";
        tabela[56][6]  = "R M -> begin F end";

        // 57
        tabela[57][4] = "R E' -> M ; E'";

        // 58
        tabela[58][10] = "R M -> variavel := EX";
        tabela[58][6]  = "R M -> variavel := EX";

        // 59
        tabela[59][12] = "E 86";
        tabela[59][16] = "R A' ->  ";
        tabela[59][48] = "87";

        // 60
        tabela[60][16] = "E 88";

        // 61
        tabela[61][3]  = "E 19";
        tabela[61][31] = "E 22";
        tabela[61][29] = "E 23";
        tabela[61][5]  = "E 24";
        tabela[61][8]  = "E 25";
        tabela[61][46] = "89";

        // 62
        setCols(62, F_EX, "R EX -> SI EX2");

        // 63
        tabela[63][15] = "E 37";
        tabela[63][31] = "E 38";
        tabela[63][17] = "E 42";
        tabela[63][30] = "E 45";
        tabela[63][54] = "90";
        tabela[63][56] = "41";
        tabela[63][58] = "43";
        tabela[63][60] = "44";

        // 64..69
        setCols(64, F_OP, "R OP -> =");
        setCols(65, F_OP, "R OP -> <");
        setCols(66, F_OP, "R OP -> >");
        setCols(67, F_OP, "R OP -> <>");
        setCols(68, F_OP, "R OP -> <=");
        setCols(69, F_OP, "R OP -> >=");

        // 70
        setCols(70, F_SI, "R SI -> + Q");

        // 71
        tabela[71][15] = "E 37";
        tabela[71][31] = "E 38";
        tabela[71][30] = "E 45";
        tabela[71][58] = "91";
        tabela[71][60] = "44";

        // 72
        tabela[72][15] = "E 37";
        tabela[72][31] = "E 38";
        tabela[72][17] = "E 71";
        tabela[72][30] = "E 45";
        setCols(72, F_SI, "R Q' ->  ");
        tabela[72][58] = "72";
        tabela[72][57] = "92";
        tabela[72][60] = "44";

        // 73
        setCols(73, F_SI, "R Q -> T Q'");

        // 74
        setCols(74, F_T, "R T -> FAT T'");

        // 75
        tabela[75][15] = "E 37";
        tabela[75][31] = "E 38";
        tabela[75][30] = "E 45";
        setCols(75, F_T_SEM_FAT, "R T' ->  ");
        tabela[75][60] = "93";

        // 76
        tabela[76][15] = "E 37";
        tabela[76][31] = "E 38";
        tabela[76][30] = "E 45";
        setCols(76, F_T_SEM_FAT, "R T' ->  ");
        tabela[76][60] = "94";

        // 77
        tabela[77][15] = "E 37";
        tabela[77][31] = "E 38";
        tabela[77][30] = "E 45";
        setCols(77, F_T_SEM_FAT, "R T' ->  ");
        tabela[77][60] = "95";

        // 78
        tabela[78][3]  = "E 19";
        tabela[78][31] = "E 22";
        tabela[78][29] = "E 23";
        tabela[78][5]  = "E 24";
        tabela[78][8]  = "E 25";
        tabela[78][46] = "96";

        // 79
        tabela[79][10] = "E 97";

        // 80
        tabela[80][13] = "R R' -> ident I";

        // 81
        tabela[81][13] = "R R -> var R'";

        // 82
        tabela[82][10] = "R P -> ( P' )";

        // 83
        tabela[83][28] = "E 98";

        // 84 (conflito: var => shift; procedure/begin => C ->  )
        tabela[84][1]  = "E 7";
        tabela[84][0]  = "R C ->  ";
        tabela[84][3]  = "R C ->  ";
        tabela[84][35] = "99";

        // 85
        tabela[85][4] = "R F -> M ; F";

        // 86
        tabela[86][15] = "E 37";
        tabela[86][31] = "E 38";
        tabela[86][17] = "E 42";
        tabela[86][30] = "E 45";
        tabela[86][51] = "100";
        tabela[86][54] = "40";
        tabela[86][56] = "41";
        tabela[86][58] = "43";
        tabela[86][60] = "44";

        // 87
        tabela[87][16] = "E 101";

        // 88
        setCols(88, F_FAT, "R FAT -> ( EX )");

        // 89 (dangling-else: else => shift)
        tabela[89][6]  = "E 103";
        tabela[89][10] = "R EL ->  ";
        tabela[89][49] = "102";

        // 90
        setCols(90, F_EX, "R EX2 -> OP SI");

        // 91
        tabela[91][15] = "E 37";
        tabela[91][31] = "E 38";
        tabela[91][17] = "E 71";
        tabela[91][30] = "E 45";
        setCols(91, F_SI, "R Q' ->  ");
        tabela[91][58] = "72";
        tabela[91][57] = "104";
        tabela[91][60] = "44";

        // 92
        setCols(92, F_SI, "R Q' -> T Q'");

        // 93
        tabela[93][18] = "E 75";
        tabela[93][19] = "E 76";
        tabela[93][20] = "E 77";
        setCols(93, F_T, "R T' ->  ");
        tabela[93][59] = "105";

        // 94
        tabela[94][18] = "E 75";
        tabela[94][19] = "E 76";
        tabela[94][20] = "E 77";
        setCols(94, F_T, "R T' ->  ");
        tabela[94][59] = "106";

        // 95
        tabela[95][18] = "E 75";
        tabela[95][19] = "E 76";
        tabela[95][20] = "E 77";
        setCols(95, F_T, "R T' ->  ");
        tabela[95][59] = "107";

        // 96
        tabela[96][10] = "R M -> while EX do M";
        tabela[96][6]  = "R M -> while EX do M";

        // 97
        tabela[97][0]  = "E 10";
        tabela[97][3]  = "R D ->  ";
        tabela[97][38] = "108";

        // 98
        tabela[98][10] = "E 109";
        tabela[98][16] = "R P' -> R : tipo";
        tabela[98][43] = "110";

        // 99
        tabela[99][1] = "R C' -> ident I : tipo ; C";
        tabela[99][0] = "R C' -> ident I : tipo ; C";
        tabela[99][3] = "R C' -> ident I : tipo ; C";

        // 100
        tabela[100][12] = "E 86";
        tabela[100][16] = "R A' ->  ";
        tabela[100][48] = "111";

        // 101
        tabela[101][10] = "R A -> ( EX A' )";
        tabela[101][6]  = "R A -> ( EX A' )";

        // 102
        tabela[102][10] = "R M -> if EX then M EL";
        tabela[102][6]  = "R M -> if EX then M EL";

        // 103
        tabela[103][3]  = "E 19";
        tabela[103][31] = "E 22";
        tabela[103][29] = "E 23";
        tabela[103][5]  = "E 24";
        tabela[103][8]  = "E 25";
        tabela[103][46] = "112";

        // 104
        setCols(104, F_SI, "R Q' -> + T Q'");

        // 105
        setCols(105, F_T, "R T' -> * FAT T'");

        // 106
        setCols(106, F_T, "R T' -> div FAT T'");

        // 107
        setCols(107, F_T, "R T' -> mod FAT T'");

        // 108
        tabela[108][3] = "R D -> procedure ident P ; B ; D";

        // 109
        tabela[109][27] = "E 48";
        tabela[109][1]  = "E 49";
        tabela[109][44] = "113";
        tabela[109][45] = "52";

        // 110
        tabela[110][16] = "R P' -> R : tipo P''";

        // 111
        tabela[111][16] = "R A' -> , EX A'";

        // 112
        tabela[112][10] = "R EL -> else M";
        tabela[112][6]  = "R EL -> else M";

        // 113
        tabela[113][13] = "E 114";

        // 114
        tabela[114][28] = "E 115";

        // 115
        tabela[115][10] = "E 109";
        tabela[115][16] = "R P'' -> ; R : tipo";
        tabela[115][43] = "116";

        // 116
        tabela[116][16] = "R P'' -> ; R : tipo P''";

        preencherSimbolos();
    }

    private static void preencherSimbolos() {
        simbolo.put("procedure", 0);
        simbolo.put("var", 1);
        simbolo.put("program", 2);
        simbolo.put("begin", 3);
        simbolo.put("end", 4);
        simbolo.put("if", 5);
        simbolo.put("else", 6);
        simbolo.put("then", 7);
        simbolo.put("while", 8);
        simbolo.put("do", 9);
        simbolo.put(";", 10);
        simbolo.put(".", 11);
        simbolo.put(",", 12);
        simbolo.put(":", 13);
        simbolo.put(":=", 14);
        simbolo.put("(", 15);
        simbolo.put(")", 16);
        simbolo.put("+", 17);
        simbolo.put("*", 18);
        simbolo.put("div", 19);
        simbolo.put("mod", 20);
        simbolo.put("=", 21);
        simbolo.put("<", 22);
        simbolo.put(">", 23);
        simbolo.put("<>", 24);
        simbolo.put("<=", 25);
        simbolo.put(">=", 26);
        simbolo.put("ident", 27);
        simbolo.put("tipo", 28);
        simbolo.put("proc", 29);
        simbolo.put("numero", 30);
        simbolo.put("variavel", 31);
        simbolo.put("$", 32);

        simbolo.put("S", 33);
        simbolo.put("B", 34);
        simbolo.put("C", 35);
        simbolo.put("C'", 36);
        simbolo.put("I", 37);
        simbolo.put("D", 38);
        simbolo.put("E", 39);
        simbolo.put("E'", 40);
        simbolo.put("P", 41);
        simbolo.put("P'", 42);
        simbolo.put("P''", 43);
        simbolo.put("R", 44);
        simbolo.put("R'", 45);
        simbolo.put("M", 46);
        simbolo.put("A", 47);
        simbolo.put("A'", 48);
        simbolo.put("EL", 49);
        simbolo.put("F", 50);
        simbolo.put("EX", 51);
        simbolo.put("EX2", 52);
        simbolo.put("OP", 53);
        simbolo.put("SI", 54);
        simbolo.put("SI'", 55);
        simbolo.put("Q", 56);
        simbolo.put("Q'", 57);
        simbolo.put("T", 58);
        simbolo.put("T'", 59);
        simbolo.put("FAT", 60);
    }
}