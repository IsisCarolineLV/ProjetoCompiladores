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
import java.util.ArrayList;

public class Tabela {
    // estados 0..116, colunas 0..60
    public static String[][] tabela = new String[117][61];

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
        tabela[0][2]  = "E2";
        tabela[0][33] = "1";

        // 1
        tabela[1][32] = "OK";

        // 2
        tabela[2][27] = "E3";

        // 3
        tabela[3][10] = "E4";

        // 4
        tabela[4][1]  = "E7";
        tabela[4][0]  = "R2";
        tabela[4][3]  = "R2";
        tabela[4][34] = "5";
        tabela[4][35] = "6";

        // 5
        tabela[5][11] = "E8";

        // 6
        tabela[6][0]  = "E10";
        tabela[6][3]  = "R8";
        tabela[6][38] = "9";

        // 7
        tabela[7][27] = "E11";
        tabela[7][36] = "12";

        // 8
        tabela[8][32] = "R0";

        // 9
        tabela[9][3]  = "E14";
        tabela[9][39] = "13";

        // 10
        tabela[10][27] = "E15";

        // 11
        tabela[11][12] = "E17";
        tabela[11][13] = "R6";
        tabela[11][37] = "16";

        // 12
        tabela[12][1]  = "E7";
        tabela[12][0]  = "R2";
        tabela[12][3]  = "R2";
        tabela[12][35] = "18";

        // 13
        tabela[13][11] = "R1";
        tabela[13][10] = "R1";

        // 14
        tabela[14][3]  = "E19";
        tabela[14][31] = "E22";
        tabela[14][29] = "E23";
        tabela[14][5]  = "E24";
        tabela[14][8]  = "E25";
        tabela[14][40] = "20";
        tabela[14][46] = "21";

        // 15
        tabela[15][15] = "E27";
        tabela[15][10] = "R13";
        tabela[15][41] = "26";

        // 16
        tabela[16][13] = "E28";

        // 17
        tabela[17][27] = "E29";

        // 18
        tabela[18][0] = "R3";
        tabela[18][3] = "R3";

        // 19
        tabela[19][3]  = "E19";
        tabela[19][31] = "E22";
        tabela[19][29] = "E23";
        tabela[19][5]  = "E24";
        tabela[19][8]  = "E25";
        tabela[19][46] = "30";
        tabela[19][50] = "31";

        // 20
        tabela[20][4] = "E32";

        // 21
        tabela[21][10] = "E33";

        // 22
        tabela[22][14] = "E34";

        // 23
        tabela[23][15] = "E35";
        tabela[23][10] = "R27";
        tabela[23][6]  = "R27";
        tabela[23][47] = "36";

        // 24
        tabela[24][15] = "E37";
        tabela[24][31] = "E38";
        tabela[24][17] = "E42";
        tabela[24][30] = "E45";
        tabela[24][51] = "39";
        tabela[24][54] = "40";
        tabela[24][56] = "41";
        tabela[24][58] = "43";
        tabela[24][60] = "44";

        // 25
        tabela[25][15] = "E37";
        tabela[25][31] = "E38";
        tabela[25][17] = "E42";
        tabela[25][30] = "E45";
        tabela[25][51] = "46";
        tabela[25][54] = "40";
        tabela[25][56] = "41";
        tabela[25][58] = "43";
        tabela[25][60] = "44";

        // 26
        tabela[26][10] = "E47";

        // 27
        tabela[27][27] = "E48";
        tabela[27][1]  = "E49";
        tabela[27][42] = "50";
        tabela[27][44] = "51";
        tabela[27][45] = "52";

        // 28
        tabela[28][28] = "E53";

        // 29
        tabela[29][12] = "E17";
        tabela[29][13] = "R6";
        tabela[29][37] = "54";

        // 30
        tabela[30][10] = "E55";

        // 31
        tabela[31][4] = "E56";

        // 32
        tabela[32][11] = "R10";
        tabela[32][10] = "R10";

        // 33
        tabela[33][3]  = "E19";
        tabela[33][31] = "E22";
        tabela[33][29] = "E23";
        tabela[33][5]  = "E24";
        tabela[33][8]  = "E25";
        tabela[33][4]  = "R12";
        tabela[33][40] = "57";
        tabela[33][46] = "21";

        // 34
        tabela[34][15] = "E37";
        tabela[34][31] = "E38";
        tabela[34][17] = "E42";
        tabela[34][30] = "E45";
        tabela[34][51] = "58";
        tabela[34][54] = "40";
        tabela[34][56] = "41";
        tabela[34][58] = "43";
        tabela[34][60] = "44";

        // 35
        tabela[35][15] = "E37";
        tabela[35][31] = "E38";
        tabela[35][17] = "E42";
        tabela[35][30] = "E45";
        tabela[35][51] = "59";
        tabela[35][54] = "40";
        tabela[35][56] = "41";
        tabela[35][58] = "43";
        tabela[35][60] = "44";

