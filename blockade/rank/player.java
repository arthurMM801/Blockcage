package br.com.mvbos.lgj.rank;

public class player {
    private String nome;

    private String time;

    private String frutas;

    public player(String nome, String frutas, String time) {
        this.nome = nome;
        this.time = time;
        this.frutas = frutas;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFrutas() {
        return frutas;
    }

    public void setFrutas(String frutas) {
        this.frutas = frutas;
    }
}
