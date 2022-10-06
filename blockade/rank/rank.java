package br.com.mvbos.lgj.rank;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.IntStream;

public class rank{
    private FileInputStream arq;
    private int nivel;
    private ArrayList<player> rank;

    public rank(int nivel) throws Exception{
        arq = new FileInputStream("C:\\Users\\USUARIO\\Documents\\BACKUP\\Desktop\\arthur\\fontes-master\\fontes-master\\Cap04\\src\\br\\com\\mvbos\\lgj\\rank\\RankNivel"+nivel+".txt");
    }

    public rank(FileInputStream arq) throws Exception{
        this.arq = arq;
    }

    public FileInputStream getArq() {
        return arq;
    }

    public void setArq(FileInputStream arq) {
        this.arq = arq;
    }

    public ArrayList<player> getRank() { return rank; }
    public void setRank(ArrayList<player> rank) {
        this.rank = rank;
    }




    public ArrayList<player> defineRank() throws IOException {
        InputStreamReader ir = new InputStreamReader(arq);
        BufferedReader reader = new BufferedReader(ir);

        reader.readLine();
        reader.readLine();
        rank = new ArrayList();

        while(reader.ready()){
            String str = "";
            if ((str = reader.readLine()) != null && str.length() != 0) {
                String linha = str;
                String[] infos = linha.split(" - ");
                player o = new player(infos[1], infos[2], infos[3]);

                rank.add(o);
            }else{
                break;
            }

        }

        return rank;
    }

    public boolean verifica(String frutas, String time) throws IOException{
        defineRank();

        if(rank.size() == 10) {
            if (Integer.parseInt(rank.get(9).getFrutas()) > Integer.parseInt(frutas)) {
                return true;
            } else if (Integer.parseInt(rank.get(9).getFrutas()) < Integer.parseInt(frutas)) {
                return false;
            } else {
                if (Integer.parseInt(rank.get(9).getTime()) > Integer.parseInt(time))
                    return true;
                else
                    return false;
            }
        }
        return true;
    }

    public void atualizaRank(player o, int nivel) throws IOException {

        rank.add(o);
        rank.sort(new comparaPontos());

        if (rank.size() == 11) {
            rank.remove(10);
        }
        writeRank(nivel);
    }

    public void writeRank(int nivel){
        try {
            FileOutputStream saida = new FileOutputStream("C:\\Users\\USUARIO\\Documents\\BACKUP\\Desktop\\arthur\\fontes-master\\fontes-master\\Cap04\\src\\br\\com\\mvbos\\lgj\\rank\\RankNivel"+nivel+".txt");
            PrintStream ps = new PrintStream(saida);

            ps.println("Ranking");
            ps.println("  Nome-Frutas-Tempo");

            for(int i = 0; i < rank.size(); i++){
                player p = rank.get(i);
                ps.println(i+1 +" - "+ p.getNome() +" - "+p.getFrutas()+" - "+p.getTime());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Ocorreu um erro, " + e);
        }

    }

    private static class comparaPontos implements Comparator<player>{
        @Override
        public int compare(player o1, player o2){

            if( Integer.parseInt(o1.getFrutas()) > Integer.parseInt(o2.getFrutas())){
                return 1;
            }else if(Integer.parseInt(o1.getFrutas()) < Integer.parseInt(o2.getFrutas()))
                return -1;
            else{
                if (Integer.parseInt(o1.getTime()) < Integer.parseInt(o2.getTime())){
                    return -1;
                }else if (Integer.parseInt(o1.getTime()) > Integer.parseInt(o2.getTime())){
                    return 1;
                }else{
                    return o1.getNome().compareTo(o2.getNome());
                }
            }

        }
    }

}