        // 36
        tabela[36][10] = "R23";
        tabela[36][6]  = "R23";

        // 37
        tabela[37][15] = "E37";
        tabela[37][31] = "E38";
        tabela[37][17] = "E42";
        tabela[37][30] = "E45";
        tabela[37][51] = "60";
        tabela[37][54] = "40";
        tabela[37][56] = "41";
        tabela[37][58] = "43";
        tabela[37][60] = "44";

        // 38
        setCols(38, F_FAT, "R56");

        // 39
        tabela[39][7] = "E61";

        // 40
        tabela[40][21] = "E64";
        tabela[40][22] = "E65";
        tabela[40][23] = "E66";
        tabela[40][24] = "E67";
        tabela[40][25] = "E68";
        tabela[40][26] = "E69";
        setCols(40, F_EX, "R36");
        tabela[40][52] = "62";
        tabela[40][53] = "63";

        // 41
        setCols(41, F_SI, "R44");

        // 42
        tabela[42][15] = "E37";
        tabela[42][31] = "E38";
        tabela[42][30] = "E45";
        tabela[42][56] = "70";
        tabela[42][58] = "43";
        tabela[42][60] = "44";

        // 43
        tabela[43][15] = "E37";
        tabela[43][31] = "E38";
        tabela[43][17] = "E71";
        tabela[43][30] = "E45";
        setCols(43, F_SI, "R49");
        tabela[43][58] = "72";
        tabela[43][57] = "73";
        tabela[43][60] = "44";

        // 44
        tabela[44][18] = "E75";
        tabela[44][19] = "E76";
        tabela[44][20] = "E77";
        setCols(44, F_T, "R54");
        tabela[44][59] = "74";

        // 45
        setCols(45, F_FAT, "R55");

        // 46
        tabela[46][9] = "E78";

        // 47
        tabela[47][1]  = "E7";
        tabela[47][0]  = "R2";
        tabela[47][3]  = "R2";
        tabela[47][34] = "79";
        tabela[47][35] = "6";

        // 48
        tabela[48][12] = "E17";
        tabela[48][13] = "R6";
        tabela[48][37] = "80";

        // 49
        tabela[49][27] = "E48";
        tabela[49][45] = "81";

        // 50
        tabela[50][16] = "E82";

        // 51
        tabela[51][13] = "E83";

        // 52
        tabela[52][13] = "R19";

        // 53
        tabela[53][10] = "E84";

        // 54
        tabela[54][13] = "R7";

        // 55
        tabela[55][3]  = "E19";
        tabela[55][31] = "E22";
        tabela[55][29] = "E23";
        tabela[55][5]  = "E24";
        tabela[55][8]  = "E25";
        tabela[55][4]  = "R34";
        tabela[55][46] = "30";
        tabela[55][50] = "85";

        // 56
        tabela[56][10] = "R26";
        tabela[56][6]  = "R26";

        // 57
        tabela[57][4] = "R11";

        // 58
        tabela[58][10] = "R22";
        tabela[58][6]  = "R22";

        // 59
        tabela[59][12] = "E86";
        tabela[59][16] = "R30";
        tabela[59][48] = "87";

        // 60
        tabela[60][16] = "E88";

        // 61
        tabela[61][3]  = "E19";
        tabela[61][31] = "E22";
        tabela[61][29] = "E23";
        tabela[61][5]  = "E24";
        tabela[61][8]  = "E25";
        tabela[61][46] = "89";

        // 62
        setCols(62, F_EX, "R35");

        // 63
        tabela[63][15] = "E37";
        tabela[63][31] = "E38";
        tabela[63][17] = "E42";
        tabela[63][30] = "E45";
        tabela[63][54] = "90";
        tabela[63][56] = "41";
        tabela[63][58] = "43";
        tabela[63][60] = "44";

        // 64..69
        setCols(64, F_OP, "R38");
        setCols(65, F_OP, "R39");
        setCols(66, F_OP, "R40");
        setCols(67, F_OP, "R41");
        setCols(68, F_OP, "R42");
        setCols(69, F_OP, "R43");

        // 70
        setCols(70, F_SI, "R45");

        // 71
        tabela[71][15] = "E37";
        tabela[71][31] = "E38";
        tabela[71][30] = "E45";
        tabela[71][58] = "91";
        tabela[71][60] = "44";

        // 72
        tabela[72][15] = "E37";
        tabela[72][31] = "E38";
        tabela[72][17] = "E71";
        tabela[72][30] = "E45";
        setCols(72, F_SI, "R49");
        tabela[72][58] = "72";
        tabela[72][57] = "92";
        tabela[72][60] = "44";

        // 73
        setCols(73, F_SI, "R46");

        // 74
        setCols(74, F_T, "R50");

