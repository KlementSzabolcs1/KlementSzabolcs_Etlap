package hu.petrik.etlap.database;

public class Fogas {
    private int id;
    private String nev;
    private FogasKategoria kategoria;
    private String leiras;
    private int ar;


    public Fogas(int id, String nev, FogasKategoria kategoria, String leiras, int ar) {
        this.id = id;
        this.nev = nev;
        this.kategoria = kategoria;
        this.leiras = leiras;
        this.ar = ar;
    }

    public Fogas(String nev, FogasKategoria kategoria, String leiras, int ar) {
        this.nev = nev;
        this.kategoria = kategoria;
        this.leiras = leiras;
        this.ar = ar;
    }

    public String getLeiras() {
        return leiras;
    }

    public int getId() {
        return id;
    }

    public String getNev() {
        return nev;
    }

    public FogasKategoria getKategoria() {
        return kategoria;
    }

    public int getAr() {
        return ar;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

    public void setKategoria(FogasKategoria kategoria) {
        this.kategoria = kategoria;
    }

    public void setLeiras(String leiras) {
        this.leiras = leiras;
    }

    public void setAr(int ar) {
        this.ar = ar;
    }
}