        // 75
        tabela[75][15] = "E37";
        tabela[75][31] = "E38";
        tabela[75][30] = "E45";
        setCols(75, F_T_SEM_FAT, "R54");
        tabela[75][60] = "93";

        // 76
        tabela[76][15] = "E37";
        tabela[76][31] = "E38";
        tabela[76][30] = "E45";
        setCols(76, F_T_SEM_FAT, "R54");
        tabela[76][60] = "94";

        // 77
        tabela[77][15] = "E37";
        tabela[77][31] = "E38";
        tabela[77][30] = "E45";
        setCols(77, F_T_SEM_FAT, "R54");
        tabela[77][60] = "95";

        // 78
        tabela[78][3]  = "E19";
        tabela[78][31] = "E22";
        tabela[78][29] = "E23";
        tabela[78][5]  = "E24";
        tabela[78][8]  = "E25";
        tabela[78][46] = "96";

        // 79
        tabela[79][10] = "E97";

        // 80
        tabela[80][13] = "R21";

        // 81
        tabela[81][13] = "R20";

        // 82
        tabela[82][10] = "R14";

        // 83
        tabela[83][28] = "E98";

        // 84 (conflito: var => shift; procedure/begin => C -> ε)
        tabela[84][1]  = "E7";
        tabela[84][0]  = "R2";
        tabela[84][3]  = "R2";
        tabela[84][35] = "99";

        // 85
        tabela[85][4] = "R33";

        // 86
        tabela[86][15] = "E37";
        tabela[86][31] = "E38";
        tabela[86][17] = "E42";
        tabela[86][30] = "E45";
        tabela[86][51] = "100";
        tabela[86][54] = "40";
        tabela[86][56] = "41";
        tabela[86][58] = "43";
        tabela[86][60] = "44";

        // 87
        tabela[87][16] = "E101";

        // 88
        setCols(88, F_FAT, "R57");

        // 89 (dangling-else: else => shift)
        tabela[89][6]  = "E103";
        tabela[89][10] = "R31";
        tabela[89][49] = "102";

        // 90
        setCols(90, F_EX, "R37");

        // 91
        tabela[91][15] = "E37";
        tabela[91][31] = "E38";
        tabela[91][17] = "E71";
        tabela[91][30] = "E45";
        setCols(91, F_SI, "R49");
        tabela[91][58] = "72";
        tabela[91][57] = "104";
        tabela[91][60] = "44";

        // 92
        setCols(92, F_SI, "R48");

        // 93
        tabela[93][18] = "E75";
        tabela[93][19] = "E76";
        tabela[93][20] = "E77";
        setCols(93, F_T, "R54");
        tabela[93][59] = "105";

        // 94
        tabela[94][18] = "E75";
        tabela[94][19] = "E76";
        tabela[94][20] = "E77";
        setCols(94, F_T, "R54");
        tabela[94][59] = "106";

        // 95
        tabela[95][18] = "E75";
        tabela[95][19] = "E76";
        tabela[95][20] = "E77";
        setCols(95, F_T, "R54");
        tabela[95][59] = "107";

        // 96
        tabela[96][10] = "R25";
        tabela[96][6]  = "R25";

        // 97
        tabela[97][0]  = "E10";
        tabela[97][3]  = "R8";
        tabela[97][38] = "108";

        // 98
        tabela[98][10] = "E109";
        tabela[98][16] = "R16";
        tabela[98][43] = "110";

        // 99
        tabela[99][1] = "R4";
        tabela[99][0] = "R4";
        tabela[99][3] = "R4";

        // 100
        tabela[100][12] = "E86";
        tabela[100][16] = "R30";
        tabela[100][48] = "111";

        // 101
        tabela[101][10] = "R28";
        tabela[101][6]  = "R28";

        // 102
        tabela[102][10] = "R24";
        tabela[102][6]  = "R24";

        // 103
        tabela[103][3]  = "E19";
        tabela[103][31] = "E22";
        tabela[103][29] = "E23";
        tabela[103][5]  = "E24";
        tabela[103][8]  = "E25";
        tabela[103][46] = "112";

        // 104
        setCols(104, F_SI, "R47");

        // 105
        setCols(105, F_T, "R51");

        // 106
        setCols(106, F_T, "R52");

        // 107
        setCols(107, F_T, "R53");

        // 108
        tabela[108][3] = "R9";

        // 109
        tabela[109][27] = "E48";
        tabela[109][1]  = "E49";
        tabela[109][44] = "113";
        tabela[109][45] = "52";

        // 110
        tabela[110][16] = "R15";

        // 111
        tabela[111][16] = "R29";

        // 112
        tabela[112][10] = "R32";
        tabela[112][6]  = "R32";

        // 113
        tabela[113][13] = "E114";

        // 114
        tabela[114][28] = "E115";

        // 115
        tabela[115][10] = "E109";
        tabela[115][16] = "R18";
        tabela[115][43] = "116";

        // 116
        tabela[116][16] = "R17";
    }

}